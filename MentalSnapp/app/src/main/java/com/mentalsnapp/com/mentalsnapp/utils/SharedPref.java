package com.mentalsnapp.com.mentalsnapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by gchandra on 14/12/16.
 */
public class SharedPref {

    public static SharedPreferences mSharedPreferences;

    public static SharedPreferences getSharedPreferences(Context context) {
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences(
                    Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        }
        return mSharedPreferences;
    }

    public static boolean isLogin(Context context) {
        return getSharedPreferences(context).
                getBoolean(Constants.LOGIN, false);
    }

    public static void setLogin(boolean email, Context context) {
        getSharedPreferences(context).edit().
                putBoolean(Constants.LOGIN, email).commit();
    }

    public static String getEmail(Context context) {
        return getSharedPreferences(context).
                getString(Constants.EMAIL, Constants.EMPTY_STRING);
    }

    public static void setEmail(String email, Context context) {
        getSharedPreferences(context).edit().
                putString(Constants.EMAIL, email).commit();
    }

    public static String getPassword(Context context) {
        return getSharedPreferences(context).
                getString(Constants.PASSWORD, Constants.EMPTY_STRING);
    }

    public static void setPassword(String password, Context context) {
        getSharedPreferences(context).edit().
                putString(Constants.PASSWORD, password).commit();
    }

    public static long getID(Context context) {
        return getSharedPreferences(context).
                getLong(Constants.ID, 0);
    }

    public static void setID(long id, Context context) {
        getSharedPreferences(context).edit().
                putLong(Constants.ID, id).commit();
    }

    public static String getAuthToken(Context context) {
        return getSharedPreferences(context).
                getString(Constants.AUTH_TOKEN, Constants.EMPTY_STRING);
    }

    public static void setAuthToken(String authToken, Context context) {
        getSharedPreferences(context).edit().
                putString(Constants.AUTH_TOKEN, authToken).commit();
    }

    public static String getProfileImagePath(Context context) {
        return getSharedPreferences(context).
                getString(Constants.IMAGE_PATH, Constants.EMPTY_STRING);
    }

    public static void setProfileImagePath(String imagePath, Context context) {
        getSharedPreferences(context).edit().
                putString(Constants.IMAGE_PATH, imagePath).commit();
    }

    public static boolean isSignUp(Context context) {
        return getSharedPreferences(context).
                getBoolean(Constants.SIGNUP, false);
    }

    public static void setSignUp(boolean firstSignUp, Context context) {
        getSharedPreferences(context).edit().
                putBoolean(Constants.SIGNUP, firstSignUp).commit();
    }

    public static String getPhone(Context context) {
        return getSharedPreferences(context).
                getString(Constants.PHONE, "");
    }

    public static void setPhone(String phone, Context context) {
        getSharedPreferences(context).edit().
                putString(Constants.PHONE, phone).commit();
    }

    public static String getDOB(Context context) {
        return getSharedPreferences(context).
                getString(Constants.DOB, "");
    }

    public static void setDOB(String dob, Context context) {
        getSharedPreferences(context).edit().
                putString(Constants.DOB, dob).commit();
    }

    public static String getName(Context context) {
        return getSharedPreferences(context).
                getString(Constants.NAME, "");
    }

    public static void setName(String name, Context context) {
        getSharedPreferences(context).edit().
                putString(Constants.NAME, name).commit();
    }

    public static String getGender(Context context) {
        return getSharedPreferences(context).
                getString(Constants.GENDER, "");
    }

    public static void setGender(String gender, Context context) {
        getSharedPreferences(context).edit().
                putString(Constants.GENDER, gender).commit();
    }

    public static boolean isRememberMeChecked(Context context) {
        return getSharedPreferences(context).
                getBoolean(Constants.REMEMBER_ME_CHECKED, false);
    }

    public static void setRememberChecked(boolean checked, Context context) {
        getSharedPreferences(context).edit().
                putBoolean(Constants.REMEMBER_ME_CHECKED, checked).commit();
    }

    public static boolean isAlertShow(Context context) {
        return getSharedPreferences(context).
                getBoolean(Constants.ALERT_SHOW, false);
    }

    public static void setAlertShow(boolean isAlertShow, Context context) {
        getSharedPreferences(context).edit().
                putBoolean(Constants.ALERT_SHOW, isAlertShow).commit();
    }

    public static String getFirstName(Context context) {
        return getSharedPreferences(context).
                getString(Constants.FIRST_NAME, Constants.EMPTY_STRING);
    }

    public static void setFirstName(Context context, String firstName) {
        getSharedPreferences(context).edit().
                putString(Constants.FIRST_NAME, firstName).commit();
    }
}
