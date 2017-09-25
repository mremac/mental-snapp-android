package com.mentalsnapp.com.mentalsnapp.network.response;

import com.google.gson.annotations.SerializedName;
import com.mentalsnapp.com.mentalsnapp.models.GuidedExercise;

import org.parceler.Parcel;

import java.util.ArrayList;

/**
 * Created by ssaxena on 15/12/16.
 */

@Parcel
public class SubCategoriesQuestionsResponse extends BaseResponse {
    //Fields must be public
    @SerializedName("current_page")
    public int currentPage;

    @SerializedName("total_pages")
    public int totalPages;

    @SerializedName("questions")
    public ArrayList<GuidedExercise> guidedExercises;

}
