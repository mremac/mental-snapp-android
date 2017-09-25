package com.mentalsnapp.com.mentalsnapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.activities.BaseActivity;
import com.mentalsnapp.com.mentalsnapp.activities.MainActivity;
import com.mentalsnapp.com.mentalsnapp.utils.SharedPref;

/**
 * Created by gchandra on 9/1/17.
 */
public class FragmentFreeform extends Fragment {

    private TextView mWelcomeMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_freeform, container, false);
        registerViews(view);
        setUserDetails();
        return view;
    }

    private void setUserDetails() {
        SpannableString spannableString = new SpannableString("Welcome back\n" + SharedPref.getFirstName(getContext()) + ",\nwhat do you want to record today?");
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.orange)),
                12, 13 + SharedPref.getFirstName(getContext()).length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        mWelcomeMessage.setText(spannableString);
    }

    private void registerViews(View view) {
        mWelcomeMessage = (TextView) view.findViewById(R.id.welcome_message);
    }
}
