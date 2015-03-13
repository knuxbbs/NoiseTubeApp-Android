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

package net.noisetube.api;


import net.noisetube.api.audio.SoundLevelMeter;
import net.noisetube.api.audio.calibration.Calibration;
import net.noisetube.api.audio.recording.AudioRecorder;
import net.noisetube.api.config.Preferences;
import net.noisetube.api.io.saving.FileSaver;
import net.noisetube.api.io.saving.HttpSaver;
import net.noisetube.api.io.saving.MultiSaver;
import net.noisetube.api.io.saving.Saver;
import net.noisetube.api.location.GeoTagger;
import net.noisetube.api.model.MeasurementListener;
import net.noisetube.api.model.NTCoordinates;
import net.noisetube.api.model.NTMeasurement;
import net.noisetube.api.model.SLMMeasurement;
import net.noisetube.api.model.Track;
import net.noisetube.api.util.Logger;


/**
 * @author mstevens, humberto
 */
public abstract class NTClient extends SLMClient {

    /**
     *
     */
    private static final long serialVersionUID = -2989156130275903717L;
    protected static AudioRecorder audioRecorder;
    //DYNAMIC----------------------------------------------
    protected Preferences preferences;
    protected GeoTagger geoTagger;
    protected Saver saver;

    protected NTClient(String clientType, String clientVersion, String clientBuildDate, boolean testVersion) {
        super(clientType, clientVersion, clientBuildDate, testVersion);
        //Subclasses must call initialize!
    }

    //STATIC-----------------------------------------------
    public static NTClient getInstance() {
        return (NTClient) INSTANCE; //!!!
    }

    public static void dispose() {
        Logger.dispose();

        INSTANCE = null;
    }

    public int getFavoriteSavingMode() {

        int savingMode = Preferences.SAVE_NO;

        if (device.supportsInternetAccess()) {
            savingMode = Preferences.SAVE_HTTP;
        } else if (device.supportsFileAccess()) {
            savingMode = Preferences.SAVE_FILE;
        }
        return savingMode;
    }

    /**
     * Sets the Device and Preferences
     */
    protected final void initialize() throws Exception {
        if (!initialized) {
            super.initialize(); //!!!

            //Check baseline functionality: reading/writing files & recording audio
            StringBuilder missingFeatures = new StringBuilder(44);
            if (!device.supportsFileAccess())
                missingFeatures.append("Cannot access filesystem, ");
            if (!device.supportsAudioRecording())
                missingFeatures.append("Cannot record audio");

            if (missingFeatures.length() != 0) {
                String errorMsg = "Unsupported device! Required functionality missing: " + missingFeatures.toString();
                //write crash log to file if we can
                try {
                    if (device.supportsFileAccess()) {
                        log.error(errorMsg);
                        log.dumpCrashLog(createPreferences().getDataFolderPath());
                    }
                } catch (Exception ignore) {
                    log.error(ignore, "initialize method");
                }
                throw new Exception(errorMsg); //!!! (will show up in GUI)
            }

            //Create preferences:
            preferences = createPreferences();


            //Last but not least:
            initialized = true;
        }
    }

    /**
     * @return the preferences
     */
    public Preferences getPreferences() {
        return preferences;
    }

    protected abstract Preferences createPreferences();

    /**
     * @param listener
     * @return an slm instance with a calibration (but not necessarily a correct/fitting one)
     * @throws Exception the difference with SLMClient.getCalibratedSLM() is that here we use the calibration from pref's, not from device
     */
    public SoundLevelMeter getCalibratedSLM(MeasurementListener listener) throws Exception {
        if (slm == null)
            slm = new SoundLevelMeter(device.getAudioSpecification(), preferences.getCalibration(), listener);
        else
            slm.setListener(listener);
        return slm;
    }


    public GeoTagger getGeoTagger() {
        if (geoTagger == null)
            geoTagger = createGeoTagger();
        return geoTagger;
    }


    protected abstract GeoTagger createGeoTagger();

    public Saver getSaver(Track track) {
        if (saver == null) {
            //Create a saver...
            switch (preferences.getSavingMode()) {
                case Preferences.SAVE_NO:
                    saver = null;
                    break;
                case Preferences.SAVE_HTTP:

                    if (preferences.isAlsoSaveToFileWhenInHTTPMode()) {
                        saver = new MultiSaver(track);
                        ((MultiSaver) saver).addSaver(new HttpSaver(track));
                        ((MultiSaver) saver).addSaver(new FileSaver(track));
                    } else {
                        saver = new HttpSaver(track);
                    }
                    break;
                case Preferences.SAVE_FILE:
                    saver = new FileSaver(track);
                    break;
                case Preferences.SAVE_SMS: //TODO pending
                    break;

            }
        } else
            saver.setTrack(track); //!!! assign saver to given track
        return saver;
    }

    /**
     * To be called from Preferences upon a change of a saving preference
     */
    public void resetSaver() {
        saver = null;
    }

    public abstract NTCoordinates getNTCoordinates(double latitude, double longitude, double altitude);

    public abstract void addTrackProcessors(Track track);

    public void annotateTrack(Track track) {
        track.addMetadata("client", clientType);
        track.addMetadata("clientVersion", clientVersion);
        track.addMetadata("clientBuildDate", clientBuildDate);
        track.addMetadata("deviceBrand", device.getBrand());
        track.addMetadata("deviceModel", device.getModel());
        track.addMetadata("deviceModelVersion", device.getModelVersion());
        track.addMetadata("devicePlatform", device.getPlatform());
        track.addMetadata("devicePlatformVersion", device.getPlatformVersion());
        track.addMetadata("deviceJavaPlatform", device.getJavaPlatform());
        track.addMetadata("deviceJavaPlatformVersion", device.getJavaPlatformVersion());
        if (preferences.getCalibration() != null) {
            track.addMetadata("calibration", preferences.getCalibration().toString());
            track.addMetadata("credibility", new String(new char[]{preferences.getCalibration().getEffeciveCredibilityIndex()}));
        } else
            track.addMetadata("credibility", new String(new char[]{Calibration.CREDIBILITY_INDEX_X}));
        //subclasses can add some more:
        additionalTrackAnnotating(track);
    }

    public SLMMeasurement createMeasurement(long timeStampMS) {
        return new NTMeasurement(timeStampMS);
    }

    /**
     * For audio classes only (they do not know about preferences)
     */
    public String getDataFolderPath() {
        if (preferences == null) {
            preferences = createPreferences();
        }
        return preferences.getDataFolderPath();
    }

    protected abstract void additionalTrackAnnotating(Track track);

}
