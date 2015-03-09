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

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;


/**
 * An assortment of UI helpers.
 */
public class UIUtils {

    /**
     * Factor applied to session color to derive the background color on panels and when
     * a session photo could not be downloaded (or while it is being downloaded)
     */
    public static final float SESSION_BG_COLOR_SCALE_FACTOR = 0.75f;
    public static final String TARGET_FORM_FACTOR_ACTIVITY_METADATA =
            "com.google.samples.apps.iosched.meta.TARGET_FORM_FACTOR";
    public static final String TARGET_FORM_FACTOR_HANDSET = "handset";
    public static final String TARGET_FORM_FACTOR_TABLET = "tablet";


    public static boolean isTablet(Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }


    // Shows whether a notification was fired for a particular session time block. In the
    // event that notification has not been fired yet, return false and set the bit.
    public static boolean isNotificationFiredForBlock(Context context, String blockId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        final String key = String.format("notification_fired_%s", blockId);
        boolean fired = sp.getBoolean(key, false);
        sp.edit().putBoolean(key, true).commit();
        return fired;
    }

//

    /**
     * Enables and disables {@linkplain android.app.Activity activities} based on their
     * {@link #TARGET_FORM_FACTOR_ACTIVITY_METADATA}" meta-data and the current device.
     * Values should be either "handset", "tablet", or not present (meaning universal).
     * <p/>
     * <a href="http://stackoverflow.com/questions/13202805">Original code</a> by Dandre Allison.
     *
     * @param context the current context of the device
     */
    public static void enableDisableActivitiesByFormFactor(Context context) {
        final PackageManager pm = context.getPackageManager();
        boolean isTablet = isTablet(context);

        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
            if (pi == null) {
                return;
            }

            final ActivityInfo[] activityInfos = pi.activities;
            for (ActivityInfo info : activityInfos) {
                String targetDevice = null;
                if (info.metaData != null) {
                    targetDevice = info.metaData.getString(TARGET_FORM_FACTOR_ACTIVITY_METADATA);
                }
                boolean tabletActivity = TARGET_FORM_FACTOR_TABLET.equals(targetDevice);
                boolean handsetActivity = TARGET_FORM_FACTOR_HANDSET.equals(targetDevice);

                boolean enable = !(handsetActivity && isTablet)
                        && !(tabletActivity && !isTablet);

                String className = info.name;
                pm.setComponentEnabledSetting(
                        new ComponentName(context, Class.forName(className)),
                        enable
                                ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (ClassNotFoundException e) {

        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isRtl(final Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return false;
        } else {
            return context.getResources().getConfiguration().getLayoutDirection()
                    == View.LAYOUT_DIRECTION_RTL;
        }
    }

    public static void setAccessibilityIgnore(View view) {
        view.setClickable(false);
        view.setFocusable(false);
        view.setContentDescription("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
    }


}
