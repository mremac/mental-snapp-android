package com.mentalsnapp.com.mentalsnapp.network.response;

import com.google.gson.annotations.SerializedName;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;

import java.util.ArrayList;

/**
 * Created by gchandra on 6/2/17.
 */
public class ScheduleListResponse extends BaseResponse {

    @SerializedName(Constants.CURRENT_PAGE)
    public long currentPage;

    @SerializedName(Constants.TOTAL_PAGES)
    public long totalPages;

    @SerializedName(Constants.SCHEDULES)
    public ArrayList<ScheduleExerciseResponse> scheduleList;
}
