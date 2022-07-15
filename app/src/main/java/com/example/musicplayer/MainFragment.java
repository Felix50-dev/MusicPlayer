package com.example.musicplayer;

import static com.example.musicplayer.MainActivity.songs;
import static com.example.musicplayer.MusicPlayerService.mediaPlayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";

    ImageView smallAlbumArt,next;
    static FloatingActionButton play_pause;
    TextView songName,songArtist;
    TextView largeSongName,largeArtistName;
    SlidingUpPanelLayout slidingUpPanelLayout;
    RelativeLayout relativeLayout;

    ImageView gradient, shuffle, prev, nextSong, repeat;
    static ImageView coverArt;

    static SeekBar seekBar;

    MusicPlayerService musicPlayerService;
    boolean isBound = false;
    int position;

    Handler handler = new Handler();
    StorageUtil storageUtil;
    Song activeSong;

    String songAlbum;

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

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.ServiceBinder binder = (MusicPlayerService.ServiceBinder) service;
            musicPlayerService = binder.getService();
            isBound = true;

            Toast.makeText(getActivity(), "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            Toast.makeText(getActivity(), "Service unBound", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        initViews(view);

        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if(newState.toString().equals("EXPANDED")){
                    //slidingUpPanelLayout.setTouchEnabled(false);
                    relativeLayout.setVisibility(View.INVISIBLE);
                    MainActivity.bottomNavigationView.setVisibility(View.INVISIBLE);

//                    RelativeLayout.LayoutParams playPause_params = new RelativeLayout.LayoutParams(200,200);
//                    playPause_params.addRule(RelativeLayout.CENTER_HORIZONTAL);
//                    playPause_params.topMargin = 1100;
//
//                    play_pause.setLayoutParams(playPause_params);

                }else{
                    MainActivity.bottomNavigationView.setVisibility(View.VISIBLE);
                    relativeLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storageUtil = new StorageUtil(getActivity());

        Intent intent = getActivity().getIntent();
        position = intent.getIntExtra("position", -1);
        Log.d(TAG, "onCreate: myCurrentPosition " + position);
        songAlbum = intent.getStringExtra("songAlbum");

        if (position != -1) {
            if (!MusicPlayerService.isRunning) {
                Intent serviceIntent = new Intent(getActivity(), MusicPlayerService.class);
                serviceIntent.putExtra("position", position);

                //get the album position
                int albumPosition = AlbumDetailsActivity.position;
                serviceIntent.putExtra("albumPosition",albumPosition);
                Log.d(TAG, "onCreate: albumPosition is " + albumPosition);

                getActivity().startService(serviceIntent);
                getActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            } else {
                Log.i(TAG, "onCreate: play new audio");
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("com.example.Broadcast");

                broadcastIntent.putExtra("position", position);
                broadcastIntent.putExtra("albumPosition",AlbumDetailsActivity.position);
                getActivity().sendBroadcast(broadcastIntent);
            }
        }
        //populateViews();

    }

    public void initViews(View view){
        smallAlbumArt = view.findViewById(R.id.miniPlayerAlbumArt);
        next = view.findViewById(R.id.miniPlayer_ic_next);
        play_pause = view.findViewById(R.id.miniPlayer_ic_play);
        songName = view.findViewById(R.id.miniPlayerSong);
        songArtist = view.findViewById(R.id.miniPlayerArtistName);
        slidingUpPanelLayout = view.findViewById(R.id.activity_main);
        relativeLayout = view.findViewById(R.id.miniPlayerLayout);

        coverArt = view.findViewById(R.id.imageViewCoverArt);
        gradient = view.findViewById(R.id.imageViewGradient);
        shuffle = view.findViewById(R.id.ic_shuffle);
        prev = view.findViewById(R.id.ic_prev);
        nextSong = view.findViewById(R.id.ic_next);
        repeat = view.findViewById(R.id.ic_repeat);


        seekBar = view.findViewById(R.id.seekBar);

        largeSongName = view.findViewById(R.id.songName);
        largeArtistName = view.findViewById(R.id.songArtist);
    }

    public void populateViews() {
        Song song = songs.get(position);
        coverArt.setImageBitmap(song.getBitmap());
        largeArtistName.setText(song.getArtist());
        largeSongName.setText(song.getName());

        //seekBar.setMax(Integer.parseInt(song.getDuration()));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        play_pause.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                // Since this is a play/pause button, you'll need to test the current state
                // and choose the action accordingly
                //MusicPlayerService.mControllerTransportControls.pause();
                int pbState = MusicPlayerService.mController.getPlaybackState().getState();
                Log.d(TAG, "onClick: pbState " + pbState);
                if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                    MusicPlayerService.mControllerTransportControls.pause();
                } else {
                    MusicPlayerService.mControllerTransportControls.play();
                }
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayerService.mControllerTransportControls.skipToPrevious();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayerService.mControllerTransportControls.skipToNext();
            }
        });
    }
    public void updateSeekBar(){
        int currentPosition = mediaPlayer.getCurrentPosition();
        seekBar.setProgress(currentPosition);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                updateSeekBar();
            }
        };
        handler.postDelayed(runnable,1000);
    }

}