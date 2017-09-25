package com.mentalsnapp.com.mentalsnapp.views;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.mentalsnapp.com.mentalsnapp.R;

/**
 * Created by Srinivas Kalyani on 9/2/17.
 */

public class CustomMarkerView extends MarkerView {

    private TextView tvContent;
    private String text;

    public CustomMarkerView(Context context, int layoutResource, String text) {
        super(context, layoutResource);
        // this markerview only displays a textview
        tvContent = (TextView) findViewById(R.id.tagText);
        this.text = text;
    }

    public void setText(String text) {
        this.text = text;
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        if (text != null) {
            if (text.length() > 20) {
                int dp = (int) (getResources().getDimension(R.dimen.medium_view_margin) / getResources().getDisplayMetrics().density);
                tvContent.setMinHeight(dp);
            } else {
                int dp = (int) (getResources().getDimension(R.dimen.view_large_margin) / getResources().getDisplayMetrics().density);
                tvContent.setMinHeight(dp);
            }
            tvContent.setText(text); // set the entry-value as the display text
        }
    }
}
