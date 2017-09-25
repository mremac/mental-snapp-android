package com.mentalsnapp.com.mentalsnapp.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;
import com.mentalsnapp.com.mentalsnapp.utils.SharedPref;

/**
 * Created by gchandra on 7/2/17.
 */
public class AlertDialogActivity extends AppCompatActivity {

    private ImageButton mContinueButton;
    private ImageButton mCancelButton;
    private Dialog mSchedulePopup;
    private TextView mScheduleMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transparent_layout);
        mSchedulePopup = new Dialog(this);
        mSchedulePopup.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(this).inflate(R.layout.schedule_popup_layout, null);
        registerViews(view);
        mSchedulePopup.setContentView(view);
        mScheduleMessage.setText(getResources().getString(R.string.schedule_message) + getIntent().getStringExtra(Constants.SCHEDULE_DESCRIPTION));
        mSchedulePopup.show();
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSchedulePopup.dismiss();
                finish();
            }
        });
        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissionCamera()) {
                    openCamera();
                    mSchedulePopup.dismiss();
                } else {
                    requestPermissionCamera();
                }
            }
        });
    }

    private boolean checkPermissionCamera() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionCamera() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                , Constants.PERMISSION_REQUEST_CODE_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_REQUEST_CODE_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                    mSchedulePopup.dismiss();
                }
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    finish();
                }
                break;
        }
    }

    private void openCamera() {
        if (!SharedPref.isAlertShow(this)) {
            showAlert();
            SharedPref.setAlertShow(true, this);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            startActivityForResult(intent, Constants.VIDEO_CAPTURE);
        }
    }

    private void showAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        alert.setMessage(getResources().getString(R.string.alert_message_camera));
        alert.setNeutralButton(getResources().getText(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 120);
                startActivityForResult(intent, Constants.VIDEO_CAPTURE);
            }
        });
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == Constants.VIDEO_CAPTURE && resultCode == RESULT_OK) {
            if (intent.getData() != null) {
                Uri videoUri = intent.getData();
                openSetMoodScreen(videoUri);
            }
        }
    }

    private void openSetMoodScreen(Uri videoUri) {
        Intent intent = new Intent(this, SetMoodActivity.class);
        intent.putExtra(Constants.VIDEO_URI_NAME, videoUri.toString());
        intent.putExtra(Constants.CATEGORY_NAME, getIntent().getStringExtra(Constants.CATEGORY_NAME));
        intent.putExtra(Constants.SUB_CATEGORY_ID, getIntent().getLongExtra(Constants.SUB_CATEGORY_ID, 0));
        startActivity(intent);
        finish();
    }

    private void registerViews(View view) {
        mContinueButton = (ImageButton) view.findViewById(R.id.continue_btn);
        mCancelButton = (ImageButton) view.findViewById(R.id.cancel);
        mScheduleMessage = (TextView) view.findViewById(R.id.schedule_message);
    }
}
