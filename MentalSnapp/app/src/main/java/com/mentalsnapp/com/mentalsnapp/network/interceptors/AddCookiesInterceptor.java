package com.mentalsnapp.com.mentalsnapp.network.interceptors;

import android.text.TextUtils;
import android.util.Log;

import com.mentalsnapp.com.mentalsnapp.app.MentalSnappApp;
import com.mentalsnapp.com.mentalsnapp.utils.SharedPref;

import java.io.IOException;
import java.util.HashSet;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by gchandra on 22/12/16.
 */
public class AddCookiesInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
//        HashSet<String> preferences = (HashSet) SharedPref.getAuthCookies(MentalSnappApp.getInstance(), new HashSet<String>());
        /*for (String cookie : preferences) {
            builder.addHeader("Authorization", SharedPref.getAuthToken(MentalSnappApp.getInstance()));
            Log.v("OkHttp", "Adding Header: " + cookie); // This is done so I know which headers are being added; this interceptor is used after the normal logging of OkHttp
        }*/
        if (!TextUtils.isEmpty(SharedPref.getAuthToken(MentalSnappApp.getInstance()))) {
            builder.addHeader("Authorization", SharedPref.getAuthToken(MentalSnappApp.getInstance()));
        }
        return chain.proceed(builder.build());
    }
}