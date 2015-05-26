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

import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.noisetube.api.NTClient;
import net.noisetube.api.audio.calibration.Calibration;
import net.noisetube.api.config.NTAccount;
import net.noisetube.api.config.Preferences;
import net.noisetube.app.util.NTUtils;

import java.util.ArrayList;

/**
 * Android specific Preferences class
 *
 * @author mstevens, sbarthol, humberto
 */
public class AndroidPreferences extends Preferences {

    public static final String PREF_TOS_ACCEPTED = "pref_tos_accepted";
    public static final String PREF_SKIPPED_SIGNIN = "pref_skipped_signin";
    public static final String PREF_CALIBRATION_STATUS_DONE = "pref_calib_stutus_done";
    public static final String PREF_WELCOME_DONE = "pref_welcome_done";
    public static final String PREF_LAST_KNOWN_LOCATION = "pref_last_known_location";
    private static final String USER_CALIBRATION = "calibration";
    //private static final String USE_DOSE_METER = "useDoseMeter";
    private static final String TAGS = "pref_tags_list";
    private static final String PREF_PAUSE_BACKGROUND = "pref_pause_background";
    private static final String USE_BATCH_HTTP = "pref_use_batch_http";
    //    private static final String PREF_SAVE_TO_FILE_WHEN_HTTP = "pref_save_http";
    private static final String PREF_SAVING_MODE = "pref_measureDataStore";
    private static final String PREF_EXTERNAL_STORE = "pref_external_store";
    private static final String PREF_DATA_POLICY = "pref_data_policy";
    private static final String PREF_MAX_TRACK_HISTORY = "pref_maxTrackHistory";
    private static final String TAG_SEPARATOR = " ";
    private static final String PREF_ACTIVE_ACCOUNT = "pref_active_account";
    private static final String PREF_NO_STORE = "pref_no_store";
    private static final String PREF_LOCAL_STORE = "pref_local_store";
    private static final String PREF_NOISETUBE_STORE = "pref_noisetube_store";
    private static final String PREF_USE_GPS = "pref_gps";


    private static SharedPreferences settings = null;
    private static AndroidPreferences instance;


    private boolean tosAccepted = false;
    private boolean skippedSignIn = true;
    private boolean welcomeDone = false;
    private boolean calibrationStatusDone = false;


    public AndroidPreferences(ContextWrapper ctx) {
        super();
//        settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        instance = this;

        loadFromStorage(); //load saved user settings

    }

    public static AndroidPreferences getInstance() {
        return instance;
    }


    public void dispose() {
        instance = null;
    }

    @Override
    public void loadFromStorage() {

        try {

            //Account
            String serializedAccount = settings.getString(PREF_ACTIVE_ACCOUNT, null);

            if (serializedAccount != null) {
                account = NTAccount.deserialise(serializedAccount);
            }

            tosAccepted = settings.getBoolean(PREF_TOS_ACCEPTED, false);

            trackHistoryValue = Integer.valueOf(settings.getString(PREF_MAX_TRACK_HISTORY, "10"));
            dataPolicy = settings.getBoolean(PREF_DATA_POLICY, true);

            skippedSignIn = settings.getBoolean(PREF_SKIPPED_SIGNIN, false);
            welcomeDone = settings.getBoolean(PREF_WELCOME_DONE, false);
            calibrationStatusDone = settings.getBoolean(PREF_CALIBRATION_STATUS_DONE, false);

            //IO

            setPreferMemoryCard(settings.getBoolean(PREF_EXTERNAL_STORE, true));
            settings.edit().putBoolean(PREF_EXTERNAL_STORE, isPreferMemoryCard()).commit();

//            int value = Integer.valueOf(settings.getString(PREF_SAVING_MODE, "2"));

            boolean pref_local_store = settings.getBoolean(PREF_LOCAL_STORE, true);
            boolean pref_noisetube_store = settings.getBoolean(PREF_NOISETUBE_STORE, true);

            if (pref_local_store && pref_noisetube_store) {// HTTP and FILE
                if (NTUtils.supportsInternetAccess()) {
                    setSavingMode(Preferences.SAVE_HTTP); // HTTP
                    setAlsoSaveToFileWhenInHTTPMode(true);
                } else {
                    setSavingMode(Preferences.SAVE_FILE); // FILE
//                    setAlsoSaveToFileWhenInHTTPMode(false);
                    settings.edit().putBoolean("pref_noisetube_store", false).commit(); // updating file config
                }

            } else if (pref_local_store) {
                setSavingMode(Preferences.SAVE_FILE); // FILE
            } else if (pref_noisetube_store) {
                setSavingMode(Preferences.SAVE_HTTP); // FILE
            } else {
                setSavingMode(Preferences.SAVE_NO);
            }

            boolean gps = settings.getBoolean(PREF_USE_GPS, true);

            setUseGPS(gps);


            //setAlsoSaveToFileWhenInHTTPMode(settings.getBoolean(PREF_SAVE_TO_FILE_WHEN_HTTP, alsoSaveToFileWhenInHTTPMode));

//            setAlwaysUseBatchModeForHTTP(settings.getBoolean(USE_BATCH_HTTP, alwaysUseBatchModeForHTTP));

            //UI
//            setPauseWhenInBackground(settings.getBoolean(PREF_PAUSE_BACKGROUND, pauseWhenInBackground));

            //TAGS
            for (String tag : settings.getString(TAGS, "").split(TAG_SEPARATOR))
                if (!tag.equals(""))
                    previousTags.add(tag);

            //AUDIO
            //setUseDoseMeter(settings.getBoolean(USE_DOSE_METER, false));
            String calibrationString = settings.getString(USER_CALIBRATION, null);
            if (calibrationString != null) {
                Calibration parsedCal = NTClient.getInstance().getCalibrationParser().parseCalibration(calibrationString, Calibration.SOURCE_USER_PREFERECES);
                if (parsedCal != null)
                    calibration = parsedCal;
            }

        } catch (Exception e) {
            log.error(e, "AndroidPreferences.java -- loadFromStorage");
        }
    }

    public void resetPreferences() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREF_LOCAL_STORE, true);
        editor.putBoolean(PREF_NOISETUBE_STORE, true);
        editor.putBoolean(PREF_EXTERNAL_STORE, true);
        editor.putBoolean(PREF_NO_STORE, false);
        editor.putBoolean(PREF_TOS_ACCEPTED, false);
        editor.putBoolean(PREF_SKIPPED_SIGNIN, false);
        editor.putBoolean(PREF_CALIBRATION_STATUS_DONE, false);
        editor.putString(PREF_MAX_TRACK_HISTORY, "10");
        editor.putString(PREF_ACTIVE_ACCOUNT, null);
        editor.commit();

    }

    public boolean isLoginRequired() {
        return (savingMode == Preferences.SAVE_HTTP && !isAuthenticated());
    }

    public boolean isLocationRequired() {
        return (savingMode == Preferences.SAVE_HTTP || savingMode == Preferences.SAVE_HTTP && !NTUtils.supportsPositioning());
    }


    public void setSavingModeAndPersist(int savingMode) {
        super.setSavingMode(savingMode);
        settings.edit().putString(PREF_SAVING_MODE, String.valueOf(savingMode)).commit();
    }

    public void setUseGPSAndPersist(boolean enable) {
        super.setUseGPS(enable);
        settings.edit().putBoolean(PREF_USE_GPS, enable).commit();
    }

    @Override
    public void setAccount(NTAccount account) {
        super.setAccount(account);
        if (account != null) {
            settings.edit().putString(PREF_ACTIVE_ACCOUNT, account.serialise()).commit();
        } else {
            settings.edit().putString(PREF_ACTIVE_ACCOUNT, null).apply();
            settings.edit().putBoolean(PREF_TOS_ACCEPTED, false).apply();
            settings.edit().putBoolean(PREF_SKIPPED_SIGNIN, false).apply();
        }
    }

    public void updateTagsList(ArrayList<String> tags) {

        for (String tag : tags) {
            if (!previousTags.contains(tag))
                previousTags.add(tag);
        }
        //TAGS
        StringBuilder bff = new StringBuilder();
        int up = previousTags.size() - 1;
        for (int i = 0; i < up; i++) {
            bff.append(previousTags.get(i));
            bff.append(TAG_SEPARATOR);
        }

        if (up >= 0) {
            bff.append(previousTags.get(up));
        }

        settings.edit().putString(TAGS, bff.toString()).commit();
    }

    public boolean isTosAccepted() {
        return tosAccepted;
    }

    public void markTosAccepted() {
        tosAccepted = true;
        settings.edit().putBoolean(PREF_TOS_ACCEPTED, true).commit();
    }

    public boolean hasSkippedSignIn() {
        return skippedSignIn;
    }

    public void markSkippedSignIn() {
        skippedSignIn = true;
        settings.edit().putBoolean(PREF_SKIPPED_SIGNIN, true).commit();
    }

    public boolean isWelcomeDone() {
        return welcomeDone;
    }

    public void markWelcomeDone() {
        welcomeDone = true;
        settings.edit().putBoolean(PREF_WELCOME_DONE, true).commit();
    }

    public boolean isCalibrationStatusDone() {
        return calibrationStatusDone;
    }

    public void markCalibrationStatusDone() {
        calibrationStatusDone = true;
        settings.edit().putBoolean(PREF_CALIBRATION_STATUS_DONE, true).commit();
    }


}
