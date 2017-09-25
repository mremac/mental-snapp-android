package com.mentalsnapp.com.mentalsnapp.utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by gchandra on 8/3/17.
 */
public class StorageUtils {

    public static File makeDirs() {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/Mentalsnapp");
        dir.mkdirs();
        return dir;
    }

    public static boolean checkForExistance() {
        boolean isExist = false;
            File file = new File(Environment.getExternalStorageDirectory()+"/Mentalsnapp");
            isExist = file.exists();
        return isExist;
    }
}
