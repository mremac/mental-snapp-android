package com.mentalsnapp.com.mentalsnapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.network.ApiClient;
import com.mentalsnapp.com.mentalsnapp.network.response.LoginResponse;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;
import com.mentalsnapp.com.mentalsnapp.utils.EmailValidator;
import com.mentalsnapp.com.mentalsnapp.utils.JsonToStringConverter;
import com.mentalsnapp.com.mentalsnapp.utils.LogHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by gchandra on 23/12/16.
 */
public class ForgotPasswordActivity extends BaseActivity {

    private ImageView mSubmitButton;
    private EditText mEmailText;
    private ImageView mCrossClearEmail;
    private Context mContext;
    private Toolbar mToolbar;
    private RelativeLayout mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        registerViews();
        initialiseVariables();
        attachListeners();
    }

    private void attachListeners() {
        mCrossClearEmail.setOnClickListener(onClickListener);
        mSubmitButton.setOnClickListener(onClickListener);
        mEmailText.addTextChangedListener(commonTextWatcher);
        mEmailText.setOnFocusChangeListener(onFocusChangeListener);
        mEmailText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (validated(mEmailText.getText().toString().trim())) {
                        sendEmail();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.cross_clear_email:
                    mEmailText.setText("");
                    break;
                case R.id.submit_button:
                    hideSoftKeyboard();
                    if (validated(mEmailText.getText().toString().trim())) {
                        sendEmail();
                    }
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
                        mCrossClearEmail.setVisibility(View.VISIBLE);
                    } else {
                        mCrossClearEmail.setVisibility(View.GONE);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        openLoginscreen();
    }

    private void hideSoftKeyboard() {
        if (getCurrentFocus() != null && getCurrentFocus() instanceof EditText) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEmailText.getWindowToken(), 0);
        }
    }

    private void openLoginscreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendEmail() {
        mProgressBar.setVisibility(View.VISIBLE);
        Call<LoginResponse> forgotPasswordCall = ApiClient.getApiInterface().forgotPassword(mEmailText.getText().toString().trim());
        forgotPasswordCall.enqueue(mResponseCallback);
        LogHelper.logInfo(mContext, "forgotPasswordCall", forgotPasswordCall.toString());
    }

    Callback mResponseCallback = new Callback() {
        @Override
        public void onResponse(Call call, Response response) {
            LogHelper.logInfo(mContext, "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()));
            mProgressBar.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                LogHelper.logInfo(mContext, "mResponseCallback", JsonToStringConverter.convertToJson(response.body()));
                Toast.makeText(mContext, getResources().getString(R.string.go_to_email), Toast.LENGTH_LONG).show();
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

    TextWatcher commonTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mEmailText.getText().toString().length() > 0) {
                mCrossClearEmail.setVisibility(View.VISIBLE);
            } else {
                mCrossClearEmail.setVisibility(View.GONE);
            }
        }
    };

    private boolean validated(String email) {
        boolean check = true;
        String message;
        if (!EmailValidator.hasText(email)) {
            check = false;
            message = getResources().getString(R.string.email_empty);
            showMessage(message);
            return check;
        }
        if (!EmailValidator.checkEmail(email)) {
            check = false;
            message = getResources().getString(R.string.email_not_valid_text);
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

    private void initialiseVariables() {
        setSupportActionBar(mToolbar);
        setHeader(getResources().getString(R.string.forgot_password_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mContext = this;
    }

    private void registerViews() {
        mEmailText = (EditText) findViewById(R.id.email_edittext);
        mCrossClearEmail = (ImageView) findViewById(R.id.cross_clear_email);
        mSubmitButton = (ImageView) findViewById(R.id.submit_button);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mProgressBar = (RelativeLayout) findViewById(R.id.progress_bar_forgot_password);
    }
}
