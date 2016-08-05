package me.jrdh.parcel.api.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("id")
    public String id;

    @SerializedName("hash")
    public String hash;

    @SerializedName("push")
    public String push;

    @SerializedName("type")
    public String type;

    @SerializedName("time_offset")
    public String timeOffset;

    @SerializedName("push_on_delivery")
    public String pushOnDelivery;

    @SerializedName("expiryDate")
    public String expiryDate;

    @SerializedName("result")
    public String result;

    public LoginResponse (String id, String hash, String push, String type, String timeOffset, String pushOnDelivery, String expiryDate, String result)
    {
        this.id = id;
        this.hash = hash;
        this.push = push;
        this.type = type;
        this.timeOffset = timeOffset;
        this.pushOnDelivery = pushOnDelivery;
        this.expiryDate = expiryDate;
        this.result = result;
    }
}
