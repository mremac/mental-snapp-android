package com.mentalsnapp.com.mentalsnapp.utils;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.RequestBody;
import okio.Buffer;

/**
 * Created by gchandra on 10/1/17.
 */
public class JsonToStringConverter {

    public static String convertToJson(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }

    public static String mergeAndConvert(String url, String method, final RequestBody request) {
        try {
            final RequestBody copy = request;
            final Buffer buffer = new Buffer();
            if (copy != null)
                copy.writeTo(buffer);
            else
                return "";
            return "Method:" + method + " " + url + " Params:" + buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }

    }

    public static String getQueryParam(HttpUrl url, String key) {
        return url.queryParameter(key);
    }

    public static String getRequestParamFromBody(String paramString, String key) {
        if (paramString.contains(key + "=")) {
//            int valueLastIndex = paramString.substring(paramString.indexOf(key+ "=")).;
            String[] params = paramString.split("&");
            for (String param : params) {
                if (param.contains(key + "=")) {
                    return param.substring(param.indexOf("=") + 1);
                }
            }
        }
        return null;
    }

}
