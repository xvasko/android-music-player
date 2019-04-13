package com.matejvasko.player.databinding;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.matejvasko.player.App;
import com.matejvasko.player.R;

import androidx.databinding.BindingAdapter;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

public class GlideBindingAdapters {

    @BindingAdapter("image")
    public static void setImage(ImageView view, String image) {

        Context context = view.getContext();

        final CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(App.getAppContext());
        circularProgressDrawable.setStrokeWidth(10f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.setStartEndTrim(0.5f, 1f);
        circularProgressDrawable.start();

        RequestOptions options = new RequestOptions()
                .placeholder(circularProgressDrawable)
                .error(circularProgressDrawable);

        if (image != null && image.equals("default")) {
            Glide.with(context)
                    .setDefaultRequestOptions(options)
                    .load(R.drawable.ic_perm_identity_black_24dp)
                    .into(view);
        } else {
            Glide.with(context)
                    .setDefaultRequestOptions(options)
                    .load(image)
                    .into(view);
        }

    }

}
