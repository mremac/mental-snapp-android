package com.mentalsnapp.com.mentalsnapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.SMobiLogger.SLog;
import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.network.ApiClient;
import com.mentalsnapp.com.mentalsnapp.network.response.LoginResponse;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;
import com.mentalsnapp.com.mentalsnapp.utils.EmailValidator;
import com.mentalsnapp.com.mentalsnapp.utils.JsonToStringConverter;
import com.mentalsnapp.com.mentalsnapp.utils.LogHelper;
import com.mentalsnapp.com.mentalsnapp.utils.RealPathUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by gchandra on 29/12/16.
 */
public class ReportAnIssueActivity extends BaseActivity {

    private EditText mIssueTitle;
    private EditText mIssueDescription;
    private RelativeLayout mAttachImageLayout;
    private ImageView mSubmitButton;
    private ImageView mClearIssueTitle;
    private Toolbar mToolbar;
    private File mScreenshot;
    private File mLogFile;
    private boolean mIsFileCreated;
    private TextView mAttachImageText;
    private ImageView mClearImage;
    private TextView mFeedbackEmail;
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    private static final int PERMISSION_REQUEST_CODE_READ = 200;
    private static final int PERMISSION_REQUEST_CODE_WRITE = 300;
    private RelativeLayout mProgresBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_an_issue);
        registerViews();
        initialiseVariables();
        getLogFile();
        attachListeners();
    }

    private void attachListeners() {
        mClearIssueTitle.setOnClickListener(onClickListener);
        mIssueTitle.setOnFocusChangeListener(onFocusChangeListener);
        mSubmitButton.setOnClickListener(onClickListener);
        mAttachImageLayout.setOnClickListener(onClickListener);
        mClearImage.setOnClickListener(onClickListener);
        mFeedbackEmail.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.cross_clear_issue_title:
                    mIssueTitle.setText("");
                    break;
                case R.id.submit_button:
                    endcodeFiles();
                    break;
                case R.id.attach_image_layout:
                    openGallery();
                    break;
                case R.id.clear_image:
                    mAttachImageText.setText(getResources().getString(R.string.attach_image));
                    mScreenshot = null;
                    mClearImage.setVisibility(View.GONE);
                    break;
                case R.id.feedback_email:
                    oepnFeedbackEmail();
            }
        }
    };

    private void oepnFeedbackEmail() {
        Calendar calendar = Calendar.getInstance();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Constants.TO, " feedback@mentalsnapp.com");
        intent.putExtra(Intent.EXTRA_SUBJECT, new SimpleDateFormat("dd/mm/yyyy").format(calendar.getTime()) + getResources().getString(R.string.feedback_subject));
        intent.setType("message/rfc822");
        startActivity(Intent.createChooser(intent,
                "Send Email Using: "));
    }

    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            switch (v.getId()) {
                case R.id.issue_title:
                    if (hasFocus) {
                        mClearIssueTitle.setVisibility(View.VISIBLE);
                    } else {
                        mClearIssueTitle.setVisibility(View.GONE);
                    }
            }
        }
    };

    private boolean checkPermissionWrite() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionWrite() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,}
                , PERMISSION_REQUEST_CODE_WRITE);
    }

    private boolean checkPermissionRead() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionRead() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,}
                , PERMISSION_REQUEST_CODE_READ);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_READ:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                }
                break;
            case PERMISSION_REQUEST_CODE_WRITE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    reportToSupport();
                break;
        }
    }

    private void openGallery() {
        if (checkPermissionRead()) {
            Intent i = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            final int ACTIVITY_SELECT_IMAGE = 1234;
            startActivityForResult(i, ACTIVITY_SELECT_IMAGE);
        } else {
            requestPermissionRead();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1234:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    String realPath;
                    if (selectedImage != null) {
                        realPath = RealPathUtil.getPath(this, data.getData());
                        mScreenshot = new File(realPath);
                        if (mScreenshot != null) {
                            mClearImage.setVisibility(View.VISIBLE);
                            mAttachImageText.setText(getResources().getString(R.string.change_screenshot));
                        } else {
                            mClearImage.setVisibility(View.GONE);
                        }
                    }
                }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    TextWatcher commonTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            /*if (mIssueTitle.getText().toString().length() > 0) {
                mClearIssueTitle.setVisibility(View.VISIBLE);
            } else {
                mClearIssueTitle.setVisibility(View.GONE);
            }*/
        }
    };

    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                MediaType.parse(MULTIPART_FORM_DATA), descriptionString);
    }

    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, String contentType, File file) {
        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
//        File file = FileUtils.getFile(this, fileUri);

        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(MediaType.parse(contentType), file);

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    private void endcodeFiles() {
        if (EmailValidator.hasText(mIssueTitle.getText().toString())) {
            if (EmailValidator.hasText(mIssueDescription.getText().toString())) {
                mProgresBar.setVisibility(View.VISIBLE);
                if (checkPermissionWrite()) {
                    reportToSupport();
                } else {
                    requestPermissionWrite();
                }
            } else {
                Toast.makeText(ReportAnIssueActivity.this, getResources().getString(R.string.enter_description), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ReportAnIssueActivity.this, getResources().getString(R.string.enter_title), Toast.LENGTH_SHORT).show();
        }
    }

    Callback mResponseCallback = new Callback() {
        @Override
        public void onResponse(Call call, Response response) {
            LogHelper.logInfo(ReportAnIssueActivity.this, "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()));
            mProgresBar.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                LogHelper.logInfo(ReportAnIssueActivity.this, "mResponseCallback", JsonToStringConverter.convertToJson(response.body()));
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.sent_successfully), Toast.LENGTH_LONG).show();
                onBackPressed();
            } else {
                try {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(getApplicationContext(), String.valueOf(jObjError.get("message")), Toast.LENGTH_LONG).show();
                    LogHelper.logError(ReportAnIssueActivity.this, "mResponseCallback", JsonToStringConverter.convertToJson(response.body()));
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
            LogHelper.logInfo(getBaseContext(), "mResponseCallback ", response.body().toString());
        }

        @Override
        public void onFailure(Call call, Throwable t) {
            LogHelper.logInfo(ReportAnIssueActivity.this, "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()));
            mProgresBar.setVisibility(View.GONE);
            try {
                if (t.getMessage().contains(Constants.NO_INTERNET_MESSAGE)) {
                    Toast.makeText(ReportAnIssueActivity.this, String.valueOf(getResources().getString(R.string.no_internet)), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ReportAnIssueActivity.this, String.valueOf(t.getMessage()), Toast.LENGTH_LONG).show();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            LogHelper.logError(ReportAnIssueActivity.this, "error", t.toString());
        }
    };

    private void getLogFile() {
        new AsyncTask<String, Void, File>() {
            @Override
            protected File doInBackground(String... params) {
                mLogFile = SLog.fetchLogs(ReportAnIssueActivity.this);
                return null;
            }

            @Override
            protected void onPostExecute(final File result) {
                mIsFileCreated = true;
            }
        }.execute();
    }

    private void initialiseVariables() {
        setSupportActionBar(mToolbar);
        setHeader(getResources().getString(R.string.report_an_issue));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void registerViews() {
        mIssueTitle = (EditText) findViewById(R.id.issue_title);
        mIssueDescription = (EditText) findViewById(R.id.description_edittext);
        mAttachImageLayout = (RelativeLayout) findViewById(R.id.attach_image_layout);
        mSubmitButton = (ImageView) findViewById(R.id.submit_button);
        mClearIssueTitle = (ImageView) findViewById(R.id.cross_clear_issue_title);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mProgresBar = (RelativeLayout) findViewById(R.id.progress_bar_layout);
        mAttachImageText = (TextView) findViewById(R.id.attach_image_text);
        mClearImage = (ImageView) findViewById(R.id.clear_image);
        mFeedbackEmail = (TextView) findViewById(R.id.feedback_email);
    }

    private void reportToSupport() {
        if (mIsFileCreated) {
            report();
        } else {
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (mIsFileCreated) {
                        cancel();
                        report();
                    }
                }
            }, 2000, 2000);
        }
    }

    private void report() {
        RequestBody requestTitle =
                RequestBody.create(
                        MediaType.parse("text/plain"), mIssueTitle.getText().toString());

        RequestBody requestDescription =
                RequestBody.create(
                        MediaType.parse("text/plain"), mIssueDescription.getText().toString());

        RequestBody logFileRequest =
                RequestBody.create(MediaType.parse("text/plain"), mLogFile);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part logFile = MultipartBody.Part.createFormData("log_file", mLogFile.getName().toLowerCase().contains(".zip") ? mLogFile.getName().replace(".zip", "") : mLogFile.getName(), logFileRequest);

        MultipartBody.Part screenshot = null;
        if (mScreenshot != null) {
            RequestBody screenshotRequest =
                    RequestBody.create(MediaType.parse("image/*"), mScreenshot);
            // MultipartBody.Part is used to send also the actual file name
            screenshot = MultipartBody.Part.createFormData("screenshot", mScreenshot.getName(), screenshotRequest);
        }

        Call<LoginResponse> reportAnIssueCall = ApiClient.getApiInterface().reportAnIssue(requestTitle,
                requestDescription, logFile, screenshot);
        reportAnIssueCall.enqueue(mResponseCallback);
    }
}
