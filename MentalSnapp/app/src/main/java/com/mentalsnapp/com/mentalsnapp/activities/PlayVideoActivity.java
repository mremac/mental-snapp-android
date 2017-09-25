package com.mentalsnapp.com.mentalsnapp.activities;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.Display;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;
import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.utils.Constants;

/**
 * Created by gchandra on 19/1/17.
 */
public class PlayVideoActivity extends BaseActivity {

    private SimpleExoPlayer mPlayer;
    private SimpleExoPlayerView mExoPlayerView;
    private boolean mIsEnded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
        registerViews();
        initialiseExoPlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayer.release();
    }

    private void registerViews() {
        mExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
    }

    private void initialiseExoPlayer() {
        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "Mentalsnapp"), transferListener);
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        mPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        mExoPlayerView.setPlayer(mPlayer);

        // This is the MediaSource representing the media to be played.
        Uri videoUri = Uri.parse(getIntent().getStringExtra(Constants.PLAY_VIDEO_URL));
        MediaSource videoSource = new ExtractorMediaSource(videoUri,
                dataSourceFactory, extractorsFactory, null, null);
// Prepare the player with the source.
        mPlayer.prepare(videoSource);

        mPlayer.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                if (!isLoading) {
                    if (!mIsEnded) {
                        if (Build.VERSION.SDK_INT >= 20) {
                            DisplayManager dm = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
                            for (Display display : dm.getDisplays()) {
                                if (display.getState() != Display.STATE_OFF) {
                                    mPlayer.setPlayWhenReady(true);
                                }
                            }
                        } else {
                            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                            if (powerManager.isScreenOn()) {
                                mPlayer.setPlayWhenReady(true);
                            }
                        }

                    }
                }
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == ExoPlayer.STATE_ENDED) {
                    mPlayer.seekTo(0);
                    mPlayer.setPlayWhenReady(false);
                    mIsEnded = true;
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity() {

            }
        });
    }

    TransferListener transferListener = new TransferListener() {
        @Override
        public void onTransferStart(Object source, DataSpec dataSpec) {

        }

        @Override
        public void onBytesTransferred(Object source, int bytesTransferred) {

        }

        @Override
        public void onTransferEnd(Object source) {

        }
    };

}
