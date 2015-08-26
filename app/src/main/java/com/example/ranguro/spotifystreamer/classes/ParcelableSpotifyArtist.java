package com.example.ranguro.spotifystreamer.classes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Randall on 12/07/2015.
 */
public class ParcelableSpotifyArtist implements Parcelable {

    public String id;
    public String name;
    public String imageUrl;

    public ParcelableSpotifyArtist(String id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public ParcelableSpotifyArtist(Parcel in) {
        id = in.readString();
        name = in.readString();
        if(in.dataAvail()>0){
            imageUrl = in.readString();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeString(name);
        if(imageUrl!= null){
            parcel.writeString(imageUrl);
        }
    }

    public static final Parcelable.Creator<ParcelableSpotifyArtist> CREATOR = new Parcelable.Creator<ParcelableSpotifyArtist>(){
        @Override
        public ParcelableSpotifyArtist createFromParcel(Parcel parcel) {
            return new ParcelableSpotifyArtist(parcel);
        }

        @Override
        public ParcelableSpotifyArtist[] newArray(int size) {
            return new ParcelableSpotifyArtist[0];
        }
    };
}
