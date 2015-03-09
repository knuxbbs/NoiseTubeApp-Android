/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Android version)
 *
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2012
 *  Portions contributed by University College London (ExCiteS group), 2012
 *  Android port by Vrije Universiteit Brussel (BrusSense team), 2010-2012
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

package net.noisetube.app.core;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.location.LocationManager;

import net.noisetube.api.NTClient;
import net.noisetube.api.audio.AudioStreamListener;
import net.noisetube.api.audio.AudioStreamSaver;
import net.noisetube.api.audio.calibration.CalibrationsParser;
import net.noisetube.api.audio.recording.AudioRecorder;
import net.noisetube.api.audio.recording.AudioSpecification;
import net.noisetube.api.config.Device;
import net.noisetube.api.config.Preferences;
import net.noisetube.api.io.FileWriter;
import net.noisetube.api.io.HttpClient;
import net.noisetube.api.location.GeoTagger;
import net.noisetube.api.model.NTCoordinates;
import net.noisetube.api.model.Track;
import net.noisetube.app.audio.AndroidAudioRecorder;
import net.noisetube.app.audio.AndroidAudioStreamSaver;
import net.noisetube.app.audio.calibration.DOMCalibrationsParser;
import net.noisetube.app.config.AndroidDevice;
import net.noisetube.app.config.AndroidPreferences;
import net.noisetube.app.io.AndroidFileWriter;
import net.noisetube.app.io.AndroidHttpClient;
import net.noisetube.app.io.FileAccess;
import net.noisetube.app.location.AndroidGeoTagger;
import net.noisetube.app.location.AndroidNTCoordinates;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author sbarthol, mstevens
 */
public class AndroidNTClient extends NTClient implements Serializable {

    //STATICS--------------------------------------------------------------------------------------

    static public final int DEFAULT_BUFFER_CAPACITY = 100;

    static {    //TODO comment both out for web release:
        //NTClient.ENVIRONMENT = NTClient.PHONE_DEV_ENV;
        //NTClient.ENVIRONMENT = NTClient.EMULATOR_ENV;
    }

    //Client info
    static public String CLIENT_TYPE = "NoiseTubeMobileAndroid";
    static public String CLIENT_VERSION = "v1.4.0";
    static public String CLIENT_BUILD_DATE = "<unknown build time>"; //will be set upon construction
    static public boolean CLIENT_IS_TEST_VERSION = false; //TODO change to false for web release
    //DYNAMICS-------------------------------------------------------------------------------------
    private ContextWrapper contextWrapper;


    public AndroidNTClient(ContextWrapper contextWrapper) throws Exception {
        super(CLIENT_TYPE, "v" + getAppVersion(contextWrapper), getAppBuildDate(contextWrapper), CLIENT_IS_TEST_VERSION);
        this.contextWrapper = contextWrapper;
        initialize(); //!!! DO NOT REMOVE


    }


    static public AndroidNTClient getInstance() {
        return (AndroidNTClient) INSTANCE;

    }

    /**
     * Gets the software version retrieved from the Manifest.
     */
    private static String getAppVersion(ContextWrapper contextWrapper) {
        try {
            PackageInfo packageInfo = contextWrapper.getPackageManager().getPackageInfo(contextWrapper.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            return CLIENT_VERSION;
        }
    }

    /**
     * Gets the software version retrieved from the Manifest.
     */
    private static String getAppBuildDate(ContextWrapper contextWrapper) {
        try {
            PackageInfo packageInfo = contextWrapper.getPackageManager().getPackageInfo(contextWrapper.getPackageName(), 0);
            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
            String date = DATE_FORMAT.format(new Date(packageInfo.lastUpdateTime));
            CLIENT_BUILD_DATE = date;
            return date;
        } catch (Exception e) {
            return CLIENT_VERSION;
        }
    }

    /**
     * @return the contextWrapper
     */
    public ContextWrapper getContextWrapper() {
        return contextWrapper;
    }

    @Override
    protected Device createDevice() {
        return new AndroidDevice(contextWrapper);
    }

    @Override
    protected Preferences createPreferences() {
        AndroidPreferences p = new AndroidPreferences(contextWrapper);

        if (ENVIRONMENT != PHONE_PROD_ENV) {
            //p.setAccount(new NTAccount("SOME_USERNAME_TO_TEST_WITH", "API_KEY_CORRESPONDING_TO_USERNAME"));
            p.setSavingMode(Preferences.SAVE_HTTP);
            //p.setSavingMode(Preferences.SAVE_FILE);
            p.setAlsoSaveToFileWhenInHTTPMode(true);
            if (ENVIRONMENT == EMULATOR_ENV) //Emulator has no GPS
                p.setUseGPS(false);
            return p;
        } else
            return p;
    }

    @Override
    protected GeoTagger createGeoTagger() {
        return new AndroidGeoTagger((LocationManager) contextWrapper.getSystemService(Context.LOCATION_SERVICE));
    }

    @Override
    public AudioSpecification deserialiseAudioSpecification(String serialisedAudioSpec) {
        return AudioSpecification.deserialise(serialisedAudioSpec);
    }

    @Override
    public AudioRecorder getAudioRecorder(AudioSpecification audioSpec, int recordTimeMS, AudioStreamListener listener) {
        audioRecorder = new AndroidAudioRecorder(audioSpec, recordTimeMS, listener);
        return audioRecorder;
    }

    @Override
    public AudioStreamSaver getAudioStreamSaver() {
        return new AndroidAudioStreamSaver(preferences.getDataFolderPath());
    }

	/*@Override
    public SoundLevelMeter getCalibratedSLM(AudioSpecification audioSpec, Calibration calibration, MeasurementListener listener) throws Exception
	{
		return new AndroidSoundLevelMeter(audioSpec, calibration, listener);
	}*/

    @Override
    public FileWriter getFileWriter(String filePath, String characterEncoding) {
        if (device.supportsFileAccess()) {
            return new AndroidFileWriter(filePath, characterEncoding);
        } else
            return null;
    }

    /**
     * @param filePath
     * @return an inputstream is the file exists, null if it does not (or if an error occurred)
     */
    @Override
    public InputStream getFileInputStream(String filePath) {
        //must return FileInputStream instance (open File for reading first)
        File folder = FileAccess.getFolder(FileAccess.getFolderPath(filePath));
        try {
            File file = new File(folder, FileAccess.getFileName(filePath));
            if (file.exists())
                return new FileInputStream(file);
            else
                return null;
        } catch (Exception e) {
            log.error(e, "AndroidNTClient.getFileInputStream() for " + filePath);
            return null;
        }
    }


    @Override
    public HttpClient getHttpClient(String agent) {
        return new AndroidHttpClient(agent);
    }

    @Override
    public NTCoordinates getNTCoordinates(double latitude, double longitude, double altitude) {
        return new AndroidNTCoordinates(latitude, longitude, altitude);
    }

    @Override
    protected void additionalTrackAnnotating(Track track) {
        //nothing (for now)
    }


    @Override
    public CalibrationsParser getCalibrationParser() {
        return new DOMCalibrationsParser();
    }

    @Override
    public String additionalErrorReporting(Throwable t) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw, true);
            t.printStackTrace(pw);
            pw.flush();
            sw.flush();
            return sw.toString();
        } catch (Exception e) {
            log.error(e, "additionalErrorReporting");
            //return "exception upon printing stack trace";
            return null;
        }
    }


    @Override
    public void addTrackProcessors(Track track) {
        //Nothing for now
    }

}
