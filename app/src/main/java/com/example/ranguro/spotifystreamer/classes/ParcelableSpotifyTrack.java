package com.example.ranguro.spotifystreamer.classes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Randall on 15/07/2015.
 */
public class ParcelableSpotifyTrack implements Parcelable{


    public String artistName;
    public String name;
    public String albumName;
    public String albumImageUrl;
    public String previewUrl;


    public ParcelableSpotifyTrack(String artistName, String name, String albumName, String albumImageUrl, String url) {
        this.artistName = artistName;
        this.name = name;
        this.albumName = albumName;
        this.albumImageUrl = albumImageUrl;
        this.previewUrl = url;
    }

    public ParcelableSpotifyTrack(Parcel in) {
        artistName = in.readString();
        name = in.readString();
        albumName = in.readString();
        albumImageUrl = in.readString();
        previewUrl = in.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(artistName);
        parcel.writeString(name);
        parcel.writeString(albumName);
        parcel.writeString(albumImageUrl);
        parcel.writeString(previewUrl);

    }

    public static final Parcelable.Creator<ParcelableSpotifyTrack> CREATOR = new Parcelable.Creator<ParcelableSpotifyTrack>(){
        @Override
        public ParcelableSpotifyTrack createFromParcel(Parcel parcel) {
            return new ParcelableSpotifyTrack(parcel);
        }

        @Override
        public ParcelableSpotifyTrack[] newArray(int size) {
            return new ParcelableSpotifyTrack[0];
        }
    };
}
