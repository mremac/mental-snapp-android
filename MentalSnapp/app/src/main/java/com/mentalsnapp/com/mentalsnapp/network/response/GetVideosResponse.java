package com.mentalsnapp.com.mentalsnapp.network.response;

import com.google.gson.annotations.SerializedName;
import com.mentalsnapp.com.mentalsnapp.models.Videos;

import java.util.ArrayList;

/**
 * Created by gchandra on 18/1/17.
 */
public class GetVideosResponse {

    @SerializedName("current_page")
    public String currentPage;

    @SerializedName("total_pages")
    public String totalPages;

    @SerializedName("posts")
    public ArrayList<Videos> videosList;
}
