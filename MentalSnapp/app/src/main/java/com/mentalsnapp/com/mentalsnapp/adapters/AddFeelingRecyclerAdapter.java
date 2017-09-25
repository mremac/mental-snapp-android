package com.mentalsnapp.com.mentalsnapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.activities.AddFeelingActivity;
import com.mentalsnapp.com.mentalsnapp.models.AddFeeling;

import java.util.ArrayList;

/**
 * Created by gchandra on 13/1/17.
 */
public class AddFeelingRecyclerAdapter extends RecyclerView.Adapter<AddFeelingRecyclerAdapter.AddFeelingViewHolder> {

    private ArrayList<AddFeeling> mFeelingList;
    private Context mContext;
    private int mSelectedPosition = -1;

    public AddFeelingRecyclerAdapter(ArrayList<AddFeeling> mFeelingList, Context mContext) {
        this.mFeelingList = mFeelingList;
        this.mContext = mContext;
    }

    @Override
    public AddFeelingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_feeling_list_item, parent, false);
        return new AddFeelingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AddFeelingViewHolder holder, int position) {
        AddFeeling item = mFeelingList.get(position);
        holder.mFeelingName.setText(item.name);
        if (!item.isSubFeeling) {
            holder.mFeelingColor.setBackgroundColor(Color.TRANSPARENT);
            holder.view.setBackgroundColor(Color.rgb(item.red, item.green, item.blue));
        } else {
            holder.view.setBackgroundColor(Color.TRANSPARENT);
            holder.mFeelingColor.setBackgroundColor(Color.rgb(item.red, item.green, item.blue));
        }
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFeelingList.get(holder.getAdapterPosition()).isSubFeeling) {
                    if (holder.getAdapterPosition() != mSelectedPosition) {
                        notifyItemChanged(mSelectedPosition);
                    }
                    mSelectedPosition = holder.getAdapterPosition();
                    holder.view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.orange_light));
                    holder.mCheckMark.setVisibility(View.VISIBLE);
                    ((AddFeelingActivity) mContext).setmSelectedFeelingPosition(holder.getAdapterPosition());
                }
            }
        });
        if (mSelectedPosition != -1) {
            if (position == mSelectedPosition) {
                holder.view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.orange_light));
                holder.mCheckMark.setVisibility(View.VISIBLE);
            } else {
                holder.view.setBackgroundColor(Color.TRANSPARENT);
                holder.mCheckMark.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mFeelingList.size();
    }

    public class AddFeelingViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView mFeelingName;
        ImageView mCheckMark;
        View mFeelingColor;

        public AddFeelingViewHolder(View view) {
            super(view);
            this.view = view;
            mFeelingName = (TextView) view.findViewById(R.id.feeling_name);
            mCheckMark = (ImageView) view.findViewById(R.id.check_mark);
            mFeelingColor = (View) view.findViewById(R.id.feeling_color);
        }
    }
}
