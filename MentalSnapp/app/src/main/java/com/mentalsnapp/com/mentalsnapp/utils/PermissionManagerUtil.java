package com.mentalsnapp.com.mentalsnapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

/**
 * Created by ssaxena on 25/12/15.
 */
public class PermissionManagerUtil {

    public static final String WRITE_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    public static boolean isAlertWindowPermissionGranted(Activity activity) {
        boolean flag = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.SYSTEM_ALERT_WINDOW)
                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW},
//                    Constants.REQUEST_CODE_ALERT_WINDOW);
            flag = false;
        }
        return flag;
    }

    public static boolean isMarshmellowOrHigher() {
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flag = true;
        }
        return flag;
    }

    public static boolean checkPermission(String permission, Context context) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }
}
