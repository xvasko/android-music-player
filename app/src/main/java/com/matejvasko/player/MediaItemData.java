package com.matejvasko.player;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

public class MediaItemData {

    public String mediaId;
    public String title;
    public String subtitle;
    public Uri albumArtUri;
    public boolean isBrowseable;

    public MediaItemData(String mediaId, String title, String subtitle, Uri albumArtUri, boolean isBrowseable) {
        this.mediaId = mediaId;
        this.title = title;
        this.subtitle = subtitle;
        this.albumArtUri = albumArtUri;
        this.isBrowseable = isBrowseable;
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
