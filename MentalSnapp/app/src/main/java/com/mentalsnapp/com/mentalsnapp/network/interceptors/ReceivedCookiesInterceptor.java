package com.mentalsnapp.com.mentalsnapp.network.interceptors;

import com.mentalsnapp.com.mentalsnapp.app.MentalSnappApp;
import com.mentalsnapp.com.mentalsnapp.utils.SharedPref;

import java.io.IOException;
import java.util.HashSet;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by gchandra on 22/12/16.
 */
public class ReceivedCookiesInterceptor implements Interceptor {
    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());

        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            HashSet<String> cookies = new HashSet<>();

            for (String header : originalResponse.headers("Set-Cookie")) {
                cookies.add(header);
            }

//            SharedPref.setAuthCookies(MentalSnappApp.getInstance(), cookies);
        }

        return originalResponse;
    }
}
