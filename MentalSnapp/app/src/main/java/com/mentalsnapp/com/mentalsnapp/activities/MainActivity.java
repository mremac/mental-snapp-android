package com.mentalsnapp.com.mentalsnapp.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.adapters.ViewPagerAdapter;
import com.mentalsnapp.com.mentalsnapp.fragments.Fragment1;
import com.mentalsnapp.com.mentalsnapp.fragments.GuidedExerciseFragment;
import com.mentalsnapp.com.mentalsnapp.fragments.RecordFragment;
import com.mentalsnapp.com.mentalsnapp.fragments.StatsFragment;
import com.mentalsnapp.com.mentalsnapp.fragments.VideosFragment;
import com.mentalsnapp.com.mentalsnapp.network.response.ScheduleExerciseResponse;
import com.mentalsnapp.com.mentalsnapp.utils.AlarmReceiver;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;
import com.mentalsnapp.com.mentalsnapp.utils.SharedPref;

import java.lang.reflect.Field;
import java.util.Calendar;


/**
 * Created by ssaxena on 16/12/16.
 */
public class MainActivity extends BaseActivity {

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private ViewPagerAdapter mAdapter;
    private NumberPicker mMonthPicker;
    private NumberPicker mYearPicker;
    private TextView mDoneButton;
    private TextView mCancelButton;
    private PopupWindow mCalendarPopup;
    private int mYear = 0;
    private int mMonth = 0;
    public Uri VIDEO_URI;
    private String mCategoryID;
    private MenuItem mCalendar;

    private int[] tabIcons = {
            R.drawable.exercise_icon_selector,
            R.drawable.video_icon_selector,
            R.drawable.record_icon_selector,
            R.drawable.stats_icon_selector
    };
    private String[] tabTitles = {
            "Exercise",
            "Videos",
            "Record",
            "Insights"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setSupportActionBar(mToolbar);
        setupViewPager(mViewPager);
        setupTabLayoutWithViewPager();
        setupTabViews();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mViewPager.addOnPageChangeListener(onPageChangeListener);

        if (getIntent().getIntExtra(Constants.SCHEDULE_RECORDING, 0) != 0) {
            if (checkPermissionWrite()) {
                openCamera(Constants.VIDEO_CAPTURE);
            } else {
                requestPermissionWrite(Constants.PERMISSION_REQUEST_CODE_CAMERA);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getStringExtra(Constants.SET_MOOD) != null) {
            selectTab(1);
        } else if (intent.getStringExtra(Constants.TO_FREE_FORM) != null) {
            selectTab(0);
            ((GuidedExerciseFragment) mAdapter.getItem(0)).selectPosition(0);
        }
    }

    private void setupTabViews() {
        for (int i = 0; i < 4; i++) {
            View tabView = View.inflate(this, R.layout.tab_custom_view, null);
            TextView tabTitle = (TextView) tabView.findViewById(R.id.tab_title);
            ImageView tabImage = (ImageView) tabView.findViewById(R.id.tab_image);
            tabTitle.setText(tabTitles[i]);
            tabImage.setImageResource(tabIcons[i]);
            mTabLayout.getTabAt(i).setCustomView(tabView);
        }
    }

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (position != 2) {
                VIDEO_URI = null;
            }
            if (mCalendar != null) {
                if (position == 3) {
                    mCalendar.setVisible(true);
                } else {
                    mCalendar.setVisible(false);
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_calendar);
        MenuItem more = menu.findItem(R.id.action_more);
        mCalendar = item;
        more.setVisible(true);
        item.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_more:
                MoreActivity.start(this);
                return true;
            case R.id.action_calendar:
                openDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void openDialog() {
        Calendar c = Calendar.getInstance();
        LayoutInflater inflater = LayoutInflater.from(this);
        View calendarLayout = inflater.inflate(R.layout.calendar_picker_layout, null);
        mMonthPicker = (NumberPicker) calendarLayout.findViewById(R.id.month_picker);
        mYearPicker = (NumberPicker) calendarLayout.findViewById(R.id.year_picker);
        mDoneButton = (TextView) calendarLayout.findViewById(R.id.done_btn);
        mCancelButton = (TextView) calendarLayout.findViewById(R.id.cancel);
        setDividerColor(mMonthPicker, ContextCompat.getColor(this, R.color.light_gray));
        setDividerColor(mYearPicker, ContextCompat.getColor(this, R.color.light_gray));
        setNumberPickerTextColor(mMonthPicker, ContextCompat.getColor(this, R.color.color_gray));
        setNumberPickerTextColor(mYearPicker, ContextCompat.getColor(this, R.color.color_gray));
        initNumberPicker(mYearPicker, 1970, c.get(Calendar.YEAR));
        initNumberPickerMonth(mMonthPicker, 1, 12);
        if (mYear == 0) {
            mYearPicker.setValue(c.get(Calendar.YEAR));
        } else {
            mYearPicker.setValue(mYear);
        }
        if (mMonth == 0) {
            mMonthPicker.setValue(c.get(Calendar.MONTH) + 1);
        } else {
            mMonthPicker.setValue(mMonth);
        }
        mCalendarPopup = new PopupWindow(calendarLayout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        mCalendarPopup.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, android.R.color.transparent)));
        mCalendarPopup.showAsDropDown(mToolbar);
        mCalendarPopup.setOutsideTouchable(true);
        mCalendarPopup.setFocusable(false);
        mCalendarPopup.setTouchable(true);
        mCalendarPopup.update();
        mDoneButton.setOnClickListener(calendarOnClickListener);
        mCancelButton.setOnClickListener(calendarOnClickListener);
    }

    View.OnClickListener calendarOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.done_btn:
                    ((StatsFragment) mAdapter.getItem(3)).getStats(mMonthPicker.getValue(), mYearPicker.getValue());
                    mMonth = mMonthPicker.getValue();
                    mYear = mYearPicker.getValue();
                    mCalendarPopup.dismiss();
                    break;
                case R.id.cancel:
                    mCalendarPopup.dismiss();
            }
        }
    };

    /**
     * To set Number Picker with minimum and maximum limit
     *
     * @param mPicker  Number Picker
     * @param maxLimit The maximum limit
     */
    private void initNumberPicker(NumberPicker mPicker, int minLimit, int maxLimit) {
        mPicker.setMinValue(minLimit);
        mPicker.setMaxValue(maxLimit);
        mPicker.setWrapSelectorWheel(false);
    }

    private void initNumberPickerMonth(NumberPicker numberPicker, int minLimit, int maxLimit) {
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October",
                "November", "December"};
        numberPicker.setMinValue(minLimit);
        numberPicker.setMaxValue(maxLimit);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setDisplayedValues(months);
    }

    /**
     * To set color for dividers of Number Picker
     *
     * @param picker Number Picker for which the color has to be set
     * @param color  Color which has to be set
     */
    private void setDividerColor(NumberPicker picker, int color) {
        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException | Resources.NotFoundException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public static boolean setNumberPickerTextColor(NumberPicker numberPicker, int color) {
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                try {
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint) selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText) child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
                    Log.w("setPickerTextColor", e);
                }
            }
        }
        return false;
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        mViewPager = (ViewPager) findViewById(R.id.viewpager_main);
        mTabLayout = (TabLayout) findViewById(R.id.tabs_main);
    }

    private void setupTabLayoutWithViewPager() {
        mViewPager.setOffscreenPageLimit(3);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        mAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mAdapter.addFragment(new GuidedExerciseFragment());
        mAdapter.addFragment(new VideosFragment());
        mAdapter.addFragment(new RecordFragment());
        mAdapter.addFragment(new StatsFragment());
        viewPager.setAdapter(mAdapter);
    }

    public void selectTab(int position) {
        mViewPager.setCurrentItem(position);
    }

    public void openDetailsScreen(String categoryName, long id) {
        Intent intent = new Intent(this, SelectedExerciseDetailsActivity.class);
        intent.putExtra(Constants.CATEGORY_NAME, categoryName);
        intent.putExtra(Constants.SUB_CATEGORY_ID, id);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                if (data != null && data.getData() != null) {
                    VIDEO_URI = data.getData();
                    openSetMoodScreen(false);
                } else {
                    selectTab(0);
                }
            } else {
                selectTab(0);
                getIntent().removeExtra(Constants.CATEGORY_NAME);
                getIntent().removeExtra(Constants.SUB_CATEGORY_ID);
            }
        }
        if (requestCode == Constants.VIDEO_CAPTURE_EXERCISE) {
            if (data != null && data.getData() != null) {
                VIDEO_URI = data.getData();
                openSetMoodScreen(true);
            }
        }
    }

    private void openSetMoodScreen(boolean isGuidedExercise) {
        Intent intent = new Intent(this, SetMoodActivity.class);
        if (getIntent().getIntExtra(Constants.SCHEDULE_RECORDING, 0) != 0) {
            intent.putExtra(Constants.CATEGORY_NAME, getIntent().getStringExtra(Constants.CATEGORY_NAME));
            intent.putExtra(Constants.SUB_CATEGORY_ID, getIntent().getLongExtra(Constants.SUB_CATEGORY_ID, 0));
        }
        if (isGuidedExercise) {
            intent.putExtra(Constants.CATEGORY_NAME, Constants.GUIDED_EXERCISE);
            intent.putExtra(Constants.SUB_CATEGORY_ID, mCategoryID);
        }
        intent.putExtra(Constants.VIDEO_URI_NAME, VIDEO_URI.toString());
        startActivity(intent);
    }

    public boolean checkPermissionWrite() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkPermissionRead() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermissionWrite(int permissionRequestCode) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}
                , permissionRequestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_REQUEST_CODE_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera(Constants.VIDEO_CAPTURE);
                }
                break;
            case Constants.PERMISSION_REQUEST_CODE_DOWNLOAD:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Fragment fr = mAdapter.getItem(1);
                    if (fr instanceof VideosFragment) {
                        ((VideosFragment) fr).downloadVideo();
                    }
                }
                break;
            case Constants.PERMISSION_REQUEST_EXERCISE_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera(Constants.VIDEO_CAPTURE_EXERCISE);
                }
                break;
        }
    }

    public void openCamera(int requestCode) {
        if (!SharedPref.isAlertShow(this)) {
            showAlert(requestCode);
            SharedPref.setAlertShow(true, this);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 120);
//            intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
//            intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
//            intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
            startActivityForResult(intent, requestCode);
        }
    }

    private void showAlert(final int requestCode) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        alert.setMessage(getResources().getString(R.string.alert_message_camera));
        alert.setNeutralButton(getResources().getText(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 120);
                startActivityForResult(intent, requestCode);
            }
        });
        alert.show();
    }

    public void scheduleAlarm(ScheduleExerciseResponse exerciseResponse) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(this, AlarmReceiver.class);
        myIntent.putExtra(Constants.SCHEDULE_DESCRIPTION, exerciseResponse.exercise.description);
        myIntent.putExtra(Constants.CATEGORY_NAME, Constants.GUIDED_EXERCISE);
        myIntent.putExtra(Constants.SUB_CATEGORY_ID, exerciseResponse.schedulableId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, Integer.parseInt(exerciseResponse.id), myIntent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, Long.parseLong(exerciseResponse.executeAt) * 1000, pendingIntent);
    }

    public void setCategoryID(String mCategoryID) {
        this.mCategoryID = mCategoryID;
    }
}
