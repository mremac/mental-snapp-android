package com.mentalsnapp.com.mentalsnapp.utils;

import android.content.Context;
import android.util.Log;

import com.SMobiLogger.SLog;

/**
 * Created by gchandra on 29/12/16.
 */
public class LogHelper {
    public static void logInfo(Context context,
                               String title,
                               String description) {
        SLog.i(context, title, description);
//        Log.i(title, description);
    }

    public static void logError(Context context,
                                String title,
                                String description) {
        SLog.e(context, title, description);
//        Log.e(title, description);
    }
}
