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

public class SongsRecyclerViewAdapter extends RecyclerView.Adapter<SongsRecyclerViewAdapter.MyViewHolder> {
    private final Context context;
    private ArrayList<Song> songs;

   // int positionOfSong = 0;

    public SongsRecyclerViewAdapter(Context context) {
        this.context = context;
    }

    public void setSongs(ArrayList<Song> songs) {
        this.songs = songs;
    }

    @NonNull
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.songs_list_view, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SongsRecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.artistTextView.setText(songs.get(position).getArtist());
        holder.songNameTextView.setText(songs.get(position).getName());
        Glide.with(context)
                .asBitmap()
                .load(songs.get(position).getBitmap())
                .into(holder.songImageView);

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context,MainActivity.class);
            intent.putExtra("position",position);
            //intent.putExtra("songAlbum",songs.get(position).getAlbum());
            Log.i("songPosition",String.valueOf(position));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView songImageView;
        private final TextView songNameTextView;
        private final TextView artistTextView;
        private final CardView cardView;

        public MyViewHolder(@NonNull @org.jetbrains.annotations.NotNull View itemView) {
            super(itemView);
            songImageView = itemView.findViewById(R.id.songImageView);
            songNameTextView = itemView.findViewById(R.id.songNameTextView);
            artistTextView = itemView.findViewById(R.id.artistTextView);
            cardView = itemView.findViewById(R.id.parent);
        }
    }
}
