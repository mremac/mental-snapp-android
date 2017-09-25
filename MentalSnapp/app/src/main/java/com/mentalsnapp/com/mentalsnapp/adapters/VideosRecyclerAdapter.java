package com.mentalsnapp.com.mentalsnapp.adapters;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.fragments.VideosFragment;
import com.mentalsnapp.com.mentalsnapp.models.Videos;
import com.mentalsnapp.com.mentalsnapp.utils.ChangeTextColor;
import com.mentalsnapp.com.mentalsnapp.utils.GlideUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by gchandra on 19/1/17.
 */
public class VideosRecyclerAdapter extends RecyclerView.Adapter<VideosRecyclerAdapter.VideosViewHolder> {
    private ArrayList<Videos> mVideosList;
    private Fragment fragment;
    private int mPosition;

    public VideosRecyclerAdapter(ArrayList mVideosList, Fragment fragment) {
        this.mVideosList = mVideosList;
        this.fragment = fragment;
    }

    @Override
    public VideosViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.videos_list_item, parent, false);
        return new VideosViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final VideosViewHolder holder, int position) {
        DisplayMetrics displayMetrics = fragment.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(width, width - 48);
        holder.mVideoThumbnail.setLayoutParams(imageParams);
        GlideUtils.loadImage(fragment.getContext(), mVideosList.get(position).coverURL, holder.mVideoThumbnail, R.drawable.video_placeholder);
        if (mVideosList.get(position).moodValue.equalsIgnoreCase("1")) {
            holder.mVideoThumbnail.setBackgroundColor(ContextCompat.getColor(fragment.getContext(), R.color.feeling_best));
        } else if (mVideosList.get(position).moodValue.equalsIgnoreCase("2")) {
            holder.mVideoThumbnail.setBackgroundColor(ContextCompat.getColor(fragment.getContext(), R.color.feeling_very_good));
        } else if (mVideosList.get(position).moodValue.equalsIgnoreCase("3")) {
            holder.mVideoThumbnail.setBackgroundColor(ContextCompat.getColor(fragment.getContext(), R.color.feeling_good));
        } else if (mVideosList.get(position).moodValue.equalsIgnoreCase("4")) {
            holder.mVideoThumbnail.setBackgroundColor(ContextCompat.getColor(fragment.getContext(), R.color.feeling_ok));
        } else if (mVideosList.get(position).moodValue.equalsIgnoreCase("5")) {
            holder.mVideoThumbnail.setBackgroundColor(ContextCompat.getColor(fragment.getContext(), R.color.feeling_bad));
        } else if (mVideosList.get(position).moodValue.equalsIgnoreCase("6")) {
            holder.mVideoThumbnail.setBackgroundColor(ContextCompat.getColor(fragment.getContext(), R.color.feeling_very_bad));
        } else if (mVideosList.get(position).moodValue.equalsIgnoreCase("7")) {
            holder.mVideoThumbnail.setBackgroundColor(ContextCompat.getColor(fragment.getContext(), R.color.feeling_worst));
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(mVideosList.get(position).createdAt) * 1000);
        String minutes = (calendar.get(Calendar.MINUTE) < 10) ? ("0" + String.valueOf(calendar.get(Calendar.MINUTE))) : String.valueOf(calendar.get(Calendar.MINUTE));
        int hours;
        if (calendar.get(Calendar.HOUR_OF_DAY) >= 12) {
            hours = calendar.get(Calendar.HOUR_OF_DAY) - 12;
            holder.mTimeText.setText(calendar.get(Calendar.DAY_OF_MONTH) + " " +
                    new SimpleDateFormat("MMM").format(calendar.getTime()) + ", " + hours + ":" + minutes + " PM");
        } else {
            hours = calendar.get(Calendar.HOUR_OF_DAY);
            holder.mTimeText.setText(calendar.get(Calendar.DAY_OF_MONTH) + " " +
                    new SimpleDateFormat("MMM").format(calendar.getTime()) + ", " + hours + ":" + minutes + " AM");
        }
        if (mVideosList.get(position).feelings.length > 0) {
            if (TextUtils.isEmpty(mVideosList.get(position).tags)) {
                SpannableString feeling = new SpannableString("Feeling " + mVideosList.get(position).feelings[0].name);
                feeling.setSpan(new StyleSpan(Typeface.BOLD), 8, feeling.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                holder.mFeelingName.setText(feeling);
            } else {
                SpannableString tagsString = null;
                String tags = mVideosList.get(position).tags;
                int count = 0;
                if (tags.contains("#")) {
                    for (int i = 0; i < tags.length(); i++) {
                        if (tags.charAt(i) == '#') {
                            count++;
                        }
                    }
                    if (count != 0) {
                        ArrayList<String> hashtags = new ArrayList<>();
                        for (int i = 0; i < count; i++) {
                            hashtags.add("#");
                        }
                        tagsString = ChangeTextColor.setColor(fragment.getActivity(), tags, hashtags);
                    }
                }
                SpannableString feeling;
                if (tagsString != null) {
                    feeling = new SpannableString("Feeling " + mVideosList.get(position).feelings[0].name);
                    feeling.setSpan(new StyleSpan(Typeface.BOLD), 8, 8 + mVideosList.get(position).feelings[0].name.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    holder.mFeelingName.setText(TextUtils.concat(feeling, ", ", tagsString));
                } else {
                    feeling = new SpannableString("Feeling " + mVideosList.get(position).feelings[0].name +
                            ", " + mVideosList.get(position).tags);
                    feeling.setSpan(new StyleSpan(Typeface.BOLD), 8, 8 + mVideosList.get(position).feelings[0].name.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    holder.mFeelingName.setText(feeling);
                }
            }
            holder.mFeelingColor.setBackgroundColor(Color.rgb(mVideosList.get(position).feelings[0].red,
                    mVideosList.get(position).feelings[0].green, mVideosList.get(position).feelings[0].blue));
        } else {
            holder.mFeelingName.setText("");
            holder.mFeelingColor.setBackgroundColor(Color.TRANSPARENT);
        }
//        mPosition = holder.getAdapterPosition();
        holder.mDeleteVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(holder.getAdapterPosition());
            }
        });
        holder.mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((VideosFragment) fragment).playVideo(holder.getAdapterPosition());
            }
        });
        holder.mDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((VideosFragment) fragment).setVideoDetails(mVideosList.get(holder.getAdapterPosition()).videoURL,
                        mVideosList.get(holder.getAdapterPosition()).name);
                ((VideosFragment) fragment).downloadVideo();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mVideosList.size();
    }

    private void showDialog(int adapterPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext(), R.style.AlertDialogTheme);
        builder.setTitle("Delete Video").setMessage("Are you sure you want to delete the video?")
                .setPositiveButton("Yes", deleteConfirmDialogClickListener)
                .setNegativeButton("No", deleteConfirmDialogClickListener).show();
        mPosition = adapterPosition;
    }

    DialogInterface.OnClickListener deleteConfirmDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    ((VideosFragment) fragment).deleteVideo(mVideosList.get(mPosition).id, mVideosList.get(mPosition).videoURL,
                            mVideosList.get(mPosition).coverURL);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };


    public class VideosViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView mVideoThumbnail;
        TextView mFeelingName;
        ImageView mDownloadButton;
        ImageView mDeleteVideo;
        ImageView mPlayButton;
        TextView mTimeText;
        View mFeelingColor;

        public VideosViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            mVideoThumbnail = (ImageView) view.findViewById(R.id.video_thumbnial_image);
            mFeelingName = (TextView) view.findViewById(R.id.feeling_name);
            mDownloadButton = (ImageView) view.findViewById(R.id.download_btn);
            mDeleteVideo = (ImageView) view.findViewById(R.id.delete_video);
            mPlayButton = (ImageView) view.findViewById(R.id.play_btn);
            mFeelingColor = (View) view.findViewById(R.id.feeling_color);
            mTimeText = (TextView) view.findViewById(R.id.time_text);
        }
    }
}
