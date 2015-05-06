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

package net.noisetube.api.io.saving;

import net.noisetube.api.config.Preferences;
import net.noisetube.api.io.NTWebAPI;
import net.noisetube.api.model.NTMeasurement;
import net.noisetube.api.model.Saveable;
import net.noisetube.api.model.TaggedInterval;
import net.noisetube.api.model.Track;
import net.noisetube.api.util.BlockingQueue;
import net.noisetube.api.util.ErrorCallback;

import java.util.ArrayList;

/**
 * HTTP based track and measurement saving class Uses the NoiseTube Web API
 *
 * @author maisonneuve, mstevens, sbarthol, humberto
 */
//TODO consider the use of the implementations of BlockingQueue of java
public class HttpSaver extends Saver implements Runnable, ErrorCallback {

    private BlockingQueue dataSendingQueue;
    private NTWebAPI api;
    private volatile boolean batchMode;
    private volatile boolean recoveryMode;
    private Cache cache; // for HTTP Connection recovery mode

    public HttpSaver(Track track) {
        super(track);
    }

    public void start() {
        if (!running) {
            // Check account & get API
            if (preferences.isLoggedIn())
                api = new NTWebAPI(preferences.getAccount());
            else
                throw new IllegalStateException("Not logged in!");
            api.setErrorCallBack(this);

            // Initialize queue
            dataSendingQueue = new BlockingQueue();

            // Initialize cache & (re)set modes
            cache = new Cache(); // disabled at first
            recoveryMode = false;
            if (preferences.isAlwaysUseBatchModeForHTTP())
                enableBatchMode();

            // Start for real...
            running = true;
            new Thread(this).start();
            log.debug("HTTPSaver started");
            if (preferences.getSavingMode() == Preferences.SAVE_HTTP)
                setStatus("Saving to NoiseTube.net (user: "
                        + preferences.getAccount().getUsername() + ")");
        }
    }

    /**
     * @see net.noisetube.api.io.saving.Saver#save(net.noisetube.api.model.Saveable)
     */
    @Override
    public void save(Saveable saveable) {
        if (running && saveable != null) // don't check for paused here,
            // otherwise taggedintervals are not
            // saved & last pre-paused
            // measurement may be lost as
            // well(?)
            dataSendingQueue.enqueue(saveable);
    }

    protected void enableBatchMode() {
        batchMode = true;
        log.debug("HttpSaver: Cache activated (batch mode)");
    }

    public void pause() {
        if (running) {
            paused = true;
            // setStatus(getStatus() + " [paused]");
        }
    }

    public void resume() {
        if (paused) {
            paused = false; // resume from pause
            // setStatus(getStatus().substring(0, getStatus().length() -
            // " [paused]".length()));
        }
    }

    public void stop() { // do not set running = false here!
        if (running)
            dataSendingQueue.enqueue(new Object()); // "stopper" object
    }

    public void error(Exception exception, String errorInfo) {
        enableRecoveryMode(exception, errorInfo);
    }

    private synchronized void enableRecoveryMode(Exception reason,
                                                 String infoMessage) {
        if (!recoveryMode) {
            recoveryMode = true; // turn on the connection recovery mode
            log.error(reason, "HttpSaver: " + infoMessage);
            log.debug("HttpSaver: Cache activated (network connection may have been lost)");
        } else {
            log.debug("HttpSaver: " + infoMessage); // only log the message (not
            // full stack trace) if we
            // are already in recovery
            // mode
        }
    }

    public void run() {
        try {
            if (!track.isTrackIDSet())
                api.startTrack(track); // send signal for a new track
        } catch (Exception se) {
            enableRecoveryMode(se, "Error upon starting track");
        }
        while (running) {
            Object queuedObject = null;
            try {
                queuedObject = dataSendingQueue.dequeue();
                if (!(queuedObject instanceof Saveable))
                    break; // got "stopper" object: break out the while loop
                else {
//                    if (!recoveryMode && !batchMode) {
//                        api.sendData(track, (Saveable) queuedObject); // send
//                        // single
//                        // savable
//                        // (measurement/taggedinterval)
//                        // directly
//                    } else {

                    cache.cache((Saveable) queuedObject); // cache the
                    // savable
                    // (measurement/taggedinterval)
                    // as JSON for
                    // batch sending
                    queuedObject = null; // so it is not re-added in the
                    // catch-block below
                    if (cache.getSize() % 30 == 0) // try to send cache
                    // contents in one batch
                    // every 30 measurements
                    {
                        if (recoveryMode && !batchMode)
                            log.debug("HttpSaver: Connection recovery attempt: trying to send data");
                        api.sendBatch(track, cache); // clears the cache if
                        // successful,
                        // throws an
                        // exception
                        // otherwise
                        if (recoveryMode) // connection recovered
                        {
                            recoveryMode = false;
                            log.debug("HttpSaver: Connection recovered, cache deactivated");
                        }
                    }
//                    }
                }
            } catch (Exception e) {
                enableRecoveryMode(e, "Error in sending loop");
                // cache to send later
                if (queuedObject != null)
                    cache.cache((Saveable) queuedObject); // cache the savable
            }
        }
        // stopping...
        try {

            // send remaining cached measurements:
            if (batchMode || recoveryMode)
                api.sendBatch(track, cache);
        } catch (Exception e) {
            log.error(e, "HttpSaver: Unable to save last " + cache.getSize()
                    + " cached measurements and tagged intervals");
        }
        try {
            api.endTrack(track);

        } catch (Exception e) {
            log.error(e, "HttpSaver: Error upon ending track");
        }
        running = false; // !!!
        log.debug("HTTPSaver stopped (Track ID " + track.getTrackID() + ")");
        if (preferences.getSavingMode() == Preferences.SAVE_HTTP)
            setStatus("Stopped");
    }

    public class Cache {

        static final int INITIAL_CAPACITY = Track.DEFAULT_BUFFER_CAPACITY;
        private ArrayList<Saveable> measurements = new ArrayList<Saveable>(
                INITIAL_CAPACITY);
        private ArrayList<Saveable> taggedIntervals = new ArrayList<Saveable>(
                INITIAL_CAPACITY);
        static final int MAXIMUM_MEASUREMENTS = 3600;
        static final int MAXIMUM_TAGGEDINTERVALS = 60;

        public void clear() {
            measurements.clear();
            taggedIntervals.clear();
        }

        public int getSize() {
            return (measurements.size() + taggedIntervals.size());
        }

        /**
         * adds the savable to the cache
         *
         * @param saveable
         */
        public void cache(Saveable saveable) {
            if (saveable instanceof NTMeasurement) {
                if (measurements.size() >= MAXIMUM_MEASUREMENTS) {
                    log.error("measurement cache is full, deleting oldest entry");
                    measurements.remove(0);
                }
                measurements.add(saveable);
            } else if (saveable instanceof TaggedInterval) {
                if (taggedIntervals.size() >= MAXIMUM_TAGGEDINTERVALS) {
                    log.error("tagged interval cache is full, deleting oldest entry");
                    taggedIntervals.remove(0);
                }
                taggedIntervals.add(saveable);
            } else
                throw new IllegalArgumentException("Unknown savable type");
        }

        /**
         * @return the measurements
         */
        public ArrayList<Saveable> getMeasurements() {
            return measurements;
        }

        /**
         * @return the taggedIntervals
         */
        public ArrayList<Saveable> getTaggedIntervals() {
            return taggedIntervals;
        }

    }

}