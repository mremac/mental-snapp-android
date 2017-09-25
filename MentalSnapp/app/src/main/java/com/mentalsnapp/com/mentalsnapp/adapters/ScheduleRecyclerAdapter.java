package com.mentalsnapp.com.mentalsnapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.activities.QueuedExerciseActivity;
import com.mentalsnapp.com.mentalsnapp.network.response.ScheduleExerciseResponse;
import com.mentalsnapp.com.mentalsnapp.utils.GlideUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by gchandra on 6/2/17.
 */
public class ScheduleRecyclerAdapter extends RecyclerView.Adapter<ScheduleRecyclerAdapter.ScheduleRecyclerAdapterViewHolder> {

    private ArrayList<ScheduleExerciseResponse> mScheduleList;
    private Context context;

    public ScheduleRecyclerAdapter(ArrayList<ScheduleExerciseResponse> mScheduleExercises, Context context) {
        mScheduleList = mScheduleExercises;
        this.context = context;
    }

    @Override
    public ScheduleRecyclerAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.schedule_list_item, null);
        return new ScheduleRecyclerAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ScheduleRecyclerAdapterViewHolder holder, int position) {
        ScheduleExerciseResponse item = mScheduleList.get(position);
        holder.mScheduleName.setText(item.exercise.name);
        holder.mScheduleDescription.setText(item.exercise.description);
        GlideUtils.loadImage(context, item.exercise.coverUrl, holder.mScheduleImage, R.drawable.sub_placeholder);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(item.executeAt) * 1000);
        String minutes = (calendar.get(Calendar.MINUTE) < 10) ? ("0" + String.valueOf(calendar.get(Calendar.MINUTE))) : String.valueOf(calendar.get(Calendar.MINUTE));
        int hours;
        if (calendar.get(Calendar.HOUR_OF_DAY) >= 12) {
            if (calendar.get(Calendar.HOUR_OF_DAY) != 12) {
                hours = calendar.get(Calendar.HOUR_OF_DAY) - 12;
            } else {
                hours = calendar.get(Calendar.HOUR_OF_DAY);
            }
            holder.mScheduleDate.setText(calendar.get(Calendar.DAY_OF_MONTH) + " " +
                    new SimpleDateFormat("MMM").format(calendar.getTime()) + ", " + hours + ":" + minutes + " PM");
        } else {
            hours = calendar.get(Calendar.HOUR_OF_DAY);
            holder.mScheduleDate.setText(calendar.get(Calendar.DAY_OF_MONTH) + " " +
                    new SimpleDateFormat("MMM").format(calendar.getTime()) + ", " + hours + ":" + minutes + " AM");
        }
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((QueuedExerciseActivity) context).setSelectedExercise(holder.getAdapterPosition());
                ((QueuedExerciseActivity) context).openDatePicker();
            }
        });

        holder.mCrossImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((QueuedExerciseActivity) context).deleteExercise(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mScheduleList.size();
    }

    public class ScheduleRecyclerAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView mScheduleName;
        TextView mScheduleDescription;
        TextView mScheduleDate;
        ImageView mScheduleImage;
        ImageView mCrossImage;
        View view;

        public ScheduleRecyclerAdapterViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            mScheduleName = (TextView) view.findViewById(R.id.item_title);
            mScheduleDescription = (TextView) view.findViewById(R.id.item_description);
            mScheduleDate = (TextView) view.findViewById(R.id.item_date);
            mScheduleImage = (ImageView) view.findViewById(R.id.item_image);
            mCrossImage = (ImageView) view.findViewById(R.id.cross_button);
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });
        }
    }
}
