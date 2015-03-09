package net.noisetube.app.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import net.noisetube.api.util.Logger;
import net.noisetube.app.core.AndroidNTService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * @author Humberto
 */
public class ImageUtils {
    private static Logger log = Logger.getInstance();

    public static String saveToInternalSorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(AndroidNTService.getInstance().getAppContext());
        // path to /data/data/noisetube/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, "user_profile.jpg");

        FileOutputStream fos = null;
        try {

            fos = new FileOutputStream(mypath);

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            log.error(e, "saveToInternalSorage");
        }
        return mypath.getAbsolutePath();
    }

    public static Bitmap loadImageFromStorage() {

        try {
            ContextWrapper cw = new ContextWrapper(AndroidNTService.getInstance().getAppContext());
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File f = new File(directory.getPath(), "user_profile.jpg");

            if (f.exists()) {
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
                return b;
            }
        } catch (FileNotFoundException e) {
            log.error(e, "loadImageFromStorage");
        }

        return null;

    }
}
