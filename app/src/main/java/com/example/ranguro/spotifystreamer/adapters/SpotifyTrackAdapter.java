package com.example.ranguro.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ranguro.spotifystreamer.R;
import com.example.ranguro.spotifystreamer.classes.ParcelableSpotifyTrack;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Randall on 17/07/2015.
 */
public class SpotifyTrackAdapter extends ArrayAdapter<ParcelableSpotifyTrack> {

    public SpotifyTrackAdapter(Context context, ArrayList<ParcelableSpotifyTrack> artistTracks) {
        super(context,0,artistTracks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        ParcelableSpotifyTrack spotifyTrack = getItem(position);


        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_track, parent, false);

            holder = new ViewHolder();

            holder.trackName = (TextView) convertView.findViewById(R.id.list_item_track);
            holder.albumName = (TextView) convertView.findViewById(R.id.list_item_album);
            holder.albumImage = (ImageView) convertView.findViewById(R.id.list_item_album_image);

            convertView.setTag(holder);


        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.trackName.setText(spotifyTrack.name);
        holder.albumName.setText(spotifyTrack.albumName);
        if (!spotifyTrack.albumImageUrl.isEmpty()){
            Picasso.with(getContext()).load(spotifyTrack.albumImageUrl).resize(200, 200).into(holder.albumImage);
        }else{
            Picasso.with(getContext()).load(R.drawable.default_image).into(holder.albumImage);
        }

        return convertView;
    }

    static class ViewHolder {

        TextView trackName;
        TextView albumName;
        ImageView albumImage;

    }


}
