package com.mentalsnapp.com.mentalsnapp.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by ssaxena on 23/12/16.
 */

@Parcel
public class GuidedExercise {

    //Fields must be public
    @SerializedName("id")
    public long id;

    @SerializedName("name")
    public String name;

    @SerializedName("cover_url")
    public String coverUrl;

    @SerializedName("description")
    public String description;

    @SerializedName("exercise_id")
    public String exerciseId;

    @SerializedName("type")
    public String type;

    @SerializedName("cover_url_medium")
    public String coverUrlMedium;

    @SerializedName("cover_url_large")
    public boolean coverUrlLarge;

    @SerializedName("cover_url_small")
    public boolean coverUrlSmall;

    // empty constructor needed by the Parceler library
    public GuidedExercise() {
    }
}
