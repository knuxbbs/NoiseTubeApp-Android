package net.noisetube.app.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import net.noisetube.R;
import net.noisetube.app.config.AndroidPreferences;
import net.noisetube.app.core.AndroidNTService;
import net.noisetube.app.util.DialogUtils;
import net.noisetube.app.util.NTUtils;

public class SplashActivity extends ActionBarActivity {

    private static SplashActivity instance;
    boolean serviceCreated = false;
    private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            serviceCreated = intent.getBooleanExtra("CREATED", false);
            if (serviceCreated) {
                initApp();
            } else {
                Toaster.displayToast("Error during the initialization process. Check your logs files.");
                finish();
            }
        }
    };
    private IntentFilter intentFilter;

    public static SplashActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        instance = this;

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (NTUtils.supportsPositioning(this)) {
            startService();
        } else {
            DialogUtils.showLocationDialog(this);
        }


    }

    public void startService() {
        if (AndroidNTService.getInstance() != null) {
            AndroidNTService.getInstance().destroyService();
        }
        startService(new Intent(getBaseContext(), AndroidNTService.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        //---intent to filter for AndoridNTService---
        intentFilter = new IntentFilter();
        intentFilter.addAction("SERVICE_CREATION_ACTION");
        //---register the receiver---
        registerReceiver(intentReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //---unregister the receiver---
        unregisterReceiver(intentReceiver);
    }

    private void initApp() {

        Intent intentAct;
        AndroidPreferences pref = AndroidPreferences.getInstance();
        // Check if the terms of use has been accepted; if not, show it.
        if (!pref.isTosAccepted()) {
            intentAct = new Intent(SplashActivity.this, WelcomeActivity.class);

        } else if (!pref.isAuthenticated() && !pref.hasSkippedSignIn()) {
            intentAct = new Intent(SplashActivity.this, LoginActivity.class);

        } else {
            intentAct = new Intent(SplashActivity.this, MainActivity.class);
        }
        startActivity(intentAct);
        finish();
    }
}
