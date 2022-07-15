package com.example.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

public class MiniPlayerFragment extends Fragment {
    private static final String TAG = "MiniPlayerFragment";

    SharedPreferences preferences;

    static FloatingActionButton play_pause;
    ImageView next, coverArt;
    TextView songName, songAlbum;

    Song activeSong;

    View view;

    static MediaControllerCompat.Callback mControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);

            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                    play_pause.setImageResource(R.drawable.ic_pause);
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    play_pause.setImageResource(R.drawable.ic_play);
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_mini_player, container, false);

        preferences = requireContext().getSharedPreferences("ActiveSong", Context.MODE_PRIVATE);

        initViews();

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        Gson gson = new Gson();
        String json = preferences.getString("MyObject", "");
        activeSong = gson.fromJson(json, Song.class);
        if (activeSong != null){
            Log.d(TAG, "onResume: " + activeSong.getName());

            //coverArt.setImageBitmap(activeSong.getBitmap());
            songAlbum.setText(activeSong.getAlbum());
            songName.setText(activeSong.getName());
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MusicPlayerService.mControllerTransportControls.skipToNext();
                }
            });
            play_pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pbState = MusicPlayerService.mController.getPlaybackState().getState();
                    Log.d(TAG, "onClick: pbState " + pbState);
                    if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                        MusicPlayerService.mControllerTransportControls.pause();
                    } else {
                        MusicPlayerService.mControllerTransportControls.play();
                    }
                }
            });
        }
    }

    private void initViews() {
        play_pause = view.findViewById(R.id.miniPlayer_ic_play);
        next = view.findViewById(R.id.miniPlayer_ic_next);
        coverArt = view.findViewById(R.id.miniPlayerAlbumArt);
        songName = view.findViewById(R.id.miniPlayerSong);
        songAlbum = view.findViewById(R.id.miniPlayerArtistName);
    }


}