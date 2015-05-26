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

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import net.noisetube.R;
import net.noisetube.api.config.Preferences;
import net.noisetube.app.config.AndroidPreferences;


public class WelcomeActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    public void onClickAccept(View v) {
        AndroidPreferences instance = AndroidPreferences.getInstance();
        instance.markTosAccepted();
        Intent intent;

        instance.setSavingModeAndPersist(Preferences.SAVE_HTTP);
        instance.setAlsoSaveToFileWhenInHTTPMode(true);
        instance.setUseGPSAndPersist(true);

        if (instance.getAccount() == null) {
            intent = new Intent(this, LoginActivity.class);

        } else {
            intent = new Intent(this, MainActivity.class);

        }
        startActivity(intent);
        finish();
    }

    public void onClickDecline(View v) {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... fields) {
                AndroidPreferences instance = AndroidPreferences.getInstance();
                instance.setSavingModeAndPersist(Preferences.SAVE_FILE);
                instance.setUseGPSAndPersist(false);
                return null;
            }
        };

        task.execute();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
