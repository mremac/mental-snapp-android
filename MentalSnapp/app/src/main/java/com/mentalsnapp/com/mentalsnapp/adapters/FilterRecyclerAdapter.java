package com.mentalsnapp.com.mentalsnapp.adapters;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.fragments.VideosFragment;
import com.mentalsnapp.com.mentalsnapp.models.GuidedExercise;

import java.util.ArrayList;

/**
 * Created by gchandra on 19/1/17.
 */
public class FilterRecyclerAdapter extends RecyclerView.Adapter<FilterRecyclerAdapter.FilterViewHolder> {

    private Fragment fragment;
    private ArrayList<GuidedExercise> mFilterList;
    private PopupWindow filterPopup;
    private int mSelectedPosition = -1;
    private int mFilterPostion;

    public FilterRecyclerAdapter(Fragment fragment, ArrayList<GuidedExercise> filterList, PopupWindow filterPopup, int mFilterPostion) {
        this.fragment = fragment;
        mFilterList = filterList;
        this.filterPopup = filterPopup;
        this.mFilterPostion = mFilterPostion;
    }

    @Override
    public FilterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(fragment.getContext()).inflate(R.layout.filter_list_item, null);
        return new FilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FilterViewHolder holder, int position) {
        GuidedExercise filterItem = mFilterList.get(position);
        holder.mFilterName.setText(filterItem.name);
        if (position == mFilterPostion) {
            holder.mCheckMark.setVisibility(View.VISIBLE);
        } else {
            holder.mCheckMark.setVisibility(View.INVISIBLE);
        }
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.getAdapterPosition() != mSelectedPosition) {
                    notifyItemChanged(mSelectedPosition);
                }
                ((VideosFragment) fragment).setFilterPosition(holder.getAdapterPosition());
                mSelectedPosition = holder.getAdapterPosition();
                if (holder.getAdapterPosition() == 0) {
                    ((VideosFragment) fragment).getListByFilter(1);
                } else if (holder.getAdapterPosition() == 1) {
                    ((VideosFragment) fragment).mCurrentPageFilter = 1;
                    ((VideosFragment) fragment).getListByFilter(0);
                } else {
                    ((VideosFragment) fragment).mCurrentPageFilter = 1;
                    ((VideosFragment) fragment).getListByFilter(mFilterList.get(holder.getAdapterPosition()).id);
                }
                filterPopup.dismiss();
            }
        });
        if (mSelectedPosition != -1) {
            if (position == mSelectedPosition) {
                holder.mCheckMark.setVisibility(View.VISIBLE);
            } else {
                holder.mCheckMark.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mFilterList.size();
    }

    public class FilterViewHolder extends RecyclerView.ViewHolder {

        View view;
        TextView mFilterName;
        ImageView mCheckMark;

        public FilterViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            mFilterName = (TextView) view.findViewById(R.id.filter_name);
            mCheckMark = (ImageView) view.findViewById(R.id.check_mark);
        }
    }
}
