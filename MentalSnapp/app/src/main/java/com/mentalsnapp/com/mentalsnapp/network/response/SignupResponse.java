package com.mentalsnapp.com.mentalsnapp.network.response;

import com.google.gson.annotations.SerializedName;
import com.mentalsnapp.com.mentalsnapp.models.User;

import org.parceler.Parcel;

/**
 * Created by ssaxena on 15/12/16.
 */

@Parcel
public class SignupResponse extends BaseResponse {
    //Fields must be public
    @SerializedName("auth_token")
    public String authToken;

    @SerializedName("users")
    public User user;

}
