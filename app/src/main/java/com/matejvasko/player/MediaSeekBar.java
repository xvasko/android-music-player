package com.matejvasko.player;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.matejvasko.player.utils.SharedPref;
import com.matejvasko.player.utils.Utils;

import org.w3c.dom.Text;

import androidx.appcompat.widget.AppCompatSeekBar;

public class MediaSeekBar extends AppCompatSeekBar {

    private static final String TAG = "MediaSeekBar";

    TextView durationTotal;
    TextView durationCurrent;

    private MediaControllerCompat mediaController;
    private MediaControllerCallback mediaControllerCallback;

    private ValueAnimator valueAnimator;
    private boolean isTracking = false;

    private SharedPref sharedPref = SharedPref.getInstance();

    private OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            // TODO this method is called every time progress is made - overkill?
            if (durationCurrent != null) {
                durationCurrent.setText(Utils.millisecondsToString(i));
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isTracking = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mediaController.getTransportControls().seekTo(getProgress());
            isTracking = false;
        }
    };

    public MediaSeekBar(Context context) {
        super(context);
        super.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    public MediaSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    public MediaSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    public void setTextViews(TextView durationCurrent, TextView durationTotal) {
        this.durationCurrent = durationCurrent;
        this.durationTotal = durationTotal;
    }

    @Override
    public final void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        // Prohibit adding seek listeners to this subclass.
        throw new UnsupportedOperationException("Cannot add listeners to a MediaSeekBar");
    }

    public void setMediaController(final MediaControllerCompat mediaController) {
        if (this.mediaController != null) {
            this.mediaController.unregisterCallback(mediaControllerCallback);
            mediaControllerCallback = null;
        }
        this.mediaController = mediaController;
        mediaControllerCallback = new MediaControllerCallback();
        this.mediaController.registerCallback(mediaControllerCallback);
        mediaControllerCallback.onPlaybackStateChanged(mediaController.getPlaybackState());
        Log.d(TAG, "setMediaController");
    }

    public void disconnectMediaController() {
        if (mediaController != null) {
            mediaController.unregisterCallback(mediaControllerCallback);
            mediaController = null;
            mediaControllerCallback = null;
        }

        Log.d(TAG, "disconnectMediaController");
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback implements ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            if (valueAnimator != null) {
                valueAnimator.cancel();
                valueAnimator = null;
            }

            final int max = sharedPref.getSong() != null ? (int) sharedPref.getSong().duration : 0;
            final int progress = state != null ? (int) state.getPosition() : 0;

            // https://stackoverflow.com/questions/4348032/android-progressbar-does-not-update-progress-view-drawable
            post(new Runnable() {
                @Override
                public void run() {
                    setProgress(0);
                    setMax(max);
                    setProgress(progress);
                }
            });

            if (state != null && state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                final int timeToEnd = max - progress;
                System.out.println("max: " + max);
                System.out.println("progress: " + progress);
                valueAnimator = ValueAnimator.ofInt(progress, max).setDuration(timeToEnd);
                valueAnimator.setInterpolator(new LinearInterpolator());
                valueAnimator.addUpdateListener(this);
                valueAnimator.start();
            }

            Log.d(TAG, "onPlaybackStateChanged");
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            long duration = sharedPref.getSong().duration;

            if (durationCurrent != null) {
                durationTotal.setText(Utils.millisecondsToString(duration));
            }


            //setProgress(0);
            setMax((int) duration);

            Log.d(TAG, "onMetadataChanged: MediaControllerCallback");
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {

            if (isTracking) {
                valueAnimator.cancel();
                return;
            }

            final int animatedIntVale = (int) valueAnimator.getAnimatedValue();
            setProgress(animatedIntVale);
        }

    }

}
