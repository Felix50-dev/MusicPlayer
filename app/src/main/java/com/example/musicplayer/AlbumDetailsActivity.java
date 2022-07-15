package com.example.musicplayer;

import static com.example.musicplayer.AlbumsActivity.albums;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumDetailsActivity extends AppCompatActivity {
    private static final String TAG = "AlbumDetailsActivity";
    public static int position = -1;
    
    ImageView albumImage;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);

        initViews();

        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);
        String albumName = intent.getStringExtra("albumName");

        Log.d(TAG, "onCreate: position = " + position);

        AlbumSongsAdapter adapter = new AlbumSongsAdapter(this);

        if(position != -1){
            Log.d(TAG, "onCreate: sizeOfMyAlbums = " + albums.size());
            Album album = albums.get(position);
            albumImage.setImageBitmap(album.getBitmap());
            Log.d(TAG, "onCreate: nameOfArtist " +album.getArtist());
            adapter.setSongs(album.getSongs());
        }
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    private void initViews(){
        albumImage = findViewById(R.id.imageView);
        recyclerView = findViewById(R.id.albumRecyclerView);
    }
}