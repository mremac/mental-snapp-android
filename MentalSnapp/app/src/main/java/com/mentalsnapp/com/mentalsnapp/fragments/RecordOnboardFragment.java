package com.mentalsnapp.com.mentalsnapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.activities.MainActivity;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;

/**
 * Created by gchandra on 24/2/17.
 */
public class RecordOnboardFragment extends Fragment {
    private TextView mTitle;
    private TextView mDescription;
    private ImageView mOnboardingImage;
    private ImageView mRecordButton;

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
        return view;
    }

    private void initialiseVariables() {
        mOnboardingImage.setImageResource(R.drawable.onboard_11);
        mRecordButton.setVisibility(View.VISIBLE);
        String titleArray[] = getResources().getStringArray(R.array.record_title_array);
        String descriptionArray[] = getResources().getStringArray(R.array.record_description_array);
        int currentScreen = getArguments().getInt(Constants.RECORD_ONBOARD_SCREEN);
        switch (currentScreen) {
            case 0:
                mTitle.setText(titleArray[0]);
                mDescription.setText(descriptionArray[0]);
                break;
            case 1:
                mTitle.setText(titleArray[1]);
                mDescription.setText(descriptionArray[1]);
                break;
            case 2:
                mTitle.setText(titleArray[2]);
                mDescription.setText(descriptionArray[2]);
                break;
            case 3:
                mTitle.setText(titleArray[3]);
                mDescription.setText(descriptionArray[3]);
                break;
            case 4:
                mTitle.setText(titleArray[4]);
                mDescription.setText(descriptionArray[4]);
                break;
            case 5:
                mTitle.setText(titleArray[5]);
                mDescription.setText(descriptionArray[5]);
                break;
        }
    }

    private void registerViews(View view) {
        mTitle = (TextView) view.findViewById(R.id.onboarding_title);
        mDescription = (TextView) view.findViewById(R.id.onboarding_description);
        mOnboardingImage = (ImageView) view.findViewById(R.id.onboarding_image);
        mRecordButton=(ImageView)view.findViewById(R.id.record_btn);
    }
}
