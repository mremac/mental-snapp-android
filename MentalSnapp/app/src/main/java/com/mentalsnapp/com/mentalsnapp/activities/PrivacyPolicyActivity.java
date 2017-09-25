package com.mentalsnapp.com.mentalsnapp.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.mentalsnapp.com.mentalsnapp.R;

/**
 * Created by gchandra on 9/1/17.
 */
public class PrivacyPolicyActivity extends BaseActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        registerViews();
        initialiseVariables();
    }

    private void initialiseVariables() {
        setSupportActionBar(mToolbar);
        setHeader(getResources().getString(R.string.privacy_policy_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void registerViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
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
