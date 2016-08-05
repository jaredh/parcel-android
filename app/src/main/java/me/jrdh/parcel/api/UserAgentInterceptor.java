package me.jrdh.parcel.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class UserAgentInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request().newBuilder()
                .header("User-Agent", "Parcel/166 CFNetwork/758.3.15 Darwin/15.4.0")
                .method(chain.request().method(), chain.request().body())
                .build();

        return chain.proceed(request);
    }
}
