package com.matejvasko.player;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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

    private OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            // TODO this method is called every time progress is made - overkill?
            durationCurrent.setText(Utils.millisecondsToString(i));
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
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);

            if (valueAnimator != null) {
                valueAnimator.cancel();
                valueAnimator = null;
            }

            final int progress = state != null ? (int) state.getPosition() : 0;
            setProgress(progress);

            if (state != null && state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                final int timeToEnd = (int) ((getMax() - progress) / state.getPlaybackSpeed());
                System.out.println("ASDASD");
                valueAnimator = ValueAnimator.ofInt(progress, getMax()).setDuration(timeToEnd);
                valueAnimator.setInterpolator(new LinearInterpolator());
                valueAnimator.addUpdateListener(this);
                valueAnimator.start();
            }

            Log.d(TAG, "onPlaybackStateChanged");
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);

            durationTotal.setText(Utils.millisecondsToString(metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)));

            final int max = metadata != null ? (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) : 0;
            setProgress(0);
            setMax(max);

            Log.d(TAG, "onMetadataChanged" + max);
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
