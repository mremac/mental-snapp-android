package com.mentalsnapp.com.mentalsnapp.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.activities.MainActivity;
import com.mentalsnapp.com.mentalsnapp.adapters.GuidedExerciseRecyclerAdapter;
import com.mentalsnapp.com.mentalsnapp.models.GuidedExercise;
import com.mentalsnapp.com.mentalsnapp.network.ApiClient;
import com.mentalsnapp.com.mentalsnapp.network.response.GuidedExerciseListResponse;
import com.mentalsnapp.com.mentalsnapp.network.response.ScheduleExerciseResponse;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;
import com.mentalsnapp.com.mentalsnapp.utils.JsonToStringConverter;
import com.mentalsnapp.com.mentalsnapp.utils.LogHelper;
import com.mentalsnapp.com.mentalsnapp.utils.SharedPref;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by gchandra on 27/12/16.
 */
public class GuidedExerciseListFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private GuidedExerciseRecyclerAdapter mGuidedExcerciseRecyclerAdapter;
    private ArrayList<GuidedExercise> mListItems;
    private RelativeLayout mNoDataLayout;
    private ImageView mCalendarButton;
    private ImageView mRecordButton;
    private TextView mGuidedExerciseDescription;
    private long mCategoryID;
    private String mCategoryName;
    private int mPerPage = 50;
    private int mCurrentPage = 1;
    private int mVisibleThreshold = 10;
    private boolean mLoading;
    private RelativeLayout mProgressBar;
    private GuidedExerciseListFragment mContext;

    public GuidedExerciseListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.guided_excercise_list_type_layout, container, false);
        registerViews(view);
        initialiseVariables();
        setUpRecyclerView();
        attachListeners();
        attachInfiniteScrollListeners();
        getListDetails();
        return view;
    }

    private void attachListeners() {
        mCalendarButton.setOnClickListener(onClickListener);
        mRecordButton.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.calendar_btn:
                    openDatePicker();
                    break;
                case R.id.record_btn:
                    ((MainActivity) getActivity()).setCategoryID(String.valueOf(mCategoryID));
                    if (((MainActivity) getActivity()).checkPermissionWrite()) {
                        ((MainActivity) getActivity()).openCamera(Constants.VIDEO_CAPTURE_EXERCISE);
                    } else {
                        ((MainActivity) getActivity()).requestPermissionWrite(Constants.PERMISSION_REQUEST_EXERCISE_CAMERA);
                    }
                    break;
            }
        }
    };

    Calendar myCalendarPresent = Calendar.getInstance();
    Calendar calendarTemp = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            calendarTemp.set(Calendar.YEAR, year);
            calendarTemp.set(Calendar.MONTH, monthOfYear);
            calendarTemp.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            if (view.isShown()) {
                openTimePicker();
                mDatePickerDialog.dismiss();
            }
        }
    };

    TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            calendarTemp.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendarTemp.set(Calendar.MINUTE, minute);
            if (view.isShown()) {
                if (calendarTemp.get(Calendar.YEAR) == myCalendarPresent.get(Calendar.YEAR) &&
                        calendarTemp.get(Calendar.MONTH) == myCalendarPresent.get(Calendar.MONTH) &&
                        calendarTemp.get(Calendar.DAY_OF_MONTH) == myCalendarPresent.get(Calendar.DAY_OF_MONTH)) {
                    if (calendarTemp.get(Calendar.HOUR_OF_DAY) == myCalendarPresent.get(Calendar.HOUR_OF_DAY)) {
                        if (calendarTemp.get(Calendar.MINUTE) < myCalendarPresent.get(Calendar.MINUTE)) {
                            Toast.makeText(getContext(), getResources().getString(R.string.schedule_time_error), Toast.LENGTH_LONG).show();
                            mTimePickerDialog.dismiss();
                            openTimePicker();
                        } else {
                            scheduleExercise();
                        }
                    } else if (calendarTemp.get(Calendar.HOUR_OF_DAY) < myCalendarPresent.get(Calendar.HOUR_OF_DAY)) {
                        mTimePickerDialog.dismiss();
                        openTimePicker();
                    } else {
                        scheduleExercise();
                    }
                } else if (calendarTemp.get(Calendar.YEAR) >= myCalendarPresent.get(Calendar.YEAR) ||
                        calendarTemp.get(Calendar.MONTH) >= myCalendarPresent.get(Calendar.MONTH) ||
                        calendarTemp.get(Calendar.DAY_OF_MONTH) >= myCalendarPresent.get(Calendar.DAY_OF_MONTH)) {
                    scheduleExercise();
                }
                mTimePickerDialog.dismiss();
            }
        }
    };

    private void scheduleExercise() {
        Call<ScheduleExerciseResponse> scheduleExercise = ApiClient.getApiInterface().scheduleExercise(String.valueOf(SharedPref.getID(getContext())),
                String.valueOf(mCategoryID), Constants.QUESTION, String.valueOf(calendarTemp.getTimeInMillis() / 1000));
        scheduleExercise.enqueue(mResponseCallback);
    }

    TimePickerDialog mTimePickerDialog;
    DatePickerDialog mDatePickerDialog;

    private void openTimePicker() {
        mTimePickerDialog = new TimePickerDialog(getContext(), R.style.AlertDialogTheme, timeSetListener, myCalendarPresent.get(Calendar.HOUR_OF_DAY),
                myCalendarPresent.get(Calendar.MINUTE), false);
        mTimePickerDialog.setCanceledOnTouchOutside(true);
        mTimePickerDialog.setCancelable(true);
        mTimePickerDialog.show();
    }

    private void openDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        mDatePickerDialog = new DatePickerDialog(getContext(), R.style.AlertDialogTheme, date, cal
                .get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        mDatePickerDialog.getDatePicker().setMinDate(cal.getTimeInMillis());
        mDatePickerDialog.setCanceledOnTouchOutside(true);
        mDatePickerDialog.setCancelable(true);
        mDatePickerDialog.show();
    }

    private void initialiseVariables() {
        mContext = this;
        mCategoryID = getArguments().getLong(Constants.CATEGORY_ID);
        mCategoryName = getArguments().getString(Constants.CATEGORY_NAME);
        mGuidedExerciseDescription.setText(getArguments().getString(Constants.GUIDED_EXERCISE_DESCRIPTION));
    }

    private void getListDetails() {
        mProgressBar.setVisibility(View.VISIBLE);
        Call<GuidedExerciseListResponse> getListItems = ApiClient.getApiInterface().getItems(mCategoryID, mCurrentPage, mPerPage);
        getListItems.enqueue(mResponseCallback);
        LogHelper.logInfo(getContext(), "getListItems", getListItems.toString());
    }

    Callback mResponseCallback = new Callback() {
        @Override
        public void onResponse(Call call, Response response) {
            if (mContext.isVisible()) {
                LogHelper.logInfo(getContext(), "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                        call.request().method(), call.request().body()));
                mProgressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    mLoading = false;
                    LogHelper.logInfo(getContext(), "mResponseCallback", JsonToStringConverter.convertToJson(response.body()));
                    GuidedExerciseListResponse listItemResponse = (GuidedExerciseListResponse) response.body();
                    if (call.request().url().toString().contains(Constants.GUIDED_EXERCISE_API)) {
                        if (listItemResponse.guidedExercises != null) {
                            mPerPage = listItemResponse.guidedExercises.size();
                            if (mCurrentPage == 1) {
                                if (listItemResponse.guidedExercises.size() > 0) {
                                    mListItems.addAll(listItemResponse.guidedExercises);
                                    setUpRecyclerView();
                                    mGuidedExcerciseRecyclerAdapter.notifyDataSetChanged();
                                } else {
                                    mRecyclerView.setVisibility(View.GONE);
                                    mNoDataLayout.setVisibility(View.VISIBLE);
                                }
                            } else {
                                mListItems.addAll(listItemResponse.guidedExercises);
                                mGuidedExcerciseRecyclerAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                    if (call.request().url().toString().contains(Constants.SCHEDULE_API)) {
                        ScheduleExerciseResponse exerciseResponse = (ScheduleExerciseResponse) response.body();
                        showMessage(exerciseResponse);
                        ((MainActivity) getActivity()).scheduleAlarm(exerciseResponse);
                    }
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(getContext(), String.valueOf(jObjError.get("message")), Toast.LENGTH_LONG).show();
                        LogHelper.logError(getContext(), "mResponseCallback", JsonToStringConverter.convertToJson(response.errorBody()));
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onFailure(Call call, Throwable t) {
            LogHelper.logInfo(getContext(), "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()));
            mProgressBar.setVisibility(View.GONE);
            try {
                if (t.getMessage().equals(Constants.NO_INTERNET_MESSAGE)) {
                    Toast.makeText(getContext(), String.valueOf(getResources().getString(R.string.no_internet)), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), String.valueOf(t.getMessage()), Toast.LENGTH_LONG).show();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            LogHelper.logError(getContext(), "error", t.toString());
        }
    };

    private void showMessage(ScheduleExerciseResponse exerciseResponse) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(exerciseResponse.executeAt) * 1000);
        String minutes = (calendar.get(Calendar.MINUTE) < 10) ? ("0" + String.valueOf(calendar.get(Calendar.MINUTE))) : String.valueOf(calendar.get(Calendar.MINUTE));
        if (calendar.get(Calendar.YEAR) == myCalendarPresent.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == myCalendarPresent.get(Calendar.MONTH) &&
                calendar.get(Calendar.DAY_OF_MONTH) == myCalendarPresent.get(Calendar.DAY_OF_MONTH)) {
            int hours;
            if (calendar.get(Calendar.HOUR_OF_DAY) > 12) {
                hours = calendar.get(Calendar.HOUR_OF_DAY) - 12;
                Toast.makeText(getContext(), "Exercise scheduled on today, " + hours + ":" + minutes + " PM", Toast.LENGTH_LONG).show();
            } else {
                hours = calendar.get(Calendar.HOUR_OF_DAY);
                Toast.makeText(getContext(), "Exercise scheduled on today, " + hours + ":" + minutes + " AM", Toast.LENGTH_LONG).show();
            }
        } else {
            int hours;
            if (calendar.get(Calendar.HOUR_OF_DAY) > 12) {
                hours = calendar.get(Calendar.HOUR_OF_DAY) - 12;
                Toast.makeText(getContext(), "Exercise scheduled on " + calendar.get(Calendar.DAY_OF_MONTH) + " " +
                        new SimpleDateFormat("MMM").format(calendar.get(Calendar.MONTH)) + ", " +
                        hours + ":" + minutes + " PM", Toast.LENGTH_LONG).show();
            } else {
                hours = calendar.get(Calendar.HOUR_OF_DAY);
                Toast.makeText(getContext(), "Exercise scheduled on " + calendar.get(Calendar.DAY_OF_MONTH) + " " +
                        new SimpleDateFormat("MMM").format(calendar.get(Calendar.MONTH)) + ", " +
                        hours + ":" + minutes + " AM", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setUpRecyclerView() {
        mGuidedExcerciseRecyclerAdapter = new GuidedExerciseRecyclerAdapter(mListItems, getActivity(), mCategoryName);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mGuidedExcerciseRecyclerAdapter);
    }

    private void registerViews(View view) {
        mListItems = new ArrayList<>();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.guided_excercise_recycler);
        mNoDataLayout = (RelativeLayout) view.findViewById(R.id.no_data_layout);
        mProgressBar = (RelativeLayout) view.findViewById(R.id.progress_bar_layout);
        mGuidedExerciseDescription = (TextView) view.findViewById(R.id.exercise_description);
        mCalendarButton = (ImageView) view.findViewById(R.id.calendar_btn);
        mRecordButton = (ImageView) view.findViewById(R.id.record_btn);
    }

    private void attachInfiniteScrollListeners() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                        .getLayoutManager();

                int totalItemCount = linearLayoutManager.getItemCount();
                int lastVisibleItem = linearLayoutManager
                        .findLastVisibleItemPosition();
                if (mPerPage > 0 && !mLoading
                        && totalItemCount <= (lastVisibleItem + mVisibleThreshold)) {
                    // End has been reached
                    // Do something
                    onLoadMore();
                    mLoading = true;
                }
            }
        });
    }

    private void onLoadMore() {
        mCurrentPage++;
        getListDetails();
    }


}
