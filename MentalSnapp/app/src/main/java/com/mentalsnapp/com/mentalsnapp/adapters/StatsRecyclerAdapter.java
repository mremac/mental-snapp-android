package com.mentalsnapp.com.mentalsnapp.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.fragments.StatsFragment;
import com.mentalsnapp.com.mentalsnapp.network.response.StatsResponseModel;

/**
 * Created by gchandra on 9/2/17.
 */
public class StatsRecyclerAdapter extends RecyclerView.Adapter<StatsRecyclerAdapter.InsightAdapterViewHolder> {

    private Context context;
    private StatsResponseModel statsResponseModel;

    public StatsRecyclerAdapter(Context context, StatsResponseModel statsResponseModel) {
        this.context = context;
        this.statsResponseModel = statsResponseModel;
    }

    @Override
    public InsightAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.insight_item_layout, null);
        return new InsightAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(InsightAdapterViewHolder holder, int position) {
        String moodValue = Integer.toString(statsResponseModel.weekData.get(StatsFragment.getPosition()).get(position).get(0));
        String moodFrequency = Integer.toString(statsResponseModel.weekData.get(StatsFragment.getPosition()).get(position).get(1));
        if (moodValue.equals("1")) {
            holder.mMoodValue.setText(context.getResources().getString(R.string.best_mood));
            holder.mMoodValue.setTextColor(ContextCompat.getColor(context, R.color.feeling_best));
            holder.mMoodFrequency.setTextColor(ContextCompat.getColor(context, R.color.feeling_best));
        }
        if (moodValue.equals("2")) {
            holder.mMoodValue.setText(context.getResources().getString(R.string.very_good_mood));
            holder.mMoodValue.setTextColor(ContextCompat.getColor(context, R.color.feeling_very_good));
            holder.mMoodFrequency.setTextColor(ContextCompat.getColor(context, R.color.feeling_very_good));
        }
        if (moodValue.equals("3")) {
            holder.mMoodValue.setText(context.getResources().getString(R.string.good_mood));
            holder.mMoodValue.setTextColor(ContextCompat.getColor(context, R.color.feeling_good));
            holder.mMoodFrequency.setTextColor(ContextCompat.getColor(context, R.color.feeling_good));
        }
        if (moodValue.equals("4")) {
            holder.mMoodValue.setText(context.getResources().getString(R.string.ok_mood));
            holder.mMoodValue.setTextColor(ContextCompat.getColor(context, R.color.feeling_ok));
            holder.mMoodFrequency.setTextColor(ContextCompat.getColor(context, R.color.feeling_ok));
        }
        if (moodValue.equals("5")) {
            holder.mMoodValue.setText(context.getResources().getString(R.string.bad_mood));
            holder.mMoodValue.setTextColor(ContextCompat.getColor(context, R.color.feeling_bad));
            holder.mMoodFrequency.setTextColor(ContextCompat.getColor(context, R.color.feeling_bad));
        }
        if (moodValue.equals("6")) {
            holder.mMoodValue.setText(context.getResources().getString(R.string.very_bad_mood));
            holder.mMoodValue.setTextColor(ContextCompat.getColor(context, R.color.feeling_very_bad));
            holder.mMoodFrequency.setTextColor(ContextCompat.getColor(context, R.color.feeling_very_bad));
        }
        if (moodValue.equals("7")) {
            holder.mMoodValue.setText(context.getResources().getString(R.string.worst_mood));
            holder.mMoodValue.setTextColor(ContextCompat.getColor(context, R.color.feeling_worst));
            holder.mMoodFrequency.setTextColor(ContextCompat.getColor(context, R.color.feeling_worst));
        }
        String numberOfTimes = Integer.parseInt(moodFrequency) > 1 ? " Times" : " Time";
        holder.mMoodFrequency.setText(moodFrequency + numberOfTimes);
    }

    @Override
    public int getItemCount() {
        return statsResponseModel.weekData.get(StatsFragment.getPosition()).size();
    }

    public class InsightAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView mMoodValue;
        TextView mMoodFrequency;
        View view;

        public InsightAdapterViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            mMoodValue = (TextView) view.findViewById(R.id.mood_value);
            mMoodFrequency = (TextView) view.findViewById(R.id.mood_frequency);
        }
    }
}
