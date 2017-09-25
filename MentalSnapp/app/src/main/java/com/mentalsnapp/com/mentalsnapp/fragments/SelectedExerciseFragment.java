package com.mentalsnapp.com.mentalsnapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.activities.SelectedExerciseDetailsActivity;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;
import com.mentalsnapp.com.mentalsnapp.utils.GlideUtils;

/**
 * Created by gchandra on 6/1/17.
 */
public class SelectedExerciseFragment extends Fragment {

    private TextView mSelectedExerciseDescription;
    private ImageView mSelectedExerciseImage;
    private TextView mSelectedExerciseName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seleted_exercise_question, container, false);
        registerViews(view);
        initialiseVariables();
        attachListeners();
        return view;
    }

    private void attachListeners() {
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            }
        }
    };

    private void initialiseVariables() {
        mSelectedExerciseName.setText(getArguments().getString(Constants.SUB_CATEGORY_NAME));
        mSelectedExerciseDescription.setText(getArguments().getString(Constants.SUB_CATEGORY_DETAILS));
        GlideUtils.loadImage(SelectedExerciseFragment.this, getArguments().getString(Constants.SUB_CATEGORY_IMAGE_URL),
                mSelectedExerciseImage, R.drawable.sub_placeholder);
    }

    private void registerViews(View view) {
        mSelectedExerciseDescription = (TextView) view.findViewById(R.id.selected_text_description);
        mSelectedExerciseImage = (ImageView) view.findViewById(R.id.selected_sub_image);
        mSelectedExerciseName = (TextView) view.findViewById(R.id.selected_sub_name);

    }
}
