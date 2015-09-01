package com.example.ranguro.spotifystreamer.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.ranguro.spotifystreamer.R;

public class PlayerActivity extends AppCompatActivity {

    public static boolean twoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);


        if(savedInstanceState == null) {


            Bundle args = new Bundle();
            args.putParcelableArrayList(PlayerActivityFragment.KEY_TRACK,
                    getIntent().getParcelableArrayListExtra(PlayerActivityFragment.KEY_TRACK));

           args.putInt(PlayerActivityFragment.KEY_CURRENT_POSITION,
                   getIntent().getIntExtra(PlayerActivityFragment.KEY_CURRENT_POSITION, 0));

            PlayerActivityFragment fragment = new PlayerActivityFragment();
            fragment.setArguments(args);
            getFragmentManager().beginTransaction()
                   .add(R.id.track_player_container, fragment)
                   .commit();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
