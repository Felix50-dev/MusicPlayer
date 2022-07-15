package com.example.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AlbumsRecyclerViewAdapter extends RecyclerView.Adapter<AlbumsRecyclerViewAdapter.MyViewHolder>{
    ArrayList<Album> albums = new ArrayList<>();
    private final Context context;

    public void setAlbums(ArrayList<Album> albums) {
        this.albums = albums;
    }

    public AlbumsRecyclerViewAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_list_view,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull AlbumsRecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.albumName.setText(albums.get(position).getName());
        holder.artistName.setText(albums.get(position).getArtist());
        Glide.with(context)
                .asBitmap()
                .load(albums.get(position).getBitmap())
                .into(holder.albumImage);

        holder.parent.setOnClickListener(v -> {
            Log.d("albumName", "onBindViewHolder: " + albums.get(position).getName());
            Intent intent = new Intent(context,AlbumDetailsActivity.class);
            intent.putExtra("position",position );
            intent.putExtra("albumName",albums.get(position).getName());
            Log.i("albumPosition",String.valueOf(position));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView albumName,artistName;
        private ImageView albumImage;
        private CardView parent;

        public MyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            albumName = itemView.findViewById(R.id.albumNameTextView);
            artistName = itemView.findViewById(R.id.albumTextView);
            albumImage = itemView.findViewById(R.id.albumImageView);
            parent = itemView.findViewById(R.id.parent2);
        }
    }
}
