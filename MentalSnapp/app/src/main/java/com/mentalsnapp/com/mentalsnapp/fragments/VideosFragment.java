package com.mentalsnapp.com.mentalsnapp.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.activities.MainActivity;
import com.mentalsnapp.com.mentalsnapp.activities.PlayVideoActivity;
import com.mentalsnapp.com.mentalsnapp.adapters.FilterRecyclerAdapter;
import com.mentalsnapp.com.mentalsnapp.adapters.VideosRecyclerAdapter;
import com.mentalsnapp.com.mentalsnapp.models.GuidedExercise;
import com.mentalsnapp.com.mentalsnapp.models.Videos;
import com.mentalsnapp.com.mentalsnapp.network.ApiClient;
import com.mentalsnapp.com.mentalsnapp.network.response.BaseResponse;
import com.mentalsnapp.com.mentalsnapp.network.response.FilterResponse;
import com.mentalsnapp.com.mentalsnapp.network.response.GetVideosResponse;
import com.mentalsnapp.com.mentalsnapp.utils.AWSHelper;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;
import com.mentalsnapp.com.mentalsnapp.utils.JsonToStringConverter;
import com.mentalsnapp.com.mentalsnapp.utils.LogHelper;
import com.mentalsnapp.com.mentalsnapp.utils.StorageUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by gchandra on 18/1/17.
 */
public class VideosFragment extends Fragment {

    private ArrayList<Videos> mVideosList;
    private VideosRecyclerAdapter mAdapter;
    private RecyclerView mVideosRecyclerView;
    private RecyclerView mFilterRecyclerView;
    private NumberPicker mDayPicker;
    private NumberPicker mMonthPicker;
    private NumberPicker mYearPicker;
    private FilterRecyclerAdapter mFilterRecyclerAdapter;
    private ArrayList<GuidedExercise> mFilterList;
    private ArrayList<Videos> mVideosListBySearch;
    private ArrayList<Videos> mVideosListByFilter;
    private RelativeLayout mProgressBarLayout;
    private ImageView mFilterButton;
    private ImageView mCalendarButton;
    private SearchView mSearchView;
    private TextView mDoneButton;
    private TextView mCancelButton;
    private TextView mNoVideosText;
    private PopupWindow mCalendarPopup;
    private long deleteVideoId;
    private String deleteVideoUrl;
    private String deleteThumbnailUrl;
    private int mPerPage = 25;
    public int mCurrentPageFilter = 1;
    private int mCurrentPageSearch = 1;
    private int mCurrentPageFullList = 1;
    private int mVisibleThreshold = 10;
    private boolean mLoading;
    private String mSearchQuery;
    private File mDownloadFile;
    private String mDownloadVideoURL;
    private String mVideoName;
    private static String mVideoListType;
    private Dialog mDownloadDialog;
    private ProgressBar mDownloadProgress;
    private ImageView mDownloadProgressCancel;
    private TextView mDownloadName;
    PopupWindow filterPopup;
    private static int mDownloadVideoID;
    private int mFilterPosition = 0;
    private long mFilterId = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_videos, container, false);
        registerViews(view);
        initialiseVariables();
        attachInfiniteScrollListeners();
        attachListeners();
        return view;
    }

    private void initialiseVariables() {
        mVideosList = new ArrayList<>();
        mFilterList = new ArrayList<>();
        mVideosListBySearch = new ArrayList<>();
        mVideosListByFilter = new ArrayList<>();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (mFilterId == 1) {
                mVideosList = new ArrayList<>();
                mPerPage = 50;
                getVideos();
            } else {
                mCurrentPageFilter = 1;
                mPerPage = 50;
                Call<GetVideosResponse> getVideosByFilter = ApiClient.getApiInterface().getVideosByFilter(mFilterId, mCurrentPageFilter, mPerPage);
                getVideosByFilter.enqueue(mResponseCallbackSearch);
            }

        } else {
            if (mSearchView != null) {
                hideSoftKeyboard();
            }
            mCurrentPageFullList = 1;
            mCurrentPageSearch = 1;
        }
    }

    private void getVideos() {
        mVideoListType = Constants.FULL_VIDEO_LIST;
        Call<GetVideosResponse> getVideosList = ApiClient.getApiInterface().getVideos(mCurrentPageFullList, mPerPage);
        getVideosList.enqueue(mResponseCallback);
        mProgressBarLayout.setVisibility(View.VISIBLE);
    }

    Callback mResponseCallback = new Callback() {
        @Override
        public void onResponse(Call call, Response response) {
            LogHelper.logInfo(getContext(), "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()));
            mProgressBarLayout.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                mLoading = false;
                LogHelper.logInfo(getContext(), "mResponseCallback", JsonToStringConverter.convertToJson(response.body()));
                if (call.request().url().toString().contains(Constants.GET_VIDEOS_API)
                        && call.request().method().equalsIgnoreCase(Constants.GET)) {
                    ArrayList<Videos> videosList = ((GetVideosResponse) response.body()).videosList;
                    if (videosList != null) {
                        mPerPage = videosList.size();
                        if (videosList.size() > 0) {
                            mVideosList.addAll(videosList);
                        }
                        mNoVideosText.setVisibility(View.GONE);
                        if (mCurrentPageFullList == 1) {
                            setupRecyclerView(mVideosList);
                        } else {
                            if (mVideosList.size() > 0) {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                    if (videosList != null && videosList.size() == 0 && mCurrentPageFullList == 1) {
                        mNoVideosText.setVisibility(View.VISIBLE);
                        mVideosRecyclerView.setVisibility(View.GONE);
                    }
                    mCurrentPageFullList++;
                    mProgressBarLayout.setVisibility(View.GONE);
                }
                if (call.request().url().toString().contains(Constants.GET_VIDEOS_API)
                        && call.request().method().equalsIgnoreCase(Constants.DELETE)) {
                    new UpdateRecyclerView().execute();
                }
                if (call.request().url().toString().contains(Constants.FILTERS_API)) {
                    ArrayList<GuidedExercise> filterList = ((FilterResponse) response.body()).guidedExercises;
                    mFilterList.addAll(filterList);
                    setupFilterRecyclerView();
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

        @Override
        public void onFailure(Call call, Throwable t) {
            LogHelper.logInfo(getContext(), "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()));
            mProgressBarLayout.setVisibility(View.GONE);
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

    private void setupFilterRecyclerView() {
        mFilterRecyclerAdapter = new FilterRecyclerAdapter(this, mFilterList, filterPopup, mFilterPosition);
        mFilterRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mFilterRecyclerView.setAdapter(mFilterRecyclerAdapter);
        mProgressBarLayout.setVisibility(View.GONE);
    }

    public void downloadVideo() {
        if (((MainActivity) getActivity()).checkPermissionWrite() || ((MainActivity) getActivity()).checkPermissionRead()) {
            if (StorageUtils.checkForExistance()) {
                mDownloadFile = new File(Environment.getExternalStorageDirectory() + "/Mentalsnapp", mVideoName + ".mp4");
            } else {
                mDownloadFile = new File(StorageUtils.makeDirs(), mVideoName + ".mp4");
            }
            if (mDownloadVideoURL.contains("https://s3-eu-west-1.amazonaws.com/mentalsnapp/")) {
                mDownloadVideoURL = mDownloadVideoURL.replace("https://s3-eu-west-1.amazonaws.com/mentalsnapp/", "");
            }
            if (!mDownloadFile.exists()) {
                try {
                    mDownloadFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            AWSHelper.downloadFile(getContext(), mDownloadVideoURL, mDownloadFile, downloadTransferListener);
        } else {
            ((MainActivity) getActivity()).requestPermissionWrite(Constants.PERMISSION_REQUEST_CODE_DOWNLOAD);
        }
        openDownloadPopup();
    }

    private void openDownloadPopup() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View downloadLayout = inflater.inflate(R.layout.download_progress_layout, null);
        mDownloadProgressCancel = (ImageView) downloadLayout.findViewById(R.id.cancel);
        mDownloadProgress = (ProgressBar) downloadLayout.findViewById(R.id.download_progress);
        mDownloadName = (TextView) downloadLayout.findViewById(R.id.download_video_name);
        mDownloadProgress.setMax(100);
        mDownloadProgress.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.color_gray));
        mDownloadDialog = new Dialog(getContext());
        mDownloadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDownloadDialog.setContentView(downloadLayout);
        mDownloadDialog.setCancelable(false);
        mDownloadDialog.show();
        mDownloadName.setText(mVideoName);
        mDownloadProgressCancel.setOnClickListener(downloadOnClickListener);
    }

    View.OnClickListener downloadOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.cancel:
                    mDownloadDialog.dismiss();
                    AWSHelper.cancelTransfer(getContext(), mDownloadVideoID);
            }
        }
    };

    TransferListener downloadTransferListener = new TransferListener() {
        @Override
        public void onStateChanged(int id, TransferState state) {
            LogHelper.logInfo(getContext(), "id:" + id, "State:" + state);
            switch (state) {
                case IN_PROGRESS:
                    mDownloadVideoID = id;
                    break;
                case COMPLETED:
                    mDownloadDialog.dismiss();
                    Toast.makeText(getContext(), getResources().getString(R.string.video_downloaded), Toast.LENGTH_LONG).show();
                    updateGallery(mDownloadFile.getAbsolutePath());
                    break;
                case CANCELED:
                    mDownloadDialog.dismiss();
            }
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            if (bytesTotal != 0) {
                mDownloadProgress.setProgress((int) (((bytesCurrent * 1.0) / (bytesTotal * 1.0)) * 100));
            }
        }

        @Override
        public void onError(int id, Exception ex) {
            LogHelper.logError(getContext(), "Error", ex.toString());
            mDownloadDialog.dismiss();
        }
    };

    public void updateGallery(String filePath) {
        getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(filePath))));
    }

    private class UpdateRecyclerView extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i < mVideosList.size(); i++) {
                if (mVideosList.get(i).id == deleteVideoId) {
                    mVideosList.remove(i);
                }
            }
            if (mVideosListBySearch != null) {
                for (int i = 0; i < mVideosListBySearch.size(); i++) {
                    if (mVideosListBySearch.get(i).id == deleteVideoId) {
                        mVideosListBySearch.remove(i);
                    }
                }
            }
            if (mVideosListByFilter != null) {
                for (int i = 0; i < mVideosListByFilter.size(); i++) {
                    if (mVideosListByFilter.get(i).id == deleteVideoId) {
                        mVideosListByFilter.remove(i);
                    }
                }
            }
            if (deleteVideoUrl.contains("https://s3-eu-west-1.amazonaws.com/mentalsnapp/")) {
                deleteVideoUrl = deleteVideoUrl.replace("https://s3-eu-west-1.amazonaws.com/mentalsnapp/", "");
                AWSHelper.deleteFile(getContext(), deleteVideoUrl);
            }
            if (deleteThumbnailUrl.contains("https://s3-eu-west-1.amazonaws.com/mentalsnapp/")) {
                deleteThumbnailUrl = deleteThumbnailUrl.replace("https://s3-eu-west-1.amazonaws.com/mentalsnapp/", "");
                AWSHelper.deleteFile(getContext(), deleteThumbnailUrl);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (mVideoListType.equals(Constants.FULL_VIDEO_LIST)) {

                if (mVideosList.size() > 0) {
                    mAdapter.notifyDataSetChanged();
                } else {
                    mNoVideosText.setVisibility(View.VISIBLE);
                    mVideosRecyclerView.setVisibility(View.GONE);
                }
            } else if (mVideoListType.equals(Constants.VIDEOS_BY_SEARCH)) {
                if (mVideosListBySearch.size() > 0) {
                    mAdapter.notifyDataSetChanged();
                } else {
                    mNoVideosText.setVisibility(View.VISIBLE);
                    mVideosRecyclerView.setVisibility(View.GONE);
                }
            } else if (mVideoListType.equals(Constants.VIDEOS_BY_FILTER)) {
                if (mVideosListByFilter.size() > 0) {
                    mAdapter.notifyDataSetChanged();
                } else {
                    mNoVideosText.setVisibility(View.VISIBLE);
                    mVideosRecyclerView.setVisibility(View.GONE);
                }
            }
            mProgressBarLayout.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView(ArrayList<Videos> videosList) {
        mNoVideosText.setVisibility(View.GONE);
        mVideosRecyclerView.setVisibility(View.VISIBLE);
        mAdapter = new VideosRecyclerAdapter(videosList, this);
        mVideosRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mVideosRecyclerView.setAdapter(mAdapter);
    }

    public void deleteVideo(long id, String videoUrl, String thumbnailUrl) {
        deleteVideoId = id;
        deleteVideoUrl = videoUrl;
        deleteThumbnailUrl = thumbnailUrl;
        Call<BaseResponse> deleteVideo = ApiClient.getApiInterface().deleteVideo(id);
        deleteVideo.enqueue(mResponseCallback);
    }

    public void playVideo(int position) {
        Intent intent = new Intent(getActivity(), PlayVideoActivity.class);
        intent.putExtra(Constants.PLAY_VIDEO_URL, mVideosList.get(position).videoURL);
        startActivity(intent);
    }

    private void attachListeners() {
        mFilterButton.setOnClickListener(onClickListener);
        mCalendarButton.setOnClickListener(onClickListener);
        mSearchView.setOnSearchClickListener(onClickListener);
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mSearchView.setBackground(null);
                mCalendarButton.setImageResource(R.drawable.calendar_blue);
                if (mVideosListByFilter != null && mVideosListByFilter.size() > 0) {
                    mVideoListType = Constants.FULL_VIDEO_LIST;
                    setupRecyclerView(mVideosListByFilter);
                } else if (mVideosList.size() > 0) {
                    mVideoListType = Constants.FULL_VIDEO_LIST;
                    setupRecyclerView(mVideosList);
                }
                return false;
            }
        });
    }

    private void hideSoftKeyboard() {
        if (getActivity().getCurrentFocus() != null && getActivity().getCurrentFocus() instanceof EditText) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
        }
    }

    private void getListBySearch(String query) {
        mPerPage = 25;
        mVideoListType = Constants.VIDEOS_BY_SEARCH;
        mProgressBarLayout.setVisibility(View.VISIBLE);
        Call<GetVideosResponse> getVideosBySearch = ApiClient.getApiInterface().getVideosBySearch(query, mCurrentPageSearch, mPerPage);
        getVideosBySearch.enqueue(mResponseCallbackSearch);
    }

    Callback mResponseCallbackSearch = new Callback() {
        @Override
        public void onResponse(Call call, Response response) {
            LogHelper.logInfo(getContext(), "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()));
            if (response.isSuccessful()) {
                mLoading = false;
                hideSoftKeyboard();
                LogHelper.logInfo(getContext(), "mResponseCallback", JsonToStringConverter.convertToJson(response.body()));
                if (call.request().url().toString().contains(Constants.VIDEOS_BY_SEARCH_API)) {
                    mVideosListBySearch = ((GetVideosResponse) response.body()).videosList;
                    if (mVideosListBySearch != null) {
                        mPerPage = mVideosListBySearch.size();
                        if (mCurrentPageSearch == 1) {
                            if (mVideosListBySearch.size() > 0) {
                                setupRecyclerView(mVideosListBySearch);
                                mNoVideosText.setVisibility(View.GONE);
                            } else {
                                mNoVideosText.setVisibility(View.VISIBLE);
                                mVideosRecyclerView.setVisibility(View.GONE);
                            }
                        } else {
                            if (mVideosListByFilter.size() > 0) {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                        mCurrentPageSearch++;
                    } else {
                        mNoVideosText.setVisibility(View.VISIBLE);
                        mVideosRecyclerView.setVisibility(View.GONE);
                    }
                    mProgressBarLayout.setVisibility(View.GONE);
                }
                if (call.request().url().toString().contains(Constants.VIDEOS_BY_FILTER_API)) {
                    mVideosListByFilter = ((GetVideosResponse) response.body()).videosList;
                    if (mVideosListByFilter != null) {
                        mPerPage = mVideosListByFilter.size();
                        mNoVideosText.setVisibility(View.GONE);
                        if (mCurrentPageFilter == 1) {
                            if (mVideosListByFilter.size() > 0) {
                                setupRecyclerView(mVideosListByFilter);
                            } else {
                                mNoVideosText.setVisibility(View.VISIBLE);
                                mVideosRecyclerView.setVisibility(View.GONE);
                            }
                        } else {
                            if (mVideosListByFilter.size() > 0) {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                        mCurrentPageFilter++;
                    } else {
                        mNoVideosText.setVisibility(View.VISIBLE);
                        mVideosRecyclerView.setVisibility(View.GONE);
                    }
                    mProgressBarLayout.setVisibility(View.GONE);
                }
            } else {
                mProgressBarLayout.setVisibility(View.GONE);
                try {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(getContext(), String.valueOf(jObjError.get("message")), Toast.LENGTH_LONG).show();
                    LogHelper.logError(getContext(), "mResponseCallback", JsonToStringConverter.convertToJson(response.errorBody()));
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure(Call call, Throwable t) {
            LogHelper.logInfo(getContext(), "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()));
            mProgressBarLayout.setVisibility(View.GONE);
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

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.sort_btn:
                    openFilterPopup();
                    break;
                case R.id.calendar_btn:
                    openCalendarDialog();
                    break;
                case R.id.search_view:
                    if (!mSearchView.isIconified()) {
                        mSearchView.setBackgroundResource(R.drawable.edittext_shape);
                    }
            }
        }
    };

    private void openCalendarDialog() {
        Calendar c = Calendar.getInstance();
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View calendarLayout = inflater.inflate(R.layout.calendar_picker_layout, null);
        mDayPicker = (NumberPicker) calendarLayout.findViewById(R.id.day_picker);
        mDayPicker.setVisibility(View.VISIBLE);
        mMonthPicker = (NumberPicker) calendarLayout.findViewById(R.id.month_picker);
        mYearPicker = (NumberPicker) calendarLayout.findViewById(R.id.year_picker);
        mDoneButton = (TextView) calendarLayout.findViewById(R.id.done_btn);
        mCancelButton = (TextView) calendarLayout.findViewById(R.id.cancel);
        setDividerColor(mDayPicker, ContextCompat.getColor(getContext(), R.color.light_gray));
        setDividerColor(mMonthPicker, ContextCompat.getColor(getContext(), R.color.light_gray));
        setDividerColor(mYearPicker, ContextCompat.getColor(getContext(), R.color.light_gray));
        setNumberPickerTextColor(mDayPicker, ContextCompat.getColor(getContext(), R.color.color_gray));
        setNumberPickerTextColor(mMonthPicker, ContextCompat.getColor(getContext(), R.color.color_gray));
        setNumberPickerTextColor(mYearPicker, ContextCompat.getColor(getContext(), R.color.color_gray));
        initNumberPickerDays(mDayPicker, 0, 31);
        initNumberPicker(mYearPicker, 1970, c.get(Calendar.YEAR));
        initNumberPickerMonth(mMonthPicker, 1, 12);
        mYearPicker.setValue(c.get(Calendar.YEAR));
        mMonthPicker.setValue(c.get(Calendar.MONTH) + 1);
        mDayPicker.setValue(c.get(Calendar.DAY_OF_MONTH));
        mCalendarPopup = new PopupWindow(calendarLayout, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        mCalendarPopup.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getContext(), android.R.color.transparent)));
        mCalendarPopup.showAsDropDown(mCalendarButton);
        mCalendarPopup.setOutsideTouchable(true);
        mCalendarPopup.setFocusable(false);
        mCalendarPopup.setTouchable(true);
        mCalendarPopup.update();
        mDoneButton.setOnClickListener(calendarOnClickListener);
        mCancelButton.setOnClickListener(calendarOnClickListener);
    }

    private void initNumberPickerDays(NumberPicker mDayPicker, int minLimit, int maxLimit) {
        mDayPicker.setMinValue(minLimit);
        mDayPicker.setMaxValue(maxLimit);
        mDayPicker.setWrapSelectorWheel(false);
        String[] daysArray = new String[32];
        for (int i = 0; i <= 31; i++) {
            if (i == 0) {
                daysArray[i] = "None";
            } else {
                daysArray[i] = String.valueOf(i);
            }
        }
        mDayPicker.setDisplayedValues(daysArray);
    }

    View.OnClickListener calendarOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.done_btn:
                    mSearchView.setIconified(false);
                    if (mDayPicker.getValue() != 0) {
                        String mDayValue;
                        if (mDayPicker.getValue() < 10) {
                            mDayValue = "0" + mDayPicker.getValue();
                        } else {
                            mDayValue = String.valueOf(mDayPicker.getValue());
                        }
                        if (mMonthPicker.getValue() < 10) {
                            mSearchView.setQuery(mYearPicker.getValue() + "-0" + mMonthPicker.getValue() + "-" + mDayValue, false);
                        } else {
                            mSearchView.setQuery(mYearPicker.getValue() + "-" + mMonthPicker.getValue() + "-" + mDayValue, false);
                        }
                    } else {
                        if (mMonthPicker.getValue() < 10) {
                            mSearchView.setQuery(mYearPicker.getValue() + "-0" + mMonthPicker.getValue(), false);
                        } else {
                            mSearchView.setQuery(mYearPicker.getValue() + "-" + mMonthPicker.getValue(), false);
                        }
                    }
                    mVideosListBySearch = new ArrayList<>();
                    mCalendarButton.setImageResource(R.drawable.calendar_orange);
                    mCurrentPageSearch = 1;
                    getListBySearch(mSearchView.getQuery().toString());
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

    private void prepareList() {
        GuidedExercise guidedExerciseNone = new GuidedExercise();
        GuidedExercise guidedExerciseFreeform = new GuidedExercise();
        guidedExerciseNone.name = "None";
        mFilterList.add(0, guidedExerciseNone);
        guidedExerciseFreeform.name = "Free form";
        mFilterList.add(1, guidedExerciseFreeform);
    }

    private void openFilterPopup() {
        prepareList();
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View sortLayout = inflater.inflate(R.layout.filter_layout, null);
        mFilterRecyclerView = (RecyclerView) sortLayout.findViewById(R.id.filter_recyclerview);
        filterPopup = new PopupWindow(sortLayout, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        filterPopup.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getContext(), android.R.color.transparent)));
        filterPopup.showAsDropDown(mFilterButton, 0, 0);
        filterPopup.setOutsideTouchable(true);
        filterPopup.setFocusable(false);
        filterPopup.setTouchable(true);
        filterPopup.update();
        getFilterList();
        filterPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mFilterList = new ArrayList<>();
            }
        });
    }

    private void getFilterList() {
        mPerPage = 25;
        mProgressBarLayout.setVisibility(View.VISIBLE);
        Call<FilterResponse> getFilterList = ApiClient.getApiInterface().getFilterList(1, mPerPage);
        getFilterList.enqueue(mResponseCallback);
    }

    public void getListByFilter(long filterId) {
        mVideoListType = Constants.VIDEOS_BY_FILTER;
        if (filterId != 1) {
            mFilterId = filterId;
            mFilterButton.setImageResource(R.drawable.filter_orange);
            mProgressBarLayout.setVisibility(View.VISIBLE);
            Call<GetVideosResponse> getVideosByFilter = ApiClient.getApiInterface().getVideosByFilter(mFilterId, mCurrentPageFilter, mPerPage);
            getVideosByFilter.enqueue(mResponseCallbackSearch);
        }
        if (filterId == 1) {
            mFilterId = filterId;
            mCurrentPageFullList = 1;
            mFilterButton.setImageResource(R.drawable.sort_icon);
            mVideosListByFilter = new ArrayList<>();
            mVideosList = new ArrayList<>();
            getVideos();
        }
    }

    private void registerViews(View view) {
        mVideosRecyclerView = (RecyclerView) view.findViewById(R.id.videos_recyclerview);
        mProgressBarLayout = (RelativeLayout) view.findViewById(R.id.progress_bar_layout);
        mSearchView = (SearchView) view.findViewById(R.id.search_view);
        mFilterButton = (ImageView) view.findViewById(R.id.sort_btn);
        mCalendarButton = (ImageView) view.findViewById(R.id.calendar_btn);
        mNoVideosText = (TextView) view.findViewById(R.id.no_videos_show);
        mNoVideosText.setVisibility(View.VISIBLE);
        EditText searchEditText = (EditText) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(ContextCompat.getColor(getActivity(), R.color.color_gray));
        searchEditText.setHintTextColor(ContextCompat.getColor(getActivity(), R.color.color_gray));
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (!TextUtils.isEmpty(mSearchView.getQuery().toString())) {
                        if (mFilterId == 1) {
                            mSearchQuery = mSearchView.getQuery().toString();
                            mVideosListBySearch = new ArrayList<>();
                            mCurrentPageSearch = 1;
                            getListBySearch(mSearchView.getQuery().toString());
                            hideSoftKeyboard();
                            return true;
                        } else {
                            mNoVideosText.setVisibility(View.VISIBLE);
                            mVideosRecyclerView.setVisibility(View.GONE);
                        }
                    } else {
                        if (mVideosList.size() > 0) {
                            setupRecyclerView(mVideosList);
                        }
                        hideSoftKeyboard();
                    }
                }
                return false;
            }
        });
    }

    private void attachInfiniteScrollListeners() {
        mVideosRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        if (mVideoListType.equals(Constants.FULL_VIDEO_LIST)) {
            getVideos();
        } else if (mVideoListType.equals(Constants.VIDEOS_BY_SEARCH)) {
            getListBySearch(mSearchQuery);
        } else if (mVideoListType.equals(Constants.VIDEOS_BY_FILTER)) {
            getListByFilter(mFilterId);
        }
    }

    public void setVideoDetails(String mDownloadVideoURL, String mVideoName) {
        this.mDownloadVideoURL = mDownloadVideoURL;
        this.mVideoName = mVideoName;
    }

    public void setFilterPosition(int mFilterPosition) {
        this.mFilterPosition = mFilterPosition;
    }
}
