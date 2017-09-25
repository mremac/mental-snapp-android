package com.mentalsnapp.com.mentalsnapp.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.activities.MainActivity;
import com.mentalsnapp.com.mentalsnapp.activities.RecordOnboardingActivity;
import com.mentalsnapp.com.mentalsnapp.activities.SignupActivity;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;
import com.mentalsnapp.com.mentalsnapp.utils.SharedPref;

/**
 * Created by gchandra on 23/2/17.
 */
public class OnboardingFragment extends Fragment {

    private TextView mTitle;
    private TextView mDescription;
    private TextView mEnjoyTellingMessage;
    private ImageView mOnboardingImage;
    private ImageView mSignImage;
    private RelativeLayout mItemLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_oboarding_item_layout, container, false);
        registerViews(view);
        initialiseVariables();
        attachListeners();
        return view;
    }

    private void attachListeners() {
        mTitle.setOnClickListener(onClickListener);
        mDescription.setOnClickListener(onClickListener);
        if (getArguments().getInt(Constants.ONBOARDING_SCREEN) != 11) {
            mTitle.setClickable(false);
            mDescription.setClickable(false);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.onboarding_title:
                        openHomeScreen();
                    break;
                case R.id.onboarding_description:
                    openRecordOnboardScreen();
            }
        }
    };

    private void openHomeScreen() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra(Constants.TO_FREE_FORM, Constants.TO_FREE_FORM);
        startActivity(intent);
    }

    private void openSignupScreen() {
        Intent intent = new Intent(getActivity(), SignupActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void openRecordOnboardScreen() {
        Intent intent = new Intent(getActivity(), RecordOnboardingActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void initialiseVariables() {
        String titleArray[] = getResources().getStringArray(R.array.onboarding_titles);
        String descriptionArray[] = getResources().getStringArray(R.array.onboarding_description);
        int currentScreen = getArguments().getInt(Constants.ONBOARDING_SCREEN);
        switch (currentScreen) {
            case 0:
                mOnboardingImage.setImageResource(R.drawable.onboard_1);
                mTitle.setText(titleArray[0]);
                mDescription.setText(descriptionArray[0]);
                break;
            case 1:
                mOnboardingImage.setImageResource(R.drawable.onboard_2);
                mTitle.setText(titleArray[1]);
                mDescription.setText(descriptionArray[1]);
                break;
            case 2:
                mOnboardingImage.setImageResource(R.drawable.onboard_3);
                mTitle.setText(titleArray[2]);
                mDescription.setText(descriptionArray[2]);
                break;
            case 3:
                mOnboardingImage.setImageResource(R.drawable.onboard_4);
                mTitle.setText(titleArray[3]);
                mDescription.setText(descriptionArray[3]);
                break;
            case 4:
                mOnboardingImage.setImageResource(R.drawable.onboard_5);
                mTitle.setText(titleArray[4]);
                mDescription.setText(descriptionArray[4]);
                break;
            case 5:
                mOnboardingImage.setImageResource(R.drawable.onboard_6);
                mTitle.setText(titleArray[5]);
                mDescription.setText(descriptionArray[5]);
                break;
            case 6:
                mOnboardingImage.setImageResource(R.drawable.onboard_7);
                mTitle.setText(titleArray[6]);
                mDescription.setText(descriptionArray[6]);
                break;
            case 7:
                mOnboardingImage.setImageResource(R.drawable.onboard_8);
                mTitle.setText(titleArray[7]);
                mDescription.setText(descriptionArray[7]);
                break;
            case 8:
                mOnboardingImage.setImageResource(R.drawable.onboard_9);
                mTitle.setText(titleArray[8]);
                mDescription.setText(descriptionArray[8]);
                break;
            case 9:
                mOnboardingImage.setImageResource(R.drawable.onboard_10);
                mTitle.setText(titleArray[9]);
                mDescription.setText(descriptionArray[9]);
                break;
            case 10:
                mOnboardingImage.setImageResource(R.drawable.onboard_11);
                mTitle.setText(titleArray[10]);
                SpannableString description = new SpannableString(descriptionArray[10]);
                description.setSpan(new StyleSpan(Typeface.ITALIC), 203, 329, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                mDescription.setText(description);
                break;
            case 11:
                mOnboardingImage.setImageResource(R.drawable.onboard_11);
                mEnjoyTellingMessage.setVisibility(View.VISIBLE);
                mTitle.setText(titleArray[11]);
                mTitle.setClickable(true);
                mDescription.setClickable(true);
                mDescription.setText(descriptionArray[11]);
                mDescription.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.tap_effect));
                mDescription.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                mDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_semi_large));
                mSignImage.setVisibility(View.VISIBLE);
                mItemLayout.setBackgroundColor(Color.WHITE);
                break;
        }
    }

    private void registerViews(View view) {
        mTitle = (TextView) view.findViewById(R.id.onboarding_title);
        mDescription = (TextView) view.findViewById(R.id.onboarding_description);
        mEnjoyTellingMessage = (TextView) view.findViewById(R.id.enjoy_telling_msg);
        mOnboardingImage = (ImageView) view.findViewById(R.id.onboarding_image);
        mSignImage = (ImageView) view.findViewById(R.id.sign_image);
        mItemLayout = (RelativeLayout) view.findViewById(R.id.onboaring_item_layout);
    }
}
