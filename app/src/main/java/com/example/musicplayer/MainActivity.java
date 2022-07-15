package com.example.musicplayer;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {
    public static BottomNavigationView bottomNavigationView;
    static ArrayList<Song> songs;

    static String seconds;

    private static final String TAG = "MainActivity";

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                songs = getSongList(MainActivity.this);
            }
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        initBottomNavigation();

        permissions();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flFragment, new MainFragment());
        transaction.commit();
    }

    private void initBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.ic_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.ic_home:
                    //TODO: fix this

                case R.id.ic_albums:
                    Intent intent = new Intent(MainActivity.this, AlbumsActivity.class);
                    startActivity(intent);
                    Log.d(TAG, "initBottomNavigation: album icon pressed");
                    break;
                case R.id.ic_artists:

                case R.id.ic_playlists:

                case R.id.ic_songs:
                    AlbumDetailsActivity.position = -1;
                    Intent myIntent = new Intent(MainActivity.this, SongsActivity.class);
                    startActivity(myIntent);
                    Log.d(TAG, "initBottomNavigation: song icon pressed");
                    break;
                default:
                    break;
            }
            return true;
        });
    }

    private void permissions(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }else{
            songs = getSongList(MainActivity.this);
        }
    }

    public static ArrayList<Song> getSongList(Context context) {
        ArrayList<Song> songs = new ArrayList<>();

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        final String[] cursor_cols = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
                MediaStore.Downloads.ARTIST,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION};
        //final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";
        final Cursor cursor = context.getContentResolver().query(collection,
                cursor_cols, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media._ID));
                String artist = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));

                String album = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                String track = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                Long albumId = cursor.getLong(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Downloads.DATA));

                MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
                metaRetriever.setDataSource(data);

                String duration2 =
                        metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                long dur = Long.parseLong(duration2);
                seconds = String.valueOf(dur / 1000);
                Log.e("mySeconds", seconds);

                // close object
                metaRetriever.release();

                Log.e("duration2", duration2);

                Bitmap bitmap = getAlbumArt(context, albumId);

                Song song = new Song(id,artist, track, album, bitmap, data);
                song.setDuration(duration2);
                Log.e("MusicArtist", song.toString());
                songs.add(song);
            } while (cursor.moveToNext());
            cursor.close();
        }

        Collections.sort(songs, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getName().compareTo(b.getName());
            }
        });

        return songs;
    }

    public static Bitmap getAlbumArt(Context context, Long album_id) {
        Bitmap albumArtBitMap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {

            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            ParcelFileDescriptor pfd = context.getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();
                albumArtBitMap = BitmapFactory.decodeFileDescriptor(fd, null,
                        options);
                pfd = null;
                fd = null;
            }
        } catch (Error ee) {
            ee.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (null != albumArtBitMap) {
            return albumArtBitMap;
        }
        return null;
    }
}