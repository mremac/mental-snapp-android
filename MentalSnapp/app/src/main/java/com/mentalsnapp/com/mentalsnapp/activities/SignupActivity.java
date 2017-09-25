package com.mentalsnapp.com.mentalsnapp.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.models.User;
import com.mentalsnapp.com.mentalsnapp.network.ApiClient;
import com.mentalsnapp.com.mentalsnapp.network.response.SignupResponse;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;
import com.mentalsnapp.com.mentalsnapp.utils.EmailValidator;
import com.mentalsnapp.com.mentalsnapp.utils.JsonToStringConverter;
import com.mentalsnapp.com.mentalsnapp.utils.LogHelper;
import com.mentalsnapp.com.mentalsnapp.utils.SharedPref;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by gchandra on 20/12/16.
 */
public class SignupActivity extends BaseActivity {

    private RadioGroup mRadioGroup;
    private RadioButton mRadioButtonMale;
    private RadioButton mRadioButtonFemale;
    private RadioButton mRadioButtonOther;
    private EditText mFirstNameText;
    private EditText mLastNameText;
    private EditText mEmailText;
    private EditText mNumberText;
    private EditText mDOBText;
    private TextView mLogoText;
    private TextInputLayout mDOBTextTitle;
    private RelativeLayout mDobLayout;
    private EditText mPasswordText;
    private EditText mConfirmPasswordText;
    private ImageView mBackButton;
    private ImageView mSignupButton;
    private TextView mLoginButton;
    private ImageView mClearName;
    private ImageView mClearEmail;
    private ImageView mClearNumber;
    private ImageView mClearPassword;
    private ImageView mClearConfirmPassword;
    private ImageView mClearLastName;
    private RelativeLayout mProgressBar;
    private CheckBox mTnCCheckbox;
    private TextView mTnCText;
    private TextView mFeedbackEmail;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        registerViews();
        initialiseVariables();
        attachListeners();
    }

    private void initialiseVariables() {
        SpannableString logoMessage = new SpannableString("Record your own story");
        logoMessage.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.orange)), 7, 11,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mLogoText.setText(logoMessage);
    }

    private void attachListeners() {
        mBackButton.setOnClickListener(onClickListener);
        mSignupButton.setOnClickListener(onClickListener);
        mLoginButton.setOnClickListener(onClickListener);
        mDobLayout.setOnClickListener(onClickListener);
        mDOBTextTitle.setOnClickListener(onClickListener);
        mDOBText.setOnClickListener(onClickListener);
        mClearName.setOnClickListener(onClickListener);
        mClearNumber.setOnClickListener(onClickListener);
        mClearEmail.setOnClickListener(onClickListener);
        mClearPassword.setOnClickListener(onClickListener);
        mClearConfirmPassword.setOnClickListener(onClickListener);
        mClearLastName.setOnClickListener(onClickListener);
        mTnCText.setOnClickListener(onClickListener);
        mFeedbackEmail.setOnClickListener(onClickListener);
        mFirstNameText.setOnFocusChangeListener(onFocusChangeListener);
        mLastNameText.setOnFocusChangeListener(onFocusChangeListener);
        mEmailText.setOnFocusChangeListener(onFocusChangeListener);
        mNumberText.addTextChangedListener(commonTextWatcher);
//        mNumberText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        mNumberText.setOnFocusChangeListener(onFocusChangeListener);
        mPasswordText.setOnFocusChangeListener(onFocusChangeListener);
        mConfirmPasswordText.setOnFocusChangeListener(onFocusChangeListener);
        mConfirmPasswordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (performValidations()) {
                        if (mTnCCheckbox.isChecked()) {
                            if (mRadioButtonMale.isChecked()) {
                                saveData((String) mRadioButtonMale.getText());
                            } else if (mRadioButtonFemale.isChecked()) {
                                saveData((String) mRadioButtonFemale.getText());
                            } else if (mRadioButtonOther.isChecked()) {
                                saveData(mRadioButtonOther.getText().toString());
                            } else {
                                saveData(null);
                            }
                        } else {
                            showMessage(getResources().getString(R.string.check_tnc_message));
                        }
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
                case R.id.back_btn:
                    onBackPressed();
                    break;
                case R.id.sign_up_btn:
                    if (performValidations()) {
                        if (mTnCCheckbox.isChecked()) {
                            if (mRadioButtonMale.isChecked()) {
                                saveData((String) mRadioButtonMale.getText());
                            } else if (mRadioButtonFemale.isChecked()) {
                                saveData((String) mRadioButtonFemale.getText());
                            } else if (mRadioButtonOther.isChecked()) {
                                saveData(mRadioButtonOther.getText().toString());
                            } else {
                                saveData(null);
                            }
                        } else {
                            showMessage(getResources().getString(R.string.check_tnc_message));
                        }
                    }
                    break;
                case R.id.login_button:
                    openLoginScreen();
                    break;
                case R.id.dob_edittext_layout:
                    openDatePicker();
                    break;
                case R.id.dob_text:
                    openDatePicker();
                    break;
                case R.id.dob_title:
                    openDatePicker();
                    break;
                case R.id.cross_clear_first_name:
                    mFirstNameText.setText("");
                    break;
                case R.id.cross_clear_email:
                    mEmailText.setText("");
                    break;
                case R.id.cross_clear_number:
                    mNumberText.setText("");
                    break;
                case R.id.cross_clear_password:
                    mPasswordText.setText("");
                    break;
                case R.id.cross_clear_confirm_password:
                    mConfirmPasswordText.setText("");
                    break;
                case R.id.cross_clear_last_name:
                    mLastNameText.setText("");
                    break;
                case R.id.tnc_text:
                    openTnCPage();
                    break;
                case R.id.feedback_email:
                    openFeedbackEmail();
                    break;
            }
        }
    };

    private void openFeedbackEmail() {
        Calendar calendar = Calendar.getInstance();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Constants.TO, " feedback@mentalsnapp.com");
        intent.putExtra(Intent.EXTRA_SUBJECT, new SimpleDateFormat("dd/mm/yyyy").format(calendar.getTime()) + getResources().getString(R.string.feedback_subject));
        intent.setType("message/rfc822");
        startActivity(Intent.createChooser(intent,
                "Send Email Using: "));
    }

    private void openTnCPage() {
        Intent intent = new Intent(this, TnCActivity.class);
        startActivity(intent);
    }

    TextWatcher dobTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            mDOBText.setFocusableInTouchMode(false);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

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
                    break;
                case R.id.first_name_edittext:
                    if (hasFocus) {
                        mClearName.setVisibility(View.VISIBLE);
                    } else {
                        mClearName.setVisibility(View.GONE);
                    }
                    break;
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
                    break;
                case R.id.confirm_password_edittext:
                    if (hasFocus) {
                        mClearConfirmPassword.setVisibility(View.VISIBLE);
                    } else {
                        mClearConfirmPassword.setVisibility(View.GONE);
                    }
                    break;
                case R.id.last_name_edittext:
                    if (hasFocus) {
                        mClearLastName.setVisibility(View.VISIBLE);
                    } else {
                        mClearLastName.setVisibility(View.GONE);
                    }
            }
        }
    };

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
            /*if(s.length()==4){
                mNumberText.setText(s + " ");
            }*/
        }

        int oldLength = 4;
        char space = ' ';

        @Override
        public void afterTextChanged(Editable s) {
            /*if (mFirstNameText.getText().toString().length() > 0) {
                mClearName.setVisibility(View.VISIBLE);
            } else {
                mClearName.setVisibility(View.GONE);
            }
            if (mEmailText.getText().toString().length() > 0) {
                mClearEmail.setVisibility(View.VISIBLE);
            } else {
                mClearEmail.setVisibility(View.GONE);
            }
            if (mNumberText.getText().toString().length() > 0) {
                mClearNumber.setVisibility(View.VISIBLE);
            } else {
                mClearNumber.setVisibility(View.GONE);
            }
            if (mDOBText.getText().toString().length() > 0) {
                mDOBText.setError(null);
                mClearDOB.setVisibility(View.VISIBLE);
            } else {
                mClearDOB.setVisibility(View.GONE);
            }
            if (mPasswordText.getText().toString().length() > 0) {
                mClearPassword.setVisibility(View.VISIBLE);
            } else {
                mClearPassword.setVisibility(View.GONE);
            }
            if (mConfirmPasswordText.getText().toString().length() > 0) {
                mClearConfirmPassword.setVisibility(View.VISIBLE);
            } else {
                mClearConfirmPassword.setVisibility(View.GONE);
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

    private boolean performValidations() {
        boolean check = true;
        String message;
        if (mFirstNameText.getText().toString().length() == 0) {
            check = false;
            message = getResources().getString(R.string.enter_your_first_name_msg);
            showMessage(message);
            return check;
        }
        if (!isAlphaNumericUsername(mFirstNameText.getText().toString().trim())) {
            check = false;
            message = getResources().getString(R.string.name_incorrect_error);
            showMessage(message);
            return check;
        }
        if (!EmailValidator.hasText(mEmailText.getText().toString().trim()) ||
                !EmailValidator.checkEmail(mEmailText.getText().toString().trim())) {
            check = false;
            message = getResources().getString(R.string.email_not_valid_text);
            showMessage(message);
            return check;
        }
        if (!mNumberText.getText().toString().equals("+44 ") && isNumeric(mNumberText.getText().toString())) {
            if (mNumberText.getText().toString().length() < 15) {
                check = false;
                message = getResources().getString(R.string.enter_valid_number);
                showMessage(message);
                return check;
            }
        }
        if (mPasswordText.getText().toString().length() == 0) {
            check = false;
            message = getResources().getString(R.string.enter_valid_password);
            showMessage(message);
            return check;
        }
        if (mPasswordText.getText().toString().length() < 6) {
            check = false;
            message = getResources().getString(R.string.minimum_password_length_message);
            showMessage(message);
            return check;
        }
        if (!isAlphaNumericPassword(mPasswordText.getText().toString())) {
            check = false;
            message = getResources().getString(R.string.password_invalid_message);
            showMessage(message);
            return check;
        }
        if (mConfirmPasswordText.getText().toString().length() == 0) {
            check = false;
            message = getResources().getString(R.string.enter_confirm_password);
            showMessage(message);
            return check;
        }
        if (!mConfirmPasswordText.getText().toString().equals(mPasswordText.getText().toString())) {
            check = false;
            message = getResources().getString(R.string.password_dont_match);
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

    private void saveData(String selectedGender) {
        mProgressBar.setVisibility(View.VISIBLE);
        String number = mNumberText.getText().toString();
        number = number.replace(" ", "");
        if (number.startsWith("+44")) {
            number = number.replace("+44", "");
        }
        Call<SignupResponse> signupResponseCall = ApiClient.getApiInterface().signup(mEmailText.getText().toString().trim(),
                mFirstNameText.getText().toString().trim(), mLastNameText.getText().toString().trim(), mDOBText.getText().toString(), selectedGender, number, "+44",
                mPasswordText.getText().toString(), mConfirmPasswordText.getText().toString());
        signupResponseCall.enqueue(mResponseCallback);
        LogHelper.logInfo(mContext, "signupResponseCall ", signupResponseCall.toString());
    }

    private Callback mResponseCallback = new Callback() {
        @Override
        public void onResponse(Call call, Response response) {
            String mPassword = JsonToStringConverter.getRequestParamFromBody(JsonToStringConverter.mergeAndConvert(call.request().url().toString()
                    , call.request().method(), call.request().body()), "user%5Bpassword%5D");
            String mConfirmPassword = JsonToStringConverter.getRequestParamFromBody(JsonToStringConverter.mergeAndConvert(call.request().url().toString()
                    , call.request().method(), call.request().body()), "user%5Bpassword_confirmation%5D");
            LogHelper.logInfo(mContext, "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()).replace(mPassword, "*****").replace(mConfirmPassword, "********"));
            mProgressBar.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                LogHelper.logInfo(mContext, "mResponseCallback", JsonToStringConverter.convertToJson(response.body()));
                User user = ((SignupResponse) response.body()).user;
                SharedPref.setID(user.id, mContext);
                String authToken = ((SignupResponse) response.body()).authToken;
                SharedPref.setAuthToken(authToken, mContext);
                SharedPref.setLogin(true, mContext);
                SharedPref.setEmail(mEmailText.getText().toString().trim(), mContext);
                SharedPref.setPassword(Constants.EMPTY_STRING, mContext);
                Toast.makeText(mContext, getResources().getString(R.string.sign_up_successful_message), Toast.LENGTH_LONG).show();
                onBackPressed();
            } else {
                try {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(getApplicationContext(), String.valueOf(jObjError.get("message")), Toast.LENGTH_LONG).show();
                    LogHelper.logError(mContext, "mResponseCallback ", JsonToStringConverter.convertToJson(response.body()));
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
                mProgressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onFailure(Call call, Throwable t) {
            String mPassword = JsonToStringConverter.getRequestParamFromBody(JsonToStringConverter.mergeAndConvert(call.request().url().toString()
                    , call.request().method(), call.request().body()), "user%5Bpassword%5D");
            String mConfirmPassword = JsonToStringConverter.getRequestParamFromBody(JsonToStringConverter.mergeAndConvert(call.request().url().toString()
                    , call.request().method(), call.request().body()), "user%5Bpassword_confirmation%5D");
            LogHelper.logInfo(mContext, "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()).replace(mPassword, "*****").replace(mConfirmPassword, "********"));
            mProgressBar.setVisibility(View.GONE);
            try {
                if (t.getMessage().equals(Constants.NO_INTERNET_MESSAGE)) {
                    Toast.makeText(getApplicationContext(), String.valueOf(getResources().getString(R.string.no_internet)), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), String.valueOf(t.getMessage()), Toast.LENGTH_LONG).show();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            LogHelper.logError(mContext, "error", t.toString());
        }
    };

    private void launchProfileScreen() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        finish();
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

    private void openDatePicker() {
        closeKeyboard(this);
        DatePickerDialog mDatePickerDialog = new DatePickerDialog(this, R.style.AlertDialogTheme, date, mYear,
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        Calendar minAdultAge = new GregorianCalendar();
        minAdultAge.add(Calendar.YEAR, -18);
        mDatePickerDialog.getDatePicker().setMaxDate(minAdultAge.getTimeInMillis());
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

    private void openLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public boolean isNumeric(String s) {
        Pattern p = Pattern.compile("\\d+(?:\\.\\d+)?");
        boolean hasSpecialChar = p.matcher(s).find();
        return hasSpecialChar;
    }


    public boolean isAlphaNumericUsername(String s) {
        Pattern p = Pattern.compile("^[a-zA-Z0-9 ]+$");
        boolean hasSpecialChar = p.matcher(s).find();
        return hasSpecialChar;
    }

    public boolean isAlphaNumericPassword(String s) {
        Pattern p = Pattern.compile("^(?=.*\\d)(?=.*[a-zA-Z]).{6,14}$");
        boolean hasSpecialChar = p.matcher(s).find();
        return hasSpecialChar;
    }

    private void registerViews() {
        mRadioGroup = (RadioGroup) findViewById(R.id.radio_group);
        mRadioButtonMale = (RadioButton) findViewById(R.id.male_button);
        mRadioButtonMale.setButtonDrawable(R.drawable.radio_button);
        mRadioButtonFemale = (RadioButton) findViewById(R.id.female_button);
        mRadioButtonFemale.setButtonDrawable(R.drawable.radio_button);
        mRadioButtonOther = (RadioButton) findViewById(R.id.other_button);
        mRadioButtonOther.setButtonDrawable(R.drawable.radio_button);
        mBackButton = (ImageView) findViewById(R.id.back_btn);
        mFirstNameText = (EditText) findViewById(R.id.first_name_edittext);
        mLastNameText = (EditText) findViewById(R.id.last_name_edittext);
        mEmailText = (EditText) findViewById(R.id.email_edittext);
        mNumberText = (EditText) findViewById(R.id.number_edittext);
        mNumberText.setText("+44 ");
        mLogoText = (TextView) findViewById(R.id.logo_text);
//        mNumberText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(10)});
        mDOBText = (EditText) findViewById(R.id.dob_text);
        mDOBTextTitle = (TextInputLayout) findViewById(R.id.dob_title);
        mDobLayout = (RelativeLayout) findViewById(R.id.dob_edittext_layout);
        mPasswordText = (EditText) findViewById(R.id.password_edittext);
        mPasswordText.setTypeface(Typeface.DEFAULT);
        mPasswordText.setTransformationMethod(new PasswordTransformationMethod());
        mConfirmPasswordText = (EditText) findViewById(R.id.confirm_password_edittext);
        mConfirmPasswordText.setTypeface(Typeface.DEFAULT);
        mConfirmPasswordText.setTransformationMethod(new PasswordTransformationMethod());
        mSignupButton = (ImageView) findViewById(R.id.sign_up_btn);
        mLoginButton = (TextView) findViewById(R.id.login_button);
        mClearName = (ImageView) findViewById(R.id.cross_clear_first_name);
        mClearEmail = (ImageView) findViewById(R.id.cross_clear_email);
        mClearNumber = (ImageView) findViewById(R.id.cross_clear_number);
        mClearPassword = (ImageView) findViewById(R.id.cross_clear_password);
        mClearConfirmPassword = (ImageView) findViewById(R.id.cross_clear_confirm_password);
        mClearLastName = (ImageView) findViewById(R.id.cross_clear_last_name);
        mProgressBar = (RelativeLayout) findViewById(R.id.progress_bar_signup);
        mTnCCheckbox = (CheckBox) findViewById(R.id.checkbox_tnc);
        mTnCText = (TextView) findViewById(R.id.tnc_text);
        mFeedbackEmail = (TextView) findViewById(R.id.feedback_email);
        mContext = this;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        openLoginScreen();
    }
}
