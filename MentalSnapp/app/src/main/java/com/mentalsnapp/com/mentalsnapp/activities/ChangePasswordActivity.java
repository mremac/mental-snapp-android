package com.mentalsnapp.com.mentalsnapp.activities;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.network.ApiClient;
import com.mentalsnapp.com.mentalsnapp.network.response.LoginResponse;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;
import com.mentalsnapp.com.mentalsnapp.utils.JsonToStringConverter;
import com.mentalsnapp.com.mentalsnapp.utils.LogHelper;
import com.mentalsnapp.com.mentalsnapp.utils.SharedPref;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by gchandra on 23/12/16.
 */
public class ChangePasswordActivity extends BaseActivity {

    private EditText mCurrentPassword;
    private EditText mNewPassword;
    private EditText mConfirmNewPassword;
    private ImageView mSaveButton;
    private Toolbar mToolbar;
    private Context mContext;
    private ImageView mCrossClearCurrentPassword;
    private ImageView mCrossClearNewPassword;
    private ImageView mCrossClearConfirmNewPassword;
    private RelativeLayout mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        registerViews();
        initialiseVariables();
        attachListeners();
    }

    private void attachListeners() {
        mCurrentPassword.addTextChangedListener(commonTextWatcher);
        mCurrentPassword.setOnFocusChangeListener(onFocusChangeListener);
        mNewPassword.addTextChangedListener(commonTextWatcher);
        mNewPassword.setOnFocusChangeListener(onFocusChangeListener);
        mConfirmNewPassword.addTextChangedListener(commonTextWatcher);
        mConfirmNewPassword.setOnFocusChangeListener(onFocusChangeListener);
        mSaveButton.setOnClickListener(onClickListener);
        mCrossClearCurrentPassword.setOnClickListener(onClickListener);
        mCrossClearNewPassword.setOnClickListener(onClickListener);
        mCrossClearConfirmNewPassword.setOnClickListener(onClickListener);
        mConfirmNewPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (performValidations()) {
                        saveNewPassword();
                    }
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
                case R.id.save_btn:
                    if (performValidations()) {
                        saveNewPassword();
                    }
                    break;
                case R.id.cross_clear_current_password:
                    mCurrentPassword.setText("");
                    break;
                case R.id.cross_clear_new_password:
                    mNewPassword.setText("");
                    break;
                case R.id.cross_clear_confirm_new_password:
                    mConfirmNewPassword.setText("");
                    break;
            }
        }
    };

    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            switch (v.getId()) {
                case R.id.current_password_edittext:
                    if (hasFocus) {
                        mCrossClearCurrentPassword.setVisibility(View.VISIBLE);
                    } else {
                        mCrossClearCurrentPassword.setVisibility(View.GONE);
                    }
                    break;
                case R.id.new_password_edittext:
                    if (hasFocus) {
                        mCrossClearNewPassword.setVisibility(View.VISIBLE);
                    } else {
                        mCrossClearNewPassword.setVisibility(View.GONE);
                    }
                    break;
                case R.id.confirm_new_password_edittext:
                    if (hasFocus) {
                        mCrossClearConfirmNewPassword.setVisibility(View.VISIBLE);
                    } else {
                        mCrossClearConfirmNewPassword.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveNewPassword() {
        mProgressBar.setVisibility(View.VISIBLE);
        Call<LoginResponse> changePasswordCall = ApiClient.getApiInterface().changePassword(mCurrentPassword.getText().toString(),
                mNewPassword.getText().toString(), mConfirmNewPassword.getText().toString());
        changePasswordCall.enqueue(mResponseCallback);
        LogHelper.logInfo(mContext, "changePasswordCall", changePasswordCall.toString());
    }

    Callback mResponseCallback = new Callback() {
        @Override
        public void onResponse(Call call, Response response) {
            String mPassword = JsonToStringConverter.getRequestParamFromBody(JsonToStringConverter.mergeAndConvert(call.request().url().toString()
                    , call.request().method(), call.request().body()), "user%5Bpassword%5D");
            String mConfirmPassword = JsonToStringConverter.getRequestParamFromBody(JsonToStringConverter.mergeAndConvert(call.request().url().toString()
                    , call.request().method(), call.request().body()), "user%5Bpassword_confirmation%5D");
            String mCurrentPassword = JsonToStringConverter.getRequestParamFromBody(JsonToStringConverter.mergeAndConvert(call.request().url().toString()
                    , call.request().method(), call.request().body()), "user%5Bcurrent_password%5D");
            LogHelper.logInfo(mContext, "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()).replace(mPassword, "*****").replace(mConfirmPassword, "******").replace(mCurrentPassword, "******"));
            mProgressBar.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                LogHelper.logInfo(mContext, "mResponseCallback", JsonToStringConverter.convertToJson(response.body()));
                Toast.makeText(mContext, getResources().getString(R.string.password_successful), Toast.LENGTH_LONG).show();
                SharedPref.setPassword(mNewPassword.getText().toString(),mContext);
                onBackPressed();
            } else {
                try {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(getApplicationContext(), String.valueOf(jObjError.get("message")), Toast.LENGTH_LONG).show();
                    LogHelper.logError(mContext, "mResponseCallback", JsonToStringConverter.convertToJson(response.errorBody()));
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure(Call call, Throwable t) {
            String mPassword = JsonToStringConverter.getRequestParamFromBody(JsonToStringConverter.mergeAndConvert(call.request().url().toString()
                    , call.request().method(), call.request().body()), "user%5Bpassword%5D");
            String mConfirmPassword = JsonToStringConverter.getRequestParamFromBody(JsonToStringConverter.mergeAndConvert(call.request().url().toString()
                    , call.request().method(), call.request().body()), "user%5Bpassword_confirmation%5D");
            String mCurrentPassword = JsonToStringConverter.getRequestParamFromBody(JsonToStringConverter.mergeAndConvert(call.request().url().toString()
                    , call.request().method(), call.request().body()), "user%5Bcurrent_password%5D");
            LogHelper.logInfo(mContext, "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()).replace(mPassword, "*****").replace(mConfirmPassword, "******").replace(mCurrentPassword, "******"));
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

    private boolean performValidations() {
        boolean check = true;
        String message;
        if (mCurrentPassword.getText().toString().length() == 0) {
            check = false;
            message = getResources().getString(R.string.enter_current_password);
            showMessage(message);
            return check;
        }
        if (mNewPassword.getText().toString().length() == 0) {
            check = false;
            message = getResources().getString(R.string.enter_new_password);
            showMessage(message);
            return check;
        }
        if (mNewPassword.getText().toString().length() < 6) {
            check = false;
            message = getResources().getString(R.string.password_length_error);
            showMessage(message);
            return check;
        }
        if (mConfirmNewPassword.getText().toString().length() == 0) {
            check = false;
            message = getResources().getString(R.string.enter_confirm_new_password);
            showMessage(message);
            return check;
        }
        if (!mConfirmNewPassword.getText().toString().equals(mNewPassword.getText().toString())) {
            check = false;
            message = getResources().getString(R.string.reenter_password_dont_match);
            showMessage(message);
            return check;
        }
        return check;
    }

    private void showMessage(String message) {
        if (message != null) {
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isAlphaNumericPassword(String s) {
        Pattern p = Pattern.compile("^(?=.*\\d)(?=.*[a-zA-Z]).{6,14}$");
        boolean hasSpecialChar = p.matcher(s).find();
        return hasSpecialChar;
    }

    private void initialiseVariables() {
        setSupportActionBar(mToolbar);
        setHeader(getResources().getString(R.string.change_password));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mContext = this;
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

        @Override
        public void afterTextChanged(Editable s) {
            /*if (mCurrentPassword.getText().toString().length() > 0) {
                mCrossClearCurrentPassword.setVisibility(View.VISIBLE);
            } else {
                mCrossClearCurrentPassword.setVisibility(View.GONE);
            }
            if (mNewPassword.getText().toString().length() > 0) {
                mCrossClearNewPassword.setVisibility(View.VISIBLE);
            } else {
                mCrossClearNewPassword.setVisibility(View.GONE);
            }
            if (mConfirmNewPassword.getText().toString().length() > 0) {
                mCrossClearConfirmNewPassword.setVisibility(View.VISIBLE);
            } else {
                mCrossClearConfirmNewPassword.setVisibility(View.GONE);
            }*/
        }
    };

    private void registerViews() {
        mCurrentPassword = (EditText) findViewById(R.id.current_password_edittext);
        mCurrentPassword.setTypeface(Typeface.DEFAULT);
        mCurrentPassword.setTransformationMethod(new PasswordTransformationMethod());
        mNewPassword = (EditText) findViewById(R.id.new_password_edittext);
        mNewPassword.setTypeface(Typeface.DEFAULT);
        mNewPassword.setTransformationMethod(new PasswordTransformationMethod());
        mConfirmNewPassword = (EditText) findViewById(R.id.confirm_new_password_edittext);
        mConfirmNewPassword.setTypeface(Typeface.DEFAULT);
        mConfirmNewPassword.setTransformationMethod(new PasswordTransformationMethod());
        mSaveButton = (ImageView) findViewById(R.id.save_btn);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mCrossClearCurrentPassword = (ImageView) findViewById(R.id.cross_clear_current_password);
        mCrossClearNewPassword = (ImageView) findViewById(R.id.cross_clear_new_password);
        mCrossClearConfirmNewPassword = (ImageView) findViewById(R.id.cross_clear_confirm_new_password);
        mProgressBar = (RelativeLayout) findViewById(R.id.progress_bar_change_password);
    }
}
