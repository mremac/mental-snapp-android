package com.mentalsnapp.com.mentalsnapp.utils;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mentalsnapp.com.mentalsnapp.views.CircleTransform;

/**
 * Created by ssaxena on 26/11/15.
 */
public class GlideUtils {

    public static void loadImage(Activity activity, String url,
                                 ImageView imageView, int placeholder) {
        if (activity != null && !activity.isFinishing()) {
            Glide.with(activity).load(url).placeholder(placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
        }
    }

    public static void loadImageAsBitmap(Activity activity, String url,
                                         ImageView imageView, int placeholder) {
        if (activity != null && !activity.isFinishing()) {
            Glide.with(activity).load(url).asBitmap().placeholder(placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
        }
    }

    public static void loadImage(Fragment fragment, String url,
                                 ImageView imageView, int placeholder) {
        if (fragment != null && !fragment.isRemoving()) {
            Glide.with(fragment).load(url).placeholder(placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
        }
    }

    public static void loadImage(Context context, String url,
                                 ImageView imageView, int placeholder) {
        if (context != null) {
            Glide.with(context).load(url).placeholder(placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
        }
    }

    public static void loadImageWithoutDiskCacheStrategy(Fragment fragment, String url,
                                                         ImageView imageView) {
        if (fragment != null) {
            Glide.with(fragment).load(url)
                    .into(imageView);
        }
    }

    public static void loadCircularImage(Fragment fragment, String url,
                                         ImageView imageView, int placeholder) {
        if (fragment != null && !fragment.isRemoving()) {
            Glide.with(fragment).load(url).placeholder(placeholder)
                    .transform(new CircleTransform(fragment.getActivity()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
        }
    }

    public static void loadCircularImage(Activity activity, String url,
                                         ImageView imageView, int placeholder) {
        if (activity != null && !activity.isFinishing()) {
            Glide.with(activity).load(url).placeholder(placeholder).centerCrop()
                    .transform(new CircleTransform(activity))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
        }
    }

}
