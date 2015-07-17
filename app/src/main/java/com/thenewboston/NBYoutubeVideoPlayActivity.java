package com.thenewboston;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import com.google.android.youtube.player.YouTubePlayerView;
import com.thenewboston.youtube.NBYouTubeFailureRecoveryActivity;




public class NBYoutubeVideoPlayActivity extends NBYouTubeFailureRecoveryActivity {

    private String GOOGLE_DEVELOPMENT_API_KEY = "AIzaSyAXQoG3RcJ_AHIjDXMPvLMb4hxnS5lGjzo";

    private String video_id = "";

    private Handler hideCloseButtonTimer;

    private Runnable hideCloseButtonRunnable;

    private YouTubePlayer videoPlayer;

    private Button closeButton;


    private PlaybackEventListener playbackEventListener = new PlaybackEventListener() {
        @Override
        public void onPlaying() {
            if (closeButton.getVisibility() == View.VISIBLE) {
                hideCloseButtonTimer.removeCallbacks(hideCloseButtonRunnable);
                hideCloseButtonTimer.postDelayed(hideCloseButtonRunnable, 5000);
            }
        }

        @Override
        public void onPaused() {
            showCloseButton();
        }

        @Override
        public void onStopped() {
            showCloseButton();
        }

        @Override
        public void onBuffering(boolean b) {

        }

        @Override
        public void onSeekTo(int i) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_video);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            video_id = extras.getString("video_id");
        }

        //Init Youtube Video Player
        YouTubePlayerView tPlayerView = (YouTubePlayerView) findViewById(R.id.youtube_player_view);
        tPlayerView.initialize(this.GOOGLE_DEVELOPMENT_API_KEY, this);

        closeButton = (Button)findViewById(R.id.close_button);

        //Init Hide Close Button
        hideCloseButtonTimer = new Handler();
        hideCloseButtonRunnable = new Runnable() {
            @Override
            public void run() {
                hideCloseButton();
            }
        };

    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        videoPlayer = player;
        if (!wasRestored) {
            videoPlayer.cueVideo(video_id);
            videoPlayer.setPlaybackEventListener(playbackEventListener);
        }
    }

    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return (YouTubePlayerView) findViewById(R.id.youtube_player_view);
    }

    public void goPrevActivity(View view) {
        super.onBackPressed();
    }

    private void showCloseButton() {
        closeButton.setVisibility(View.VISIBLE);
    }

    private void hideCloseButton() {
        closeButton.setVisibility(View.GONE);
    }

    public void clickPlayer(View view) {
        if (closeButton.getVisibility() != View.VISIBLE) {
            showCloseButton();
            //Hide Close Button After 5 seconds
            hideCloseButtonTimer.removeCallbacks(hideCloseButtonRunnable);
            hideCloseButtonTimer.postDelayed(hideCloseButtonRunnable, 5000);
        }
    }
}
