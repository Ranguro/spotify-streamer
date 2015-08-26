package com.example.ranguro.spotifystreamer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.ranguro.spotifystreamer.R;
import com.example.ranguro.spotifystreamer.classes.ParcelableSpotifyArtist;

public class MainActivity extends AppCompatActivity{


    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String TOPTRACKS_TAG = "TTTAG";

    public static boolean twoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView artistListView = (ListView) findViewById(R.id.listview_artist);

        if (findViewById(R.id.artist_track_container) != null){
            twoPane = true;
        }
        else{
            twoPane = false;
        }

        artistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {

                ParcelableSpotifyArtist selectedArtist = (ParcelableSpotifyArtist) parent.getAdapter().getItem(i);
                Bundle args = new Bundle();
                args.putParcelable(TopTracksActivityFragment.KEY_ARTIST, selectedArtist);

                if (twoPane) {
                    TopTracksActivityFragment fragment = new TopTracksActivityFragment();
                    fragment.setArguments(args);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.artist_track_container, fragment, TOPTRACKS_TAG)
                            .commit();
                } else {
                    Intent topTracksIntent = new Intent(MainActivity.this, TopTracksActivity.class);
                    topTracksIntent.putExtras(args);
                    startActivity(topTracksIntent);
                }
            }});
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}



