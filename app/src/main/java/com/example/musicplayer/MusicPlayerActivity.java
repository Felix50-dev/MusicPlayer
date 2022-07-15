package com.example.musicplayer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.musicplayer.MainActivity.songs;

import static com.example.musicplayer.MusicPlayerService.activeSong;
import static com.example.musicplayer.MusicPlayerService.mediaPlayer;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MusicPlayerActivity extends AppCompatActivity {
    private static final String TAG = "MusicPlayerActivity";

    ImageView backBtn, menuBtn, gradient, shuffle, prev, next, repeat;
    static ImageView coverArt;
    static FloatingActionButton play;
    static TextView songName, songArtist;

    static SeekBar seekBar;

    MusicPlayerService musicPlayerService;
    boolean isBound = false;

    int position;
    String songAlbum;

    Handler handler = new Handler();
    StorageUtil storageUtil;
    Song activeSong;

    static MediaControllerCompat.Callback mControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);

            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                    play.setImageResource(R.drawable.ic_pause);
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    play.setImageResource(R.drawable.ic_play);
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

            Toast.makeText(MusicPlayerActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            Toast.makeText(MusicPlayerActivity.this, "Service unBound", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        storageUtil = new StorageUtil(this);

        initViews();

        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);
        songAlbum = intent.getStringExtra("songAlbum");

        if (position != -1) {
            if (!MusicPlayerService.isRunning) {
                Intent serviceIntent = new Intent(this, MusicPlayerService.class);
                serviceIntent.putExtra("position", position);

                //get the album position
                int albumPosition = AlbumDetailsActivity.position;
                serviceIntent.putExtra("albumPosition",albumPosition);
                Log.d(TAG, "onCreate: albumPosition is " + albumPosition);

                startService(serviceIntent);
                bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            } else {
                Log.i(TAG, "onCreate: play new audio");
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("com.example.Broadcast");

                broadcastIntent.putExtra("position", position);
                broadcastIntent.putExtra("albumPosition",AlbumDetailsActivity.position);
                sendBroadcast(broadcastIntent);
            }
        }
        populateViews();
}

    @Override
    protected void onStart() {
        super.onStart();

        //MusicPlayerService.mController.registerCallback(mControllerCallback);
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop: invoked");
        super.onStop();
        //unbindService(serviceConnection);
    }

    public void initViews() {
        coverArt = findViewById(R.id.imageViewCoverArt);
        gradient = findViewById(R.id.imageViewGradient);
        shuffle = findViewById(R.id.ic_shuffle);
        prev = findViewById(R.id.ic_prev);
        next = findViewById(R.id.ic_next);
        repeat = findViewById(R.id.ic_repeat);

        play = findViewById(R.id.ic_play);

        seekBar = findViewById(R.id.seekBar);

        songName = findViewById(R.id.songName);
        songArtist = findViewById(R.id.songArtist);
    }

    public void populateViews() {
        Song song = songs.get(position);
        coverArt.setImageBitmap(song.getBitmap());
        songArtist.setText(song.getArtist());
        songName.setText(song.getName());

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
        play.setOnClickListener(new View.OnClickListener() {
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