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
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import net.noisetube.R;
import net.noisetube.api.config.Preferences;
import net.noisetube.api.util.Logger;
import net.noisetube.app.config.AndroidPreferences;


/**
 * @author Humberto
 */
public class BasicSettingsActivity extends SimpleActionBarActivity {
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
                BasicSettingsActivity.this.finish();
            }
        });


        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.main_content, new SettingsFragment())
                    .commit();
        }
    }


    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        SharedPreferences sp;
        private AndroidPreferences pref;

        public SettingsFragment() {
            pref = AndroidPreferences.getInstance();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.basic_preferences);

            sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sp.registerOnSharedPreferenceChangeListener(this);
//            findPreference("pref_local_store").setSelectable(true);
//            findPreference("pref_no_store").setSelectable(true);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sp.unregisterOnSharedPreferenceChangeListener(this);
        }


        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            boolean pref_local_store = sp.getBoolean("pref_local_store", true);
            if (pref_local_store) {
                sp.edit().putBoolean("pref_no_store", false).commit();
                pref.setSavingMode(Preferences.SAVE_FILE);
                pref.setAlsoSaveToFileWhenInHTTPMode(false);
            } else {
                pref.setSavingMode(Preferences.SAVE_NO);
                sp.edit().putBoolean("pref_no_store", true).commit();
            }
        }
    }
}
