package com.mentalsnapp.com.mentalsnapp.utils;

import android.content.Context;
import android.provider.Settings;

/**
 * Created by gchandra on 23/12/16.
 */
public class NetworkManagement {

    private static boolean isAirplaneModeOn(Context context) {

        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

}
