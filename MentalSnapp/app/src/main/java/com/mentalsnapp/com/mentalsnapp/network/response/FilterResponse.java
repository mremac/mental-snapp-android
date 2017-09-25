package com.mentalsnapp.com.mentalsnapp.network.response;

import com.google.gson.annotations.SerializedName;
import com.mentalsnapp.com.mentalsnapp.models.GuidedExercise;

import java.util.ArrayList;

/**
 * Created by gchandra on 19/1/17.
 */
public class FilterResponse extends BaseResponse {

    @SerializedName("current_page")
    public int currentPage;

    @SerializedName("total_pages")
    public int totalPages;

    @SerializedName("filters")
    public ArrayList<GuidedExercise> guidedExercises;
}
