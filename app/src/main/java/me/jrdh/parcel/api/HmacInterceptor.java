package me.jrdh.parcel.api;

import android.util.Base64;

import java.io.IOException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HmacInterceptor implements Interceptor{
    public static final String SALT = "ASD_?hhhd&^&^";
    private String userId;

    public HmacInterceptor (String userId) {
        this.userId = userId;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String time = getTimestamp();
        
        Request request = chain.request().newBuilder()
                .url(chain.request().url().newBuilder()
                    .addQueryParameter("id", userId)
                    .addQueryParameter("verification", generateHmac(time))
                    .addQueryParameter("time", time)
                    .build())
                .build();

        return chain.proceed(request);
    }

    private String generateHmac(String key) {
        String hmac = "";
        String message = userId + HmacInterceptor.SALT;

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secret = new SecretKeySpec(key.getBytes("UTF-8"), mac.getAlgorithm());
            mac.init(secret);
            byte[] digest = mac.doFinal(message.getBytes());

            // Base64 encode and leave off newline
            hmac = Base64.encodeToString(digest, Base64.NO_WRAP);
        } catch (Exception e) {
            // TODO: Log exception
        }

        return hmac;
    }

    private String getTimestamp() {
        return String.valueOf(System.currentTimeMillis() / 1000 / 120);
    }
}
