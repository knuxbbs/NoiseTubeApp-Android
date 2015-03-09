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

package net.noisetube.api.config;

import net.noisetube.api.SLMClient;
import net.noisetube.api.audio.calibration.Calibration;
import net.noisetube.api.audio.calibration.CalibrationFactory;
import net.noisetube.api.audio.recording.AudioRecorder;
import net.noisetube.api.audio.recording.AudioSpecification;
import net.noisetube.api.util.Logger;

import java.io.Serializable;


public abstract class Device implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3L;

    //STATIC-----------------------------------------------
    protected static int AUDIO_RECORDING_TEST_DURATION_MS = 250; //ms

    //DYNAMIC----------------------------------------------
    protected SLMClient client;
    protected Logger log = Logger.getInstance();

    //Hard- & software
    protected String brand = null;
    protected String model = null;
    protected String modelVersion = null;
    protected String firmwareVersion = null;
    protected String platform = null;
    protected String platformVersion = null;
    protected String javaPlatform = null;
    protected String javaPlatformVersion = null;

    protected Calibration calibration = null;
    protected AudioSpecification audioSpecification = null;
    private boolean triedToFindAudioSpec = false;


    public Device() {
        client = SLMClient.getInstance();
        identifyDevice(); //!!!
        if (SLMClient.ENVIRONMENT == SLMClient.EMULATOR_ENV) {
            if (brand == null)
                brand = "SDK";
            if (model == null)
                model = "emulator";
        }
    }

    public void logIdentification() {
        log.info("Device info:");
        log.info(" - Brand: " + brand);
        log.info(" - Model: " + (model == null ? "unknown" : model));
        log.info(" - Model version: " + (modelVersion == null ? "unknown" : modelVersion));
        log.info(" - Firmware version: " + (firmwareVersion == null ? "unknown" : firmwareVersion));
        log.info(" - Software platform: " + (platform == null ? "unknown" : platform));
        log.info(" - Software platform version: " + (platformVersion == null ? "unknown" : platformVersion));
        log.info(" - Java platform: " + (javaPlatform == null ? "unknown" : javaPlatform));
        log.info(" - Java platform version: " + (javaPlatformVersion == null ? "unknown" : javaPlatformVersion));
    }

    public void logFunctionalities() {
        if (getAudioSpecification() != null)
            log.info(" - Audio specification: " + audioSpecification.toVerboseString());
        if (!supportsPositioning())
            log.info(" - Warning: No positioning support, GPS will be unavailable.");
    }

    /**
     * Should determine brand, model, platform, versions, etc.
     */
    protected abstract void identifyDevice();

    public abstract boolean supportsFileAccess();

    public abstract boolean supportsPositioning();

    public boolean supportsAudioRecording() {
        return getAudioSpecification() != null;
    }

    public abstract boolean supportsBluetooth();

    public abstract boolean hasTouchScreen();

    public abstract boolean supportsInternetAccess();

    public AudioSpecification getAudioSpecification() {
        if (audioSpecification == null && !triedToFindAudioSpec) {
            audioSpecification = getSuitableAudioSpecification();
            triedToFindAudioSpec = true;
        }
        return audioSpecification;
    }

    protected abstract AudioSpecification getSuitableAudioSpecification();

    protected boolean testAudioSpecification(AudioSpecification as) {
        AudioRecorder recorder = null;
        try {
            recorder = client.getAudioRecorder(as, AUDIO_RECORDING_TEST_DURATION_MS, null);
            return recorder.testRecord();
        } catch (Exception e) {
            log.error(e, "testAudioRecorder");
            return false; //Something went wrong...
        } finally {
            if (recorder != null)
                recorder.release();
        }
    }

    public Calibration getCalibration() {
        if (calibration == null) {
            CalibrationFactory factory = new CalibrationFactory(); //the factory will fetch calibration settings from different sources
            if (SLMClient.ENVIRONMENT == SLMClient.EMULATOR_ENV)
                calibration = factory.getDummyCalibation();
            else
                calibration = factory.getCalibration(); //gets the most fitting calibration for current device
        }
        return calibration;
    }

    /**
     * @return the brand
     */
    public String getBrand() {
        return brand;
    }

    /**
     * @return the model
     */
    public String getModel() {
        return model;
    }

    /**
     * @return the modelVersion
     */
    public String getModelVersion() {
        return modelVersion;
    }

    /**
     * @return the firmwareVersion
     */
    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    /**
     * @return the platform
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * @return the platformVersion
     */
    public String getPlatformVersion() {
        return platformVersion;
    }

    /**
     * @return the javaPlatform
     */
    public String getJavaPlatform() {
        return javaPlatform;
    }

    /**
     * @return the javaPlatformVersion
     */
    public String getJavaPlatformVersion() {
        return javaPlatformVersion;
    }

    public abstract String getIMEI();

    public abstract String getDataFolderPath(boolean preferMemoryCard);

}
