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

import net.noisetube.api.NTClient;
import net.noisetube.api.audio.calibration.Calibration;
import net.noisetube.api.util.ComboList;
import net.noisetube.api.util.Logger;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Abstract Preferences class
 *
 * @author humberto (updated)
 */

public abstract class Preferences implements Serializable {

    //STATICS----------------------------------------------
    public static final int SAVE_NO = 0;
    protected int savingMode = SAVE_NO;                        //YES
    public static final int SAVE_HTTP = 1;
    public static final int SAVE_FILE = 2;
    public static final int SAVE_SMS = 3;
    /**
     *
     */
    private static final long serialVersionUID = -5414284225185770198L;
    //DYNAMICS---------------------------------------------
    protected Logger log = Logger.getInstance();
    protected NTClient ntClient;

    //SETTINGS												CAN BE STORED?
    protected Device device;
    protected boolean dataPolicy = true;
    protected int trackHistoryValue = 10;
    //Account
    protected NTAccount account;                            //YES, if logged-in
    //IO
    protected boolean preferMemoryCard = true;                //YES
    protected String dataFolderPath = null;                    //NO!
    protected boolean alsoSaveToFileWhenInHTTPMode = false;    //YES
    protected boolean alwaysUseBatchModeForHTTP = true;    //YES

    //AUDIO
    protected Calibration calibration;                        //YES, if manually changed
    protected boolean useDoseMeter = false;                    //YES

    //TAGGING
    protected boolean pauseDuringTagTyping = true;            //YES
    protected ArrayList<String> previousTags;                //YES

    //GPS
    protected boolean useGPS = true;                        //YES
    protected boolean forceGPS = true;                        //YES
    protected boolean useCoordinateInterpolation = true;    //YES

    //UI
    protected boolean pauseWhenInBackground = false;        //YES


    /**
     * Preferences constructor
     * Sets defaults, loads stored pref's & checks/corrects consistency
     */
    public Preferences() {
        this.ntClient = NTClient.getInstance();
        this.device = ntClient.getDevice();
        if (device == null)
            throw new NullPointerException("Device must be initialized before Preferences!");
        setDefaults(); //load default settings

    }

    protected void setDefaults() {
        preferMemoryCard = true;

        // Default saving mode
        if (device.supportsInternetAccess())
            savingMode = SAVE_HTTP;
        else if (device.supportsFileAccess())
            savingMode = SAVE_FILE;
        /*
         * else if(device.supportsSMS()) savingMode = SAVE_SMS;
		 */
        else
            savingMode = SAVE_NO;

        alsoSaveToFileWhenInHTTPMode = false;

        useDoseMeter = false;

        pauseDuringTagTyping = true;
        previousTags = new ArrayList<String>();

        useGPS = device.supportsPositioning();
        forceGPS = true;
        useCoordinateInterpolation = isUseGPS();

        pauseWhenInBackground = false;

    }

    /**
     * Load saved user preferences from local storage
     */
    public abstract void loadFromStorage();

    /**
     * Save user preferences to local storage
     */
    public void saveToStorage() {
    }

    public void checkSettings() {

        if (savingMode == SAVE_FILE || (savingMode == SAVE_HTTP && alsoSaveToFileWhenInHTTPMode)) {
            if (getDataFolderPath() == null) {    //No working path found
                log.error("No accessible data folder found, file saving disabled.");
                if (savingMode == SAVE_FILE)
                    setSavingMode(SAVE_NO);
                alsoSaveToFileWhenInHTTPMode = false;
            }
        }

    }

    public boolean isPreferMemoryCard() {
        return preferMemoryCard;
    }

    public void setPreferMemoryCard(boolean preferMemoryCard) {
        if (this.preferMemoryCard != preferMemoryCard) {
            this.preferMemoryCard = preferMemoryCard;
            dataFolderPath = null; // reset folderpath!
        }
    }

    /**
     * @return the account
     */
    public NTAccount getAccount() {
        return account;
    }

    /**
     * @param account the account to set
     */
    public void setAccount(NTAccount account) {
        this.account = account;
    }

    public boolean isLoggedIn() {
        return account != null;
    }

    /**
     * @return the dataFolderPath
     */
    public String getDataFolderPath() {
        if (dataFolderPath == null)
            setDataFolderPath(device.getDataFolderPath(preferMemoryCard));
        return dataFolderPath;
    }

    /**
     * @param dataFolderPath the dataFolderPath to set
     */
    protected void setDataFolderPath(String dataFolderPath) {
        if (dataFolderPath == null) {
            log.error("dataFolderPath cannot be null");
            return;
        }
        if (this.dataFolderPath == null || !this.dataFolderPath.equals(dataFolderPath))
            log.info(" - Data folder in use: " + dataFolderPath);
        this.dataFolderPath = dataFolderPath;
    }

    public int getSavingMode() {
        return savingMode;
    }

    public void setSavingMode(int savingMode) {

        if (this.savingMode != savingMode) {
            if (savingMode >= SAVE_NO && savingMode <= SAVE_SMS) {
                this.savingMode = savingMode;
                ntClient.resetSaver(); //!!!
            } else
                throw new IllegalArgumentException("Invalid saving mode: " + savingMode);
        }
    }

    public ComboList getAvailableSavingModes() {
        ComboList saveModes = new ComboList();
        saveModes.addItem("None", SAVE_NO, (savingMode == SAVE_NO));
        if (device.supportsInternetAccess())
            saveModes.addItem("NoiseTube.net", SAVE_HTTP, (savingMode == SAVE_HTTP));
        if (device.supportsFileAccess())
            saveModes.addItem("File", SAVE_FILE, (savingMode == SAVE_FILE));
        //if(Device.supportsSMS ...)
        //	saveModes.addItem("SMS", SAVE_SMS, (savingMode == SAVE_SMS));
        return saveModes;
    }

    /**
     * @return the alsoSaveToFileWhenInHTTPMode
     */
    public boolean isAlsoSaveToFileWhenInHTTPMode() {
        return alsoSaveToFileWhenInHTTPMode;
    }

    /**
     * @param alsoSaveToFileWhenInHTTPMode the alsoSaveToFileWhenInHTTPMode to set
     */
    public void setAlsoSaveToFileWhenInHTTPMode(boolean alsoSaveToFileWhenInHTTPMode) {
        if (this.alsoSaveToFileWhenInHTTPMode != alsoSaveToFileWhenInHTTPMode) {
            this.alsoSaveToFileWhenInHTTPMode = alsoSaveToFileWhenInHTTPMode;
            ntClient.resetSaver(); //!!!
        }
    }

    /**
     * @return the alwaysUseBatchModeForHTTP
     */
    public boolean isAlwaysUseBatchModeForHTTP() {
        return alwaysUseBatchModeForHTTP;
    }

    /**
     * @param alwaysUseBatchModeForHTTP the alwaysUseBatchModeForHTTP to set
     */
    public void setAlwaysUseBatchModeForHTTP(boolean alwaysUseBatchModeForHTTP) {
        this.alwaysUseBatchModeForHTTP = alwaysUseBatchModeForHTTP;
    }

    /**
     * @return the pauseDuringTagTyping
     */
    public boolean isPauseDuringTagTyping() {
        return pauseDuringTagTyping;
    }

    /**
     * @param pauseDuringTagTyping the pauseDuringTagTyping to set
     */
    public void setPauseDuringTagTyping(boolean pauseDuringTagTyping) {
        this.pauseDuringTagTyping = pauseDuringTagTyping;
    }

    public ArrayList<String> getTags() {
        return previousTags;
    }

    public void addTag(String tag) {
        if (previousTags == null) //just to sure
            previousTags = new ArrayList<String>();
        previousTags.add(tag);
    }

    public boolean isUseGPS() {
        return useGPS;
    }

    public void setUseGPS(boolean useGPS) {
        this.useGPS = useGPS;
    }

    /**
     * @return the forceGPS
     */
    public boolean isForceGPS() {
        return forceGPS;
    }

    /**
     * @param forceGPS the forceGPS to set
     */
    public void setForceGPS(boolean forceGPS) {
        this.forceGPS = forceGPS;
    }

    /**
     * @return the useCoordinateInterpolation
     */
    public boolean isUseCoordinateInterpolation() {
        return useCoordinateInterpolation;
    }

    /**
     * @param useCoordinateInterpolation the useCoordinateInterpolation to set
     */
    public void setUseCoordinateInterpolation(boolean useCoordinateInterpolation) {
        this.useCoordinateInterpolation = useCoordinateInterpolation;
    }

    public Calibration getCalibration() {
        if (calibration == null) {    //not loaded from stored Pref's, so ask one from the Device
            calibration = device.getCalibration();
        }
        return calibration;
    }

    public boolean isUseDoseMeter() {
        return useDoseMeter;
    }

    public void setUseDoseMeter(boolean useDoseMeter) {
        this.useDoseMeter = useDoseMeter;
    }

    /**
     * @return the pauseWhenInBackground
     */
    public boolean isPauseWhenInBackground() {
        return pauseWhenInBackground;
    }

    /**
     * @param pauseWhenInBackground the pauseWhenInBackground to set
     */
    public void setPauseWhenInBackground(boolean pauseWhenInBackground) {
        this.pauseWhenInBackground = pauseWhenInBackground;
    }

    public boolean isAuthenticated() {
        return (account != null);
    }

    public boolean isDataPolicy() {
        return dataPolicy;
    }

    public void setDataPolicy(boolean dataPolicy) {
        this.dataPolicy = dataPolicy;
    }


    public int getTrackHistoryValue() {
        return trackHistoryValue;
    }

    public void setTrackHistoryValue(int trackHistoryValue) {
        this.trackHistoryValue = trackHistoryValue;
    }
}