package com.mentalsnapp.com.mentalsnapp.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.models.User;
import com.mentalsnapp.com.mentalsnapp.network.ApiClient;
import com.mentalsnapp.com.mentalsnapp.network.response.LoginResponse;
import com.mentalsnapp.com.mentalsnapp.network.response.ScheduleExerciseResponse;
import com.mentalsnapp.com.mentalsnapp.network.response.ScheduleListResponse;
import com.mentalsnapp.com.mentalsnapp.utils.AlarmReceiver;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;
import com.mentalsnapp.com.mentalsnapp.utils.EmailValidator;
import com.mentalsnapp.com.mentalsnapp.utils.JsonToStringConverter;
import com.mentalsnapp.com.mentalsnapp.utils.LogHelper;
import com.mentalsnapp.com.mentalsnapp.utils.SharedPref;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by gchandra on 15/12/16.
 */
public class LoginActivity extends BaseActivity {

    private EditText mEmailText;
    private EditText mPasswordText;
    private CheckBox mCheckRememberMe;
    private TextView mForgotPassword;
    private ImageView mSignUp;
    private ImageView mLoginButton;
    private String mEmail, mPassword;
    private RelativeLayout mProgressBar;
    private ImageView mClearEmail;
    private ImageView mClearPassword;
    private ArrayList<ScheduleExerciseResponse> mSchedulesList;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        registerViews();
        attachListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initialiseVariables();
    }

    private void initialiseVariables() {
        mContext = this;
        mSchedulesList = new ArrayList<>();
        if (!SharedPref.getEmail(mContext).equals(Constants.EMPTY_STRING)) {
            mEmailText.setText(SharedPref.getEmail(this));
            if (SharedPref.isRememberMeChecked(mContext)) {
                mCheckRememberMe.setChecked(true);
                mPasswordText.setText(SharedPref.getPassword(mContext));
            }
        }
    }

    public void onCheckedChanged(View view) {
        boolean isChecked = ((CheckBox) view).isChecked();
        if (isChecked) {
            Log.i("something", "toggle on or whatever");
            mPasswordText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            Log.i("whatever", "toggle off or something");
            mPasswordText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
    }

    private void attachListeners() {
//        ToggleButton toggle = (ToggleButton) findViewById(R.id.showPassword);
//        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    // The toggle is enabled
//                    Log.v("abcd", "efgh");
//                } else {
//                    // The toggle is disabled
//                    Log.v("some", "thing");
//                }
//            }
//        });
        mLoginButton.setOnClickListener(onClickListener);
//        CommonUtilities.buttonEffect(mLoginButton);
        mClearEmail.setOnClickListener(onClickListener);
        mClearPassword.setOnClickListener(onClickListener);
        mEmailText.setOnClickListener(onClickListener);
        mEmailText.setOnFocusChangeListener(onFocusChangeListener);
        mPasswordText.setOnClickListener(onClickListener);
        mPasswordText.setOnFocusChangeListener(onFocusChangeListener);
        mSignUp.setOnClickListener(onClickListener);
        mForgotPassword.setOnClickListener(onClickListener);
        mEmailText.addTextChangedListener(textWatcherEmail);
        mPasswordText.addTextChangedListener(textWatcherPassword);
        mPasswordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    performValidation();
                    return true;
                }
                return false;
            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.login_button:
                    performValidation();
                    break;
                case R.id.cross_clear_email:
                    mEmailText.setText("");
                    break;
                case R.id.cross_clear_password:
                    mPasswordText.setText("");
                    break;
                case R.id.sign_up:
                    openSignupScreen();
                    break;
                case R.id.forgot_password:
                    openForgotPasswordScreen();
                    break;
            }
        }
    };

    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            switch (v.getId()) {
                case R.id.email_edittext:
                    if (hasFocus) {
                        mClearEmail.setVisibility(View.VISIBLE);
                    } else {
                        mClearEmail.setVisibility(View.GONE);
                    }
                    break;
                case R.id.password_edittext:
                    if (hasFocus) {
                        mClearPassword.setVisibility(View.VISIBLE);
                    } else {
                        mClearPassword.setVisibility(View.GONE);
                    }
            }
        }
    };

    private void openForgotPasswordScreen() {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
        finish();
    }

    private void openSignupScreen() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
        finish();
    }

    TextWatcher textWatcherEmail = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            /*if (mEmailText.getText().toString().length() > 0) {
                mClearEmail.setVisibility(View.VISIBLE);
            } else {
                mClearEmail.setVisibility(View.GONE);
            }*/
        }
    };

    TextWatcher textWatcherPassword = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            /*if (mPasswordText.getText().toString().length() > 0) {
                mClearPassword.setVisibility(View.VISIBLE);
            } else {
                mClearPassword.setVisibility(View.GONE);
            }*/
        }
    };

    private void performValidation() {
        String email = mEmailText.getText().toString();
        email = email.trim();
        String password = mPasswordText.getText().toString();
        if (validate(email, password)) {
            mEmail = email;
            mPassword = password;
            mProgressBar.setVisibility(View.VISIBLE);
            Call<LoginResponse> loginResponseCall = ApiClient.getApiInterface().login(mEmail, mPassword);
            loginResponseCall.enqueue(mResponseCallback);
        }
    }

    private Callback mResponseCallback = new Callback() {
        @Override
        public void onResponse(Call call, Response response) {
            if (call.request().url().toString().contains(Constants.LOGIN_API)) {
                String mPassword = JsonToStringConverter.getRequestParamFromBody(JsonToStringConverter.mergeAndConvert(call.request().url().toString()
                        , call.request().method(), call.request().body()), "password");
                LogHelper.logInfo(mContext, "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                        call.request().method(), call.request().body()).replace(mPassword, "*****"));
            } else {
                LogHelper.logInfo(mContext, "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                        call.request().method(), call.request().body()));
            }
            if (response.isSuccessful()) {
                if (call.request().url().toString().contains(Constants.LOGIN_API)) {
                    SharedPref.setLogin(true, mContext);
                    User user = ((LoginResponse) response.body()).user;
                    SharedPref.setID(user.id, mContext);
                    String authToken = ((LoginResponse) response.body()).authToken;
                    SharedPref.setAuthToken(authToken, mContext);
                    SharedPref.setEmail(((LoginResponse) response.body()).user.email, mContext);
                    SharedPref.setPassword(mPassword, mContext);
                    SharedPref.setPhone(((LoginResponse) response.body()).user.phoneNumber, mContext);
                    SharedPref.setDOB(((LoginResponse) response.body()).user.dateOfBirth, mContext);
                    if (!TextUtils.isEmpty(((LoginResponse) response.body()).user.firstName) && !TextUtils.isEmpty(((LoginResponse) response.body()).user.lastName)) {
                        SharedPref.setName(((LoginResponse) response.body()).user.firstName + " " + ((LoginResponse) response.body()).user.lastName, mContext);
                        SharedPref.setFirstName(mContext, ((LoginResponse) response.body()).user.firstName);
                    } else if (!TextUtils.isEmpty(((LoginResponse) response.body()).user.firstName)) {
                        SharedPref.setName(((LoginResponse) response.body()).user.firstName, mContext);
                        SharedPref.setFirstName(mContext, ((LoginResponse) response.body()).user.firstName);
                    } else {
                        SharedPref.setName(((LoginResponse) response.body()).user.name, mContext);
                        String name[]=((LoginResponse)response.body()).user.name.split(" ");
                        SharedPref.setFirstName(mContext, name[0]);
                    }
                    SharedPref.setGender(((LoginResponse) response.body()).user.gender, mContext);
                    SharedPref.setAlertShow(false, mContext);
                    if (mCheckRememberMe.isChecked()) {
                        SharedPref.setRememberChecked(true, mContext);
                    }
                    getScheduleList();
                    if (((LoginResponse) response.body()).isFirstUser != null) {
                        if (((LoginResponse) response.body()).isFirstUser) {
                            sendUserDetailsGA();
                            SharedPref.setSignUp(true, mContext);
                            openOnboardingScreen();
                        } else {
                            openGuidedExercisesScreen();
                        }
                    }
                }
                if (call.request().url().toString().contains(Constants.SCHEDULE_API)) {
                    ArrayList<ScheduleExerciseResponse> list = ((ScheduleListResponse) response.body()).scheduleList;
                    mSchedulesList.addAll(list);
                    new scheduleAlarms().execute();
                }
                LogHelper.logInfo(mContext, "mResponseCallback", JsonToStringConverter.convertToJson(response.body()));
                mProgressBar.setVisibility(View.GONE);
            } else {
                try {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(getApplicationContext(), String.valueOf(jObjError.get("message")), Toast.LENGTH_LONG).show();
                    LogHelper.logError(mContext, "mResponseCallback", JsonToStringConverter.convertToJson(response.errorBody()));
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
                mProgressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onFailure(Call call, Throwable t) {
            String mPassword = JsonToStringConverter.getRequestParamFromBody(JsonToStringConverter.mergeAndConvert(call.request().url().toString()
                    , call.request().method(), call.request().body()), "password");
            LogHelper.logInfo(mContext, "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()).replace(mPassword, "*****"));
            mProgressBar.setVisibility(View.GONE);
            try {
                if (t.getMessage().contains(Constants.NO_INTERNET_MESSAGE)) {
                    Toast.makeText(getApplicationContext(), String.valueOf(getResources().getString(R.string.no_internet)), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), String.valueOf(t.getMessage()), Toast.LENGTH_LONG).show();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            LogHelper.logError(mContext, "error", t.getMessage());
        }
    };

    private void sendUserDetailsGA() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(mContext);
        Tracker mTracker = analytics.newTracker(R.xml.global_tracker);
        if (!SharedPref.getDOB(mContext).isEmpty()) {
            mTracker.setScreenName("User Info");
            mTracker.send(new HitBuilders.EventBuilder().setCategory(SharedPref.getID(mContext) + "_" + SharedPref.getName(mContext)).
                    setAction("Date of birth").setLabel(SharedPref.getDOB(mContext)).build());
        }
        if (!SharedPref.getGender(mContext).isEmpty()) {
            mTracker.setScreenName("User Info");
            mTracker.send(new HitBuilders.EventBuilder().setCategory(SharedPref.getID(mContext) + "_" + SharedPref.getName(mContext)).
                    setAction("Gender").setLabel(SharedPref.getGender(mContext)).build());
        }
    }

    public class scheduleAlarms extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i < mSchedulesList.size(); i++) {
                ScheduleExerciseResponse item = mSchedulesList.get(i);
                schedule(item);
            }
            return null;
        }
    }

    private void schedule(ScheduleExerciseResponse exerciseResponse) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(this, AlarmReceiver.class);
        myIntent.putExtra(Constants.SCHEDULE_DESCRIPTION, exerciseResponse.exercise.description);
        myIntent.putExtra(Constants.CATEGORY_NAME, Constants.QUESTION);
        myIntent.putExtra(Constants.SUB_CATEGORY_ID, getIntent().getLongExtra(Constants.SUB_CATEGORY_ID, 0));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, Integer.parseInt(exerciseResponse.id), myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, Long.parseLong(exerciseResponse.executeAt) * 1000, pendingIntent);
    }

    private void getScheduleList() {
        Call<ScheduleListResponse> getScheduleList = ApiClient.getApiInterface().getScheduleList();
        getScheduleList.enqueue(mResponseCallback);
    }

    private void openOnboardingScreen() {
        Intent intent = new Intent(this, OnboardingActivity.class);
        startActivity(intent);
        finish();
    }

    private void openGuidedExercisesScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public boolean isAlphaNumeric(String s) {
        Pattern p = Pattern.compile("^(?=.*\\d)(?=.*[a-zA-Z]).{6,14}$");
        boolean hasSpecialChar = p.matcher(s).find();
        return hasSpecialChar;
    }

    private boolean validate(String email, String password) {
        boolean result = true;
        if (email.length() != 0) {
            if (!EmailValidator.checkEmail(email)) {
                Toast.makeText(mContext, getResources().getString(R.string.email_not_valid_text), Toast.LENGTH_SHORT).show();
                result = false;
            } else if (password.length() == 0) {
                Toast.makeText(mContext, getResources().getString(R.string.password_not_valid_text), Toast.LENGTH_SHORT).show();
                result = false;
            } else if (!isAlphaNumericPassword(password)) {
                Toast.makeText(mContext, getResources().getString(R.string.invalid_credentials_message), Toast.LENGTH_LONG).show();
                result = false;
            }
        } else {
            Toast.makeText(mContext, getResources().getString(R.string.email_empty), Toast.LENGTH_SHORT).show();
            result = false;
        }
        return result;
    }

    public boolean isAlphaNumericPassword(String s) {
        Pattern p = Pattern.compile("^(?=.*\\d)(?=.*[a-zA-Z]).{6,14}$");
        boolean hasSpecialChar = p.matcher(s).find();
        return hasSpecialChar;
    }

    private void registerViews() {
        mEmailText = (EditText) findViewById(R.id.email_edittext);
        mPasswordText = (EditText) findViewById(R.id.password_edittext);
        mPasswordText.setTypeface(Typeface.DEFAULT);
        mPasswordText.setTransformationMethod(new PasswordTransformationMethod());
        mCheckRememberMe = (CheckBox) findViewById(R.id.checkbox_remember_me);
        mForgotPassword = (TextView) findViewById(R.id.forgot_password);
        mSignUp = (ImageView) findViewById(R.id.sign_up);
        mLoginButton = (ImageView) findViewById(R.id.login_button);
        mProgressBar = (RelativeLayout) findViewById(R.id.progress_bar_login);
        mClearEmail = (ImageView) findViewById(R.id.cross_clear_email);
        mClearPassword = (ImageView) findViewById(R.id.cross_clear_password);
    }
}
