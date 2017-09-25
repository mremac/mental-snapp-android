package com.mentalsnapp.com.mentalsnapp.utils;

/**
 * Created by gchandra on 21/12/16.
 */
public interface Constants {

    // Make true for production builds
    boolean isProduction = true;

    String STAGING_URL = "http://mentalsnapp.systech-soft.com/";
    String PRODUCTION_URL = "https://api.mentalsnapp.com";
    String LOGIN = "login";
    String EMPTY_STRING = "";
    String SHARED_PREFERENCES = "shared_preferences";
    String EMAIL = "email";
    String PASSWORD = "password";
    String IMAGE_PATH = "image_path";
    String AUTH_TOKEN = "auth_token";
    String ID = "id";
    String AUTH_COOKIES = "auth_cookies";
    String LOGIN_API = "/api/v1/authenticate";
    String SIGNUP_API = "/api/v1/users";
    String GET_PROFILE_API = "/api/v1/users/";
    String UPDATE_PROFILE_API = "api/v1/users/";
    String GET = "GET";
    String POST = "POST";
    String PATCH = "PATCH";
    String CATEGORY_ID = "category_id";
    String LOGOUT_API = "/api/v1/users/logout";
    String DELETE_POFILE_API = "/api/v1/users/deactivate_account";
    String SUB_CATEGORY_DETAILS = "sub_category_details";
    String SIGNUP = "first_signup";
    String SUB_CATEGORY_NAME = "sub_category_name";
    String PHONE = "phone";
    String DOB = "dob";
    String NAME = "name";
    String GENDER = "gender";
    String SUB_CATEGORY_IMAGE_URL = "sub_category_cover_url";
    String CATEGORY_NAME = "category_name";
    String SUB_CATEGORY_ID = "sub_category_id";
    String REMEMBER_ME_CHECKED = "remember_me_checked";
    String STAGING_KEY = "10dd6208a8164d928d5bbc2d77f38d0000555300";
    String PRODUCTION_KEY = "fb2d4940d97842d4b17adf96ea9cc14100555300";
    String GUIDED_EXERCISE_DESCRIPTION = "guided_exercise_description";
    String SELECTED_EXERCISE_NAME = "selected_exercise_name";

    //AWS
    String COGNITO_POOL_ID = "eu-west-1:3361fea2-28fb-4b73-b82e-3f50f8c25bed";
    String BUCKET_NAME = "mentalsnapp";
    String S3_PRODUCTION = "/production";
    String S3_STAGING = "staging";
    String ENVIRONMENT_DIRECTORY = isProduction ? S3_PRODUCTION : S3_STAGING;
    String S3_PROFILE_PATH = "/profile_images/";
    String PROFILE_UPLOAD_PATH = ENVIRONMENT_DIRECTORY + S3_PROFILE_PATH;
    String S3_VIDEO_THUMBNAIL_PATH = "/video_thumbnails/";
    String S3_VIDEO_PATH = "/videos/";
    String VIDEO_UPLOAD_PATH = ENVIRONMENT_DIRECTORY + S3_VIDEO_PATH;
    String VIDEO_THUMBNAIL_UPLOAD_PATH = ENVIRONMENT_DIRECTORY + S3_VIDEO_THUMBNAIL_PATH;
    String AWS_PATH = "https://s3-eu-west-1.amazonaws.com/";

    String SET_MOOD = "set_mood";
    int SELECTED_FEELING = 151;
    String SELECTED_FEELING_NAME = "selected_feeling_name";
    String VIDEO_URI_NAME = "video_uri_name";
    String SELECTED_FEELING_ID = "selected_feeling_id";
    String GET_VIDEOS_API = "/api/v1/posts";
    String DELETE = "DELETE";
    String PLAY_VIDEO_URL = "play_video_url";
    String FILTERS_API = "/api/v1/filters/get_filters_list";
    String VIDEOS_BY_SEARCH_API = "/api/v1/posts/search_posts";
    String FULL_VIDEO_LIST = "full_video_list";
    String VIDEOS_BY_SEARCH = "videos_by_search";
    int PERMISSION_REQUEST_CODE_CAMERA = 110;
    int PERMISSION_REQUEST_CODE_DOWNLOAD = 120;
    String ALERT_SHOW = "is_alert_show";
    String GET_VIDEO_LIST_API = "api/v1/posts/get_video_url";
    String SELECTED_FEELING_RED = "selected_feeling_red";
    String SELECTED_FEELING_GREEN = "selected_feeling_green";
    String SELECTED_FEELING_BLUE = "selected_feeling_blue";
    String SELECTED_FEELING_DETAILS = "selected_feeling_details";
    String VIDEOS_BY_FILTER_API = "api/v1/filters/get_filter_posts";
    String GET_QUESTIONS_API = "/api/v1/sub_categories/";
    String SCHEDULE_API = "/api/v1/schedules";
    String CREATED_AT = "created_at";
    String SCHEDULABLE_TYPE = "schedulable_type";
    String SCHEDULABLE_ID = "schedulable_id";
    String EXECUTE_AT = "execute_at";
    String EXERCISE = "exercise";
    String DESCRIPTION = "description";
    String EXERCISE_ID = "exercise_id";
    String COVER_URL = "cover_url";
    String TYPE = "type";
    String QUESTION = "Question";
    String CURRENT_PAGE = "current_page";
    String TOTAL_PAGES = "total_pages";
    String SCHEDULES = "schedules";
    int PERMISSION_REQUEST_WRITE = 200;
    String SCHEDULE_RECORDING = "schedule_record_code";
    String SCHEDULE_DESCRIPTION = "schedule_description";
    int VIDEO_CAPTURE = 400;
    String GUIDED_EXERCISE = "Guided_Exercise";
    String GUIDED_EXERCISE_API = "api/v1/guided_exercise/";
    int PERMISSION_REQUEST_EXERCISE_CAMERA = 170;
    int VIDEO_CAPTURE_EXERCISE = 180;
    String JSON_FIELD_POSTS = "posts";
    String JSON_FIELD_BAR_CHART = "bar_chart";
    String JSON_FIELD_MONTH_DATA = "month_data";

    String JSON_FIELD_WEEK_DATA = "week_data";
    String VIDEOS_BY_FILTER = "videos_by_filter";
    String NO_INTERNET_STAGING = "Unable to resolve host \"mentalsnapp.systech-soft.com\": No address associated with hostname";
    String NO_INTERNET_PRODUCTION = "Unable to resolve host \"api.mentalsnapp.com\": No address associated with hostname";
    String NO_INTERNET_MESSAGE = isProduction ? NO_INTERNET_PRODUCTION : NO_INTERNET_STAGING;
    String ONBOARDING_SCREEN = "onboarding_screen";
    String FIRST_NAME = "first_time_install";
    String RECORD_ONBOARD = "record_onboard";
    String RECORD_ONBOARD_SCREEN = "record_onboard_screen";
    String TO = "to";
    String TUTORIAL = "tutorial";
    String TO_FREE_FORM = "to_free_form";
}
