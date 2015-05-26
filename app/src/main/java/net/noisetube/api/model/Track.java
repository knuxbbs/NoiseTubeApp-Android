/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation)
 *
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2012
 *  Portions contributed by University College London (ExCiteS group), 2012
 * --------------------------------------------------------------------------------
 *  This library is free software; you can redistribute it and/or modify it under
 *  the terms of the GNU Lesser General Public License, version 2.1, as published
 *  by the Free Software Foundation.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along
 *  with this library; if not, write to:
 *    Free Software Foundation, Inc.,
 *    51 Franklin Street, Fifth Floor,
 *    Boston, MA  02110-1301, USA.
 *
 *  Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *  NoiseTube project source code repository: http://code.google.com/p/noisetube
 * --------------------------------------------------------------------------------
 *  More information:
 *   - NoiseTube project website: http://www.noisetube.net
 *   - Sony Computer Science Laboratory Paris: http://csl.sony.fr
 *   - VUB BrusSense team: http://www.brussense.be
 * --------------------------------------------------------------------------------
 */

package net.noisetube.api.model;

import net.noisetube.api.NTClient;
import net.noisetube.api.Pausable;
import net.noisetube.api.Processor;
import net.noisetube.api.TrackStatistics;
import net.noisetube.api.audio.SoundLevelMeter;
import net.noisetube.api.config.Preferences;
import net.noisetube.api.experiment.DoseMeter;
import net.noisetube.api.io.saving.Saver;
import net.noisetube.api.location.CoordinateInterpolator;
import net.noisetube.api.location.GeoTagger;
import net.noisetube.api.ui.TrackUI;
import net.noisetube.api.util.CyclicQueue;
import net.noisetube.api.util.IStringEncoder;
import net.noisetube.api.util.Logger;
import net.noisetube.api.util.MathNT;
import net.noisetube.api.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Track
 * <p/>
 * Represents the local data model for a measurement session
 * JavaMEv2.2.0/Androidv1.3.0: Absorbed all remaining functionality of Engine
 *
 * @author sbarthol, mstevens, humberto
 *         <p/>
 *         Code copied form Engine was by maisonneuve, mstevens & sbarthol. Updated and optimized by humberto
 */
public class Track implements Pausable, MeasurementListener {

    // STATIC
    static public final int UNSPECIFIED_TRACK_ID = -1;
    // Session info
    private int trackID = UNSPECIFIED_TRACK_ID;
    static public final int DEFAULT_BUFFER_CAPACITY = 60;
    static public int WAIT_FOR_SAVING_TO_COMPLETE_MS = 30000; // 30 seconds
    // Sensor
    protected SoundLevelMeter slm;
    /**
     * called from SoundLevelMeter
     * <p/>
     * This function adds a new Measurement to the measurementQueue. This will
     * trigger both the saving of the element that was on top and is removed
     * from the queue, as running all the Processors over the new
     * measurementQueue.
     *
     * @param m measurement
     */
    boolean flag = true;
    int mesurementCount = 0;
    // DYNAMIC
    private NTClient ntClient;
    private Preferences preferences;
    private Logger log = Logger.getInstance();
    private int bufferCapacity;
    private CyclicQueue<SLMMeasurement> measurementBuffer;
    private ArrayList<TaggedInterval> taggedIntervalBuffer;
    // Processors
    private ArrayList<Processor> processors;
    private GeoTagger geoTagger;
    private DoseMeter doseMeter;
    // Saver
    private Saver saver = null;
    // UI
    private List<TrackUI> listeners;
    private HashMap<String, String> metaDataTable;
    private long startTime;
    private long pausedSince = 0;
    private long timeSpentEarlier = 0;
    private long timeSpentInPauseMS = 0;
    // setting one here avoids some rare exceptions
    private TrackStatistics statistics = new TrackStatistics();
    //    private int beginSegment = 0;
//    private int endSegment = 0;
    private Date created;

    /**
     * Creates a new NoiseTube Track
     */
    public Track() {
        created = new Date();
        try {

            ntClient = NTClient.getInstance();
            preferences = ntClient.getPreferences();
            listeners = new ArrayList<TrackUI>();


            // Data structures:
            this.bufferCapacity = DEFAULT_BUFFER_CAPACITY;
            measurementBuffer = new CyclicQueue<SLMMeasurement>(bufferCapacity);
            taggedIntervalBuffer = new ArrayList<TaggedInterval>();
            processors = new ArrayList<Processor>();
            metaDataTable = new HashMap<String, String>();

            // Sensor
            slm = ntClient.getCalibratedSLM(this); // will return existing one if there is one

            // Initialize processors (except statistics):
            geoTagger = ntClient.getGeoTagger(); // will return existing one if there is one

            addProcessor(geoTagger); // Do not move this behind CoordinateInterpolator

            if (preferences.isUseCoordinateInterpolation())
                addProcessor(new CoordinateInterpolator());
            if (preferences.isUseDoseMeter()) {
                doseMeter = new DoseMeter();
                addProcessor(doseMeter);
            }

            ntClient.addTrackProcessors(this); // clients can add additional processors

            // Saving...
            saver = ntClient.getSaver(this); // will return existing one if there is one and if preferences have not changed
        } catch (Exception e) {
            log.error(e, "Track constructor");
        }
    }

    public void addProcessor(Processor processor) {
        processors.add(processor);
    }

    public void start() {
        restart(UNSPECIFIED_TRACK_ID, new TrackStatistics(), 0);
    }

    public void restart(int trackID, TrackStatistics statistics, long elapsedTime) {
        if (slm.isRunning()) {
            log.error("Track already running");
            return;
        }
        log.info((elapsedTime > 0 ? "Res" : "S") + "tarting track");

        this.trackID = trackID;
        this.timeSpentEarlier = elapsedTime;
        this.startTime = System.currentTimeMillis();
        this.statistics = statistics;
        addProcessor(statistics); // !!! should never be moved before
        // locationComponent

        ntClient.annotateTrack(this); // !!!

        if (saver != null) // preferences.getSavingMode() != Preferences.SAVE_NO
        {
            try {
                saver.start();
            } catch (Exception e) {
                log.error(e, "starting saver failed; not saving measurements");
                saver = null;
            }
        } else
            log.info("Not saving measurements");
        geoTagger.start();
        slm.start();

        for (TrackUI listener : listeners) {
            listener.measuringStarted(this);
        }
    }

    public void newMeasurement(SLMMeasurement m) {

        if (slm.isRunning()) {
            try {
                NTMeasurement ntM = (NTMeasurement) m;
                ntM.setNumber(mesurementCount);

                mesurementCount++;

                // if the buffer was full the offer operation returns the
                // element that was removed (the oldest one in the queue) to
                // make room for the one being offered
                NTMeasurement oldMeasurement = (NTMeasurement) measurementBuffer
                        .offer(ntM);

                // Run all Processors, using the measurementQueue as their
                // parameter:
                for (int i = 0; i < processors.size(); i++) {
                    processors.get(i).process(ntM, this);
                }

                // statistics have been updated, now purge taggedinterval buffer
                if (oldMeasurement != null) {
                    for (TaggedInterval item : taggedIntervalBuffer) {
                        if (!isInBuffer(item.getEndMeasurement())) {
                            taggedIntervalBuffer.remove(item); // because the tagged measurements are no longer
                        }                                       // buffered (and therefore not drawn on the graph)
                    }
                } else if (flag && measurementBuffer.getSize() == 1 && saver.isRunning()) {
                    oldMeasurement = (NTMeasurement) measurementBuffer.removeFirstElement(); // needed by the server
                    flag = false;

                }
                for (TrackUI listener : listeners) {
                    listener.newMeasurement(this, ntM, oldMeasurement);
                }

                // Save oldest element which was in the buffer
                if (oldMeasurement != null && saver != null
                        && saver.isRunning())
                    saver.save(oldMeasurement);
            } catch (Exception e) {
                log.error(e, "Track.receiveMeasurement()");
            }
        }
    }

    public boolean isIntervalTaggedUser(int measurementNumber) {
        if (statistics == null)
            return false;
//        int trackIdx = measurementNumber
//                + (statistics.getNumMeasurements() - getBufferSize());

        for (TaggedInterval item : taggedIntervalBuffer) {
            if (!item.isAutomatic() && measurementNumber >= item.getBeginMeasurement()
                    && measurementNumber <= item.getEndMeasurement()) {

                return true; // measurement is tagged by this interval
            }
        }

        return false;
    }


    public boolean isInBuffer(int measurementNumber) {
        if (statistics.getNumMeasurements() <= 0) {
            return false; // track is empty
        }
        if (measurementNumber > statistics.getNumMeasurements()) {
            return false; // this measurement has not been made yet
        }
        return (measurementNumber >= (statistics.getNumMeasurements() - getBufferSize()));
    }

    public void addTaggedInterval(TaggedInterval taggedInterval) {
        if (saver != null && saver.isRunning())
            saver.save(taggedInterval);
        // only buffer the interval if its last measurement is still in view:
        if (isInBuffer(taggedInterval.getEndMeasurement())) {
            taggedIntervalBuffer.add(taggedInterval);
        }

    }

    public void pause() {
        pause(false);
    }

    public void pause(boolean preStop) {
        if (isRunning() && !isPaused()) {
            slm.pause();
            if (!preStop) {
                pausedSince = System.currentTimeMillis();
                if (saver != null)
                    saver.pause();

                for (TrackUI listener : listeners) {
                    listener.measuringPaused(this);
                }

                log.info("Measuring paused");
            }
        }

    }

    public void resume() {
        if (isRunning() && isPaused()) {
//            beginSegment = endSegment + 1;

            slm.resume();
            if (pausedSince != 0) {
                timeSpentInPauseMS += System.currentTimeMillis() - pausedSince;
                pausedSince = 0;
            }

            if (saver != null)
                saver.resume();
            for (TrackUI listener : listeners) {
                listener.measuringResumed(this);
            }

            log.info("Measuring resumed");

        }
    }

    public void stop() {

        if (!slm.isRunning())
            return;
        log.info("Stopping track...");
        slm.stop();
        geoTagger.stop();

        if (saver != null) {
            flushBuffer(); // !!!
            saver.stop(); // don't move this before flushBuffer!
            // Block up to WAIT_FOR_SAVING_TO_COMPLETE_MS ms (in naps of 500ms)
            // until saver is done (or all savers in case of a multisaver):

            int waited = 0;
            while (saver.isRunning() && waited < WAIT_FOR_SAVING_TO_COMPLETE_MS) {
                try {
                    Thread.sleep(500);
                    waited += 500;
                } catch (InterruptedException e) {
                    break;
                }
            }
            if (saver.isRunning())
                log.error("Saver still running after waiting "
                        + MathNT.round(WAIT_FOR_SAVING_TO_COMPLETE_MS / 1000f)
                        + "s");
        }


        for (TrackUI listener : listeners) {
            listener.measuringStopped(this);

        }


        log.info("Track"
                + (trackID != UNSPECIFIED_TRACK_ID ? " " + trackID : "")
                + " summary:");
        log.info(" - duration: "
                + StringUtils.formatTimeSpanColons(getTotalElapsedTime()));
        log.info(" - # measurements: " + statistics.getNumMeasurements());
    }

    /**
     * Saves all the measurements in the buffer and resets the buffer.
     */
    public void flushBuffer() {
        if (saver != null && saver.isRunning()) {
            List<Saveable> items = new ArrayList<Saveable>(measurementBuffer.getValues());
            saver.saveBatch(items);
        }
        measurementBuffer.clear();
    }

    public boolean isRunning() {
        return slm.isRunning();
    }

    public boolean isPaused() {
        return slm.isPaused();
    }

    public NTMeasurement getNewestMeasurement() {
        return (NTMeasurement) measurementBuffer.tail();
    }

    public SLMMeasurement getOldestMeasurement() {
        return measurementBuffer.getElement(0);
    }

    /**
     * Gives us an enumeration which enumerates starting from the oldest element
     * (the first element added).
     *
     * @return The enumeration.
     */
    public Enumeration<SLMMeasurement> getMeasurements() {
        return measurementBuffer.getEnumeration();
    }

    /**
     * Gives us a reversed enumeration which enumerates starting from the newest
     * element (the last element added).
     *
     * @return The reversed enumeration.
     */
    public Enumeration<SLMMeasurement> getMeasurementsNewestFirst() {
        return measurementBuffer.getElementsReversed();
    }

    public void addMetadata(String key, String value) {
        if (key == null || key.equals(""))
            throw new IllegalArgumentException("Invalid key");
        if (value != null)
            metaDataTable.put(key, value);
    }

    /**
     * Builds up a String containing all the key-value combinations.
     *
     * @param equals
     * @param separator
     * @param keysAsLowerCase
     * @param stringEncorder
     * @return
     */
    public String getMetaDataString(String equals, String separator,
                                    boolean keysAsLowerCase, IStringEncoder stringEncorder) {
        return getMetaDataString(equals, separator, null, keysAsLowerCase,
                stringEncorder);
    }

    /**
     * Builds up a String containing all the key-value combinations.
     *
     * @param equals
     * @param separator
     * @param valueQuote
     * @param keysAsLowerCase
     * @param stringEncorder
     * @return The resulting String
     */
    public String getMetaDataString(String equals, String separator,
                                    char valueQuote, boolean keysAsLowerCase,
                                    IStringEncoder stringEncorder) {
        return getMetaDataString(equals, separator, new Character(valueQuote),
                keysAsLowerCase, stringEncorder);
    }

    /**
     * Builds up a String containing all the key-value combinations.
     *
     * @param equals
     * @param separator
     * @param valueQuote
     * @param keysAsLowerCase
     * @param stringEncorder
     * @return The resulting String
     */
    private String getMetaDataString(String equals, String separator,
                                     Character valueQuote, boolean keysAsLowerCase,
                                     IStringEncoder stringEncorder) {
        StringBuilder bff = new StringBuilder();

        Set<Entry<String, String>> keys = metaDataTable.entrySet();
        for (Entry<String, String> entry : keys) {
            String key = entry.getKey(), value = entry.getValue();

            if (bff.length() > 0)
                bff.append(separator);
            bff.append(encodeString(keysAsLowerCase ? key.toLowerCase() : key,
                    stringEncorder));
            bff.append(equals);
            bff.append((valueQuote != null ? valueQuote.toString() : "")
                    + encodeString(value, stringEncorder)
                    + (valueQuote != null ? valueQuote.toString() : ""));
        }

        return bff.toString();
    }

    private String encodeString(String str, IStringEncoder encoder) {
        if (encoder != null)
            return encoder.encode(str);
        else
            return str;
    }

    public void addTrackUIListener(TrackUI ui) {
        this.listeners.add(ui);
    }

    public boolean removeTrackUIListener(TrackUI ui) {
        return listeners.remove(ui);
    }

    public int getTrackID() {
        return trackID;
    }

    /**
     * @param trackID the trackID to set
     */
    public void setTrackID(int trackID) {
        this.trackID = trackID;
    }

    public boolean isTrackIDSet() {
        return trackID != UNSPECIFIED_TRACK_ID;
    }

    /**
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }

    public long getTotalElapsedTime() {
        return timeSpentEarlier
                + (System.currentTimeMillis() - startTime - timeSpentInPauseMS);
    }

    public TrackStatistics getStatistics() {
        return statistics;
    }

    /**
     * Return the maximum number of measurements the buffer can hold
     *
     * @return buffer capacity
     */
    public int getBufferCapacity() {
        return measurementBuffer.getCapacity();
    }

    /**
     * Returns how many measurements are present in the internal buffer.
     *
     * @return The amount of measurements present in the internal buffer.
     */
    public int getBufferSize() {
        return measurementBuffer.getSize();
    }

    /**
     * @return the saver
     */
    public Saver getSaver() {
        return saver;
    }

    /**
     * @return the doseMeter
     */
    public DoseMeter getDoseMeter() {
        return doseMeter;
    }

    public String getFormattedElapsedTime() {
        return StringUtils.formatTimeSpanColons(getTotalElapsedTime());
    }

//    public int getBeginSegment() {
//        return beginSegment;
//    }
//
//    public int getEndSegment() {
//        return endSegment;
//    }

    public void freeResources() {
        this.measurementBuffer.clear();
        this.metaDataTable.clear();
        this.processors.clear();
    }

    public ArrayList<SLMMeasurement> getMeasurementsList() {
        return measurementBuffer.getValues();
    }

    public HashMap<String, String> getMetaData() {
        return metaDataTable;
    }

    public Date getCreatedDate() {
        return created;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + created.hashCode();
        return hash;
    }
}
