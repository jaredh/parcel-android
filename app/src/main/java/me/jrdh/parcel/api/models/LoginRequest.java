package me.jrdh.parcel.api.models;


import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("account_email")
    public String accountEmail;

    @SerializedName("current_id")
    public String currentId;

    @SerializedName("plain_password")
    public String plainPassword;

    @SerializedName("platform")
    public String platform;

    @SerializedName("push_token")
    public String pushToken;

    public LoginRequest (String accountEmail, String currentId, String plainPassword, String platform, String pushToken) {
        this.accountEmail = accountEmail;
        this.currentId = currentId;
        this.plainPassword = plainPassword;
        this.platform = platform;
        this.pushToken = pushToken;
    }
}

