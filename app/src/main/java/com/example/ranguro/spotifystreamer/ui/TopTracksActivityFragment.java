package com.example.ranguro.spotifystreamer.ui;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ranguro.spotifystreamer.R;
import com.example.ranguro.spotifystreamer.adapters.SpotifyTrackAdapter;
import com.example.ranguro.spotifystreamer.classes.ParcelableSpotifyArtist;
import com.example.ranguro.spotifystreamer.classes.ParcelableSpotifyTrack;
import com.example.ranguro.spotifystreamer.classes.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.RetrofitError;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksActivityFragment extends Fragment {

    private final String LOG_TAG = TopTracksActivityFragment.class.getSimpleName();

    private ArrayList<ParcelableSpotifyTrack> artistTracks;
    private SpotifyTrackAdapter mTrackAdapter;
    private ParcelableSpotifyArtist artist;
    public static final String KEY_ARTIST = "artist";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        artist = (ParcelableSpotifyArtist) getArguments().getParcelable(KEY_ARTIST);

        if (savedInstanceState == null || !savedInstanceState.containsKey("tracks")) {
            if (Utils.isNetworkAvailable(getActivity())) {
                artistTracks = new ArrayList<>();
                SearchTopArtistTracksTask topArtistTracksTask = new SearchTopArtistTracksTask();
                topArtistTracksTask.execute();
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.toast_no_internet), Toast.LENGTH_SHORT).show();
            }
        } else {
                artistTracks = savedInstanceState.getParcelableArrayList("tracks");
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("tracks", artistTracks);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        final ListView tracksList = (ListView) rootView.findViewById(R.id.listview_track);



        if (savedInstanceState != null){
            mTrackAdapter = new SpotifyTrackAdapter(getActivity().getApplicationContext(), artistTracks);
            tracksList.setAdapter(mTrackAdapter);
            mTrackAdapter.notifyDataSetChanged();
        }

        tracksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                if (Utils.isNetworkAvailable(getActivity())) {

                    FragmentManager fragmentManager = getActivity().getFragmentManager();
                    PlayerActivityFragment newFragment = new PlayerActivityFragment().newInstance(artistTracks, position);

                    if (getResources().getBoolean(R.bool.has_two_panes)) {;
                        newFragment.show(fragmentManager, "dialog");
                    } else {

                        Intent topTracksIntent = new Intent(getActivity(), PlayerActivity.class);

                        Bundle args = new Bundle();
                        args.putParcelableArrayList(PlayerActivityFragment.KEY_TRACK, artistTracks);
                        args.putInt(PlayerActivityFragment.KEY_CURRENT_POSITION, position);

                        topTracksIntent.putExtras(args);
                        startActivity(topTracksIntent);

//                        android.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
//                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//                        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
                    }
                }else{
                    Toast.makeText(getActivity(), R.string.toast_no_internet, Toast.LENGTH_SHORT).show();
                }

            }
        });


        return rootView;
    }

    public class SearchTopArtistTracksTask extends AsyncTask<String, Void, ArrayList<Track>> {
        @Override
        protected ArrayList<Track> doInBackground(String... query) {
            try {

                Map<String, Object> countryCode = new HashMap<>();
                countryCode.put("country", Locale.getDefault().getCountry()); // Could be improved to ip-Address

                String artistId = artist.id;
                SpotifyApi api = new SpotifyApi();
                SpotifyService service = api.getService();
                ArrayList<Track> topTracks = (ArrayList) service.getArtistTopTrack(artistId, countryCode).tracks;



                return topTracks;
            } catch (RetrofitError error) {
                Log.e(LOG_TAG, error.getLocalizedMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Track> topTracks) {
            if (topTracks != null ) {
                artistTracks = new ArrayList<>();

                for (int i = 0; i < topTracks.size(); i++) {
                    String artistName = artist.name;
                    String trackName = topTracks.get(i).name;
                    String albumName = topTracks.get(i).album.name;
                    String albumImageUrl = topTracks.get(i).album.images.get(0).url;
                    String url = topTracks.get(i).preview_url;
                    artistTracks.add(new ParcelableSpotifyTrack(artistName, trackName, albumName, albumImageUrl, url));
                }

                if(artistTracks.isEmpty()){
                    Toast.makeText(getActivity(), getResources().getString(R.string.toast_no_tracks_found), Toast.LENGTH_SHORT).show();
                }

                mTrackAdapter = new SpotifyTrackAdapter(getActivity().getApplicationContext(), artistTracks);
                ListView listView = (ListView) getActivity().findViewById(R.id.listview_track);
                listView.setAdapter(mTrackAdapter);
                mTrackAdapter.notifyDataSetChanged();
            }
            else{
                Toast.makeText(getActivity(), getResources().getString(R.string.toast_error_found), Toast.LENGTH_SHORT).show();
            }
        }



    }

}



