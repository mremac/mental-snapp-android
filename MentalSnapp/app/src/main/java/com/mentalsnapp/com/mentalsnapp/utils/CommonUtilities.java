package com.mentalsnapp.com.mentalsnapp.utils;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ssaxena on 22/12/16.
 */
public class CommonUtilities {

    public static int getDisplayWidth(Activity activity) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//        int height = displaymetrics.heightPixels;
//        int width = displaymetrics.widthPixels;
        return displaymetrics.widthPixels;
    }

    public static void buttonEffect(View button) {
        button.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.getBackground().clearColorFilter();
                        v.invalidate();
                        break;
                    }
                }
                return false;
            }
        });
    }
}
