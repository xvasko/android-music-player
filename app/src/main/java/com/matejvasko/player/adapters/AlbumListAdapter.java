package com.matejvasko.player.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Html;
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
import com.matejvasko.player.utils.Utils;
import com.matejvasko.player.viewmodels.NowPlaying;
import com.thoughtbot.expandablerecyclerview.ExpandableListUtils;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        holder.bindTo(song, childIndex);
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
        Album album1 = new Album(album.id, album.title, songs, album.artist);
        albums.set(albumPos, album1);
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
            arrow = itemView.findViewById(R.id.arrow);
        }

        void bindTo(final Album album) {
            this.album = album;
            albumTitle.setText(album.title);
            albumArtist.setText(album.artist);
            Bitmap iconBitmap = getBitmapFromMediaStore(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), album.id));
            if (iconBitmap == null) {
                albumCover.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_launcher_background));
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

    private Map<Uri, Bitmap> map = new HashMap<>();

    private Bitmap getBitmapFromMediaStore(Uri iconUri) {
        if (map.containsKey(iconUri)) {
            return map.get(iconUri);
        } else {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), iconUri);
                map.put(iconUri, bitmap);
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                map.put(iconUri, null);
                return null;
            }
        }
    }

}
