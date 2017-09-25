package com.mentalsnapp.com.mentalsnapp.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;


/**
 * Created by ssaxena on 20/5/16.
 */
public class MoreActivity extends BaseActivity {

    private Toolbar mToolbar;

    private RelativeLayout mQueuedExerciseLayout;
    private RelativeLayout mProfileLayout;
    private RelativeLayout mReportIssueLayout;
    private RelativeLayout mTnCLayout;
    private RelativeLayout mPrivacyPolicyLayout;
    private RelativeLayout mOnboardingLayout;
    private RelativeLayout mRecordOnboardLayout;

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, MoreActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_activity);
        initViews();
        setHeader(getResources().getString(R.string.more));
        attachListeners();
    }

    private void attachListeners() {
        mProfileLayout.setOnClickListener(mOnClickListener);
        mReportIssueLayout.setOnClickListener(mOnClickListener);
        mTnCLayout.setOnClickListener(mOnClickListener);
        mPrivacyPolicyLayout.setOnClickListener(mOnClickListener);
        mQueuedExerciseLayout.setOnClickListener(mOnClickListener);
        mOnboardingLayout.setOnClickListener(mOnClickListener);
        mRecordOnboardLayout.setOnClickListener(mOnClickListener);
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.profile_layout:
                    openProfileScreen();
                    break;

                case R.id.exercise_layout:
                    openQueuedExerciseScreen();
                    break;

                case R.id.report_an_issue_layout:
                    if (checkPermissionWrite()) {
                        openReportAnIssueScreen();
                    } else {
                        requestPermissionWrite(Constants.PERMISSION_REQUEST_WRITE);
                    }
                    break;
                case R.id.tnc_layout:
                    openTncScreen();
                    break;
                case R.id.privacy_policy_layout:
                    openPrivacyPolicyScreen();
                    break;
                case R.id.onboarding_layout:
                    openOnboardingScreens();
                    break;
                case R.id.record_onboard_layout:
                    openRecordOnboardScreens();
                    break;
            }
        }
    };

    private void openRecordOnboardScreens() {
        Intent intent = new Intent(this, RecordOnboardingActivity.class);
        intent.putExtra(Constants.TUTORIAL, Constants.TUTORIAL);
        startActivity(intent);
    }

    private void openOnboardingScreens() {
        Intent intent = new Intent(this, OnboardingActivity.class);
        intent.putExtra(Constants.TUTORIAL, Constants.TUTORIAL);
        startActivity(intent);
    }

    public boolean checkPermissionWrite() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermissionWrite(int permissionRequestCode) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                , permissionRequestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_REQUEST_WRITE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openReportAnIssueScreen();
                }
                break;
        }
    }

    private void openQueuedExerciseScreen() {
        Intent intent = new Intent(this, QueuedExerciseActivity.class);
        startActivity(intent);
    }

    private void openPrivacyPolicyScreen() {
        Intent intent = new Intent(this, PrivacyPolicyActivity.class);
        startActivity(intent);
    }

    private void openTncScreen() {
        Intent intent = new Intent(this, TnCActivity.class);
        startActivity(intent);
    }

    private void openReportAnIssueScreen() {
        Intent intent = new Intent(this, ReportAnIssueActivity.class);
        startActivity(intent);
    }

    private void openProfileScreen() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mQueuedExerciseLayout = (RelativeLayout) findViewById(R.id.exercise_layout);
        mProfileLayout = (RelativeLayout) findViewById(R.id.profile_layout);
        mReportIssueLayout = (RelativeLayout) findViewById(R.id.report_an_issue_layout);
        mTnCLayout = (RelativeLayout) findViewById(R.id.tnc_layout);
        mPrivacyPolicyLayout = (RelativeLayout) findViewById(R.id.privacy_policy_layout);
        mOnboardingLayout = (RelativeLayout) findViewById(R.id.onboarding_layout);
        mRecordOnboardLayout = (RelativeLayout) findViewById(R.id.record_onboard_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
