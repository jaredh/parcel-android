package me.jrdh.parcel.api;

import me.jrdh.parcel.api.models.ShipmentUpdates;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import rx.Observable;

public interface ParcelService {

    @POST("settings.php")
    void settings();

    @GET("data.php?caller=no&compression=yes&version=4")
    Observable<ShipmentUpdates> getShipments();

    @GET("add.php?allow=yes&extra=")
    Observable<Void> add(@Query("type") String type,
             @Query("code") String code,
             @Query("name") String name);

    @GET("edit.php?allow=yes")
    Observable<Void> edit(@Query("oldtype") String oldType,
              @Query("oldnumber") String oldNumber,
              @Query("code") String code,
              @Query("type") String type,
              @Query("language") String language,
              @Query("oldlabel") String oldLabel,
              @Query("name") String name);

    @GET("delete.php?allow=yes")
    Observable<Void> delete(@Query("number") String number,
                @Query("type") String type);
}
