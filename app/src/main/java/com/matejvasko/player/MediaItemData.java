package com.matejvasko.player;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

public class MediaItemData {

    public String mediaId;
    public boolean isBrowseable;
    public String title;
    public String subtitle;
    public Uri albumArtUri;
    public long duration;
    public int cursorPosition;

    private MediaItemData(String mediaId, boolean isBrowseable, String title, String subtitle, Uri albumArtUri, long duration, int cursorPosition) {
        this.mediaId = mediaId;
        this.isBrowseable = isBrowseable;
        this.title = title;
        this.subtitle = subtitle;
        this.albumArtUri = albumArtUri;
        this.duration = duration;
        this.cursorPosition = cursorPosition;
    }

    public static class Builder {
        private String mediaId;
        private boolean isBrowseable;
        private String title;
        private String subtitle;
        private Uri albumArtUri;
        private long duration;
        private int cursorPosition;

        public Builder(String mediaId) {
            this.mediaId = mediaId;
        }

        public Builder setBrowseable(boolean isBrowseable) {
            this.isBrowseable = isBrowseable;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setSubtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public Builder setAlbumArtUri(Uri albumArtUri) {
            this.albumArtUri = albumArtUri;
            return this;
        }

        public Builder setDuration(long duration) {
            this.duration = duration;
            return this;
        }

        public Builder setCursorPosition(int cursorPosition) {
            this.cursorPosition = cursorPosition;
            return this;
        }

        public MediaItemData build() {
            return new MediaItemData(mediaId, isBrowseable, title, subtitle, albumArtUri, duration, cursorPosition);
        }

    }

    public static DiffUtil.ItemCallback<MediaItemData> DIFF_CALLBACK = new DiffUtil.ItemCallback<MediaItemData>() {
        @Override
        public boolean areItemsTheSame(@NonNull MediaItemData oldItem, @NonNull MediaItemData newItem) {
            return oldItem.mediaId.equals(newItem.mediaId);
        }

        @Override
        public boolean areContentsTheSame(@NonNull MediaItemData oldItem, @NonNull MediaItemData newItem) {
            return oldItem.equals(newItem);
        }
    };

}
