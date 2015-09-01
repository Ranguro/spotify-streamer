package com.example.ranguro.spotifystreamer.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.ranguro.spotifystreamer.R;
import com.example.ranguro.spotifystreamer.classes.ParcelableSpotifyArtist;


public class TopTracksActivity extends AppCompatActivity {


    public static final String ARTIST_ID_KEY = "artist_id";
    public static final String ARTIST_NAME_KEY = "artist_name";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        if(savedInstanceState == null){

            ParcelableSpotifyArtist artist = getIntent().getParcelableExtra(TopTracksActivityFragment.KEY_ARTIST);

            ActionBar ab = getSupportActionBar();
            ab.setSubtitle(artist.name);
            ab.setDisplayHomeAsUpEnabled(true);

            Bundle args = new Bundle();
            args.putParcelable(TopTracksActivityFragment.KEY_ARTIST,
                    artist);

            TopTracksActivityFragment fragment = new TopTracksActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.artist_track_container, fragment)
                    .commit();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
