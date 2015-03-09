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

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.IntentCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;

import net.noisetube.R;
import net.noisetube.api.exception.AuthenticationException;
import net.noisetube.api.io.NTWebAPI;
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
                navigateUpToFromChild(SettingsActivity.this,
                        IntentCompat.makeMainActivity(new ComponentName(SettingsActivity.this,
                                MainActivity.class)));
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

        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sp.unregisterOnSharedPreferenceChangeListener(this);
        }


        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            if (key.equals("pref_pause_background")) {
                pref.setPauseWhenInBackground(!pref.isPauseWhenInBackground());
            }
            if (key.equals("pref_use_batch_http")) {
                pref.setAlwaysUseBatchModeForHTTP(!pref.isAlwaysUseBatchModeForHTTP());

            } else if (key.equals("pref_external_store")) {
                pref.setPreferMemoryCard(!pref.isPreferMemoryCard());
            } else if (key.equals("pref_measureDataStore")) {
                int value = Integer.valueOf(sharedPreferences.getString("pref_measureDataStore", "2"));
                if (value == 1) {
                    pref.setAlsoSaveToFileWhenInHTTPMode(true);
                } else {
                    pref.setSavingMode(value);
                }

            } else if (key.equals("pref_data_policy")) {

                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        //TODO call to the update service of data policy
                        NTWebAPI ntWebAPI = new NTWebAPI(AndroidPreferences.getInstance().getAccount());
                        try {
                            ntWebAPI.changeDataPolicySetting(pref.isDataPolicy());
                        } catch (AuthenticationException e) {
                            log.error(e, "onSharedPreferenceChanged");
                        }
                        return null;
                    }
                };
                task.execute();
                pref.setDataPolicy(sharedPreferences.getBoolean("pref_data_policy", true));


            } else if (key.equals("pref_maxTrackHistory")) {
                int capacity = Integer.valueOf(sharedPreferences.getString("pref_maxTrackHistory", "10"));
                pref.setTrackHistoryValue(capacity);
                AndroidNTService.getInstance().setUserTracesCapacity(capacity);
            }

        }
    }
}