package me.jrdh.parcel.api;

import me.jrdh.parcel.api.models.LoginRequest;
import me.jrdh.parcel.api.models.LoginResponse;

import retrofit2.http.Body;
import retrofit2.http.POST;

import rx.Observable;

public interface AuthenticationService {
    @POST("login.php")
    Observable<LoginResponse> login(@Body LoginRequest loginRequest);
}
