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

import net.noisetube.api.audio.AudioStreamListener;
import net.noisetube.api.audio.AudioStreamSaver;
import net.noisetube.api.audio.SoundLevelMeter;
import net.noisetube.api.audio.calibration.CalibrationsParser;
import net.noisetube.api.audio.recording.AudioRecorder;
import net.noisetube.api.audio.recording.AudioSpecification;
import net.noisetube.api.config.Device;
import net.noisetube.api.io.FileWriter;
import net.noisetube.api.io.HttpClient;
import net.noisetube.api.model.MeasurementListener;
import net.noisetube.api.model.SLMMeasurement;
import net.noisetube.api.util.Logger;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Abstract super class for NTClient
 * For use in Calibrator/Tester apps and NT SLM Library
 *
 * @author mstevens
 */
public abstract class SLMClient implements Serializable {

    //STATIC-----------------------------------------------
    static final public int EMULATOR_ENV = 0;        //Emulator environment
    static final public int PHONE_DEV_ENV = 1;        //Development phone environment
    static final public int PHONE_PROD_ENV = 2;    //Production phone environment
    //Set active environment:
    static public int ENVIRONMENT = PHONE_PROD_ENV; //default
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    protected static SLMClient INSTANCE = null;
    //DYNAMIC----------------------------------------------
    protected String clientType;
    protected String clientVersion;
    protected String clientBuildDate;
    protected boolean testVersion;
    protected Logger log;
    protected Device device;
    protected SoundLevelMeter slm;
    protected boolean initialized = false;

    protected SLMClient(String clientType, String clientVersion, String clientBuildDate, boolean testVersion) {
        if (INSTANCE != null)
            throw new IllegalStateException("Cannot create more than one instance of NTClient (Singleton)");
        INSTANCE = this; //!!!
        this.clientType = clientType;
        this.clientVersion = clientVersion;
        this.clientBuildDate = clientBuildDate;
        this.testVersion = testVersion;

        //Logger
        log = Logger.getInstance();
        log.setClient(this);
        if (testVersion || ENVIRONMENT == EMULATOR_ENV)
            log.setLevel(Logger.DEBUG);

        //Subclasses must call initialize!
    }

    public static String getEnvironmentName(int environment) {
        String name = null;
        switch (environment) {
            case PHONE_PROD_ENV:
                name = "Phone/Production";
                break;
            case PHONE_DEV_ENV:
                name = "Phone/Development";
                break;
            case EMULATOR_ENV:
                name = "Emulator";
                break;
            default:
                name = "unknown";
        }
        return name;
    }

    public static SLMClient getInstance() {
        return INSTANCE;
    }

    public static void dispose() {
        Logger.dispose();
        INSTANCE = null;
    }

    /**
     * Sets the Device and Preferences
     */
    protected void initialize() throws Exception {
        if (!initialized) {
            //Print log info:
            log.info(clientType + " " + clientVersion + " (build: " + clientBuildDate + ") started");
            log.info("Environment: " + getEnvironmentName(ENVIRONMENT));

            //Create device & log info:
            device = createDevice();
            device.logIdentification();
            device.logFunctionalities();

            //Last but not least:
            initialized = true;
        }
    }

    /**
     * @return the clientType
     */
    public String getClientType() {
        return clientType;
    }

    /**
     * @return the clientVersion
     */
    public String getClientVersion() {
        return clientVersion;
    }

    /**
     * @return the clientBuildDate
     */
    public String getClientBuildDate() {
        return clientBuildDate;
    }

    public String getClientIdentification() {
        return getClientType() + " " + getClientVersion();
    }

    /**
     * @return the testVersion
     */
    public boolean isTestVersion() {
        return testVersion;
    }

    /**
     * @return the device
     */
    public Device getDevice() {
        return device;
    }

    /**
     * @return the restartingModeEnabled
     */
    public boolean isRestartingModeEnabled() {
        return false;
    }

    /**
     * @return the firstRun
     */
    public boolean isFirstRun() {
        return true;
    }

    /**
     * @return the lastRun
     */
    public boolean isLastRun() {
        return true;
    }

    protected abstract Device createDevice();

    public abstract AudioSpecification deserialiseAudioSpecification(String serialisedAudioSpec);

    public abstract AudioRecorder getAudioRecorder(AudioSpecification audioSpec, int recordTimeMS, AudioStreamListener listener);

    public abstract AudioStreamSaver getAudioStreamSaver();

    /**
     * @param listener
     * @return an slm instance with a calibration (but not necessarily a correct/fitting one)
     * @throws Exception
     */
    public SoundLevelMeter getCalibratedSLM(MeasurementListener listener) throws Exception {
        if (slm == null)
            slm = new SoundLevelMeter(device.getAudioSpecification(), device.getCalibration(), listener);
        else
            slm.setListener(listener);
        return slm;
    }

    /**
     * @param audioSpec
     * @param listener
     * @return a calibration-less slm instance
     * @throws Exception
     */
    public SoundLevelMeter getUncalibratedSLM(AudioSpecification audioSpec, MeasurementListener listener) throws Exception {
        return new SoundLevelMeter(audioSpec, null, listener);
    }

    public abstract CalibrationsParser getCalibrationParser();


    public FileWriter getUTF8FileWriter(String filePath) {
        return getFileWriter(filePath, "UTF-8");
    }

    public FileWriter getFileWriter(String filePath) {
        return getFileWriter(filePath, null);
    }

    public abstract FileWriter getFileWriter(String filePath, String characterEncoding);

    public abstract HttpClient getHttpClient(String agent);

    /**
     * @param filePath
     * @return an inputstream is the file exists, null if it does not (or if an error occurred)
     */
    public abstract InputStream getFileInputStream(String filePath);

    public SLMMeasurement createMeasurement(long timeStampMS) {
        return new SLMMeasurement(timeStampMS);
    }

    /**
     * For audio classes only (they do not know about preferences)
     */
    public String getDataFolderPath() {
        return device.getDataFolderPath(true);
    }

    public abstract String additionalErrorReporting(Throwable t);

}
