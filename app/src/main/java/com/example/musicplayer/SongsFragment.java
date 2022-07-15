package com.example.musicplayer;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Comparator;

import static com.example.musicplayer.MainActivity.songs;

public class SongsFragment extends Fragment {

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_songs, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.songsRecView);
        SongsRecyclerViewAdapter adapter = new SongsRecyclerViewAdapter(getActivity());
        if(!(songs.size()  < 1)){
            adapter.setSongs(songs);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }

        return view;
    }

}