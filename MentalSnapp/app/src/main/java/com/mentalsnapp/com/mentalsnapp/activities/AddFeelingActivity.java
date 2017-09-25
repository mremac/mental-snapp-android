package com.mentalsnapp.com.mentalsnapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.adapters.AddFeelingRecyclerAdapter;
import com.mentalsnapp.com.mentalsnapp.models.AddFeeling;
import com.mentalsnapp.com.mentalsnapp.network.ApiClient;
import com.mentalsnapp.com.mentalsnapp.network.response.AddFeelingResponse;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;
import com.mentalsnapp.com.mentalsnapp.utils.JsonToStringConverter;
import com.mentalsnapp.com.mentalsnapp.utils.LogHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by gchandra on 13/1/17.
 */
public class AddFeelingActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private EditText mSearchFeeling;
    private Toolbar mToolbar;
    private ImageView mCrossSearch;
    private RelativeLayout mProgressBarLayout;
    private ArrayList<AddFeeling> mAllFeelingsList;
    private ArrayList<AddFeeling> mNewFeelingList;
    private AddFeelingRecyclerAdapter mFeelingRecyclerAdapter;
    private ArrayList<AddFeeling> mSelectedFeelingList;
    private ImageView mDoneButton;
    private static final int page = 1;
    private static final int perPage = 100;
    private Integer mSelectedFeelingPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_feelings);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        registerViews();
        initialiseVariables();
        attachListeners();
        getFeelingList();
    }

    private void getFeelingList() {
        Call<AddFeelingResponse> getFeelingList = ApiClient.getApiInterface().getFeelingList(page, perPage);
        getFeelingList.enqueue(mResponseCallback);
        mProgressBarLayout.setVisibility(View.VISIBLE);
    }

    Callback mResponseCallback = new Callback() {
        @Override
        public void onResponse(Call call, Response response) {
            LogHelper.logInfo(AddFeelingActivity.this, "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
                    call.request().method(), call.request().body()));
            mProgressBarLayout.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                LogHelper.logInfo(AddFeelingActivity.this, "mResponseCallback", JsonToStringConverter.convertToJson(response.body()));
                ArrayList<AddFeeling> feelingList = ((AddFeelingResponse) response.body()).mFeelingList;
                mAllFeelingsList.addAll(feelingList);
                prepareArrayList();
                setupRecyclerview(mNewFeelingList);
            } else {
                try {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(getApplicationContext(), String.valueOf(jObjError.get("message")), Toast.LENGTH_LONG).show();
                    LogHelper.logError(AddFeelingActivity.this, "mResponseCallback", JsonToStringConverter.convertToJson(response.errorBody()));
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
                mProgressBarLayout.setVisibility(View.GONE);
            }
        }

        @Override
        public void onFailure(Call call, Throwable t) {
            LogHelper.logInfo(AddFeelingActivity.this, "Request", JsonToStringConverter.mergeAndConvert(call.request().url().toString(),
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
            LogHelper.logError(AddFeelingActivity.this, "error", t.getMessage());
        }
    };

    private void prepareArrayList() {
        for (int i = 0; i < mAllFeelingsList.size(); i++) {
            mNewFeelingList.add(mAllFeelingsList.get(i));
            for (int j = 0; j < mAllFeelingsList.get(i).subFeelingList.size(); j++) {
                mAllFeelingsList.get(i).subFeelingList.get(j).isSubFeeling = true;
                mNewFeelingList.add(mAllFeelingsList.get(i).subFeelingList.get(j));
            }
        }
    }

    private void setupRecyclerview(ArrayList<AddFeeling> mFeelingList) {
        mFeelingRecyclerAdapter = new AddFeelingRecyclerAdapter(mFeelingList, this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mFeelingRecyclerAdapter);
    }

    private void attachListeners() {
        mSearchFeeling.addTextChangedListener(textWatcher);
        mCrossSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchFeeling.setText("");
                hideSoftKeyboard();
            }
        });
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mSearchFeeling.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mCrossSearch.setVisibility(View.VISIBLE);
                } else {
                    mCrossSearch.setVisibility(View.GONE);
                }
            }
        });
    }

    private void hideSoftKeyboard() {
        if (getCurrentFocus() != null && getCurrentFocus() instanceof EditText) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mSearchFeeling.getWindowToken(), 0);
        }
    }

    private void initialiseVariables() {
        mAllFeelingsList = new ArrayList<>();
        mNewFeelingList = new ArrayList<>();
        mSelectedFeelingList = new ArrayList<>();
        setSupportActionBar(mToolbar);
//        setHeader(getResources().getString(R.string.feelings_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mSelectedFeelingPosition != null) {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.SELECTED_FEELING_NAME, mNewFeelingList.get(mSelectedFeelingPosition).name);
            bundle.putString(Constants.SELECTED_FEELING_ID, String.valueOf(mNewFeelingList.get(mSelectedFeelingPosition).id));
            bundle.putInt(Constants.SELECTED_FEELING_RED, mNewFeelingList.get(mSelectedFeelingPosition).red);
            bundle.putInt(Constants.SELECTED_FEELING_GREEN, mNewFeelingList.get(mSelectedFeelingPosition).green);
            bundle.putInt(Constants.SELECTED_FEELING_BLUE, mNewFeelingList.get(mSelectedFeelingPosition).blue);
            intent.putExtra(Constants.SELECTED_FEELING_DETAILS, bundle);
            setResult(Constants.SELECTED_FEELING, intent);
        }
        super.onBackPressed();
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            search(s.toString());
        }
    };

    private void search(String keyword) {
        mSelectedFeelingList.clear();
        for (AddFeeling feeling : mNewFeelingList) {
            if (!feeling.isSubFeeling) {
                mSelectedFeelingList.add(feeling);
            } else {
                if (feeling.name.toLowerCase().contains(keyword.toLowerCase())) {
                    mSelectedFeelingList.add(feeling);
                }
            }
        }
        if (mSelectedFeelingList != null) {
            mFeelingRecyclerAdapter = new AddFeelingRecyclerAdapter(mSelectedFeelingList, this);
            mRecyclerView.setAdapter(mFeelingRecyclerAdapter);
        }
    }

    private void registerViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.feeling_recycler);
        mSearchFeeling = (EditText) findViewById(R.id.search_text);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mProgressBarLayout = (RelativeLayout) findViewById(R.id.progress_bar_layout);
        mCrossSearch = (ImageView) findViewById(R.id.cross_clear_add_feeling);
        mDoneButton = (ImageView) findViewById(R.id.done_btn);
    }

    public void setmSelectedFeelingPosition(int mSelectedFeelingPosition) {
        this.mSelectedFeelingPosition = mSelectedFeelingPosition;
    }
}
