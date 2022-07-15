package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.example.musicplayer.MainActivity.songs;

public class AlbumsActivity extends AppCompatActivity {
    private static final String TAG = "AlbumsActivity";

    ArrayList<String> albumNames;
    static ArrayList<Album> albums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);

        getAlbumNames();
        getAllAlbums();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.albumsFragment, new AlbumFragment());
        transaction.commit();
    }

    public ArrayList<String> getAlbumNames() {
        albumNames = new ArrayList<>();
        Log.d(TAG, "getAlbumNames: total songs are " + songs.size());
        for (int i = 0; i < songs.size(); i++) {
            if (albumNames.isEmpty()) {
                albumNames.add(songs.get(i).getAlbum());
                Log.d(TAG, "AlbumNames is empty: " + albumNames.get(0) + 0);
            } else {
                for (int j = 0; j < albumNames.size(); j++) {
                    if (albumNames.contains(songs.get(i).getAlbum())) {
                        Log.d(TAG, "getAlbumNames: " + songs.get(i).getAlbum() + " already added");
                        break;
                    } else {
                        albumNames.add(songs.get(i).getAlbum());
                        Log.d(TAG, "getAlbumNames: " + albumNames.get(j) + j);
                        Log.d(TAG, "getAlbumNames: " + "total albums are " + albumNames.size());
                    }
                }
            }
        }
        return albumNames;
    }

    public ArrayList<Song> getAlbum(String albumName) {
        ArrayList<Song> album = new ArrayList<>();
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).getAlbum().equals(albumName)) {
                album.add(songs.get(i));
            }
        }
        return album;
    }

    public ArrayList<Album> getAllAlbums() {
        albums = new ArrayList<>();
        for (int i = 0; i < albumNames.size(); i++) {
            Album album = new Album();
            ArrayList<Song> albumSongs = getAlbum(albumNames.get(i));
            album.setSongs(albumSongs);
            for(int j = 0;j < albumSongs.size();j++){
                Log.d(TAG, "onCreate: nameOfSong " +album.getSongs().get(j).getName());
            }
            album.setName(albumNames.get(i));
            album.setBitmap(albumSongs.get(0).getBitmap());
            album.setArtist(albumSongs.get(0).getArtist());
            albums.add(album);
        }
        Collections.sort(albums, new Comparator<Album>() {
            public int compare(Album a, Album b) {
                return a.getName().compareTo(b.getName());
            }
        });

        return albums;
    }
}