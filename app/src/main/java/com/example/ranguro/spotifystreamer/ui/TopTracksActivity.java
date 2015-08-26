package com.example.ranguro.spotifystreamer.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.ranguro.spotifystreamer.R;


public class TopTracksActivity extends ActionBarActivity {


    public static final String ARTIST_ID_KEY = "artist_id";
    public static final String ARTIST_NAME_KEY = "artist_name";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        if(savedInstanceState == null){

            Bundle args = new Bundle();
            args.putParcelable(TopTracksActivityFragment.KEY_ARTIST,
                    getIntent().getParcelableExtra(TopTracksActivityFragment.KEY_ARTIST));

            TopTracksActivityFragment fragment = new TopTracksActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.artist_track_container, fragment)
                    .commit();
        }

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        String artistName = getIntent().getExtras().getString("ARTIST_NAME");
        ab.setSubtitle(artistName);
        ab.setDisplayHomeAsUpEnabled(true);

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
