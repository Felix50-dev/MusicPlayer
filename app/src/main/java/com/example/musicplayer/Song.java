package com.example.musicplayer;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Song implements Parcelable{
    private int id;
    private String path;
    private String duration;
    private String artist;
    private String name;
    private String album;
    private Bitmap bitmap;
    private String data;

    protected Song(Parcel in) {
        id = in.readInt();
        path = in.readString();
        duration = in.readString();
        artist = in.readString();
        name = in.readString();
        album = in.readString();
        bitmap = in.readParcelable(Bitmap.class.getClassLoader());
        data = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Song(int id, String artist, String name, String album, Bitmap bitmap, String data) {
        this.id = id;
        this.artist = artist;
        this.name = name;
        this.album = album;
        this.bitmap = bitmap;
        this.data = data;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", duration='" + duration + '\'' +
                ", artist='" + artist + '\'' +
                ", name='" + name + '\'' +
                ", album='" + album + '\'' +
                ", bitmap=" + bitmap +
                ", data='" + data + '\'' +
                '}';
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }



    public Song() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(path);
        dest.writeString(duration);
        dest.writeString(artist);
        dest.writeString(name);
        dest.writeString(album);
        dest.writeParcelable(bitmap, flags);
        dest.writeString(data);
    }
}
