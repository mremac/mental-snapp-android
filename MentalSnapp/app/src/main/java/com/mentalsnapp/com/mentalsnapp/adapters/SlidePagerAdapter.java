package com.mentalsnapp.com.mentalsnapp.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.mentalsnapp.com.mentalsnapp.fragments.Fragment1;
import com.mentalsnapp.com.mentalsnapp.fragments.FragmentFreeform;
import com.mentalsnapp.com.mentalsnapp.fragments.GuidedExerciseListFragment;
import com.mentalsnapp.com.mentalsnapp.models.GuidedExercise;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;

import java.util.ArrayList;

/**
 * Created by ssaxena on 21/12/16.
 */
public class SlidePagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<GuidedExercise> mGuidedExercise;

    // Build a Constructor and assign the passed Values to appropriate values in the class
    public SlidePagerAdapter(FragmentManager fragmentManager, ArrayList<GuidedExercise> guidedExercises) {
        super(fragmentManager);
        mGuidedExercise = guidedExercises;
        mGuidedExercise.add(0, new GuidedExercise());
        mGuidedExercise.add(1, new GuidedExercise());
        mGuidedExercise.add(guidedExercises.size(), new GuidedExercise());

    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
        if (position == 0 || position == mGuidedExercise.size() - 1) {
            return new Fragment1();
        }
        if (position == 1) {
            return new FragmentFreeform();
        }
        GuidedExerciseListFragment tab1 = new GuidedExerciseListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.GUIDED_EXERCISE_DESCRIPTION, mGuidedExercise.get(position).description);
        bundle.putLong(Constants.CATEGORY_ID, mGuidedExercise.get(position).id);
        bundle.putString(Constants.CATEGORY_NAME, mGuidedExercise.get(position).name);
        tab1.setArguments(bundle);
        return tab1;

    }

    // This method return the titles for the Tabs in the Tab Strip
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0 || position == mGuidedExercise.size() - 1) {
            return "";
        }
        if (position == 1) {
            return "Freeform";
        }
        return mGuidedExercise.get(position).name;
    }

    // This method return the images for the Tabs in the Tab Strip
    public String getPageImage(int position) {
        if (position == 0 || position == mGuidedExercise.size() - 1) {
            return "";
        }
        if (position == 1) {
            return "";
        }
        return mGuidedExercise.get(position).coverUrl;
    }

    // This method return the Number of tabs for the tabs Strip
    @Override
    public int getCount() {
        return mGuidedExercise.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }
}