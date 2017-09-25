package com.mentalsnapp.com.mentalsnapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.activities.MainActivity;
import com.mentalsnapp.com.mentalsnapp.adapters.SlidePagerAdapter;
import com.mentalsnapp.com.mentalsnapp.models.GuidedExercise;
import com.mentalsnapp.com.mentalsnapp.network.ApiClient;
import com.mentalsnapp.com.mentalsnapp.network.response.GuidedExerciseResponse;
import com.mentalsnapp.com.mentalsnapp.utils.CommonUtilities;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;
import com.mentalsnapp.com.mentalsnapp.utils.JsonToStringConverter;
import com.mentalsnapp.com.mentalsnapp.utils.LogHelper;
import com.mentalsnapp.com.mentalsnapp.views.SlidingTabLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ssaxena on 16/12/16.
 */
public class GuidedExerciseFragment extends Fragment implements ViewPager.OnPageChangeListener {

    private ViewPager mViewPager;
    private SlidingTabLayout mTabLayout;
    private ProgressBar mProgressBar;

    private static final int PER_PAGE = 50;
    private int mPreviousSelection = 1;
    private int mPage = 1;
    private int mPerPage = PER_PAGE;

    private ArrayList<GuidedExercise> mGuidedExercise;
    private SlidePagerAdapter mAdapter;
    private GuidedExerciseFragment mContext;

    public GuidedExerciseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_guided_exercise, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager_main);
        mTabLayout = (SlidingTabLayout) view.findViewById(R.id.tabs_main);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mGuidedExercise = new ArrayList<>();
        mContext = this;
        mProgressBar.setVisibility(View.VISIBLE);
        getGuidedExercises(mPage, mPerPage);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        Log.i("Scroll", "position: " + position + "positionOffset: " + positionOffset + "positionOffsetPixels: " + positionOffsetPixels);
    }

    @Override
    public void onPageSelected(int position) {
        Log.i("PageSelected", "position: " + position);
        if (position == 0) {
            position = 1;
        } else if (position == mGuidedExercise.size() - 1) {
            position = mGuidedExercise.size() - 2;
            selectParentTab(1);
        }

        selectTab(position);
        if (getTabViewAt(mPreviousSelection) != null && position != mPreviousSelection) {
            deSelectTab(mPreviousSelection);
        }
        mPreviousSelection = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        Log.i("ScrollState", "state: " + state);

    }

    private void getGuidedExercises(int page, int perPage) {
        Call<GuidedExerciseResponse> guidedExerciseCall = ApiClient.getApiInterface().getGuidedExercise(page, perPage);
        guidedExerciseCall.enqueue(mResponseCallback);
        LogHelper.logInfo(getContext(), "guidedExerciseCall", guidedExerciseCall.request().url().toString());
    }

    private Callback mResponseCallback = new Callback() {
        @Override
        public void onResponse(Call call, Response response) {
            if (mContext.isVisible()) {
                LogHelper.logInfo(getContext(), "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                        call.request().method(), call.request().body()));
                if (response.isSuccessful()) {
                    LogHelper.logInfo(getContext(), "mResponseCallback", JsonToStringConverter.convertToJson(response.body()));
                    if (response.body() != null && response.body() instanceof GuidedExerciseResponse) {
                        GuidedExerciseResponse exerciseResponse = (GuidedExerciseResponse) response.body();
                        if (exerciseResponse.guidedExercises != null && exerciseResponse.guidedExercises.size() > 0) {
                            if (mPage == 1) {
                                mGuidedExercise.clear();
                            }
                            mGuidedExercise.addAll(exerciseResponse.guidedExercises);
                        }
                        setupViewPager();
                        selectTab(mPreviousSelection);
                        mPage = exerciseResponse.currentPage;
                        mProgressBar.setVisibility(View.GONE);
                    }
//                mProgressBar.setVisibility(View.GONE);
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(getContext(), String.valueOf(jObjError.get("message")), Toast.LENGTH_LONG).show();
                        LogHelper.logError(getContext(), "mResponseCallback ", JsonToStringConverter.convertToJson(response.errorBody()));
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onFailure(Call call, Throwable t) {
            LogHelper.logInfo(getContext(), "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()));
            try {
                if (t.getMessage().equals(Constants.NO_INTERNET_MESSAGE)) {
                    Toast.makeText(getContext(), String.valueOf(getResources().getString(R.string.no_internet)), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), String.valueOf(t.getMessage()), Toast.LENGTH_LONG).show();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            mProgressBar.setVisibility(View.GONE);
            LogHelper.logError(getContext(), "error", t.getMessage());
        }
    };

    private void setupViewPager() {
        mAdapter = new SlidePagerAdapter(getChildFragmentManager(), mGuidedExercise);
        // Assigning ViewPager View and setting the adapter
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setCustomTabView(R.layout.sliding_tab, R.id.textView, R.id.imageView);
        mTabLayout.setViewPager(mViewPager);
        mTabLayout.setOnPageChangeListener(this);
        setDefualtCategoryIcon(1);
        setTabWidth();
    }

    private void setTabWidth() {
        int width = CommonUtilities.getDisplayWidth(getActivity()) / 3;
        for (int i = 0; i < ((LinearLayout) mTabLayout.getChildAt(0)).getChildCount(); i++) {
            RelativeLayout layout = getTabViewAt(i);
            layout.getLayoutParams().width = width;
        }
    }

    private RelativeLayout getTabViewAt(int position) {
        return (RelativeLayout) ((LinearLayout) mTabLayout.getChildAt(0)).getChildAt(position);
    }

    private void selectTab(int position) {
        try {
            mViewPager.setCurrentItem(position);
            getTabViewAt(position).animate().scaleX(1.7f).scaleY(1.7f).start();
            getTabViewAt(position).getChildAt(0).setBackgroundResource(R.drawable.orange_circle_bg);
            ((TextView) getTabViewAt(position).getChildAt(1)).setTextColor(ContextCompat.getColor(getActivity(), R.color.orange));
            ((TextView) getTabViewAt(position).getChildAt(1)).setTextSize(getResources().getDimension(R.dimen.font_xsmall) / getResources().getDisplayMetrics().density);
        } catch (NullPointerException e) {

        }
    }

    private void deSelectTab(int position) {
        getTabViewAt(position).animate().scaleX(1f).scaleY(1f).start();
        getTabViewAt(position).getChildAt(0).setBackgroundResource(R.drawable.blue_circle_bg);
        ((TextView) getTabViewAt(position).getChildAt(1)).setTextColor(ContextCompat.getColor(getActivity(), R.color.blue));
        ((TextView) getTabViewAt(position).getChildAt(1)).setTextSize(getResources().getDimension(R.dimen.font_medium) / getResources().getDisplayMetrics().density);
    }

    private void setDefualtCategoryIcon(int position) {
        ((ImageView) getTabViewAt(position).getChildAt(0)).setImageResource(R.drawable.freeform_icon);
        ((ImageView) getTabViewAt(position).getChildAt(0)).setVisibility(View.VISIBLE);
    }

    private void selectParentTab(int position) {
        ((MainActivity) getActivity()).selectTab(position);
    }

    public void selectPosition(int position) {
        mViewPager.setCurrentItem(position);
    }
}
