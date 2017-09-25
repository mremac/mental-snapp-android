package com.mentalsnapp.com.mentalsnapp.fragments;

import android.graphics.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.activities.MainActivity;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;

import java.io.IOException;

/**
 * Created by gchandra on 11/1/17.
 */
public class RecordFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_screen, container, false);
        return view;
    }

    private void checkForUri() throws IOException {
        if (((MainActivity) getActivity()).VIDEO_URI == null) {
            if (((MainActivity) getActivity()).checkPermissionWrite()) {
                ((MainActivity) getActivity()).openCamera(Constants.VIDEO_CAPTURE);
            } else {
                ((MainActivity) getActivity()).requestPermissionWrite(Constants.PERMISSION_REQUEST_CODE_CAMERA);
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            try {
                checkForUri();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
