package com.matejvasko.player;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

public class Song implements Parcelable {

    public int id;
    public String data;
    public String title;
    public String artist;
    public Uri iconUri;
    public long duration;
    public int cursorPosition;

    public boolean isFromView() {
        return isFromView;
    }

    public void setFromView(boolean fromView) {
        isFromView = fromView;
    }

    private boolean isFromView;



    protected Song(Parcel in) {
        id = in.readInt();
        data = in.readString();
        title = in.readString();
        artist = in.readString();
        iconUri = in.readParcelable(Uri.class.getClassLoader());
        duration = in.readLong();
        cursorPosition = in.readInt();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(data);
        parcel.writeString(title);
        parcel.writeString(artist);
        parcel.writeParcelable(iconUri, i);
        parcel.writeLong(duration);
        parcel.writeInt(cursorPosition);
    }

    public static class Builder {
        private int id;
        private String data;
        private String title;
        private String artist;
        private Uri iconUri;
        private long duration;
        private int cursorPosition;

        public Builder(int id) {
            this.id = id;
        }

        public Builder setData(String data) {
            this.data = data;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setArtist(String artist) {
            this.artist = artist;
            return this;
        }

        public Builder setIconUri(Uri iconUri) {
            this.iconUri = iconUri;
            return this;
        }

        public Builder setDuration(long duration) {
            this.duration = duration;
            return this;
        }

        public Builder setcursorPosition(int cursorPosition) {
            this.cursorPosition = cursorPosition;
            return this;
        }

        public Song build() {
            Song song = new Song();
            song.id = this.id;
            song.data = this.data;
            song.title = this.title;
            song.artist = this.artist;
            song.iconUri = this.iconUri;
            song.duration = this.duration;
            song.cursorPosition = this.cursorPosition;

            return song;
        }
    }

    private Song() {

    }

    public static DiffUtil.ItemCallback<Song> DIFF_CALLBACK = new DiffUtil.ItemCallback<Song>() {
        @Override
        public boolean areItemsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
            return oldItem.equals(newItem);
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        Song song = (Song) obj;
        return song.id == this.id;
    }

}
