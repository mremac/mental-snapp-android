package com.mentalsnapp.com.mentalsnapp.app;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.mentalsnapp.com.mentalsnapp.R;

/**
 * Created by gchandra on 22/12/16.
 */
public class MentalSnappApp extends Application {

    static MentalSnappApp app;
    private Tracker mTracker;

    public static MentalSnappApp getInstance() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        app = this;
    }

    /*synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }*/
}
