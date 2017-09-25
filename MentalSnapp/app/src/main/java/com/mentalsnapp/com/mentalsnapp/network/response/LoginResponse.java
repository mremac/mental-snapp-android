package com.mentalsnapp.com.mentalsnapp.network.response;

import com.google.gson.annotations.SerializedName;
import com.mentalsnapp.com.mentalsnapp.models.User;

import org.parceler.Parcel;

/**
 * Created by ssaxena on 15/12/16.
 */

@Parcel
public class LoginResponse extends BaseResponse {
    //Fields must be public
    @SerializedName("auth_token")
    public String authToken;

    @SerializedName("user")
    public User user;

    @SerializedName("is_first_user")
    public Boolean isFirstUser;

}
