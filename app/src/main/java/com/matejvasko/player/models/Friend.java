package com.matejvasko.player.models;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

public class Friend {

    private String name, email, thumb_image;

    public Friend() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public static DiffUtil.ItemCallback<Friend> DIFF_CALLBACK = new DiffUtil.ItemCallback<Friend>() {
        @Override
        public boolean areItemsTheSame(@NonNull Friend oldItem, @NonNull Friend newItem) {
            return oldItem.name.equals(newItem.name); // TODO change name to id
        }

        @Override
        public boolean areContentsTheSame(@NonNull Friend oldItem, @NonNull Friend newItem) {
            return oldItem.equals(newItem);
        }
    };

}
