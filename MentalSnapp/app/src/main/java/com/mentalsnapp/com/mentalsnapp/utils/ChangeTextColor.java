package com.mentalsnapp.com.mentalsnapp.utils;

import android.app.Activity;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import com.mentalsnapp.com.mentalsnapp.R;

import java.util.ArrayList;

/**
 * Created by gchandra on 7/3/17.
 */
public class ChangeTextColor {
    public static SpannableString setColor(Activity activity, String rawMessage, ArrayList<String> message) {
        SpannableString multiFontMessage = new SpannableString(rawMessage);
        try {
            int oldIndex = -1;
            for (String msg : message) {
                if (msg != null) {
                    int startIndex;
                    int endIndex;
                    if (oldIndex == -1) {
                        startIndex = rawMessage.indexOf(msg);
                        endIndex = rawMessage.indexOf(msg)
                                + msg.length();
                    } else {
                        startIndex = rawMessage.indexOf(msg, oldIndex + 1);
                        endIndex = rawMessage.indexOf(msg, oldIndex + 1)
                                + msg.length();
                    }
                    oldIndex = startIndex;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        multiFontMessage.setSpan(new ForegroundColorSpan(activity.getResources().getColor(R.color.blue, null)), startIndex, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    } else {
                        multiFontMessage.setSpan(new ForegroundColorSpan(activity.getResources().getColor(R.color.blue)), startIndex, endIndex, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    }

                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            multiFontMessage = new SpannableString("");
        }

        return multiFontMessage;
    }
}
