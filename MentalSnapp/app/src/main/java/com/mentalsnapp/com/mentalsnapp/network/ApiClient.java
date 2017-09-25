package com.mentalsnapp.com.mentalsnapp.network;

import com.mentalsnapp.com.mentalsnapp.network.interceptors.AddCookiesInterceptor;
import com.mentalsnapp.com.mentalsnapp.network.interceptors.ReceivedCookiesInterceptor;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ssaxena on 15/12/16.
 */
public class ApiClient {
    private static final String BASE_URL = Constants.PRODUCTION_URL;
    //    private static final String BASE_URL = "https://api.mentalsnapp.com";
//    private static final String BASE_URL = "http://192.168.1.38:3000";
    private static ApiInterface apiInterface;
//    private static Retrofit retrofit = null;

    public static ApiInterface getApiInterface() {

//        Gson gson = new GsonBuilder()
//                .setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
//                .create();

        if (apiInterface == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(getUnsafeOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            apiInterface = retrofit.create(ApiInterface.class);
        }
        return apiInterface;
    }

    public static OkHttpClient getUnsafeOkHttpClient() {

        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[0];
                }
            }};

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts,
                    new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext
                    .getSocketFactory();

            OkHttpClient okHttpClient = new OkHttpClient();
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            okHttpClient = okHttpClient.newBuilder()
                    .sslSocketFactory(sslSocketFactory)
                    .hostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER).addInterceptor(new AddCookiesInterceptor())
                    .addInterceptor(new ReceivedCookiesInterceptor()).addInterceptor(interceptor).build();

            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
