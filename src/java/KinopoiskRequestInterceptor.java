import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

// Interceptor for outcoming requests to kinopoisk API
public class KinopoiskRequestInterceptor implements Interceptor {

    private final String KP_SECRET = "a17qbcw1du0aedm";
    private final String KP_UUID = "b551edb50f87445ba338f307a2e6baee";

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request original = chain.request();
        HttpUrl originalHttpUrl = original.url();

        // Put additional params to auth
        HttpUrl url = originalHttpUrl.newBuilder()
                .addQueryParameter("key", KP_SECRET)
                .addQueryParameter("uuid", KP_UUID)
                .build();

        Request request = chain.request();

        // Generate user token
        Random rand = new Random();
        int clientToken = rand.nextInt((9999 - 1) + 1) + 1;
        String clientId = DigestUtils.md5Hex(String.valueOf(clientToken));

        // Generate req date
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("H:i m.d.Y");
        String clientDate = dateFormat.format(dateFormat.format(date));


        // Put additional headers to look like KP client
        request = request.newBuilder()
                .addHeader("Android-Api-Version", "android")
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
                .build();
        Response response = chain.proceed(request);
        return response;
    }
}