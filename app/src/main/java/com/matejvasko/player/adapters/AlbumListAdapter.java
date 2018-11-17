package com.matejvasko.player.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.matejvasko.player.Album;
import com.matejvasko.player.MediaProvider;
import com.matejvasko.player.R;
import com.matejvasko.player.Song;
import com.matejvasko.player.utils.Utils;
import com.matejvasko.player.viewmodels.NowPlaying;
import com.thoughtbot.expandablerecyclerview.ExpandableListUtils;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.util.List;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class AlbumListAdapter extends ExpandableRecyclerViewAdapter<AlbumListAdapter.AlbumViewHolder, AlbumListAdapter.SongViewHolder> {

    private Context context;
    private MediaProvider mediaProvider;

    public AlbumListAdapter(Context context) {
        super(MediaProvider.getInstance().getAlbums());
        this.context = context;
        this.mediaProvider = MediaProvider.getInstance();
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
    public void onBindGroupViewHolder(AlbumViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.bindTo(((Album)group));
    }

    @Override
    public void onBindChildViewHolder(SongViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        Song song = (Song) group.getItems().get(childIndex);
        song.setFromAlbumTab(true);
        holder.bindTo(song, childIndex);
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
        Album clickedAlbum = albums.get(albumPos);
        List<Song> songs = mediaProvider.getAlbumSongs(String.valueOf(clickedAlbum.id));
        Album albumWithSongs = new Album(clickedAlbum.id, clickedAlbum.title, songs, clickedAlbum.artist);
        albums.set(albumPos, albumWithSongs);
        refreshDataSet();
    }

    class AlbumViewHolder extends GroupViewHolder {

        Album album;

        final ImageView albumCover;
        final TextView albumTitle;
        final TextView albumArtist;
        final ImageView arrow;

        AlbumViewHolder(final View itemView) {
            super(itemView);
            albumCover  = itemView.findViewById(R.id.album_cover_item);
            albumTitle  = itemView.findViewById(R.id.album_title_item);
            albumArtist = itemView.findViewById(R.id.album_artist_item);
            arrow       = itemView.findViewById(R.id.arrow);
        }

        void bindTo(final Album album) {
            this.album = album;
            albumTitle.setText(album.title);
            albumArtist.setText(album.artist);
            Bitmap iconBitmap = Utils.getBitmapFromMediaStore((Uri.parse("content://media/external/audio/albumart/" + album.id)));
            if (iconBitmap == null) {
                albumCover.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_audiotrack_black_24dp));
            } else {
                albumCover.setImageBitmap(iconBitmap);
            }
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

        Song song;

        private TextView index;
        private TextView songTitle;
        private TextView duration;

        SongViewHolder(View itemView) {
            super(itemView);
            index = itemView.findViewById(R.id.album_song_item_index);
            songTitle = itemView.findViewById(R.id.album_song_item_title);
            duration = itemView.findViewById(R.id.album_song_item_duration);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NowPlaying.getNowPlaying().setValue(song);
                }
            });
        }

        void bindTo(Song song, int index) {
            this.song = song;
            String indexString;
            if (index < 9) {
                indexString = index + 1 + "&#160;&#160;";
            } else {
                indexString = index + 1 + "";
            }
            this.index.setText(Html.fromHtml(indexString));
            songTitle.setText(song.title);
            duration.setText(Utils.millisecondsToString(song.duration));
        }

    }

    void refreshDataSet() {
        ExpandableListUtils.notifyGroupDataChanged(this);
        notifyDataSetChanged();
    }

}
