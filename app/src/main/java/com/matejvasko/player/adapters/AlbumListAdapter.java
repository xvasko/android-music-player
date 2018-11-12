package com.matejvasko.player.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.matejvasko.player.Album;
import com.matejvasko.player.AlbumProvider;
import com.matejvasko.player.R;
import com.matejvasko.player.Song;
import com.matejvasko.player.viewmodels.NowPlaying;
import com.thoughtbot.expandablerecyclerview.ExpandableListUtils;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.util.List;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class AlbumListAdapter extends ExpandableRecyclerViewAdapter<AlbumListAdapter.AlbumViewHolder, AlbumListAdapter.SongViewHolder> {

    Context context;
    AlbumProvider albumProvider;

    public AlbumListAdapter(Context context, List<? extends ExpandableGroup> groups, AlbumProvider albumProvider) {
        super(groups);
        this.context = context;
        this.albumProvider = albumProvider;
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
        holder.bindTo(((Album)group));
    }

    private ExpandableGroup expanded;
    private int numOfSongs = 0;
    private int expandedIndex = -1;

    @Override
    public boolean onGroupClick(int flatPos) {

        // nothing is open
        if (expanded == null) {
            querySongs(flatPos);
            expanded = getGroups().get(flatPos);
            numOfSongs = expanded.getItems().size();
            toggleGroup(expanded);
            expandedIndex = flatPos;
            return false;
        } else {

            if (expandedIndex < flatPos) {
                flatPos -= numOfSongs;
            }

            // close open group
            if (expanded == getGroups().get(flatPos)) {
                toggleGroup(expanded);
                expanded = null;
                numOfSongs = 0;
                expandedIndex = -1;
                return true;
            // switch open group
            } else {
                toggleGroup(expanded);
                querySongs(flatPos);
                expanded = getGroups().get(flatPos);
                numOfSongs = expanded.getItems().size();
                toggleGroup(expanded);
                expandedIndex = flatPos;
                return true;
            }
        }
    }

    private void querySongs(int albumPos) {
        List<Album> albums = ((List<Album>) getGroups());
        Album album = albums.get(albumPos);
        List<Song> songs = albumProvider.getAlbumSongs(String.valueOf(album.id));
        Album album1 = new Album(album.id, album.title, songs);
        albums.set(albumPos, album1);
        refreshDataSet();
    }

    class AlbumViewHolder extends GroupViewHolder {

        Album album;

        final ImageView albumCover;
        final TextView albumTitle;
        final ImageView arrow;

        AlbumViewHolder(final View itemView) {
            super(itemView);
            albumCover = itemView.findViewById(R.id.album_cover_item);
            albumTitle = itemView.findViewById(R.id.album_title_item);
            arrow = itemView.findViewById(R.id.arrow);
        }

        void bindTo(final Album album) {
            this.album = album;
            albumTitle.setText(album.title);
        }

        @Override
        public void expand() {
            RotateAnimation rotate = new RotateAnimation(360, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            arrow.setAnimation(rotate);
        }

        @Override
        public void collapse() {
            RotateAnimation rotate = new RotateAnimation(180, 360, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            arrow.setAnimation(rotate);
        }

    }

     class SongViewHolder extends ChildViewHolder {

        private TextView songTitle;

        SongViewHolder(View itemView) {
            super(itemView);
            songTitle = itemView.findViewById(R.id.album_song_item_title);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, "LALA", Toast.LENGTH_SHORT).show();
                    NowPlaying.getNowPlaying().setValue(new Song.Builder(1).setIconUri(Uri.parse("")).setTitle("new song").setData("/storage/emulated/0/Music/Moja Rec - Offilne/02 Všetko ok feat. Majk Spirit.mp3").build());
                }
            });
        }

        void setSongTitle(Song song) {
            songTitle.setText(song.title);
        }

    }

    void refreshDataSet() {
        ExpandableListUtils.notifyGroupDataChanged(this);
        notifyDataSetChanged();
    }

}
