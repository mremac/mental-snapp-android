package com.mentalsnapp.com.mentalsnapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.adapters.ViewPagerAdapter;
import com.mentalsnapp.com.mentalsnapp.fragments.RecordOnboardFragment;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;

/**
 * Created by gchandra on 23/2/17.
 */
public class RecordOnboardingActivity extends BaseActivity {
    private TextView mSkipButton;
    private ViewPager mViewPager;
    private Context mContext;
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
        for (int item = 0; item < 6; item++) {
            RecordOnboardFragment recordOnboardFragment = new RecordOnboardFragment();
            Bundle bundle = new Bundle();
            if (getIntent().getStringExtra(Constants.TUTORIAL) != null) {
                bundle.putString(Constants.TUTORIAL, Constants.TUTORIAL);
            }
            bundle.putInt(Constants.RECORD_ONBOARD_SCREEN, item);
            recordOnboardFragment.setArguments(bundle);
            mViewPagerAdapter.addFragment(recordOnboardFragment);
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
                if (mViewPager.getCurrentItem() == 5) {
                    mSkipButton.setText(getString(R.string.finish));
                } else {
                    mSkipButton.setText(getString(R.string.skip));
                }
                switch (position) {
                    case 0:
                        mOnboardProgress.setImageResource(R.drawable.record_progress_1);
                        break;
                    case 1:
                        mOnboardProgress.setImageResource(R.drawable.record_progress_2);
                        break;
                    case 2:
                        mOnboardProgress.setImageResource(R.drawable.record_progress_3);
                        break;
                    case 3:
                        mOnboardProgress.setImageResource(R.drawable.record_progress_4);
                        break;
                    case 4:
                        mOnboardProgress.setImageResource(R.drawable.record_progress_5);
                        break;
                    case 5:
                        mOnboardProgress.setImageResource(R.drawable.record_progress_6);
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
                    openHomePage();
            }
        });
    }

    private void openHomePage() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Constants.TO_FREE_FORM,Constants.TO_FREE_FORM);
        startActivity(intent);
        finish();
    }

    private void initialiseVariables() {
        mOnboardProgress.setImageResource(R.drawable.record_progress_1);
        mContext = this;
    }

    private void registerViews() {
        mSkipButton = (TextView) findViewById(R.id.skip_button);
        mViewPager = (ViewPager) findViewById(R.id.onboarding_viewpager);
        mOnboardProgress = (ImageView) findViewById(R.id.onboarding_progress);
    }
}
