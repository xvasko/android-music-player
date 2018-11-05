package com.matejvasko.player.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.matejvasko.player.R;
import com.matejvasko.player.Song;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.util.List;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class AlbumListAdapter extends ExpandableRecyclerViewAdapter<AlbumListAdapter.AlbumViewHolder, AlbumListAdapter.SongViewHolder> {


    public AlbumListAdapter(List<? extends ExpandableGroup> groups) {
        super(groups);
    }

    @Override
    public AlbumViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.album_item, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public SongViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.album_song_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(SongViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        final Song song = (Song) group.getItems().get(childIndex);
        holder.setSongTitle(song);
    }

    @Override
    public void onBindGroupViewHolder(AlbumViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.setAlbumTitle(group);
    }

    class AlbumViewHolder extends GroupViewHolder {

        final TextView albumTitle;
        final ImageView arrow;

        public AlbumViewHolder(View itemView) {
            super(itemView);

            albumTitle = itemView.findViewById(R.id.album_title_item);
            arrow = itemView.findViewById(R.id.arrow);

        }

        void setAlbumTitle(ExpandableGroup group) {
            albumTitle.setText(group.getTitle());
        }

        @Override
        public void expand() {
            animateExpand();
        }

        @Override
        public void collapse() {
            animateCollapse();
        }

        private void animateExpand() {
            RotateAnimation rotate =
                    new RotateAnimation(360, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            arrow.setAnimation(rotate);
        }

        private void animateCollapse() {
            RotateAnimation rotate =
                    new RotateAnimation(180, 360, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            arrow.setAnimation(rotate);
        }

    }

    public class SongViewHolder extends ChildViewHolder {

        private TextView songTitle;

        public SongViewHolder(View itemView) {
            super(itemView);
            songTitle = itemView.findViewById(R.id.album_song_item_title);
        }

        public void setSongTitle(Song song) {
            songTitle.setText(song.title);
        }
    }

}
