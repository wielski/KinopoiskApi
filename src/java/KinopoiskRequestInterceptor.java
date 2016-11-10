package com.x1unix.avi.kp;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class KinopoiskRequestInterceptor implements Interceptor {

    private final String KP_SECRET = "a17qbcw1du0aedm";
    private final String KP_UUID = "b551edb50f87445ba338f307a2e6baee";

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request original = chain.request();
        HttpUrl originalHttpUrl = original.url();
        URL oUrl = originalHttpUrl.url();

        String commandName = originalHttpUrl.pathSegments().get(2);

        // Build encoded key for query
        String encodedKey = new String(
                Hex.encodeHex(DigestUtils.md5(
                        commandName + "?" + oUrl.getQuery() + "&uuid=" + KP_UUID + KP_SECRET
                ))
        );

        // Put additional params to auth
        HttpUrl url = originalHttpUrl.newBuilder()
                .addQueryParameter("uuid", KP_UUID)
                .addQueryParameter("key", encodedKey)
                .build();

        Request request = chain.request();

        // Generate user token
        Random rand = new Random();
        int clientToken = rand.nextInt((9999 - 1) + 1) + 1;
        String clientId = new String(Hex.encodeHex(DigestUtils.md5(String.valueOf(clientToken))));


        // Generate req date
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm MM.dd.yyyy");
        String clientDate = dateFormat.format(date);


        // Put additional headers to look like KP client
        request = request.newBuilder()
                .addHeader("Android-Api-Version", "22")
                .removeHeader("User-Agent")
                .addHeader("User-Agent", "Android client (5.1 / api22), ru.kinopoisk/3.7.0 (45)")
                .addHeader("countryID", "2")
                .addHeader("ClientId", clientId)
                .addHeader("clientDate", clientDate)
                .addHeader("cityID", "2")
                .addHeader("Image-Scale", "3")
                .addHeader("Cache-Control", "max-stale=0")
                .addHeader("Accept-Encoding", "gzip")
                .addHeader("Cookie", "user_country=ru")
                .url(url)
                .build();
        Response response = chain.proceed(request);
        return response;
    }
}