package me.jrdh.parcel.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class AuthCookieInterceptor implements Interceptor {
    private final String token;

    public AuthCookieInterceptor (String token) {
        this.token = token;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request().newBuilder()
                .addHeader("Cookie", "token=" + token)
                .build();

        return chain.proceed(request);
    }
}
