package com.kinopoisk;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class KinopoiskRequestInterceptor implements Interceptor {

    private static final String KP_SECRET = "a17qbcw1du0aedm";
    private static final String KP_UUID = "84e8b92499a32a3d0d8ea956e6a05d76";
    private static final String KP_CLIENT_ID = "55decdcf6d4cd1bcaa1b3856";
    private static final String API_CHECK_FOR_UPDATE = "ios/check-new-version.php";

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request interceptedRequest = chain.request();
        HttpUrl originalHttpUrl = interceptedRequest.url();

        chain.proceed(buildCheckForUpdateRequest(originalHttpUrl.host())).close();
        return chain.proceed(buildMainRequest(interceptedRequest));
    }

    private String encodeWithSecret(String action) {
        return new String(Hex.encodeHex(DigestUtils.md5(action + KP_SECRET)));
    }

    /** build main request with additional parameters, required by kinopoisk */
    private Request buildMainRequest(Request interceptedRequest) {
        HttpUrl interceptedUrl = interceptedRequest.url();
        String commandName = interceptedUrl.pathSegments().get(2);
        URL oUrl = interceptedUrl.url();

        String action;
        if (oUrl.getQuery() == null) {
            action = commandName + "?" + "uuid=" + KP_UUID;
        } else {
            action = commandName + "?" + oUrl.getQuery() + "&uuid=" + KP_UUID;
        }

        // Build encoded key for query
        String encodedKey = encodeWithSecret(action);

        // Put additional params to auth
        HttpUrl url = interceptedUrl.newBuilder()
                .addQueryParameter("uuid", KP_UUID)
                .addQueryParameter("key", encodedKey)
                .build();

        // Put additional headers to look like KP client
        interceptedRequest = basicHeaders(interceptedRequest.newBuilder()
                .removeHeader("User-Agent")
                .url(url))
                .build();

        return interceptedRequest;
    }

    /** build a request to check if our client is deprecated
     * with new kinopoisk api we should do it with any request to setup cookies */
    private Request buildCheckForUpdateRequest(String host) {

        String action = "check-new-version.php" + "?" + "appVersion=" + Constants.API_VERSION + "&uuid=" + KP_UUID;
        String encodedKey = encodeWithSecret(action);

        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(host)
                .addPathSegments(API_CHECK_FOR_UPDATE)
                .addQueryParameter("key", encodedKey)
                .addQueryParameter("appVersion", Constants.API_VERSION)
                .addQueryParameter("uuid", KP_UUID)
                .build();

        Request.Builder builder = basicHeaders(new Request.Builder())
                .url(url)
                .addHeader("Cookie", "user_country=ru");

        return builder.build();
    }


    private Request.Builder basicHeaders(Request.Builder builder) {
        // Generate req date
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm MM.dd.yyyy", Locale.getDefault());
        String clientDate = dateFormat.format(date);

        builder
                .addHeader("device", "android")
                .addHeader("Android-Api-Version", "22")
                .addHeader("countryID", "2")
                .addHeader("ClientId", KP_CLIENT_ID)
                .addHeader("clientDate", clientDate)
                .addHeader("cityID", "2")
                .addHeader("Image-Scale", "3")
                .addHeader("Cache-Control", "max-stale=0")
                .addHeader("User-Agent", "Android client (5.1 / api22), ru.kinopoisk/3.7.0 (45)")
                .addHeader("Accept-Encoding", "gzip");

        return builder;
    }
}