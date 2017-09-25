package com.mentalsnapp.com.mentalsnapp.network.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by gchandra on 16/1/17.
 */
public class SetMoodResponse extends BaseResponse {

    @SerializedName("id")
    public long id;

    @SerializedName("name")
    public String name;

    @SerializedName("exercisable_type")
    public String exerciseType;

    @SerializedName("exercisable_id")
    public String exerciseID;

    @SerializedName("cover_url")
    public String coverURL;

    @SerializedName("tags")
    public String tags;

    @SerializedName("video_url")
    public String videoURL;

    @SerializedName("user_id")
    public String userID;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("mood_value")
    public String moodValue;

    @SerializedName("feelings")
    public Feelings[] feelings;

    public class Feelings {

        @SerializedName("id")
        public long id;

        @SerializedName("name")
        public String name;
    }
}
