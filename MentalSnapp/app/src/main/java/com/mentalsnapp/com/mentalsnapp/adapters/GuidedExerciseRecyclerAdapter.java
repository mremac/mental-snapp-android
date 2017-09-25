package com.mentalsnapp.com.mentalsnapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.activities.MainActivity;
import com.mentalsnapp.com.mentalsnapp.activities.SelectedExerciseDetailsActivity;
import com.mentalsnapp.com.mentalsnapp.models.GuidedExercise;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;
import com.mentalsnapp.com.mentalsnapp.utils.GlideUtils;

import java.util.ArrayList;

/**
 * Created by gchandra on 27/12/16.
 */
public class GuidedExerciseRecyclerAdapter extends RecyclerView.Adapter<GuidedExerciseRecyclerAdapter.GuidedExcerciseViewHolder> {

    private ArrayList<GuidedExercise> mList;
    private Context mContext;
    private String categoryName;

    @Override
    public GuidedExcerciseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.guided_excercise_list_item, parent, false);
        return new GuidedExcerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GuidedExcerciseViewHolder holder, final int position) {
        GuidedExercise itemList = mList.get(position);
        holder.mTitle.setText(itemList.name);
        holder.mDescription.setText(itemList.description);
        GlideUtils.loadImage(mContext, itemList.coverUrl, holder.mExcerciseImage, R.drawable.sub_placeholder);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) mContext).openDetailsScreen(categoryName, mList.get(position).id);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public GuidedExerciseRecyclerAdapter(ArrayList<GuidedExercise> mList, Context mContext, String categoryName) {
        this.mList = mList;
        this.mContext = mContext;
        this.categoryName = categoryName;
    }

    public class GuidedExcerciseViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView mTitle;
        TextView mDescription;
        ImageView mExcerciseImage;

        public GuidedExcerciseViewHolder(View view) {
            super(view);
            this.view = view;
            mTitle = (TextView) view.findViewById(R.id.item_title);
            mDescription = (TextView) view.findViewById(R.id.item_description);
            mExcerciseImage = (ImageView) view.findViewById(R.id.item_image);
        }
    }
}
