package me.jrdh.parcel.api;

import me.jrdh.parcel.api.models.ShipmentUpdates;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import rx.Observable;

public interface ParcelService {

    @POST("settings.php")
    void settings();

    @GET("data.php?caller=no&compression=yes&version=4&time=1460440816&version=4")
    Observable<ShipmentUpdates> getShipments(@Query("id") String userId);

    @GET("add.php?allow=yes&extra=")
    void add(@Query("type") String type,
             @Query("code") String code,
             @Query("id") String userId,
             @Query("name") String name,
             @Query("verification") String verification);

    @GET("edit.php?allow=yes")
    void edit(@Query("id") String userId,
              @Query("oldtype") String oldType,
              @Query("oldnumber") String oldNumber,
              @Query("code") String code,
              @Query("type") String type,
              @Query("language") String language,
              @Query("oldlabel") String oldLabel,
              @Query("name") String name,
              @Query("verification") String verification);

    @GET("delete.php?allow=yes")
    void delete(@Query("id") String userId,
                @Query("number") String number,
                @Query("type") String type,
                @Query("verification") String verification);
}
