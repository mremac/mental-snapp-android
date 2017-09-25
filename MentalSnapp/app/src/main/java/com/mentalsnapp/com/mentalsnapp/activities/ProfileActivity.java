package com.mentalsnapp.com.mentalsnapp.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mentalsnapp.com.mentalsnapp.BuildConfig;
import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.models.User;
import com.mentalsnapp.com.mentalsnapp.network.ApiClient;
import com.mentalsnapp.com.mentalsnapp.network.response.GetVideoListResponse;
import com.mentalsnapp.com.mentalsnapp.network.response.LoginResponse;
import com.mentalsnapp.com.mentalsnapp.utils.AWSHelper;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;
import com.mentalsnapp.com.mentalsnapp.utils.GlideUtils;
import com.mentalsnapp.com.mentalsnapp.utils.JsonToStringConverter;
import com.mentalsnapp.com.mentalsnapp.utils.LogHelper;
import com.mentalsnapp.com.mentalsnapp.utils.PermissionManagerUtil;
import com.mentalsnapp.com.mentalsnapp.utils.SharedPref;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by gchandra on 22/12/16.
 */
public class ProfileActivity extends BaseActivity {

    private static final int REQUEST_LOAD_IMAGE = 1;
    private final int REQUEST_CAPTURE_IMAGE = 2;
    private final int REQUEST_CROP_IMAGE = 3;
    final int REQUEST_CODE_WRITE_STORAGE = 21;

    private Toolbar mToolbar;
    private TextView mNametext;
    private EditText mDOBText;
    private TextInputLayout mDOBTitle;
    private EditText mNumberText;
    private EditText mEmailText;
    private RadioButton mMaleButton;
    private RadioButton mFemaleButton;
    private RadioButton mOtherButton;
    private ImageView mProfileImage;
    private RelativeLayout mChangePassword;
    private ImageView mSaveButton;
    private LinearLayout mDeleteProfile;
    private RelativeLayout mDOBLayout;
    private RelativeLayout mProgressBar;
    private Context mContext;
    private ImageView mClearNumber;
    private TextView mSkipButton;
    private LinearLayout mProfileToolbarLayout;
    private String[] mVideosList;
    private int videosIndex = 0;
    private Dialog mDownloadDialog;
    private ProgressBar mDownloadProgress;
    private ImageView mDownloadProgressCancel;
    private TextView mDownloadVideoName;
    private String mVideoName;
    private int mDownloadVideoId;
    private boolean mDownloadNext;

    private boolean mIsChooseFromLibrary;
    private File mCroppedImageFile;
    private File mCameraPhoto;
    private Uri mImageCaptureUri;
    private String mProfilePicUrl;
    private File mImageFile;
    private File mVideoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        registerViews();
        initialiseVariables();
        setUserDetails();
        setProfileImage(SharedPref.getProfileImagePath(mContext));
        attachListeners();
        getDetails();
    }

    private void setUserDetails() {
        mEmailText.setText(SharedPref.getEmail(mContext));
        mNumberText.setText("+44 " + SharedPref.getPhone(mContext));
        mNumberText.setSelection(mNumberText.getText().toString().length());
        mDOBText.setText(SharedPref.getDOB(mContext));
        mNametext.setText(SharedPref.getName(mContext));
        if (SharedPref.getGender(mContext).equalsIgnoreCase("male")) {
            mMaleButton.setChecked(true);
        } else if (SharedPref.getGender(mContext).equalsIgnoreCase("female")) {
            mFemaleButton.setChecked(true);
        } else if (SharedPref.getGender(mContext).equalsIgnoreCase("other")) {
            mOtherButton.setChecked(true);
        }
    }

    private void getDetails() {
        mProgressBar.setVisibility(View.VISIBLE);
        Call<LoginResponse> profileDetails = ApiClient.getApiInterface().profileDetails(SharedPref.getID(mContext));
        profileDetails.enqueue(mResponseCallback);
        LogHelper.logInfo(mContext, "profileDetails ", profileDetails.toString());
    }

    Callback mResponseCallback = new Callback() {
        @Override
        public void onResponse(Call call, Response response) {
            LogHelper.logInfo(mContext, "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()));
            mProgressBar.setVisibility(View.GONE);
            String callURL = call.request().url().toString();
            if (response.isSuccessful()) {
                LogHelper.logInfo(mContext, "mResponseCallback", JsonToStringConverter.convertToJson(response.body()));
                if (callURL.contains(Constants.GET_PROFILE_API + SharedPref.getID(mContext))
                        && call.request().method().equalsIgnoreCase(Constants.GET)) {
                    User user = ((LoginResponse) response.body()).user;
                    updateUI(user);
                } else if (callURL.contains(Constants.UPDATE_PROFILE_API + SharedPref.getID(mContext)) &&
                        call.request().method().equalsIgnoreCase(Constants.PATCH)) {
                    User user = ((LoginResponse) response.body()).user;
                    Toast.makeText(mContext, getResources().getString(R.string.successfully_updated), Toast.LENGTH_LONG).show();
                    GoogleAnalytics analytics = GoogleAnalytics.getInstance(mContext);
                    Tracker mTracker = analytics.newTracker(R.xml.global_tracker);
                    if (user.dateOfBirth != null && !user.dateOfBirth.isEmpty()) {
                        SharedPref.setDOB(user.dateOfBirth, mContext);
                        mTracker.setScreenName("User Info");
                        mTracker.send(new HitBuilders.EventBuilder().setCategory(SharedPref.getID(mContext) + "_" + SharedPref.getName(mContext)).
                                setAction("Date of birth").setLabel(SharedPref.getDOB(mContext)).build());
                    }
                    if (user.phoneNumber != null && !user.phoneNumber.isEmpty()) {
                        SharedPref.setPhone(user.phoneNumber, mContext);
                    }
                    if (user.gender != null && !user.gender.isEmpty()) {
                        SharedPref.setGender(user.gender, mContext);
                        mTracker.setScreenName("User Info");
                        mTracker.send(new HitBuilders.EventBuilder().setCategory(SharedPref.getID(mContext) + "_" + SharedPref.getName(mContext)).
                                setAction("Gender").setLabel(SharedPref.getGender(mContext)).build());
                    }
                    onBackPressed();
                } else if (callURL.contains(Constants.LOGOUT_API)) {
                    openLoginScreen();
                } else if (callURL.contains(Constants.DELETE_POFILE_API)) {
                    SharedPref.setEmail(Constants.EMPTY_STRING, mContext);
                    SharedPref.setPassword(Constants.EMPTY_STRING, mContext);
                    Toast.makeText(mContext, getResources().getString(R.string.goodbye_message), Toast.LENGTH_LONG).show();
                    openLoginScreen();
                } else if (callURL.contains(Constants.GET_VIDEO_LIST_API)) {
                    mVideosList = ((GetVideoListResponse) response.body()).getVideosUrls;
                    downloadAllVideos();
                }
            } else {
                try {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(mContext, String.valueOf(jObjError.get("message")), Toast.LENGTH_LONG).show();
                    LogHelper.logError(mContext, "mResponseCallback", JsonToStringConverter.convertToJson(response.body()));
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure(Call call, Throwable t) {
            LogHelper.logInfo(mContext, "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()));
            mProgressBar.setVisibility(View.GONE);
            try {
                if (t.getMessage().contains(Constants.NO_INTERNET_MESSAGE)) {
                    Toast.makeText(mContext, String.valueOf(getResources().getString(R.string.no_internet)), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, String.valueOf(t.getMessage()), Toast.LENGTH_LONG).show();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            LogHelper.logError(mContext, "error", t.toString());
        }
    };

    private void downloadAllVideos() {
        if (!mDownloadNext) {
            if (mVideosList[videosIndex].contains("https://s3-eu-west-1.amazonaws.com/mentalsnapp/")) {
                mVideosList[videosIndex] = mVideosList[videosIndex].replace("https://s3-eu-west-1.amazonaws.com/mentalsnapp/", "");
            }
            int nameIndex = mVideosList[videosIndex].lastIndexOf("/") + 1;
            mVideoName = mVideosList[videosIndex].substring(nameIndex);
            mVideoFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                    mVideosList[videosIndex].substring(nameIndex));
            AWSHelper.downloadFile(mContext, mVideosList[videosIndex], mVideoFile, downloadAllTransferListener);
            openDownloadPopup();
        }
    }

    private void openDownloadPopup() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View downloadLayout = inflater.inflate(R.layout.download_progress_layout, null);
        mDownloadProgressCancel = (ImageView) downloadLayout.findViewById(R.id.cancel);
        mDownloadProgress = (ProgressBar) downloadLayout.findViewById(R.id.download_progress);
        mDownloadVideoName = (TextView) downloadLayout.findViewById(R.id.download_video_name);
        mDownloadVideoName.setText(mVideoName);
        mDownloadProgress.setMax(100);
        mDownloadProgress.setBackgroundColor(ContextCompat.getColor(mContext, R.color.color_gray));
        mDownloadDialog = new Dialog(mContext);
        mDownloadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDownloadDialog.setContentView(downloadLayout);
        mDownloadDialog.setCancelable(false);
        mDownloadDialog.show();
        mDownloadProgressCancel.setOnClickListener(downloadOnClickListener);
    }

    View.OnClickListener downloadOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.cancel:
                    mDownloadDialog.dismiss();
                    AWSHelper.cancelTransfer(mContext, mDownloadVideoId);
                    mDownloadNext = true;
            }
        }
    };

    TransferListener downloadAllTransferListener = new TransferListener() {
        @Override
        public void onStateChanged(int id, TransferState state) {
            switch (state) {
                case COMPLETED:
                    ++videosIndex;
                    updateGallery(mVideoFile.getAbsolutePath());
                    if (videosIndex < mVideosList.length) {
                        mDownloadDialog.dismiss();
                        downloadAllVideos();
                    } else {
                        mDownloadDialog.dismiss();
                        Toast.makeText(mContext, getResources().getString(R.string.all_video_downloaded), Toast.LENGTH_LONG).show();
                        deleteProfile();
                    }
                    break;
                case IN_PROGRESS:
                    mDownloadVideoId = id;
            }
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            if (bytesTotal != 0) {
                mDownloadProgress.setProgress((int) (((bytesCurrent * 1.0) / (bytesTotal * 1.0)) * 100));
            }
        }

        @Override
        public void onError(int id, Exception ex) {

        }
    };

    public void updateGallery(String filePath) {
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(filePath))));
    }

    private void openLoginScreen() {
        SharedPref.setLogin(false, mContext);
        SharedPref.setAuthToken("", mContext);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void updateUI(User user) {
        if (!TextUtils.isEmpty(user.firstName) && !TextUtils.isEmpty(user.lastName)) {
            mNametext.setText(user.firstName + " " + user.lastName);
        } else if (!TextUtils.isEmpty(user.firstName)) {
            mNametext.setText(user.firstName);
        } else {
            mNametext.setText(user.name);
        }
        mEmailText.setText(user.email);
        if (user.dateOfBirth != null) {
            String myFormat = "dd MMMM yyyy"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date tempDate = inputFormat.parse(user.dateOfBirth);
                String date = sdf.format(tempDate);
                mDOBText.setText(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (user.phoneNumber != null) {
            mNumberText.setText("+44 " + user.phoneNumber);
            mNumberText.setSelection(mNumberText.getText().toString().length());
        }
        if (user.gender != null) {
            if (user.gender.equalsIgnoreCase("male")) {
                mMaleButton.setChecked(true);
            } else if (user.gender.equalsIgnoreCase("female")) {
                mFemaleButton.setChecked(true);
            } else if (user.gender.equalsIgnoreCase("other")) {
                mOtherButton.setChecked(true);
            }
        }
        SharedPref.setProfileImagePath(user.profileUrl, this);
        setProfileImage(user.profileUrl);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout_btn:
                confirmLogout();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        /*if (SharedPref.isSignUp(mContext)) {
            openHomeScreen();
        } else {*/
        super.onBackPressed();
//        }
    }

    private void openHomeScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void logout() {
        mProgressBar.setVisibility(View.VISIBLE);
        Call<LoginResponse> logoutCall = ApiClient.getApiInterface().logout(SharedPref.getID(mContext));
        logoutCall.enqueue(mResponseCallback);
        LogHelper.logInfo(mContext, "logoutCall", logoutCall.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    Calendar myCalendar = Calendar.getInstance();
    private int currentYear = myCalendar.get(Calendar.YEAR);
    private int mYear = currentYear - 18;
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setDOB();
        }
    };

    public boolean isNumeric(String s) {
        Pattern p = Pattern.compile("\\d+(?:\\.\\d+)?");
        boolean hasSpecialChar = p.matcher(s).find();
        return hasSpecialChar;
    }


    public boolean isAlphaNumericUsername(String s) {
        Pattern p = Pattern.compile("^[a-zA-Z0-9]+$");
        boolean hasSpecialChar = p.matcher(s).find();
        return hasSpecialChar;
    }

    private DatePickerDialog mDatePickerDialog;

    private void openDatePicker() {
        closeKeyboard(this);
        mDatePickerDialog = new DatePickerDialog(this, R.style.AlertDialogTheme, date, mYear,
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        Calendar minAdultAge = new GregorianCalendar();
        minAdultAge.add(Calendar.YEAR, -18);
        mDatePickerDialog.getDatePicker().setMaxDate(minAdultAge.getTimeInMillis());
        mDatePickerDialog.setCanceledOnTouchOutside(true);
        mDatePickerDialog.setCancelable(true);
        mDatePickerDialog.show();
    }

    private void setDOB() {
        String myFormat = "dd MMMM yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        String date = sdf.format(myCalendar.getTime());
        mDOBText.setText(date);
    }

    public void closeKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private void attachListeners() {
        mDOBLayout.setOnClickListener(onClickListener);
        mDOBTitle.setOnClickListener(onClickListener);
        mDOBText.setOnClickListener(onClickListener);
        mClearNumber.setOnClickListener(onClickListener);
        mSaveButton.setOnClickListener(onClickListener);
        mNumberText.addTextChangedListener(commonTextWatcher);
        mNumberText.setOnFocusChangeListener(onFocusChangeListener);
//        mNumberText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        mChangePassword.setOnClickListener(onClickListener);
        mDeleteProfile.setOnClickListener(onClickListener);
        mSkipButton.setOnClickListener(onClickListener);
        mProfileImage.setOnClickListener(onClickListener);
        mEmailText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.dob_edittext_layout:
                    openDatePicker();
                    break;
                case R.id.dob_text:
                    openDatePicker();
                    break;
                case R.id.dob_title:
                    openDatePicker();
                    break;
                case R.id.cross_clear_number:
                    mNumberText.setText("");
                    break;
                case R.id.save_btn:
                    if (performValidations()) {
                        if (mImageFile != null) {
                            uploadProfilePicToS3();
                        } else {
                            if (mMaleButton.isChecked()) {
                                saveData(mMaleButton.getText().toString(), mProfilePicUrl);
                            } else if (mFemaleButton.isChecked()) {
                                saveData(mFemaleButton.getText().toString(), mProfilePicUrl);
                            } else if (mOtherButton.isChecked()) {
                                saveData(mOtherButton.getText().toString(), mProfilePicUrl);
                            } else {
                                saveData(null, mProfilePicUrl);
                            }
                        }
                    }
                    break;
                case R.id.change_password_layout:
                    openChangePasswordScreen();
                    break;
                case R.id.delete_profile_layout:
                    mDownloadNext = false;
                    deleteConfirm();
                    break;
                case R.id.skip_button:
                    onBackPressed();
                    break;

                case R.id.profile_image:
                    showChoosePictureDialog();
                    break;
            }
        }
    };

    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            switch (v.getId()) {
                case R.id.number_edittext:
                    if (hasFocus) {
                        mClearNumber.setVisibility(View.VISIBLE);
                    } else {
                        mClearNumber.setVisibility(View.GONE);
                    }
            }
        }
    };

    private void deleteConfirm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);
        builder.setTitle("Delete Profile").setMessage("Are you sure you want to delete your profile?").setPositiveButton("Yes", deleteProfileDialogClickListener)
                .setNegativeButton("No", deleteProfileDialogClickListener).show();
    }

    DialogInterface.OnClickListener deleteProfileDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    downloadVideosConfirm();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    private void downloadVideosConfirm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);
        builder.setTitle("Download All videos").setMessage("Let us know if you want to download videos you have recorded.")
                .setPositiveButton("Yes", downloadVideosDialogClickListener)
                .setNegativeButton("No", downloadVideosDialogClickListener).show();
    }

    DialogInterface.OnClickListener downloadVideosDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    getVideosList();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    deleteProfile();
                    break;
            }
        }
    };

    private void getVideosList() {
        Call<GetVideoListResponse> getVideosList = ApiClient.getApiInterface().getVideosList();
        getVideosList.enqueue(mResponseCallback);
    }

    private void deleteProfile() {
        mProgressBar.setVisibility(View.VISIBLE);
        Call<LoginResponse> deleteProfileCall = ApiClient.getApiInterface().deleteProfile(SharedPref.getID(mContext));
        deleteProfileCall.enqueue(mResponseCallback);
    }

    private void confirmLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);
        String logout = "Logout";
        SpannableString str = new SpannableString(logout);
        str.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.blue)), 0, logout.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setTitle(str).setMessage("Are you sure you want to logout?").setPositiveButton("Yes", logoutDialogClickListener)
                .setNegativeButton("No", logoutDialogClickListener).show();
    }

    DialogInterface.OnClickListener logoutDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    logout();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };

    private void openChangePasswordScreen() {
        Intent intent = new Intent(this, ChangePasswordActivity.class);
        startActivity(intent);
    }

    private void saveData(String selectedGender, String profileImagePath) {
        mProgressBar.setVisibility(View.VISIBLE);
        String number = mNumberText.getText().toString();
        number = number.replace(" ", "");
        if (number.startsWith("+44")) {
            number = number.replace("+44", "");
        }
        Call<LoginResponse> updateProfile = ApiClient.getApiInterface().updateProfile(SharedPref.getID(mContext), SharedPref.getEmail(mContext),
                mNametext.getText().toString().trim(), mDOBText.getText().toString(), selectedGender, number, "+44", mProfilePicUrl);
        updateProfile.enqueue(mResponseCallback);
        LogHelper.logInfo(mContext, "updateProfile", updateProfile.toString());
    }

    private boolean performValidations() {
        boolean check = true;
        if (!mNumberText.getText().toString().equals("+44 ") && isNumeric(mNumberText.getText().toString())) {
            if (mNumberText.getText().toString().length() < 15) {
                check = false;
                Toast.makeText(mContext, getResources().getString(R.string.enter_valid_number), Toast.LENGTH_SHORT).show();
                return check;
            }
        }
        return check;
    }

    TextWatcher commonTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
                /*if(mNumberText.getText().toString().length()>0){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        String formattedNumber = PhoneNumberUtils.formatNumber(mNumberText.getText().toString(), Locale.getDefault().getCountry());
                        mNumberText.setText(formattedNumber);
                    }else {
                        String formattedNumber=PhoneNumberUtils.formatNumber(mNumberText.getText().toString());
                        mNumberText.setText(formattedNumber);
                    }
                }*/
        }

        int oldLength = 4;
        char space = ' ';

        @Override
        public void afterTextChanged(Editable s) {
            /*if (mNumberText.getText().toString().length() > 0) {
                mClearNumber.setVisibility(View.VISIBLE);
            } else {
                mClearNumber.setVisibility(View.GONE);
            }
            if (mDOBText.getText().toString().length() > 0) {
                mClearDOB.setVisibility(View.VISIBLE);
                mDOBText.setError(null);
            } else {
                mClearDOB.setVisibility(View.GONE);
            }*/
            if (!s.toString().startsWith("+44 ")) {
                mNumberText.setText("+44 ");
                mNumberText.setSelection(s.length());
            }
            String s1 = s.toString();
            if (s1.length() == 8) {
                if (oldLength > s.length()) {
                    s1 = s1.substring(4, s.length() - 1);
                    mNumberText.setText("+44 " + s1);
                    mNumberText.setSelection(s1.length());
                } else {
                    s1 = s1 + String.valueOf(space);
                    mNumberText.setText(s1);
                    mNumberText.setSelection(s1.length());
                }
                oldLength = s1.length();
            } else {


                if (s1.length() > 7 && s1.length() != oldLength && s1.charAt(8) != space) {
                    s1 = s1.substring(4, 8) + " " + s1.substring(8).replace(" ", "");

                    oldLength = s1.length();
                    mNumberText.setText("+44 " + s1);
                    mNumberText.setSelection(s1.length());
                } else {
                    oldLength = s1.length();
                }
            }
            mNumberText.setSelection(mNumberText.getText().toString().length());
        }
    };


    private void initialiseVariables() {
        setSupportActionBar(mToolbar);
        /*if (SharedPref.isSignUp(mContext)) {
            mSkipButton.setVisibility(View.VISIBLE);
            mProfileToolbarLayout.setVisibility(View.VISIBLE);
        } else {*/
        mSkipButton.setVisibility(View.GONE);
        mProfileToolbarLayout.setVisibility(View.GONE);
        setHeader(getResources().getString(R.string.profile));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        }
        mContext = this;
        mProfilePicUrl = SharedPref.getProfileImagePath(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPref.setSignUp(false, mContext);
    }

    private void registerViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mNametext = (TextView) findViewById(R.id.user_name);
        mEmailText = (EditText) findViewById(R.id.email_edittext);
        mDOBText = (EditText) findViewById(R.id.dob_text);
        mNumberText = (EditText) findViewById(R.id.number_edittext);
        mNumberText.setText("+44 ");
        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mChangePassword = (RelativeLayout) findViewById(R.id.change_password_layout);
        mDeleteProfile = (LinearLayout) findViewById(R.id.delete_profile_layout);
        mMaleButton = (RadioButton) findViewById(R.id.male_button);
        mMaleButton.setButtonDrawable(R.drawable.radio_button);
        mFemaleButton = (RadioButton) findViewById(R.id.female_button);
        mFemaleButton.setButtonDrawable(R.drawable.radio_button);
        mOtherButton = (RadioButton) findViewById(R.id.other_button);
        mOtherButton.setButtonDrawable(R.drawable.radio_button);
        mSaveButton = (ImageView) findViewById(R.id.save_btn);
        mDOBLayout = (RelativeLayout) findViewById(R.id.dob_edittext_layout);
        mProgressBar = (RelativeLayout) findViewById(R.id.progress_bar_profile);
        mDOBTitle = (TextInputLayout) findViewById(R.id.dob_title);
        mClearNumber = (ImageView) findViewById(R.id.cross_clear_number);
        mSkipButton = (TextView) findViewById(R.id.skip_button);
        mProfileToolbarLayout = (LinearLayout) findViewById(R.id.profile_toolbar_layout);
    }

    private void showChoosePictureDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.choose_pic_dialog);
        TextView deletePhoto = (TextView) dialog.findViewById(R.id.delete_photo);
        TextView takePhotoFromLibrary = (TextView) dialog.findViewById(R.id.take_photo_library);
        TextView takePhoto = (TextView) dialog.findViewById(R.id.take_photo);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel);

        takePhotoFromLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (handleFilePermission()) {
                    chooseFromLibrary();
                } else {
                    mIsChooseFromLibrary = true;
                }
                dialog.dismiss();
            }
        });

        deletePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetProfile();
                dialog.dismiss();
            }
        });

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (handleFilePermission()) {
                    takePicture();
                } else {
                    mIsChooseFromLibrary = false;
                }
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (getWindowManager().getDefaultDisplay().getWidth() - getResources().getDimension(R.dimen.dialog_margin));
        dialog.getWindow().setAttributes(params);
    }

    private void resetProfile() {
        mIsChooseFromLibrary = false;
        mCroppedImageFile = null;
        mCameraPhoto = null;
        mImageCaptureUri = null;
        mProfilePicUrl = null;
        mImageFile = null;
        mProfileImage.setImageResource(R.drawable.default_profile_image);

    }

    private void performCrop() {
        if (mCroppedImageFile != null && mCroppedImageFile.exists()) {
            mCroppedImageFile.delete();
        }
        mCroppedImageFile = new File(android.os.Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".png");
        //call the standard crop action intent (the user device may not support it)
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= 24) {
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        //indicate image type and Uri
        cropIntent.setDataAndType(mImageCaptureUri, "image/*");

//            cropIntent.setData(mImageCaptureUri);
        //set crop properties
        cropIntent.putExtra("crop", "true");
        //indicate aspect of desired crop
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        //indicate output X and Y
        cropIntent.putExtra("outputX", 512);
        cropIntent.putExtra("outputY", 512);

//            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, pic);
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCroppedImageFile));
        //start the activity - we handle returning in onActivityResult
        startActivityForResult(cropIntent, REQUEST_CROP_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOAD_IMAGE && resultCode == Activity.RESULT_OK
                && null != data) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                File file = new File(selectedImage.getPath());
                mImageCaptureUri = data.getData();

                selectedImageHandling(file.getAbsolutePath(), file);

                performCrop();
            }
        } else if (requestCode == REQUEST_CAPTURE_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    performCrop();
                } catch (ActivityNotFoundException anfe) {
                    selectedImageHandling(mCameraPhoto.getAbsolutePath(), mCameraPhoto);
                }
            }
        } else if (requestCode == REQUEST_CROP_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (mCroppedImageFile.exists()) {
                    selectedImageHandling(mCroppedImageFile.getAbsolutePath(), mCroppedImageFile);
                }
            }
        }
    }

    private void selectedImageHandling(String selectedImagePath, File file) {
        mImageFile = file;
//        mProfilePicUrl = selectedImagePath;
        if (!TextUtils.isEmpty(selectedImagePath)) {
            setProfileImage(selectedImagePath);
        }
    }

    private void takePicture() {
        Intent cameraIntent = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        mCameraPhoto = new File(android.os.Environment.getExternalStorageDirectory(), "temp1.png");
        if (Build.VERSION.SDK_INT >= 24) {
            mImageCaptureUri = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", mCameraPhoto);
            mContext.grantUriPermission("com.android.camera", mImageCaptureUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            mImageCaptureUri = Uri.fromFile(mCameraPhoto);
        }
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        startActivityForResult(cameraIntent, REQUEST_CAPTURE_IMAGE);
    }

    private void chooseFromLibrary() {
        Intent browsePhotoIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(browsePhotoIntent, REQUEST_LOAD_IMAGE);
    }

    private boolean handleFilePermission() {
        if (!PermissionManagerUtil.checkPermission(PermissionManagerUtil.WRITE_EXTERNAL_STORAGE_PERMISSION, this)) {
            ActivityCompat.requestPermissions(this, new String[]{PermissionManagerUtil.WRITE_EXTERNAL_STORAGE_PERMISSION}, REQUEST_CODE_WRITE_STORAGE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_WRITE_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mIsChooseFromLibrary) {
                        chooseFromLibrary();
                    } else {
                        takePicture();
                    }
                }
                break;
        }
    }

    private void uploadProfilePicToS3() {
        mProgressBar.setVisibility(View.VISIBLE);
        AWSHelper.uploadProfileImage(this, mImageFile, mTransferListener);
    }

    TransferListener mTransferListener = new TransferListener() {
        @Override
        public void onStateChanged(int id, TransferState state) {
            switch (state) {
                case COMPLETED:
                    mProfilePicUrl = getUploadedImagePath();
                    saveData(getGender(), mProfilePicUrl);
                    mProgressBar.setVisibility(View.VISIBLE);
                    break;

                case CANCELED:
                    mProgressBar.setVisibility(View.VISIBLE);
                    break;
            }
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
        }

        @Override
        public void onError(int id, Exception ex) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    };

    private String getUploadedImagePath() {
        return Constants.AWS_PATH + Constants.BUCKET_NAME + "/" + Constants.PROFILE_UPLOAD_PATH + mImageFile.getName();
    }

    private String getGender() {
        if (mMaleButton.isChecked()) {
            return mMaleButton.getText().toString();
        } else if (mFemaleButton.isChecked()) {
            return mFemaleButton.getText().toString();
        } else if (mOtherButton.isChecked()) {
            return mOtherButton.getText().toString();
        }
        return null;
    }

    private void setProfileImage(String imagePath) {
        GlideUtils.loadCircularImage(this, imagePath, mProfileImage, R.drawable.default_profile_image);
    }
}
