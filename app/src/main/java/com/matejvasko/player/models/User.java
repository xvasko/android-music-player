package com.matejvasko.player.models;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

public class User {

    private String uid;
    private String currentSong;
    private String deviceToken;
    private String email;
    private String image;
    private String name;
    private Boolean online;
    private String thumbImage;

    public User() {

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(String currentSong) {
        this.currentSong = currentSong;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public String getThumbImage() {
        return thumbImage;
    }

    public void setThumbImage(String thumbImage) {
        this.thumbImage = thumbImage;
    }

    public static DiffUtil.ItemCallback<User> DIFF_CALLBACK = new DiffUtil.ItemCallback<User>() {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.uid.equals(newItem.uid);
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.equals(newItem);
        }
    };

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", currentSong='" + currentSong + '\'' +
                ", deviceToken='" + deviceToken + '\'' +
                ", email='" + email + '\'' +
                ", image='" + image + '\'' +
                ", name='" + name + '\'' +
                ", online=" + online +
                ", thumbImage='" + thumbImage + '\'' +
                '}';
    }
}
