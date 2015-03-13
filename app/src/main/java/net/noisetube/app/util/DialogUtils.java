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

package net.noisetube.app.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.noisetube.R;
import net.noisetube.app.ui.SplashActivity;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * This is a set of helper methods for showing contextual help information in the app.
 */
public class DialogUtils {

    public static void showAbout(Activity activity) {
        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog_about");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        new AboutDialog().show(ft, "dialog_about");
    }

    public static void showHelp(Activity activity) {
        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog_help");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        new HelpDialog().show(ft, "dialog_help");
    }

    public static LoadingDialog showLoading(Activity activity) {
        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog_loading");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        LoadingDialog d = new LoadingDialog();
        d.show(ft, "dialog_loading");

        return d;
    }

    public static void showInternetDialog(Activity activity) {
        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog_internet");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        InternetDialog d = new InternetDialog();
        d.show(ft, "dialog_internet");

    }

    public static void showMapInternetDialog(Activity activity) {
        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog_map_internet");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        MapInternetDialog d = new MapInternetDialog();
        d.show(ft, "dialog_map_internet");

    }

    public static void showLocationDialog(Activity activity) {
        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog_location");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        LocationDialog d = new LocationDialog();


        d.show(ft, "dialog_location");

    }

    public static class AboutDialog extends DialogFragment {

        private static final String UNAVAILABLE = "Version N/A. Build date: N/A";

        public AboutDialog() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get app version
            PackageManager pm = getActivity().getPackageManager();
            String packageName = getActivity().getPackageName();
            String versionDetails;
            String buildDate;
            try {
                PackageInfo info = pm.getPackageInfo(packageName, 0);
                SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
                String date = DATE_FORMAT.format(new Date(info.lastUpdateTime));
                versionDetails = "Version: " + info.versionName + "    Build date: " + date;
            } catch (PackageManager.NameNotFoundException e) {
                versionDetails = UNAVAILABLE;

            }


            // Build the about body view and append the link to see OSS licenses
            SpannableStringBuilder aboutBody = new SpannableStringBuilder();
            aboutBody.append(Html.fromHtml(getString(R.string.about_body, versionDetails)));


            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            TextView aboutBodyView = (TextView) layoutInflater.inflate(R.layout.dialog_about, null);
            aboutBodyView.setText(aboutBody);
            aboutBodyView.setMovementMethod(new LinkMovementMethod());

            return new AlertDialog.Builder(getActivity()).setIcon(R.drawable.ic_launcher)
                    .setTitle(R.string.title_about)
                    .setView(aboutBodyView)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            }
                    )
                    .create();
        }
    }

    public static class HelpDialog extends DialogFragment {

        private static final String VERSION_UNAVAILABLE = "N/A";

        public HelpDialog() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {


            // Get app version
            PackageManager pm = getActivity().getPackageManager();
            String packageName = getActivity().getPackageName();
            String versionName;
            try {
                PackageInfo info = pm.getPackageInfo(packageName, 0);
                versionName = info.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                versionName = VERSION_UNAVAILABLE;
            }

            // Build the about help view
            SpannableStringBuilder aboutBody = new SpannableStringBuilder();
            aboutBody.append(Html.fromHtml(getString(R.string.help_body, versionName)));


            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            TextView aboutBodyView = (TextView) layoutInflater.inflate(R.layout.dialog_help, null);
            aboutBodyView.setText(aboutBody);
            aboutBodyView.setMovementMethod(new LinkMovementMethod());

            return new AlertDialog.Builder(getActivity()).setIcon(R.drawable.ic_launcher)
                    .setTitle(R.string.title_help)
                    .setView(aboutBodyView)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            }
                    )
                    .create();
        }
    }

    public static class LoadingDialog extends DialogFragment {
        private static LoadingDialog instance;


        public LoadingDialog() {
        }

        public static LoadingDialog getInstance() {
            return instance;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);

            LinearLayout body = (LinearLayout) layoutInflater.inflate(R.layout.dialog_loading, null);

            return new AlertDialog.Builder(getActivity()).setView(body)
                    .setPositiveButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                    getActivity().finish();
                                }
                            }
                    )
                    .create();
        }
    }

    public static class InternetDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);

            TextView body = (TextView) layoutInflater.inflate(R.layout.dialog_text_view, null);
            body.setText(getActivity().getText(R.string.msg_internet_warning_dialog));


            return new AlertDialog.Builder(getActivity()).setView(body)
                    .setPositiveButton("Accept",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));


                                }
                            }
                    ).setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.cancel();
                                }
                            }
                    )
                    .create();
        }
    }

    public static class MapInternetDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);

            TextView body = (TextView) layoutInflater.inflate(R.layout.dialog_text_view, null);
            body.setText(getActivity().getText(R.string.msg_map_internet_warning_dialog));


            return new AlertDialog.Builder(getActivity()).setView(body)
                    .setPositiveButton("Accept",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));


                                }
                            }
                    ).setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.cancel();
                                }
                            }
                    )
                    .create();
        }
    }

    public static class LocationDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);

            TextView body = (TextView) layoutInflater.inflate(R.layout.dialog_text_view, null);
            body.setText(getActivity().getText(R.string.msg_location_warning_dialog));


            return new AlertDialog.Builder(getActivity()).setView(body)
                    .setPositiveButton("Accept",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));


                                }
                            }
                    ).setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    if (SplashActivity.getInstance() != null)
                                        SplashActivity.getInstance().startService();
                                    dialog.cancel();
                                }
                            }
                    )
                    .create();
        }
    }


}
