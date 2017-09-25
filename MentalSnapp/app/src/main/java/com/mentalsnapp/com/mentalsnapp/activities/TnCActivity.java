package com.mentalsnapp.com.mentalsnapp.activities;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.MenuItem;
import android.widget.TextView;

import com.mentalsnapp.com.mentalsnapp.R;

/**
 * Created by gchandra on 6/1/17.
 */
public class TnCActivity extends BaseActivity {

    private TextView mMessage7;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tnc);
        registerViews();
        initialiseVariables();
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

    private void initialiseVariables() {
        setSupportActionBar(mToolbar);
        setHeader(getResources().getString(R.string.tnc_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }


    private void registerViews() {
        mMessage7 = (TextView) findViewById(R.id.tnc_message_7);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
    }
}
