package com.matejvasko.player.databinding;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.matejvasko.player.R;

import androidx.databinding.BindingAdapter;

public class GlideBindingAdapters {

    @BindingAdapter("image")
    public static void setImage(ImageView view, String image) {

        Context context = view.getContext();

        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_perm_identity_black_24dp)
                .error(R.drawable.ic_perm_identity_black_24dp);

        Glide.with(context)
                .setDefaultRequestOptions(options)
                .load(image)
                .into(view);

    }

}
