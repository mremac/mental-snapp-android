package com.mentalsnapp.com.mentalsnapp.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.network.ApiClient;
import com.mentalsnapp.com.mentalsnapp.network.response.SetMoodResponse;
import com.mentalsnapp.com.mentalsnapp.utils.AWSHelper;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;
import com.mentalsnapp.com.mentalsnapp.utils.JsonToStringConverter;
import com.mentalsnapp.com.mentalsnapp.utils.LogHelper;
import com.mentalsnapp.com.mentalsnapp.utils.SharedPref;
import com.mentalsnapp.com.mentalsnapp.views.CircularSeekBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by gchandra on 3/1/17.
 */
public class SetMoodActivity extends BaseActivity {
    private CircularSeekBar mCircularSeekBar;
    private TextView mMoodTitle;
    private Toolbar mToolbar;
    private Context mContext;
    private Uri mActualVideoUri;
    private String mDefaultFileName;
    private String mFeelingName;
    private String mMoodName;
    private TextView mAddFeelingButton;
    private View mSelectedFeelingColor;
    private EditText mMoodDescription;
    private EditText mAddVideoName;
    private RelativeLayout mProgressBarLayout;
    private ImageView mUploadButton;
    private File mCoverUrlFile;
    private File mVideoFile;
    private File newFileName;
    private static String mMoodValue = "0";
    private String mSelectedFeelingId;
    private String finalVideoName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_mood);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        registerViews();
        initialiseVariables();
        attachListeners();
    }

    private void readyVideoFile() {
        if (mActualVideoUri != null) {
            String pathFromUri = getPathFromURI(mActualVideoUri);
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(pathFromUri, MediaStore.Video.Thumbnails.MICRO_KIND);
            if (mCoverUrlFile == null) {
                try {
                    mCoverUrlFile = getThumbnailFile(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            finalVideoName = mAddVideoName.getText().toString().replace(" ", "_") + ".mp4";
            newFileName = new File(android.os.Environment.getExternalStorageDirectory(), finalVideoName);
            if (!newFileName.exists()) {
                try {
                    newFileName.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (mVideoFile.renameTo(newFileName)) {
                uploadVideo();
            }
            /*if(mCoverUrlFile!=null){
                AWSHelper.uploadFile(mContext,mCoverUrlFile,Constants.VIDEO_THUMBNAIL_UPLOAD_PATH,mTransferListenerVideo);
            }*/
        }
    }

    TransferListener mTransferListenerVideo = new TransferListener() {
        @Override
        public void onStateChanged(int id, TransferState state) {
            switch (state) {
                case COMPLETED:
                    uploadVideoThumbnail();
                    break;
                case CANCELED:
                    mProgressBarLayout.setVisibility(View.GONE);
                    break;
            }
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

        }

        @Override
        public void onError(int id, Exception ex) {
            Toast.makeText(mContext, ex.toString(), Toast.LENGTH_LONG).show();
            mProgressBarLayout.setVisibility(View.GONE);
        }
    };

    private void uploadVideoThumbnail() {
        if (mCoverUrlFile != null) {
            AWSHelper.uploadFile(mContext, mCoverUrlFile, Constants.VIDEO_THUMBNAIL_UPLOAD_PATH, mTransferListenerVideoThumbnail);
        }
    }

    TransferListener mTransferListenerVideoThumbnail = new TransferListener() {
        @Override
        public void onStateChanged(int id, TransferState state) {
            switch (state) {
                case COMPLETED:
                    saveVideoDetails();
                    break;
            }
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

        }

        @Override
        public void onError(int id, Exception ex) {
            Toast.makeText(mContext, ex.toString(), Toast.LENGTH_LONG).show();
            mProgressBarLayout.setVisibility(View.GONE);
        }
    };

    private void saveVideoDetails() {
        String categoryName = null;
        String subCategoryId = null;
        if (getIntent().getStringExtra(Constants.CATEGORY_NAME) != null) {
            categoryName = getIntent().getStringExtra(Constants.CATEGORY_NAME);
        }
        if (getIntent().getLongExtra(Constants.SUB_CATEGORY_ID, 0) != 0) {
            subCategoryId = String.valueOf(getIntent().getLongExtra(Constants.SUB_CATEGORY_ID, 0));
        }
        Call<SetMoodResponse> uploadVideoDetails = ApiClient.getApiInterface().uploadVideo(mAddVideoName.getText().toString(),
                categoryName, subCategoryId, getUploadedVideoThumbnailPath(), mMoodDescription.getText().toString(),
                getUploadedVideoPath(), String.valueOf(SharedPref.getID(mContext)), mMoodValue, mSelectedFeelingId);
        uploadVideoDetails.enqueue(mResponseCallback);
    }

    Callback mResponseCallback = new Callback() {
        @Override
        public void onResponse(Call call, Response response) {
            LogHelper.logInfo(mContext, "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()));
            if (response.isSuccessful()) {
                LogHelper.logInfo(mContext, "mResponseCallback", JsonToStringConverter.convertToJson(response.body()));
                mProgressBarLayout.setVisibility(View.GONE);
                Toast.makeText(mContext, "Uploaded successfully", Toast.LENGTH_LONG).show();
                sendDetailsForAnalytics();
                openHomeScreen();
            } else {
                try {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(getApplicationContext(), String.valueOf(jObjError.get("message")), Toast.LENGTH_LONG).show();
                    LogHelper.logError(mContext, "mResponseCallback", JsonToStringConverter.convertToJson(response.errorBody()));
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
                mProgressBarLayout.setVisibility(View.GONE);
            }
        }

        @Override
        public void onFailure(Call call, Throwable t) {
            LogHelper.logInfo(mContext, "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()));
            mProgressBarLayout.setVisibility(View.GONE);
            try {
                if (t.getMessage().contains(Constants.NO_INTERNET_MESSAGE)) {
                    Toast.makeText(getApplicationContext(), String.valueOf(getResources().getString(R.string.no_internet)), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), String.valueOf(t.getMessage()), Toast.LENGTH_LONG).show();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            LogHelper.logError(mContext, "error", t.getMessage());
        }
    };

    private void sendDetailsForAnalytics() {
        Calendar calendar = Calendar.getInstance();
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        Tracker mTracker = analytics.newTracker(R.xml.global_tracker);
        mTracker.setScreenName("Record Video");
        mTracker.send(new HitBuilders.EventBuilder().setCategory(SharedPref.getID(mContext) + "_" + SharedPref.getName(mContext)).
                setAction(finalVideoName).setLabel("Time of Recording: " + getCurrentDateTime()).build());
        mTracker = analytics.newTracker(R.xml.global_tracker);
        mTracker.setScreenName("Record Video");
        mTracker.send(new HitBuilders.EventBuilder().setCategory(SharedPref.getID(mContext) + "_" + SharedPref.getName(mContext)).
                setAction(finalVideoName).setLabel("Day of weak of Recording: " + calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())).build());
        mTracker = analytics.newTracker(R.xml.global_tracker);
        mTracker.setScreenName("Record Video");

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mContext, Uri.fromFile(newFileName));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time);

        mTracker.send(new HitBuilders.EventBuilder().setCategory(SharedPref.getID(mContext) + "_" + SharedPref.getName(mContext)).
                setAction(finalVideoName).setLabel("Length of Recording: " + String.valueOf(timeInMillisec / 1000) + " Sec").build());
        mTracker = analytics.newTracker(R.xml.global_tracker);
        mTracker.setScreenName("Record Video");
        mTracker.send(new HitBuilders.EventBuilder().setCategory(SharedPref.getID(mContext) + "_" + SharedPref.getName(mContext)).
                setAction(finalVideoName).setLabel("Feeling Tags: " + mFeelingName).build());
        mTracker = analytics.newTracker(R.xml.global_tracker);
        mTracker.setScreenName("Record Video");
        mTracker.send(new HitBuilders.EventBuilder().setCategory(SharedPref.getID(mContext) + "_" + SharedPref.getName(mContext)).
                setAction(finalVideoName).setLabel("Mood rating: " + mMoodValue + " (" + mMoodName + ")").build());
        if (!mMoodDescription.getText().toString().isEmpty()) {
            mTracker = analytics.newTracker(R.xml.global_tracker);
            mTracker.setScreenName("Record Video");
            mTracker.send(new HitBuilders.EventBuilder().setCategory(SharedPref.getID(mContext) + "_" + SharedPref.getName(mContext)).
                    setAction(finalVideoName).setLabel("Free text associated: " + mMoodDescription.getText().toString()).build());
        }
    }

    private void openHomeScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Constants.SET_MOOD, Constants.SET_MOOD);
        startActivity(intent);
        finish();
    }

    private String getUploadedVideoPath() {
        return Constants.AWS_PATH + Constants.BUCKET_NAME + "/" + Constants.VIDEO_UPLOAD_PATH + newFileName.getName();
    }

    private String getUploadedVideoThumbnailPath() {
        return Constants.AWS_PATH + Constants.BUCKET_NAME + "/" + Constants.VIDEO_THUMBNAIL_UPLOAD_PATH + mCoverUrlFile.getName();
    }

    private void attachListeners() {
        mAddFeelingButton.setOnClickListener(onClickListener);
        mUploadButton.setOnClickListener(onClickListener);
        mMoodDescription.setOnFocusChangeListener(onFocusChangeListener);
        mMoodDescription.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!mMoodValue.equals("0")) {
                        if (!mAddFeelingButton.getText().toString().equals(getResources().getString(R.string.add_feelings))) {
                            readyVideoFile();
                        } else {
                            Toast.makeText(mContext, getResources().getString(R.string.select_feeling), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(mContext, getResources().getString(R.string.select_mood), Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
                return false;
            }
        });
        mCircularSeekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    mMoodTitle.setText("");
                    mCircularSeekBar.setBackgroundResource(R.drawable.wheel_blank);
                    mMoodValue = "0";
                } else if (progress > 0 && progress <= 14.28) {
                    mCircularSeekBar.setBackgroundResource(R.drawable.wheel_best);
                    mMoodTitle.setText(getResources().getString(R.string.best_mood_capital));
                    mMoodValue = "1";
                    if (mFeelingName != null) {
                        changeVideoFileNameWithFeeling(getResources().getString(R.string.best_mood));
                    } else {
                        changeVideoNameWithoutFeeling(getResources().getString(R.string.best_mood));
                    }
                } else if (progress > 14.28 && progress <= 28.56) {
                    mCircularSeekBar.setBackgroundResource(R.drawable.wheel_very_good);
                    mMoodTitle.setText(getResources().getString(R.string.very_good_mood_capital));
                    mMoodValue = "2";
                    if (mFeelingName != null) {
                        changeVideoFileNameWithFeeling(getResources().getString(R.string.very_good_mood));
                    } else {
                        changeVideoNameWithoutFeeling(getResources().getString(R.string.very_good_mood));
                    }
                } else if (progress > 28.56 && progress <= 42.84) {
                    mCircularSeekBar.setBackgroundResource(R.drawable.wheel_good);
                    mMoodTitle.setText(getResources().getString(R.string.good_mood_capital));
                    mMoodValue = "3";
                    if (mFeelingName != null) {
                        changeVideoFileNameWithFeeling(getResources().getString(R.string.good_mood));
                    } else {
                        changeVideoNameWithoutFeeling(getResources().getString(R.string.good_mood));
                    }
                } else if (progress > 42.84 && progress <= 57.12) {
                    mCircularSeekBar.setBackgroundResource(R.drawable.wheel_ok);
                    mMoodTitle.setText(getResources().getString(R.string.ok_mood_capital));
                    mMoodValue = "4";
                    if (mFeelingName != null) {
                        changeVideoFileNameWithFeeling(getResources().getString(R.string.ok_mood));
                    } else {
                        changeVideoNameWithoutFeeling(getResources().getString(R.string.ok_mood));
                    }
                } else if (progress > 57.12 && progress <= 71.4) {
                    mCircularSeekBar.setBackgroundResource(R.drawable.wheel_bad);
                    mMoodTitle.setText(getResources().getString(R.string.bad_mood_capital));
                    mMoodValue = "5";
                    if (mFeelingName != null) {
                        changeVideoFileNameWithFeeling(getResources().getString(R.string.bad_mood));
                    } else {
                        changeVideoNameWithoutFeeling(getResources().getString(R.string.bad_mood));
                    }
                } else if (progress > 71.4 && progress <= 85.68) {
                    mCircularSeekBar.setBackgroundResource(R.drawable.wheel_very_bad);
                    mMoodTitle.setText(getResources().getString(R.string.very_bad_mood_capital));
                    mMoodValue = "6";
                    if (mFeelingName != null) {
                        changeVideoFileNameWithFeeling(getResources().getString(R.string.very_bad_mood));
                    } else {
                        changeVideoNameWithoutFeeling(getResources().getString(R.string.very_bad_mood));
                    }
                } else if (progress > 85.68) {
                    mCircularSeekBar.setBackgroundResource(R.drawable.wheel_worst);
                    mMoodTitle.setText(getResources().getString(R.string.worst_mood_capital));
                    mMoodValue = "7";
                    if (mFeelingName != null) {
                        changeVideoFileNameWithFeeling(getResources().getString(R.string.worst_mood));
                    } else {
                        changeVideoNameWithoutFeeling(getResources().getString(R.string.worst_mood));
                    }
                }
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {

            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.add_feelings_btn:
                    openAddFeelingScreen();
                    break;
                case R.id.upload_icon:
                    if (!mMoodValue.equals("0")) {
                        if (!mAddFeelingButton.getText().toString().equals(getResources().getString(R.string.add_feelings))) {
                            readyVideoFile();
                        } else {
                            Toast.makeText(mContext, getResources().getString(R.string.select_feeling), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(mContext, getResources().getString(R.string.select_mood), Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    };

    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            switch (v.getId()) {
                case R.id.mood_description_edittext:
                    if (hasFocus) {
                        mMoodDescription.setHint("");
                    }
            }
        }
    };

    private void changeVideoFileNameWithFeeling(String mood) {
        mMoodName = mood;
        if(mAddVideoName.getText().toString().equals(mDefaultFileName)) {
            mAddVideoName.setText(SharedPref.getName(mContext) + "_" + mFeelingName + "_" + mood + "_" + getCurrentDateTime());
            mDefaultFileName = mAddVideoName.getText().toString();
        }
    }

    private void changeVideoNameWithoutFeeling(String mood) {
        mMoodName = mood;
        if(mAddVideoName.getText().toString().equals(mDefaultFileName)) {
            mAddVideoName.setText(SharedPref.getName(mContext) + "_" + mood + "_" + getCurrentDateTime());
            mDefaultFileName = mAddVideoName.getText().toString();
        }
    }

    private void openAddFeelingScreen() {
        Intent intent = new Intent(this, AddFeelingActivity.class);
        startActivityForResult(intent, Constants.SELECTED_FEELING);
    }

    private void initialiseVariables() {
        mContext = this;
        mActualVideoUri = Uri.parse(getIntent().getStringExtra(Constants.VIDEO_URI_NAME));
        String pathFromUri = getPathFromURI(mActualVideoUri);
        mVideoFile = new File(pathFromUri);
        try {
            mVideoFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mDefaultFileName = SharedPref.getName(mContext) + "_" + getCurrentDateTime();
        mAddVideoName.setText(mDefaultFileName);
        mCircularSeekBar.setCircleColor(Color.TRANSPARENT);
        setSupportActionBar(mToolbar);
//        setHeader(getResources().getString(R.string.set_mood_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoFile != null) {
            mVideoFile.delete();
            updateGallery(mVideoFile.getAbsolutePath());
        }
        if (newFileName != null) {
            newFileName.delete();
            updateGallery(newFileName.getAbsolutePath());
        }
    }

    public void updateGallery(String filePath) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        } else{
            MediaScannerConnection.scanFile(mContext, new String[]{filePath}, null, new MediaScannerConnection.OnScanCompletedListener() {
                /*
                 *   (non-Javadoc)
                 * @see android.media.MediaScannerConnection.OnScanCompletedListener#onScanCompleted(java.lang.String, android.net.Uri)
                 */
                public void onScanCompleted(String path, Uri uri)
                {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);
                }
            });
        }
    }

    private String getCurrentDateTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd_MMM_yyyy_HH_mm_ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
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

    private void uploadVideo() {
        if (newFileName != null) {
            mProgressBarLayout.setVisibility(View.VISIBLE);
//            new upload().execute();
            AWSHelper.uploadFile(mContext, newFileName, Constants.VIDEO_UPLOAD_PATH, mTransferListenerVideo);
        }
    }

    /*public class upload extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            AWSHelper.uploadFile(mContext, newFileName, Constants.VIDEO_UPLOAD_PATH, mTransferListenerVideo);
            return null;
        }
    }*/

    @Override
    public void onBackPressed() {
        confirmBack();
    }

    private void confirmBack() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);
        builder.setTitle("Discard Video").setMessage("Are you sure you want to discard your recording?").setPositiveButton("Yes", confirmBackDialogClickListener)
                .setNegativeButton("No", confirmBackDialogClickListener).show();
    }

    DialogInterface.OnClickListener confirmBackDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    openHomeScreen();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    private void registerViews() {
        mCircularSeekBar = (CircularSeekBar) findViewById(R.id.circular_seek_bar);
        mMoodTitle = (TextView) findViewById(R.id.mood_title);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mAddFeelingButton = (TextView) findViewById(R.id.add_feelings_btn);
        mMoodDescription = (EditText) findViewById(R.id.mood_description_edittext);
        mAddVideoName = (EditText) findViewById(R.id.add_video_name_edittext);
        mProgressBarLayout = (RelativeLayout) findViewById(R.id.progress_bar_layout);
        mSelectedFeelingColor = (View) findViewById(R.id.selected_feeling_color);
        mUploadButton = (ImageView) findViewById(R.id.upload_icon);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.SELECTED_FEELING) {
            if (data != null) {
                Bundle selectedFeelingDetails = data.getBundleExtra(Constants.SELECTED_FEELING_DETAILS);
                mAddFeelingButton.setText("Feeling: " + selectedFeelingDetails.getString(Constants.SELECTED_FEELING_NAME));
                mSelectedFeelingId = selectedFeelingDetails.getString(Constants.SELECTED_FEELING_ID);
                mSelectedFeelingColor.setBackgroundColor(Color.rgb(selectedFeelingDetails.getInt(Constants.SELECTED_FEELING_RED),
                        selectedFeelingDetails.getInt(Constants.SELECTED_FEELING_GREEN), selectedFeelingDetails.getInt(Constants.SELECTED_FEELING_BLUE)));
                if (mAddVideoName.getText().toString().equals(mDefaultFileName)) {
                    mFeelingName = selectedFeelingDetails.getString(Constants.SELECTED_FEELING_NAME);
                    if (mMoodName != null) {
                        mDefaultFileName = SharedPref.getName(mContext) + "_" + mFeelingName
                                + "_" + mMoodName + "_" + getCurrentDateTime();
                    } else {
                        mDefaultFileName = SharedPref.getName(mContext) + "_" + mFeelingName
                                + "_" + getCurrentDateTime();
                    }
                    mAddVideoName.setText(mDefaultFileName);
                }
            }
        }
    }

    public String getPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        //Source not from device capture or selection
        if (cursor == null) {
            return uri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
            if (idx == -1) {
                return uri.getPath();
            }

            String path = cursor.getString(idx);
            cursor.close();
            return path;
        }
    }

    private File getThumbnailFile(Bitmap thumbnail) throws IOException {
        File f = new File(mContext.getCacheDir(), mAddVideoName.getText().toString() + "_CoverURL");
        f.createNewFile();

//Convert bitmap to byte array
        Bitmap bitmap = thumbnail;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();
        return f;
    }
}
