package net.noisetube.app.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import net.noisetube.app.core.AndroidNTClient;

/**
 * @author Humberto
 */
public class NTUtils {

    // require that the AndroidNTService be running
    public static boolean supportsInternetAccess() {
        return AndroidNTClient.getInstance().getDevice().supportsInternetAccess();
    }

    // require that the AndroidNTService be running
    public static boolean supportsPositioning() {
        return AndroidNTClient.getInstance().getDevice().supportsPositioning();
    }

    public static boolean supportsPositioning(ContextWrapper contextWrapper) {
        final LocationManager manager = (LocationManager) contextWrapper.getSystemService(Context.LOCATION_SERVICE);
        boolean status = false;
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            status = true;
        }
        return status;
    }

    public boolean supportsInternetAccess(ContextWrapper contextWrapper) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) contextWrapper.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
