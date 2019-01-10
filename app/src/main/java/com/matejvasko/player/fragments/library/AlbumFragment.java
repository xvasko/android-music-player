package com.matejvasko.player.fragments.library;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.matejvasko.player.App;
import com.matejvasko.player.MediaItemData;
import com.matejvasko.player.MediaProvider;
import com.matejvasko.player.R;
import com.matejvasko.player.adapters.AlbumSongsListAdapter;
import com.matejvasko.player.utils.Utils;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumFragment extends Fragment {

    private static final String TAG = "AlbumFragment";

    public AlbumFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        final View view = inflater.inflate(R.layout.fragment_album, container, false);

        Bundle bundle = getArguments();

        MediaProvider mediaProvider = MediaProvider.getInstance();
        String albumId = bundle.getString("album_id");
        List<MediaItemData> songs = mediaProvider.getAlbumSongs(albumId);
        AlbumSongsListAdapter albumSongsListAdapter = new AlbumSongsListAdapter(getActivity(), songs, albumId);
        RecyclerView recyclerView = view.findViewById(R.id.albums_songs_list);
        recyclerView.setAdapter(albumSongsListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ImageView albumArt = view.findViewById(R.id.album_art);
        albumArt.setImageBitmap(Utils.getBitmapFromMediaStore(songs.get(0).albumArtUri));

        TextView albumTitle = view.findViewById(R.id.album_title);
        TextView albumArtist = view.findViewById(R.id.album_artist);
        albumTitle.setText(bundle.getString("album_title"));
        albumArtist.setText(songs.get(0).subtitle);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigateUp();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();

        Log.d(TAG, "onDetach");
    }

}
