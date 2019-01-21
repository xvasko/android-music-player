package com.matejvasko.player.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

public class Album implements Parcelable {

    public String id;
    public String title;
    public String artist;
    public Uri albumArtUri;
    public int cursorPosition;

    public Album(String id, String title, String artist, Uri albumArtUri, int cursorPosition) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.albumArtUri = albumArtUri;
        this.cursorPosition = cursorPosition;
    }

    public static DiffUtil.ItemCallback<Album> DIFF_CALLBACK = new DiffUtil.ItemCallback<Album>() {
        @Override
        public boolean areItemsTheSame(@NonNull Album oldItem, @NonNull Album newItem) {
            return oldItem.id.equals(newItem.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Album oldItem, @NonNull Album newItem) {
            return oldItem.equals(newItem);
        }
    };

    protected Album(Parcel in) {
        id = in.readString();
        title = in.readString();
        artist = in.readString();
        albumArtUri = in.readParcelable(Uri.class.getClassLoader());
        cursorPosition = in.readInt();
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeParcelable(albumArtUri, flags);
        dest.writeInt(cursorPosition);
    }
}
