package me.jrdh.parcel.api;

import android.util.Base64;

import java.io.IOException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HmacInterceptor implements Interceptor{
    public static final String DATA_SALT = "ASD_?hhhd&^&^";
    public static final String ADD_SALT = "hdfj";

    public static final String DELETE_SECRET = "hMAsEditAn&77Delete";
    public static final String ADDEDIT_SECRET = "hMAsEditAn&65ADD";

    private String userId;

    public HmacInterceptor (String userId) {
        this.userId = userId;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String time = getTimestamp();

        Request.Builder builder = chain.request().newBuilder();

        if (chain.request().url().encodedPath().startsWith("/data.php")) {
            builder.url(chain.request().url().newBuilder()
                    .addQueryParameter("id", userId)
                    .addQueryParameter("verification", generateHmac(time, userId + HmacInterceptor.DATA_SALT))
                    .addQueryParameter("time", time)
                    .build());
        } else if (chain.request().url().encodedPath().startsWith("/add.php")) {
            String type = chain.request().url().queryParameter("type");
            String code = chain.request().url().queryParameter("code");

            builder.url(chain.request().url().newBuilder()
                    .addQueryParameter("id", userId)
                    .addQueryParameter("verification", generateHmac(HmacInterceptor.ADDEDIT_SECRET, userId + HmacInterceptor.ADD_SALT + type + "_" + code))
                    .build());
        } else if (chain.request().url().encodedPath().startsWith("/delete.php")) {
            String type = chain.request().url().queryParameter("type");
            String code = chain.request().url().queryParameter("code");

            builder.url(chain.request().url().newBuilder()
                    .addQueryParameter("id", userId)
                    .addQueryParameter("verification", generateHmac(HmacInterceptor.DELETE_SECRET, userId + HmacInterceptor.ADD_SALT + type + "_" + code))
                    .build());
        } else if (chain.request().url().encodedPath().startsWith("/edit.php")) {
            String type = chain.request().url().queryParameter("type");
            String code = chain.request().url().queryParameter("code");

            builder.url(chain.request().url().newBuilder()
                    .addQueryParameter("id", userId)
                    .addQueryParameter("verification", generateHmac(HmacInterceptor.ADDEDIT_SECRET, userId + HmacInterceptor.ADD_SALT + type + "_" + code))
                    .build());
        }

        return chain.proceed(builder.build());
    }

    private String generateHmac(String key, String message) {
        String hmac = "";

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
