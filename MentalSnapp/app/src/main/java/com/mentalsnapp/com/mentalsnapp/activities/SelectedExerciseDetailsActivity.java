package com.mentalsnapp.com.mentalsnapp.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.adapters.ViewPagerAdapter;
import com.mentalsnapp.com.mentalsnapp.fragments.SelectedExerciseFragment;
import com.mentalsnapp.com.mentalsnapp.models.GuidedExercise;
import com.mentalsnapp.com.mentalsnapp.network.ApiClient;
import com.mentalsnapp.com.mentalsnapp.network.response.ScheduleExerciseResponse;
import com.mentalsnapp.com.mentalsnapp.network.response.SubCategoriesQuestionsResponse;
import com.mentalsnapp.com.mentalsnapp.utils.AlarmReceiver;
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
 * Created by gchandra on 28/12/16.
 */
public class SelectedExerciseDetailsActivity extends BaseActivity {

    private Toolbar mToolbar;
    private Context mContext;
    private ViewPager mQuestionsViewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private ImageView mNextButton;
    private ImageView mPreviousButton;
    private int mPerPage = 50;
    private ImageView mCalendarButton;
    private ImageView mRecordButton;
    private ArrayList<GuidedExercise> mQuestionList;
    private int mCurrentPage = 1;
    private RelativeLayout mProgressBar;
    private long mCurrentQuestionId;
    private static final int PERMISSION_REQUEST_CODE_CAMERA = 200;
    final int VIDEO_CAPTURE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_exercise_details);
        registerViews();
        getQuestionDetails();
        initialiseVariables();
        attachListeners();
    }

    private void attachListeners() {
        mNextButton.setOnClickListener(onClickListener);
        mPreviousButton.setOnClickListener(onClickListener);
        mQuestionsViewPager.addOnPageChangeListener(pageChangeListener);
        mCalendarButton.setOnClickListener(onClickListener);
        mRecordButton.setOnClickListener(onClickListener);
    }

    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (position == 0) {
                mPreviousButton.setImageResource(R.drawable.previous_icon_unselected);
            } else {
                mPreviousButton.setImageResource(R.drawable.previous_tap_effect);
            }
            if (position == mViewPagerAdapter.getCount() - 1) {
                mNextButton.setImageResource(R.drawable.next_icon_unselected);
            } else {
                mNextButton.setImageResource(R.drawable.next_tap_effect);
            }
            mCurrentQuestionId = mQuestionList.get(position).id;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.next_btn:
                    jumpToNextPage();
                    break;
                case R.id.previous_btn:
                    jumpToPreviousPage();
                    break;
                case R.id.calendar_btn:
                    openDatePicker();
                    break;
                case R.id.record_btn:
                    if (checkPermissionCamera()) {
                        openCamera();
                    } else {
                        requestPermissionCamera();
                    }
            }
        }
    };

    private void openCamera() {
        if (!SharedPref.isAlertShow(this)) {
            showAlert();
            SharedPref.setAlertShow(true, this);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            startActivityForResult(intent, VIDEO_CAPTURE);
        }
    }

    private void showAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        alert.setMessage(getResources().getString(R.string.alert_message_camera));
        alert.setNeutralButton(getResources().getText(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 120);
                startActivityForResult(intent, VIDEO_CAPTURE);
            }
        });
        alert.show();
    }

    private boolean checkPermissionCamera() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionCamera() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                , PERMISSION_REQUEST_CODE_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == VIDEO_CAPTURE && resultCode == RESULT_OK) {
            if (intent.getData() != null) {
                Uri videoUri = intent.getData();
                openSetMoodScreen(videoUri);
            }
        }
    }

    private void openSetMoodScreen(Uri videoUri) {
        Intent intent = new Intent(this, SetMoodActivity.class);
        intent.putExtra(Constants.VIDEO_URI_NAME, videoUri.toString());
        intent.putExtra(Constants.CATEGORY_NAME, Constants.QUESTION);
        intent.putExtra(Constants.SUB_CATEGORY_ID, mCurrentQuestionId);
        startActivity(intent);
        finish();
    }

    Calendar myCalendarPresent;
    Calendar calendarTemp;
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
                            Toast.makeText(mContext, getResources().getString(R.string.schedule_time_error), Toast.LENGTH_LONG).show();
                            mTimePickerDialog.dismiss();
                            openTimePicker();
                        } else {
                            scheduleExercise();
                        }
                    } else if (calendarTemp.get(Calendar.HOUR_OF_DAY) < myCalendarPresent.get(Calendar.HOUR_OF_DAY)) {
                        Toast.makeText(mContext, getResources().getString(R.string.schedule_time_error), Toast.LENGTH_LONG).show();
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
        Call<ScheduleExerciseResponse> scheduleExercise = ApiClient.getApiInterface().scheduleExercise(String.valueOf(SharedPref.getID(mContext)),
                String.valueOf(mQuestionList.get(mQuestionsViewPager.getCurrentItem()).id), Constants.QUESTION,
                String.valueOf(calendarTemp.getTimeInMillis() / 1000));
        scheduleExercise.enqueue(mResponseCallback);
    }

    TimePickerDialog mTimePickerDialog;
    DatePickerDialog mDatePickerDialog;

    private void openTimePicker() {
        mTimePickerDialog = new TimePickerDialog(mContext, R.style.AlertDialogTheme, timeSetListener, myCalendarPresent.get(Calendar.HOUR_OF_DAY),
                myCalendarPresent.get(Calendar.MINUTE), false);
        mTimePickerDialog.setCanceledOnTouchOutside(true);
        mTimePickerDialog.setCancelable(true);
        mTimePickerDialog.show();
    }

    private void openDatePicker() {
        myCalendarPresent = Calendar.getInstance();
        myCalendarPresent.set(Calendar.MINUTE, myCalendarPresent.get(Calendar.MINUTE) + 10);
        calendarTemp = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        mDatePickerDialog = new DatePickerDialog(mContext, R.style.AlertDialogTheme, date, cal
                .get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        mDatePickerDialog.getDatePicker().setMinDate(cal.getTimeInMillis());
        mDatePickerDialog.setCanceledOnTouchOutside(true);
        mDatePickerDialog.setCancelable(true);
        mDatePickerDialog.show();
    }

    private void getQuestionDetails() {
        mProgressBar.setVisibility(View.VISIBLE);
        Call<SubCategoriesQuestionsResponse> getQuestionsCall = ApiClient.getApiInterface().getQuestions(getIntent().getLongExtra(Constants.SUB_CATEGORY_ID, 0),
                mCurrentPage, mPerPage);
        getQuestionsCall.enqueue(mResponseCallback);
    }

    Callback mResponseCallback = new Callback() {
        @Override
        public void onResponse(Call call, Response response) {
            LogHelper.logInfo(mContext, "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()));
            mProgressBar.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                LogHelper.logInfo(mContext, "mResponseCallback", JsonToStringConverter.convertToJson(response.body()));
                if (call.request().url().toString().contains(Constants.GET_QUESTIONS_API)) {
                    SubCategoriesQuestionsResponse questionsList = (SubCategoriesQuestionsResponse) response.body();
                    if (questionsList.guidedExercises.size() == 1) {
                        mNextButton.setImageResource(R.drawable.next_icon_unselected);
                        mPreviousButton.setImageResource(R.drawable.previous_icon_unselected);
                    } else {
                        mNextButton.setImageResource(R.drawable.next_tap_effect);
                        mPreviousButton.setImageResource(R.drawable.previous_icon_unselected);
                    }
                    setupViewPager(questionsList);
                }
                if (call.request().url().toString().contains(Constants.SCHEDULE_API)) {
                    ScheduleExerciseResponse exerciseResponse = (ScheduleExerciseResponse) response.body();
                    showMessage(exerciseResponse);
                    scheduleAlarm(exerciseResponse);
                }
            } else {
                try {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(getApplicationContext(), String.valueOf(jObjError.get("message")), Toast.LENGTH_LONG).show();
                    LogHelper.logError(mContext, "mResponseCallback", JsonToStringConverter.convertToJson(response.body()));
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure(Call call, Throwable t) {
            LogHelper.logInfo(mContext, "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()));
            mProgressBar.setVisibility(View.GONE);
            try {
                if (t.getMessage().contains(Constants.NO_INTERNET_MESSAGE)) {
                    Toast.makeText(mContext, String.valueOf(getResources().getString(R.string.no_internet)), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, String.valueOf(t.getMessage()), Toast.LENGTH_LONG).show();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            LogHelper.logError(mContext, "error", t.toString());
        }
    };

    private void scheduleAlarm(ScheduleExerciseResponse exerciseResponse) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(this, AlarmReceiver.class);
        myIntent.putExtra(Constants.SCHEDULE_DESCRIPTION, exerciseResponse.exercise.description);
        myIntent.putExtra(Constants.CATEGORY_NAME, Constants.QUESTION);
        myIntent.putExtra(Constants.SUB_CATEGORY_ID, exerciseResponse.schedulableId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, Integer.parseInt(exerciseResponse.id), myIntent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, (Long.parseLong(exerciseResponse.executeAt) * 1000 - 300000), pendingIntent);
    }

    private void showMessage(ScheduleExerciseResponse exerciseResponse) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(exerciseResponse.executeAt) * 1000);
        String minutes = (calendar.get(Calendar.MINUTE) < 10) ? ("0" + String.valueOf(calendar.get(Calendar.MINUTE))) : String.valueOf(calendar.get(Calendar.MINUTE));
        if (calendar.get(Calendar.YEAR) == myCalendarPresent.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == myCalendarPresent.get(Calendar.MONTH) &&
                calendar.get(Calendar.DAY_OF_MONTH) == myCalendarPresent.get(Calendar.DAY_OF_MONTH)) {
            int hours;
            if (calendar.get(Calendar.HOUR_OF_DAY) >= 12) {
                if (calendar.get(Calendar.HOUR_OF_DAY) != 12) {
                    hours = calendar.get(Calendar.HOUR_OF_DAY) - 12;
                } else {
                    hours = calendar.get(Calendar.HOUR_OF_DAY);
                }
                Toast.makeText(mContext, "Exercise scheduled on today, " + hours + ":" + minutes + " PM", Toast.LENGTH_LONG).show();
            } else {
                hours = calendar.get(Calendar.HOUR_OF_DAY);
                Toast.makeText(mContext, "Exercise scheduled on today, " + hours + ":" + minutes + " AM", Toast.LENGTH_LONG).show();
            }
        } else {
            int hours;
            if (calendar.get(Calendar.HOUR_OF_DAY) >= 12) {
                if (calendar.get(Calendar.HOUR_OF_DAY) != 12) {
                    hours = calendar.get(Calendar.HOUR_OF_DAY) - 12;
                } else {
                    hours = calendar.get(Calendar.HOUR_OF_DAY);
                }
                Toast.makeText(mContext, "Exercise scheduled on " + calendar.get(Calendar.DAY_OF_MONTH) + " " +
                        new SimpleDateFormat("MMM").format(calendar.getTime()) + ", " +
                        hours + ":" + minutes + " PM", Toast.LENGTH_LONG).show();
            } else {
                hours = calendar.get(Calendar.HOUR_OF_DAY);
                Toast.makeText(mContext, "Exercise scheduled on " + calendar.get(Calendar.DAY_OF_MONTH) + " " +
                        new SimpleDateFormat("MMM").format(calendar.getTime()) + ", " +
                        hours + ":" + minutes + " AM", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void openSchedulePopup() {
        Dialog window = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.schedule_popup_layout, null);
        window.setContentView(view);
        window.show();
    }

    public void jumpToNextPage() {
        int currentPosition = mQuestionsViewPager.getCurrentItem();
        mQuestionsViewPager.setCurrentItem(currentPosition + 1);
    }

    public void jumpToPreviousPage() {
        int currentPosition = mQuestionsViewPager.getCurrentItem();
        mQuestionsViewPager.setCurrentItem(currentPosition - 1);
    }

    private void setupViewPager(SubCategoriesQuestionsResponse questionsListRespone) {
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        if (questionsListRespone != null && questionsListRespone.guidedExercises.size() > 0) {
            mQuestionList = questionsListRespone.guidedExercises;
            mCurrentQuestionId = mQuestionList.get(0).id;
            for (int item = 0; item < mQuestionList.size(); item++) {
                SelectedExerciseFragment selectedExerciseFragment = new SelectedExerciseFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.SUB_CATEGORY_NAME, mQuestionList.get(item).name);
                bundle.putString(Constants.SUB_CATEGORY_DETAILS, mQuestionList.get(item).description);
                bundle.putString(Constants.SUB_CATEGORY_IMAGE_URL, mQuestionList.get(item).coverUrl);
                selectedExerciseFragment.setArguments(bundle);
                mViewPagerAdapter.addFragment(selectedExerciseFragment);
                mQuestionsViewPager.setAdapter(mViewPagerAdapter);
            }
        }
    }

    private void initialiseVariables() {
        setSupportActionBar(mToolbar);
        setHeader(getIntent().getStringExtra(Constants.CATEGORY_NAME));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mContext = this;
        mQuestionList = new ArrayList<>();
    }

    private void registerViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mQuestionsViewPager = (ViewPager) findViewById(R.id.exercise_viewpager);
        mNextButton = (ImageView) findViewById(R.id.next_btn);
        mPreviousButton = (ImageView) findViewById(R.id.previous_btn);
        mProgressBar = (RelativeLayout) findViewById(R.id.progress_bar_selected_exercise);
        mCalendarButton = (ImageView) findViewById(R.id.calendar_btn);
        mRecordButton = (ImageView) findViewById(R.id.record_btn);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
