package com.mentalsnapp.com.mentalsnapp.network;

import com.mentalsnapp.com.mentalsnapp.network.response.ScheduleExerciseResponse;
import com.mentalsnapp.com.mentalsnapp.network.response.BaseResponse;
import com.mentalsnapp.com.mentalsnapp.network.response.FilterResponse;
import com.mentalsnapp.com.mentalsnapp.network.response.GetVideoListResponse;
import com.mentalsnapp.com.mentalsnapp.network.response.GetVideosResponse;
import com.mentalsnapp.com.mentalsnapp.network.response.ScheduleListResponse;
import com.mentalsnapp.com.mentalsnapp.network.response.SetMoodResponse;
import com.mentalsnapp.com.mentalsnapp.network.response.AddFeelingResponse;
import com.mentalsnapp.com.mentalsnapp.network.response.GuidedExerciseListResponse;
import com.mentalsnapp.com.mentalsnapp.network.response.GuidedExerciseResponse;
import com.mentalsnapp.com.mentalsnapp.network.response.LoginResponse;
import com.mentalsnapp.com.mentalsnapp.network.response.SignupResponse;
import com.mentalsnapp.com.mentalsnapp.network.response.StatsResponseModel;
import com.mentalsnapp.com.mentalsnapp.network.response.SubCategoriesQuestionsResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by ssaxena on 15/12/16.
 */
public interface ApiInterface {

    @FormUrlEncoded
    @POST("/api/v1/authenticate")
    Call<LoginResponse> login(@Field("email") String email, @Field("password") String password);

    @FormUrlEncoded
    @POST("/api/v1/users")
    Call<SignupResponse> signup(@Field("user[email]") String email, @Field("user[first_name]") String firstName, @Field("user[last_name]") String lastName,
                                @Field("user[date_of_birth]") String dob, @Field("user[gender]") String gender, @Field("user[phone_number]") String number,
                                @Field("user[phone_country_code]") String countryCode, @Field("user[password]") String password,
                                @Field("user[password_confirmation]") String confirmPassword);

    @GET("/api/v1/guided_exercise")
    Call<GuidedExerciseResponse> getGuidedExercise(@Query("page") int page, @Query("per_page") int perPage);

    @GET("/api/v1/users/{id}")
    Call<LoginResponse> profileDetails(@Path("id") long id);

    @FormUrlEncoded
    @PATCH("api/v1/users/{id}")
    Call<LoginResponse> updateProfile(@Path("id") long id, @Field("user[email]") String email, @Field("user[name]") String name,
                                      @Field("user[date_of_birth]") String dob, @Field("user[gender]") String gender,
                                      @Field("user[phone_number]") String number, @Field("user[phone_country_code]") String phoneCountryCode,
                                      @Field("user[profile_url]") String profileUrl);

    @FormUrlEncoded
    @POST("api/v1/users/logout")
    Call<LoginResponse> logout(@Field("user_id") long userID);

    @FormUrlEncoded
    @PUT("api/v1/users/update_password")
    Call<LoginResponse> changePassword(@Field("user[current_password]") String currentPassword, @Field("user[password]") String password,
                                       @Field("user[password_confirmation]") String confirmPassword);

    @GET("/api/v1/users/forgot_password")
    Call<LoginResponse> forgotPassword(@Query("email") String email);

    @GET("/api/v1/guided_exercise/{guided_exercise_id}/sub_categories")
    Call<GuidedExerciseListResponse> getItems(@Path("guided_exercise_id") long guidedExcerciseId, @Query("page") long page,
                                              @Query("per_page") long perPage);

    @FormUrlEncoded
    @PUT("/api/v1/users/deactivate_account")
    Call<LoginResponse> deleteProfile(@Field("user_id") long profileId);

    //    @Headers("Content-Type: application/json; charset=utf-8")
    @Headers({
            "Cache-Control: no-cache",
            "Connection: keep-alive"
    })
    @Multipart
    @POST("/api/v1/supports/record")
    Call<LoginResponse> reportAnIssue(@Part("title") RequestBody title, @Part("description") RequestBody description,
                                      @Part MultipartBody.Part log_file,
                                      @Part MultipartBody.Part screenshot);

    @GET("/api/v1/sub_categories/{id}/get_questions")
    Call<SubCategoriesQuestionsResponse> getQuestions(@Path("id") long id, @Query("page") long page, @Query("per_page") long perPage);

    @GET("/api/v2/feelings")
    Call<AddFeelingResponse> getFeelingList(@Query("page") long page, @Query("per_page") long per_page);

    @FormUrlEncoded
    @POST("/api/v1/posts")
    Call<SetMoodResponse> uploadVideo(@Field("posts[name]") String videoName, @Field("posts[exercisable_type]") String exerciseType,
                                      @Field("posts[exercisable_id]") String exerciseId, @Field("posts[cover_url]") String coverURL,
                                      @Field("posts[tags]") String tags, @Field("posts[video_url]") String videoURL,
                                      @Field("posts[user_id]") String userID, @Field("posts[mood_value]") String moodValue,
                                      @Field("posts[feeling_ids]") String feelingId);

    @GET("/api/v1/posts")
    Call<GetVideosResponse> getVideos(@Query("page") long page, @Query("per_page") long perPage);

    @DELETE("/api/v1/posts/{id}")
    Call<BaseResponse> deleteVideo(@Path("id") long videoId);

    @GET("/api/v1/filters/get_filters_list")
    Call<FilterResponse> getFilterList(@Query("page") long page, @Query("per_page") long perPage);

    @GET("/api/v1/posts/search_posts")
    Call<GetVideosResponse> getVideosBySearch(@Query("search_text") String searchText, @Query("page") long page,
                                              @Query("per_page") long perPage);

    @GET("api/v1/posts/get_video_url")
    Call<GetVideoListResponse> getVideosList();

    @GET("api/v1/filters/get_filter_posts")
    Call<GetVideosResponse> getVideosByFilter(@Query("exercise_ids") long exerciseId, @Query("page") long page,
                                              @Query("per_page") long perpage);

    @FormUrlEncoded
    @POST("/api/v1/schedules")
    Call<ScheduleExerciseResponse> scheduleExercise(@Field("schedules[user_id]") String userId, @Field("schedules[schedulable_id]") String exerciseId,
                                                    @Field("schedules[schedulable_type]") String exerciseType, @Field("schedules[execute_at]") String date);

    @GET("/api/v1/schedules")
    Call<ScheduleListResponse> getScheduleList();

    @FormUrlEncoded
    @PATCH("/api/v1/schedules/{id}")
    Call<ScheduleExerciseResponse> reSchedule(@Path("id") long id, @Field("schedules[schedulable_id]") String exerciseId,
                                              @Field("schedules[schedulable_type]") String exerciseType,
                                              @Field("schedules[execute_at]") String executeAt);

    @GET("/api/v1/statics/get_stats")
    Call<StatsResponseModel> getStats(@Query("month") long month, @Query("year") long year);

    @DELETE("/api/v1/schedules/{id}")
    Call<ScheduleExerciseResponse> deleteScheduled(@Path("id") String id);
}
