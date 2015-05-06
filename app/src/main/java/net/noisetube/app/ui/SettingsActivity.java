/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.noisetube.app.ui;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import net.noisetube.R;
import net.noisetube.api.config.Preferences;
import net.noisetube.api.util.Logger;
import net.noisetube.app.config.AndroidPreferences;
import net.noisetube.app.core.AndroidNTService;


/**
 * @author Humberto
 */
public class SettingsActivity extends SimpleActionBarActivity {
    private static Logger log = Logger.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = getActionBarToolbar();
        toolbar.setTitle(R.string.title_activity_settings);
        toolbar.setNavigationIcon(R.drawable.ic_up);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                navigateUpToFromChild(SettingsActivity.this,
//                        IntentCompat.makeMainActivity(new ComponentName(SettingsActivity.this,
//                                MainActivity.class)));
                SettingsActivity.this.finish();
            }
        });


        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.main_content, new SettingsFragment())
                    .commit();
        }
    }


    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private AndroidPreferences pref;

        public SettingsFragment() {
            pref = AndroidPreferences.getInstance();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sp.registerOnSharedPreferenceChangeListener(this);
            setStoreOptions(sp);

        }

        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            setStoreOptions(PreferenceManager.getDefaultSharedPreferences(getActivity()));
        }

        private void setStoreOptions(SharedPreferences sp) {

            boolean pref_no_store = sp.getBoolean("pref_no_store", false);
            boolean pref_local_store = sp.getBoolean("pref_local_store", true);
            boolean pref_noisetube_store = sp.getBoolean("pref_noisetube_store", true);

            findPreference("pref_local_store").setEnabled(!pref_no_store);
            findPreference("pref_noisetube_store").setEnabled(!pref_no_store);
            findPreference("pref_external_store").setEnabled(!pref_no_store && pref_local_store);
            findPreference("pref_no_store").setEnabled(pref_no_store || ((!pref_local_store && !pref_noisetube_store)));
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sp.unregisterOnSharedPreferenceChangeListener(this);
        }


        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

//            if (key.equals("pref_pause_background")) {
//                pref.setPauseWhenInBackground(!pref.isPauseWhenInBackground());
//            }
//            if (key.equals("pref_use_batch_http")) {
//                pref.setAlwaysUseBatchModeForHTTP(!pref.isAlwaysUseBatchModeForHTTP());
//
//            } else

            if (key.equals("pref_external_store")) {
                pref.setPreferMemoryCard(!pref.isPreferMemoryCard());
            } else if (key.equals("pref_local_store") || key.equals("pref_noisetube_store")) { //

                boolean pref_local_store = sharedPreferences.getBoolean("pref_local_store", true);
                boolean pref_noisetube_store = sharedPreferences.getBoolean("pref_noisetube_store", true);
                boolean pref_no_store = sharedPreferences.getBoolean("pref_no_store", false);

                if (pref_local_store && pref_noisetube_store) { // both
                    pref.setSavingMode(Preferences.SAVE_HTTP);
                    pref.setAlsoSaveToFileWhenInHTTPMode(true);
                } else if (pref_local_store) { //
                    pref.setSavingMode(Preferences.SAVE_FILE);
                    pref.setAlsoSaveToFileWhenInHTTPMode(false);
                } else if (pref_noisetube_store) {
                    pref.setSavingMode(Preferences.SAVE_HTTP);
                    pref.setAlsoSaveToFileWhenInHTTPMode(false);
                } else if (pref_no_store) { // not store
                    pref.setAlsoSaveToFileWhenInHTTPMode(false);
                    pref.setSavingMode(Preferences.SAVE_NO);
                }

                if (!pref_local_store && sharedPreferences.getBoolean("pref_external_store", true)) {
                    sharedPreferences.edit().putBoolean("pref_local_store", false).commit();
                }

                setStoreOptions(sharedPreferences);

            } else if (key.equals("pref_no_store")) {
                pref.setSavingMode(Preferences.SAVE_NO);
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putBoolean("pref_local_store", false);
                edit.putBoolean("pref_noisetube_store", false);
                edit.putBoolean("pref_external_store", false);
                edit.commit();
                setStoreOptions(sharedPreferences);

            } else if (key.equals("pref_maxTrackHistory")) {
                int capacity = Integer.valueOf(sharedPreferences.getString("pref_maxTrackHistory", "10"));
                pref.setTrackHistoryValue(capacity);
                AndroidNTService.getInstance().setUserTracesCapacity(capacity);
            }

        }
    }
}
