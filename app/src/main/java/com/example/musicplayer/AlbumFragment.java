package com.example.musicplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.musicplayer.AlbumsActivity.albums;
import static com.example.musicplayer.MainActivity.songs;

import java.util.Collections;
import java.util.Comparator;


public class AlbumFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_album, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.albumRecView);
        AlbumsRecyclerViewAdapter adapter = new AlbumsRecyclerViewAdapter(getActivity());
        if (!(songs.size() < 1)) {
            adapter.setAlbums(albums);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }

        return view;
    }
}