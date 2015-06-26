package net.noisetube.app.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import net.noisetube.api.util.HexPlus;
import net.noisetube.app.core.AndroidNTClient;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Humberto
 */
public class NTUtils {

    // require that the AndroidNTService be running
    public static boolean supportsInternetAccess() {
        try {
            return AndroidNTClient.getInstance().getDevice().supportsInternetAccess();
        } catch (NullPointerException e) {
            return false;
        }
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

    public static String encryptPassword(String pass) throws Exception {
        final String skeyString = "95128424a97797a166913557a6b4cc8e";
        byte[] skey = HexPlus.decodeHex(skeyString.toCharArray());

        //iv
        final String ivString = "82e8c3ea8b59a0e293941d1cba0a39c3";
        byte[] iv = HexPlus.decodeHex(ivString.toCharArray());

        //encrypt
        SecretKeySpec skeySpec1 = new SecretKeySpec(skey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec1, new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(pass.getBytes());
        String encryptedString = HexPlus.encodeHexString(encrypted);

        return encryptedString;
    }

    public boolean supportsInternetAccess(ContextWrapper contextWrapper) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) contextWrapper.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
