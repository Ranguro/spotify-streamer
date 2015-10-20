package com.example.ranguro.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ranguro.spotifystreamer.R;
import com.example.ranguro.spotifystreamer.classes.ParcelableSpotifyArtist;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Randall on 28/06/2015.
 */
public class SpotifyArtistAdapter extends ArrayAdapter<ParcelableSpotifyArtist>  {


    public SpotifyArtistAdapter(Context context, ArrayList<ParcelableSpotifyArtist> spotifyArtist) {
        super(context, 0, spotifyArtist);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ParcelableSpotifyArtist spotifyArtist = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist, parent, false);

            holder = new ViewHolder();

            holder.artistImage = (ImageView) convertView.findViewById(R.id.list_item_image);
            holder.artistName = (TextView) convertView.findViewById(R.id.list_item_artist);
            convertView.setTag(holder);

        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        if (!spotifyArtist.imageUrl.isEmpty()) {
            Picasso.with(getContext()).load(spotifyArtist.imageUrl).resize(200, 200).into(holder.artistImage);
        }else{
            Picasso.with(getContext()).load(R.drawable.default_image).into(holder.artistImage);
        }
        holder.artistName.setText(spotifyArtist.name);

        return convertView;
    }

    static class ViewHolder {

        ImageView artistImage;
        TextView artistName;

    }

}
