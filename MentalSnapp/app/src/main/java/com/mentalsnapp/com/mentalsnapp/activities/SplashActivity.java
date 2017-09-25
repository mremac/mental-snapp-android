package com.mentalsnapp.com.mentalsnapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.crittercism.app.Crittercism;
import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;
import com.mentalsnapp.com.mentalsnapp.utils.SharedPref;

/**
 * Created by gchandra on 19/12/16.
 */
public class SplashActivity extends BaseActivity {
    private Handler mHandler;
    private Context mContext;
    private int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initialiseVariables();
        Crittercism.initialize(getApplicationContext(), Constants.PRODUCTION_KEY);
        startHandler();
    }

    private void startHandler() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!SharedPref.isLogin(mContext)) {
                    launchLoginScreen();
                } else {
                    launchHomeScreen();
                }
            }
        }, SPLASH_TIME_OUT);
    }

    private void launchRecordOnboardScreen() {
        Intent intent = new Intent(this, RecordOnboardingActivity.class);
        startActivity(intent);
        finish();
    }

    private void launchOnBoardingScreen() {
        Intent intent = new Intent(this, OnboardingActivity.class);
        startActivity(intent);
        finish();
    }

    private void launchHomeScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        if (getIntent().getIntExtra(Constants.SCHEDULE_RECORDING, 0) != 0) {
            intent.putExtra(Constants.SCHEDULE_RECORDING, getIntent().getIntExtra(Constants.SCHEDULE_RECORDING, 0));
            intent.putExtra(Constants.CATEGORY_NAME, getIntent().getStringExtra(Constants.CATEGORY_NAME));
            intent.putExtra(Constants.SUB_CATEGORY_ID, getIntent().getLongExtra(Constants.SUB_CATEGORY_ID, 0));
        }
        startActivity(intent);
        finish();
    }

    private void launchLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void initialiseVariables() {
        mHandler = new Handler();
        mContext = this;
    }

}
