package com.mentalsnapp.com.mentalsnapp.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by gchandra on 13/1/17.
 */
public class AddFeeling {
    @SerializedName("id")
    public long id;

    @SerializedName("name")
    public String name;

    @SerializedName("red")
    public int red;

    @SerializedName("green")
    public int green;

    @SerializedName("blue")
    public int blue;

    @SerializedName("sub_feelings")
    public ArrayList<AddFeeling> subFeelingList;

    public boolean isSubFeeling;
}
