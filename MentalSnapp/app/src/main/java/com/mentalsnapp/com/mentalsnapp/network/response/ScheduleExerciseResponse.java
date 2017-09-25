package com.mentalsnapp.com.mentalsnapp.network.response;

import com.google.gson.annotations.SerializedName;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;

/**
 * Created by gchandra on 6/2/17.
 */
public class ScheduleExerciseResponse extends BaseResponse {

    @SerializedName(Constants.ID)
    public String id;

    @SerializedName(Constants.CREATED_AT)
    public String createdAt;

    @SerializedName(Constants.SCHEDULABLE_TYPE)
    public String schedulableType;

    @SerializedName(Constants.SCHEDULABLE_ID)
    public long schedulableId;

    @SerializedName(Constants.EXECUTE_AT)
    public String executeAt;

    @SerializedName(Constants.EXERCISE)
    public Exercise exercise;


    public class Exercise {
        @SerializedName(Constants.ID)
        public String id;

        @SerializedName(Constants.CREATED_AT)
        public String createdAt;

        @SerializedName(Constants.DESCRIPTION)
        public String description;

        @SerializedName(Constants.EXERCISE_ID)
        public long exerciseId;

        @SerializedName(Constants.NAME)
        public String name;

        @SerializedName(Constants.COVER_URL)
        public String coverUrl;

        @SerializedName(Constants.TYPE)
        public String type;
    }
}
