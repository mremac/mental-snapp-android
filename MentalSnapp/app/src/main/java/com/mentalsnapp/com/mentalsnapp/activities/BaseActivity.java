package com.mentalsnapp.com.mentalsnapp.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.fragments.VideosFragment;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;
import com.mentalsnapp.com.mentalsnapp.utils.SharedPref;

public class BaseActivity extends AppCompatActivity {

    private static int sActivityCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

    }

    public static int getsActivityCount() {
        return sActivityCount;
    }

    @Override
    protected void onStart() {
        super.onStart();
        sActivityCount++;
    }

    @Override
    protected void onStop() {
        super.onStop();
        sActivityCount--;
    }

    public void setHeader(String title) {
        getSupportActionBar().setTitle(title);
    }

}
