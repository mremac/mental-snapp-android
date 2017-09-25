package com.mentalsnapp.com.mentalsnapp.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by ssaxena on 15/12/16.
 * User model class
 */

@Parcel
public class User {
    //Fields must be public
    @SerializedName("id")
    public long id;

    @SerializedName("email")
    public String email;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;

    @SerializedName("name")
    public String name;

    @SerializedName("first_name")
    public String firstName;

    @SerializedName("last_name")
    public String lastName;

    @SerializedName("date_of_birth")
    public String dateOfBirth;

    @SerializedName("gender")
    public String gender;

    @SerializedName("phone_number")
    public String phoneNumber;

    @SerializedName("phone_country_code")
    public String phoneCountryCode;

    @SerializedName("profile_url")
    public String profileUrl;

    @SerializedName("is_active")
    public boolean isActive;

    // empty constructor needed by the Parceler library
    public User() {
    }
}
