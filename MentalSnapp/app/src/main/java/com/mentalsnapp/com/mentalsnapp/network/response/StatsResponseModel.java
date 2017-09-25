package com.mentalsnapp.com.mentalsnapp.network.response;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.mentalsnapp.com.mentalsnapp.models.Videos;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by Srinivas Kalyani on 2/2/17.
 */

public class StatsResponseModel extends BaseResponse {
    @SerializedName(Constants.JSON_FIELD_POSTS)
    public ArrayList<Videos> posts;

    @SerializedName(Constants.JSON_FIELD_BAR_CHART)
    public Object barChart;

    @SerializedName(Constants.JSON_FIELD_MONTH_DATA)
    public JSONObject monthData;

    @SerializedName(Constants.JSON_FIELD_WEEK_DATA)
    public ArrayList<ArrayList<ArrayList<Integer>>> weekData;


}
