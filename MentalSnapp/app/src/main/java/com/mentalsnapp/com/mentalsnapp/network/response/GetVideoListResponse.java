package com.mentalsnapp.com.mentalsnapp.network.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by gchandra on 31/1/17.
 */
public class GetVideoListResponse extends BaseResponse {

    @SerializedName("video_url")
    public String[] getVideosUrls;
}
