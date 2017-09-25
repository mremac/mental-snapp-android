package com.mentalsnapp.com.mentalsnapp.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.adapters.ViewPagerAdapter;
import com.mentalsnapp.com.mentalsnapp.fragments.OnboardingFragment;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;

/**
 * Created by gchandra on 23/2/17.
 */
public class OnboardingActivity extends BaseActivity {

    private TextView mSkipButton;
    private ViewPager mViewPager;
    private Context mContext;
    private RelativeLayout mOnboardingLayout;
    private ImageView mOnboardProgress;
    private ViewPagerAdapter mViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_layout);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        registerViews();
        initialiseVariables();
        setupViewPager();
        attachListeners();
    }

    private void setupViewPager() {
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        for (int item = 0; item < 12; item++) {
            OnboardingFragment onboardingFragment = new OnboardingFragment();
            Bundle bundle = new Bundle();
            if (getIntent().getStringExtra(Constants.TUTORIAL) != null) {
                bundle.putString(Constants.TUTORIAL, Constants.TUTORIAL);
            }
            bundle.putInt(Constants.ONBOARDING_SCREEN, item);
            onboardingFragment.setArguments(bundle);
            mViewPagerAdapter.addFragment(onboardingFragment);
            mViewPager.setAdapter(mViewPagerAdapter);
        }
    }

    private void attachListeners() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mViewPager.getCurrentItem() == 11) {
                    mSkipButton.setVisibility(View.GONE);
                } else {
                    mSkipButton.setVisibility(View.VISIBLE);
                }
                mOnboardingLayout.setBackgroundColor(Color.TRANSPARENT);
                switch (position) {
                    case 0:
                        mOnboardProgress.setImageResource(R.drawable.onboard_page_1);
                        break;
                    case 1:
                        mOnboardProgress.setImageResource(R.drawable.onboard_page_2);
                        break;
                    case 2:
                        mOnboardProgress.setImageResource(R.drawable.onboard_page_3);
                        break;
                    case 3:
                        mOnboardProgress.setImageResource(R.drawable.onboard_page_4);
                        break;
                    case 4:
                        mOnboardProgress.setImageResource(R.drawable.onboard_page_5);
                        break;
                    case 5:
                        mOnboardProgress.setImageResource(R.drawable.onboard_page_6);
                        break;
                    case 6:
                        mOnboardProgress.setImageResource(R.drawable.onboard_page_7);
                        break;
                    case 7:
                        mOnboardProgress.setImageResource(R.drawable.onboard_page_8);
                        break;
                    case 8:
                        mOnboardProgress.setImageResource(R.drawable.onboard_page_9);
                        break;
                    case 9:
                        mOnboardProgress.setImageResource(R.drawable.onboard_page_10);
                        break;
                    case 10:
                        mOnboardProgress.setImageResource(R.drawable.onboard_page_011);
                        break;
                    case 11:
                        mOnboardProgress.setImageResource(R.drawable.onboard_page_012);
                        mOnboardingLayout.setBackgroundColor(Color.WHITE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    openRecordOnBoardingScreen();
            }
        });
    }

    private void openRecordOnBoardingScreen() {
        Intent intent = new Intent(this, RecordOnboardingActivity.class);
        startActivity(intent);
        finish();
    }

    private void initialiseVariables() {
        mOnboardProgress.setImageResource(R.drawable.onboard_page_1);
        mContext = this;
    }

    private void registerViews() {
        mSkipButton = (TextView) findViewById(R.id.skip_button);
        mViewPager = (ViewPager) findViewById(R.id.onboarding_viewpager);
        mOnboardProgress = (ImageView) findViewById(R.id.onboarding_progress);
        mOnboardingLayout = (RelativeLayout) findViewById(R.id.onboarding_layout);
    }
}
