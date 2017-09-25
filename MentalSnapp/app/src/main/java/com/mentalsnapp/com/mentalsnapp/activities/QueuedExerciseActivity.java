package com.mentalsnapp.com.mentalsnapp.activities;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.adapters.ScheduleRecyclerAdapter;
import com.mentalsnapp.com.mentalsnapp.network.ApiClient;
import com.mentalsnapp.com.mentalsnapp.network.response.ScheduleExerciseResponse;
import com.mentalsnapp.com.mentalsnapp.network.response.ScheduleListResponse;
import com.mentalsnapp.com.mentalsnapp.utils.AlarmReceiver;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;
import com.mentalsnapp.com.mentalsnapp.utils.JsonToStringConverter;
import com.mentalsnapp.com.mentalsnapp.utils.LogHelper;

import java.util.ArrayList;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by gchandra on 11/1/17.
 */
public class QueuedExerciseActivity extends BaseActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ArrayList<ScheduleExerciseResponse> mScheduleList;
    private ScheduleRecyclerAdapter mAdapter;
    private RelativeLayout mProgressBarLayout;
    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;
    //    private ItemTouchHelper touchHelper;
    private Context mContext;
    private long deletedId;
    private int mSelectedExercise = 0;
//    private Paint p = new Paint();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queued_exercise);
        registerViews();
        initialiseVariables();
        getScheduleList();
    }

    private void getScheduleList() {
        mProgressBarLayout.setVisibility(View.VISIBLE);
        Call<ScheduleListResponse> getScheduleList = ApiClient.getApiInterface().getScheduleList();
        getScheduleList.enqueue(mResponseCallback);
    }

    Callback mResponseCallback = new Callback() {
        @Override
        public void onResponse(Call call, Response response) {
            LogHelper.logInfo(mContext, "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()));
            if (response.isSuccessful()) {
                LogHelper.logInfo(mContext, "mResponseCallback", JsonToStringConverter.convertToJson(response.body()));
                if (call.request().method().equalsIgnoreCase(Constants.GET)) {
                    ArrayList<ScheduleExerciseResponse> list = ((ScheduleListResponse) response.body()).scheduleList;
                    mScheduleList.addAll(list);
                    setupRecyclerView();
                }
                if (call.request().method().equalsIgnoreCase(Constants.PATCH)) {
                    ScheduleExerciseResponse exerciseResponse = (ScheduleExerciseResponse) response.body();
                    updateRecyclerView(exerciseResponse);
                    scheduleAlarm(exerciseResponse);
                }
                if (call.request().method().equalsIgnoreCase(Constants.DELETE)) {
                    Toast.makeText(mContext, getResources().getString(R.string.schedule_delete_message), Toast.LENGTH_LONG).show();
                    new updateRecycler().execute();
                    mAdapter.notifyDataSetChanged();
                }
            }
            mProgressBarLayout.setVisibility(View.GONE);
        }

        @Override
        public void onFailure(Call call, Throwable t) {
            mProgressBarLayout.setVisibility(View.GONE);
            LogHelper.logInfo(mContext, "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()));
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

    private class updateRecycler extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i < mScheduleList.size(); i++) {
                if (Long.parseLong(mScheduleList.get(i).id) == deletedId) {
                    mScheduleList.remove(i);
                    cancelAlarm();
                }
            }
            return null;
        }
    }

    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) deletedId, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    public void deleteExercise(int adapterPosition) {
        deletedId = Long.parseLong(mScheduleList.get(adapterPosition).id);
        Call<ScheduleExerciseResponse> deleteExercise = ApiClient.getApiInterface().deleteScheduled(mScheduleList.get(adapterPosition).id);
        deleteExercise.enqueue(mResponseCallback);
    }

    private void updateRecyclerView(ScheduleExerciseResponse exerciseResponse) {
        ScheduleExerciseResponse item = mScheduleList.get(mSelectedExercise);
        item.executeAt = exerciseResponse.executeAt;
        mAdapter.notifyDataSetChanged();
    }

    /*ItemTouchHelper.SimpleCallback touchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            if (direction == ItemTouchHelper.LEFT) {
                int position = viewHolder.getAdapterPosition();
                mScheduleList.remove(position);
                mAdapter.notifyItemRemoved(position);
            }
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            Bitmap icon;
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                View itemView = viewHolder.itemView;
                float height = (float) itemView.getBottom() - (float) itemView.getTop();
                float width = height / 3;

                if (dX < 0) {
                    p.setColor(Color.parseColor("#D32F2F"));
                    RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                    c.drawRect(background, p);
                    c.clipRect(background);
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.delete_btn);
                    RectF icon_dest = new RectF((float) itemView.getRight() - 2 * width, (float) itemView.getTop() + width, (float) itemView.getRight() - width, (float) itemView.getBottom() - width);
                    c.drawBitmap(icon, null, icon_dest, p);
                    c.restore();
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }
    };*/

    private void scheduleAlarm(ScheduleExerciseResponse exerciseResponse) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(this, AlarmReceiver.class);
        myIntent.putExtra(Constants.SCHEDULABLE_TYPE, exerciseResponse.schedulableType);
        myIntent.putExtra(Constants.SCHEDULABLE_ID, exerciseResponse.schedulableId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, Integer.parseInt(exerciseResponse.id), myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, (Long.parseLong(exerciseResponse.executeAt) * 1000) - 300000, pendingIntent);
    }

    private void setupRecyclerView() {
        mAdapter = new ScheduleRecyclerAdapter(mScheduleList, this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        touchHelper = new ItemTouchHelper(touchCallback);
//        touchHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initialiseVariables() {
        setSupportActionBar(mToolbar);
        setHeader(getResources().getString(R.string.queued_exercises));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mContext = this;
        mScheduleList = new ArrayList<>();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void registerViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.schedule_recycler_view);
        mProgressBarLayout = (RelativeLayout) findViewById(R.id.progress_bar_layout);
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
        mProgressBarLayout.setVisibility(View.VISIBLE);
        Call<ScheduleExerciseResponse> scheduleExercise = ApiClient.getApiInterface().reSchedule(Long.parseLong(mScheduleList.get(mSelectedExercise).id),
                String.valueOf(mScheduleList.get(mSelectedExercise).schedulableId), Constants.QUESTION,
                String.valueOf(calendarTemp.getTimeInMillis() / 1000));
        scheduleExercise.enqueue(mResponseCallback);
    }

    private void openTimePicker() {
        mTimePickerDialog = new TimePickerDialog(mContext, R.style.AlertDialogTheme, timeSetListener, myCalendarPresent.get(Calendar.HOUR_OF_DAY),
                myCalendarPresent.get(Calendar.MINUTE), false);
        mTimePickerDialog.setCanceledOnTouchOutside(true);
        mTimePickerDialog.setCancelable(true);
        mTimePickerDialog.show();
    }

    public void openDatePicker() {
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

    public void setSelectedExercise(int mSelectedExercise) {
        this.mSelectedExercise = mSelectedExercise;
    }
}
