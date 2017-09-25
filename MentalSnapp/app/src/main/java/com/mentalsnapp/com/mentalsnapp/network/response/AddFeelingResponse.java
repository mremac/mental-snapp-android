package com.mentalsnapp.com.mentalsnapp.network.response;

import com.google.gson.annotations.SerializedName;
import com.mentalsnapp.com.mentalsnapp.models.AddFeeling;

import java.util.ArrayList;

/**
 * Created by gchandra on 13/1/17.
 */
public class AddFeelingResponse extends BaseResponse {

    @SerializedName("feelings")
    public ArrayList<AddFeeling> mFeelingList;

    @SerializedName("current_page")
    public long currentPage;

    @SerializedName("total_pages")
    public long totalPages;
}
