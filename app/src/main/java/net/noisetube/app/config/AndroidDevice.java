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

package net.noisetube.app.config;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.ContextWrapper;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;

import net.noisetube.api.audio.recording.AudioFormat;
import net.noisetube.api.audio.recording.AudioSpecification;
import net.noisetube.api.config.Device;
import net.noisetube.app.audio.AndroidAudioSpecification;

/**
 * @author mstevens, sbarthol
 */
public class AndroidDevice extends Device {
    // relativeDataFolderPath is the relative path where data should be stored
    // It uses the dataFolderPath as a starting point
    private static final String relativeDataFolderPath = "/data/";
    private static final String tracksFolderPath = "tracks/";
    private ContextWrapper contextWrapper;
    private String androidDeviceInfo;


    public AndroidDevice(ContextWrapper cw) {
        super();
        this.contextWrapper = cw;
    }

    /**
     * Returns if their is a memory card connected to the device
     *
     * @return true, false in other case
     */
    public static boolean isMemoryCardPresent() {
        //Check if an external storage is connected:
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    @Override
    protected void identifyDevice() {
        androidDeviceInfo = Build.BRAND + "|" + Build.MANUFACTURER + "|" + Build.MODEL + "|" + Build.PRODUCT + "|" + Build.DEVICE + "|" + Build.DISPLAY + "|" + Build.VERSION.CODENAME + "|" + Build.VERSION.INCREMENTAL + "|" + Build.VERSION.SDK_INT;
        /*Examples:
         * DESCRIPTION					BRAND		MANUFACTURER	MODEL		PRODUCT			DEVICE	DISPLAY		VERSION.CODENAME	VERSION.INCREMENTAL			VERSION.SDK_INT
		 * ------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		 * HTC Desire Z					htc_wwe		HTC				HTC Vision	htc_vision		vision	FRG83D		REL					317545						8
		 * HTC Desize Z (after update)	htc_wwe		HTC			2	HTC Vision	htc_vision		vision	GRI40		REL					192788.4					10
		 * Sony Ericsson Xperia X10 	SEMC		Sony Ericsson	X10i		X10i_1235-7836	X10		2.1.A.0.435	REL					TP7d						7
		 * HTC Hero (stock firmware) 	htc_be		HTC				HTC Hero	htc_hero		hero	ERE27		REL					327574						7
		 * HTC Hero (cyanogen firmware) htc			HTC				Hero		cyanogen_hero	hero	GWK74		REL					eng.aria.20111009.220759	10
		 */

        brand = Build.MANUFACTURER; //used to be BRAND in versions prior to v1.2.3 (the server has a hack that delivers a calibration.xml file with BRAND instead of MANUFACTURER to these versions)
        model = Build.MODEL;
        modelVersion = "unknown";
        firmwareVersion = "unknown";
        platform = "Android";
        platformVersion = Integer.toString(Build.VERSION.SDK_INT); //= API level; use Build.VERSION.RELEASE (e.g. "2.2") instead?
        javaPlatform = "Dalvik";
        javaPlatformVersion = "unknown";

        //Brand specific hacks
        if (brand.equalsIgnoreCase("HTC")) {
            //Strip "HTC " from model
            if (model.substring(0, 4).equalsIgnoreCase("HTC "))
                model = model.substring(4);
        }



		/*//Java info (debug)
        log.debug("java.version: " + System.getProperty("java.version"));
		log.debug("java.specification.version: " + System.getProperty("java.specification.version"));
		log.debug("java.specification.vendor: " + System.getProperty("java.specification.vendor"));
		log.debug("java.specification.name: " + System.getProperty("java.specification.name"));
		log.debug("java.vm.version: " + System.getProperty("java.vm.version"));
		log.debug("java.vm.vendor: " + System.getProperty("java.vm.vendor"));
		log.debug("java.vm.name: " + System.getProperty("java.vm.name"));
		log.debug("java.vm.specification.version: " + System.getProperty("java.vm.specification.version"));
		log.debug("java.vm.specification.vendor: " + System.getProperty("java.vm.specification.vendor"));
		log.debug("java.vm.specification.name: " + System.getProperty("java.vm.specification.name"));
		log.debug("os.version: " + System.getProperty("os.version"));	*/
    }

    @Override
    public void logIdentification() {
        super.logIdentification();
        log.debug("Android device info: " + androidDeviceInfo);
    }

    @Override
    public boolean supportsFileAccess() {
        return true; //TODO check if permission is set
    }

    /**
     * 2
     * Returns the path to the internal data folder root for this package
     *
     * @return internal data folder root. e.g. "/data/data/com.noisetube/files"
     */
    public String getInternalRoot() {
        if (supportsFileAccess())
            return contextWrapper.getFilesDir().getAbsolutePath();
        else
            return null;
    }

    /**
     * Returns the path to the memory card root for this package
     *
     * @return memory card root. e.g. "/mnt/sdcard/com.noisetube/files/"
     */
    public String getMemoryCardRoot() {
        if (supportsFileAccess() && isMemoryCardPresent())
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + contextWrapper.getPackageName() + "/files";
        else
            return null;
    }

    @Override
    public boolean supportsPositioning() {
        final LocationManager manager = (LocationManager) contextWrapper.getSystemService(Context.LOCATION_SERVICE);
        boolean status = false;
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            status = true;
        }
        return status;
    }

    @Override
    public boolean supportsBluetooth() {
        return (BluetoothAdapter.getDefaultAdapter().getState() == BluetoothAdapter.STATE_ON);
    }

    @Override
    public boolean supportsInternetAccess() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) contextWrapper.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * @return the dataFolderPath (e.g. "/path/to/memory/root/my/data/")
     */
    @Override
    public String getDataFolderPath(boolean preferMemoryCard) {
        String path = null;
        try {
            if (preferMemoryCard) {
                String memoryCardRoot = getMemoryCardRoot();
                if (memoryCardRoot != null) {
                    path = memoryCardRoot + relativeDataFolderPath;
                }
            }
            if (path == null)
                //memory card is not preferred or we could not substract the path
                path = getInternalRoot() + relativeDataFolderPath;
        } catch (Exception e) {
            log.error(e, "datafolder could not be made");
            path = getInternalRoot() + relativeDataFolderPath;
        }
        return path;
    }

    @Override
    public String getIMEI() {
        TelephonyManager manager = (TelephonyManager) contextWrapper.getSystemService(Context.TELEPHONY_SERVICE);
        return manager.getDeviceId();
    }

    @Override
    protected AudioSpecification getSuitableAudioSpecification() {
        AUDIO_RECORDING_TEST_DURATION_MS = 1000; //default time (250ms) seems to result in missing samples on Android phones
        for (int s = 0; s < AudioFormat.SAMPLE_RATES.length; s++) {
            AudioSpecification as = new AndroidAudioSpecification(AudioFormat.SAMPLE_RATES[s], 16, AudioFormat.CHANNELS_MONO);
            if (testAudioSpecification(as))
                return as; //working spec found, return it
        }
        //if we get here no suitable audio specification was found
        return null;
    }

    @Override
    public boolean hasTouchScreen() {
        //TODO do actual system check
        return true;
    }

    /**
     * @return the androidDeviceInfo
     */
    public String getAndroidDeviceInfo() {
        return androidDeviceInfo;
    }

}
