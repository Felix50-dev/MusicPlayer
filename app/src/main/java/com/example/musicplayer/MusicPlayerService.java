package com.example.musicplayer;

import static com.example.musicplayer.AlbumsActivity.albums;
import static com.example.musicplayer.MainActivity.songs;
import static com.example.musicplayer.MusicPlayerActivity.seekBar;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener {
    private static final String TAG = "MusicPlayerService";


    public static String Broadcast_PLAY_NEW_AUDIO = "com.example.musicplayer.MusicPlayerService.PlayNewAudio ";
    private static final String CHANNEL_ID = "101";
    private static final int NOTIFICATION_ID = 101;

    private static final String ACTION_PLAY = "com.example.musicplayer.MyService.action.PLAY";
    public static final String ACTION_PAUSE = "com.example.musicplayer.MyService.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.example.musicplayer.MyService.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.example.musicplayer.MyService.ACTION_NEXT";
    public static final String ACTION_STOP = "com.example.musicplayer.MyService.ACTION_STOP";

    static boolean isRunning;
    int resumePosition;
    int audioIndex;
    int notificationAction;

    NotificationCompat.Builder builder;

    // Binder given to clients
    private final IBinder binder = new ServiceBinder();

    PlaybackStateCompat.Builder stateBuilder;

    static MediaPlayer mediaPlayer = null;
    private ArrayList<Song> songArrayList;
    static Song activeSong;

    Handler handler;

    public MediaSessionCompat mediaSession;
    static MediaControllerCompat mController;
    static MediaControllerCompat.TransportControls mControllerTransportControls;

    SharedPreferences preferences;

    int typeOfMusic;

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setSound(null, null);
            channel.setShowBadge(false);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        stopMedia();
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info
        return false;
    }

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void updateSeekBar() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
            }
        }, 0, 1000);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        seekBar.setMax(mediaPlayer.getDuration());
        playMedia();
        updateSeekBar();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //Invoked indicating the completion of a seek operation
    }

    public class ServiceBinder extends Binder {
        MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        Log.d(TAG, "onServiceCreate: first");
        super.onCreate();
        isRunning = true;

        preferences = getSharedPreferences("ActiveSong", MODE_PRIVATE);

        createNotificationChannel();

        handler = new Handler();

        // Create a MediaSessionCompat
        mediaSession = new MediaSessionCompat(this, "My mediaSession");


        // Enable callbacks from MediaButtons and TransportControls
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mediaSession.setPlaybackState(stateBuilder.build());
        // MySessionCallback() has methods that handle callbacks from a media controller
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPlay() {
                super.onPlay();
                Log.d(TAG, "onPlay: mediaSession working");
                resumeMedia();
                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                        mediaPlayer.getCurrentPosition(), 1.0f, SystemClock.elapsedRealtime());
                mediaSession.setPlaybackState(stateBuilder.build());
                updateMetaData();
                buildPlayerNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                Log.d(TAG, "onPause: mediaSession working");
                pauseMedia();
                stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                        mediaPlayer.getCurrentPosition(), 1.0f, SystemClock.elapsedRealtime());
                mediaSession.setPlaybackState(stateBuilder.build());
                stopForeground(false);
            }

            @Override
            public void onStop() {
                super.onStop();
                stopMedia();
                stopForeground(true);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
                buildPlayerNotification(PlaybackStatus.PLAYING);
                Log.d(TAG, "onSkipToPrevious: skipping to next");
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                buildPlayerNotification(PlaybackStatus.PLAYING);
                Log.d(TAG, "onSkipToPrevious: skipping to previous");
            }

        });
        mediaSession.setActive(true);
        mController = new MediaControllerCompat(this, mediaSession);
        mControllerTransportControls = mController.getTransportControls();

        register_playNewAudio();
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            mControllerTransportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            mControllerTransportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            mControllerTransportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            mControllerTransportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            mControllerTransportControls.stop();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onServiceCreate: second");

        MediaButtonReceiver.handleIntent(mediaSession, intent);

        int position = intent.getIntExtra("position", -1);

        int albumPosition = intent.getIntExtra("albumPosition", -1);

        Log.i(TAG, "onStartCommand: albumPosition " + albumPosition);
        Log.i(TAG, "onStartCommand: position = " + position);
        if (position != -1) {
            //playAllSongs or play an album
            if (albumPosition != -1) {
                songArrayList = albums.get(albumPosition).getSongs();
            } else {
                songArrayList = songs;
            }
            //identify all songs available

            for (int i = 0; i < songArrayList.size(); i++) {
                Log.d(TAG, "onStartCommand: " + songArrayList.get(i).getName());
            }


            activeSong = songArrayList.get(position);
            Log.i(TAG, "onStartCommand: " + activeSong.getName());

            //save the active song
            SharedPreferences.Editor prefsEditor = preferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(activeSong);
            prefsEditor.putString("MyObject", json);
            prefsEditor.apply();

            initMediaPlayer();
//            stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.getCurrentPosition(), 0.1f, SystemClock.elapsedRealtime());
//            mediaSession.setPlaybackState(stateBuilder.build());
//            updateMetaData();
            MusicPlayerActivity.play.setImageResource(R.drawable.ic_pause);
            mControllerTransportControls.play();
        }

        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void skipToNext() {

        if (audioIndex == songArrayList.size() - 1) {
            //if last in playlist
            audioIndex = 0;
        } else {
            Log.d(TAG, "skipToNext: currentAudioIndex" + audioIndex);
            //get next in playlist
            audioIndex = audioIndex + 1;
            Log.d(TAG, "skipToNext: newAudioIndex" + audioIndex);
        }
        activeSong = songArrayList.get(audioIndex);
        SharedPreferences.Editor prefsEditor = preferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(activeSong);

        prefsEditor.putString("MyObject", json);
        prefsEditor.apply();

        Log.d(TAG, "skipToNext: onSkipToNextSong " + activeSong.getName());
        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        //TODO:implement in a better way(views should not be static)
        MusicPlayerActivity.songName.setText(activeSong.getName());
        MusicPlayerActivity.songArtist.setText(activeSong.getArtist());
        MusicPlayerActivity.coverArt.setImageBitmap(activeSong.getBitmap());

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
        updateMetaData();
    }

    private void skipToPrevious() {

        if (audioIndex == 0) {
            //if first in playlist
            //set index to the last of audioList
            audioIndex = songArrayList.size() - 1;
            activeSong = songArrayList.get(audioIndex);
        } else {
            //get previous in playlist
            activeSong = songArrayList.get(--audioIndex);
        }
        Log.d(TAG, "skipToNext: onSkipToPrevSong " + activeSong.getName());
        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        MusicPlayerActivity.songName.setText(activeSong.getName());
        MusicPlayerActivity.songArtist.setText(activeSong.getArtist());
        MusicPlayerActivity.coverArt.setImageBitmap(activeSong.getBitmap());

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
        updateMetaData();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void buildPlayerNotification(PlaybackStatus playbackStatus) {
        notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        Log.i("notificationAction", String.valueOf(notificationAction));
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            //create the play action
            play_pauseAction = playbackAction(0);
        }
        // Given a media session and its context (usually the component containing the session)
// Create a NotificationCompat.Builder
// Get the session's metadata
        MediaMetadataCompat mediaMetadata = mController.getMetadata();
        MediaDescriptionCompat description = mediaMetadata.getDescription();

        Log.d(TAG, "buildPlayerNotification: " + description.getTitle());

        builder = new NotificationCompat.Builder(this, CHANNEL_ID);

        builder
                // Add the metadata for the currently playing track
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setSubText(description.getDescription())
                .setLargeIcon(description.getIconBitmap())
                .setPriority(Notification.PRIORITY_LOW)


                // Enable launching the player by clicking the notification
                .setContentIntent(mController.getSessionActivity())

                // Stop the service when the notification is swiped away
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_STOP))

                // Make the transport controls visible on the lockscreen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                //add this
                .setCategory(Notification.CATEGORY_SERVICE)

                // Add an app icon and set its accent color
                // Be careful about the color
                .setSmallIcon(R.drawable.ic_music)
                .setColor(ContextCompat.getColor(this, R.color.black))

                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2))

                // Take advantage of MediaStyle features
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0)

                        // Add a cancel button
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                                PlaybackStateCompat.ACTION_STOP)));

        // Display the notification
        startForeground(NOTIFICATION_ID, builder.build());
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MusicPlayerService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private void updateMetaData() {
        Bitmap albumArt = activeSong.getBitmap();
        // Update the current metadata
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeSong.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeSong.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeSong.getName())
                .build());
    }


    private void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
            notificationAction = android.R.drawable.ic_media_play;
        }
    }

    private void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    public void initMediaPlayer() {
        stopMedia();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);

        Log.i("songData", activeSong.getData());

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            // Set the data source to the mediaFile location
            mediaPlayer.setDataSource(activeSong.getData());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
    }

    private void buildNotification(PlaybackStatus playbackStatus) {

        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        Log.i("notificationAction", String.valueOf(notificationAction));
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            //create the play action
            play_pauseAction = playbackAction(0);
        }
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.image); //replace with your own image

        // Create a new Notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setShowWhen(false)
                // Set the Notification style
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mediaSession.getSessionToken())
                        // Show our playback controls in the compact notification view.
                        .setShowActionsInCompactView(0, 1, 2))
                // Set the Notification color
                .setColor(getResources().getColor(R.color.black))
                // Set the large and small icons
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                // Set Notification content information
                .setContentText(activeSong.getArtist())
                .setContentTitle(activeSong.getAlbum())
                .setContentInfo(activeSong.getName())
                // Add playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    public final BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "working", Toast.LENGTH_SHORT).show();

            int position = intent.getIntExtra("position", -1);

            int albumPosition = intent.getIntExtra("albumPosition", -1);

            Log.i(TAG, "playNewAudio: albumPosition " + albumPosition);
            Log.i(TAG, "playNewAudio : position = " + position);

            if (position != -1) {

                if (albumPosition != -1) {
                    songArrayList = albums.get(albumPosition).getSongs();
                } else {
                    songArrayList = songs;
                }
                activeSong = songArrayList.get(position);

                SharedPreferences.Editor prefsEditor = preferences.edit();
                Gson gson = new Gson();
                String json = gson.toJson(activeSong);
                prefsEditor.putString("MyObject", json);
                prefsEditor.apply();
            } else {
                stopSelf();
            }
            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopMedia();
            mediaPlayer.reset();
            initMediaPlayer();
            mControllerTransportControls.play();
            Log.i(TAG, "onReceive: callleeed");
        }
    };

    private void register_playNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.Broadcast");
        registerReceiver(playNewAudio, filter);
    }

    private void unregister_playNewAudio() {
        unregisterReceiver(playNewAudio);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        unregister_playNewAudio();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        mController.registerCallback(MusicPlayerActivity.mControllerCallback);
        mController.registerCallback(MiniPlayerFragment.mControllerCallback);
    }
}
