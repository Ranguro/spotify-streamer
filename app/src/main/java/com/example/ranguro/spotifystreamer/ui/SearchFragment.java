package com.example.ranguro.spotifystreamer.ui;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.ranguro.spotifystreamer.R;
import com.example.ranguro.spotifystreamer.adapters.SpotifyArtistAdapter;
import com.example.ranguro.spotifystreamer.classes.ParcelableSpotifyArtist;
import com.example.ranguro.spotifystreamer.classes.Utils;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;


public class SearchFragment extends Fragment{


    private final String ARTIST_ID = "ARTIST_ID";
    private final String ARTIST_NAME = "ARTIST_NAME";
    private SpotifyArtistAdapter mSpotifySpotifyArtistAdapter;
    private ArrayList<ParcelableSpotifyArtist> artists;
    private final String LOG_TAG = TopTracksActivityFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null || !savedInstanceState.containsKey("artists")){
            artists = new ArrayList<ParcelableSpotifyArtist>();
        }
        else{
            artists = savedInstanceState.getParcelableArrayList("artists");
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("artists", artists);
        super.onSaveInstanceState(outState);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        final SearchView searchText = (SearchView) rootView.findViewById(R.id.searchText);


        searchText.setIconifiedByDefault(false);
        searchText.setQueryHint(getResources().getString(R.string.artist_search_hint));
        searchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (Utils.isNetworkAvailable(getActivity())) {
                    try {
                        SearchSpotifyTask task = new SearchSpotifyTask();
                        task.execute(query);
                    } catch (RetrofitError error) {
                        Log.e(LOG_TAG, error.getLocalizedMessage());
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.toast_no_internet, Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }


        });



        if(savedInstanceState!=null){
            mSpotifySpotifyArtistAdapter = new SpotifyArtistAdapter(getActivity().getApplicationContext(), artists);
            ListView listView = (ListView) rootView.findViewById(R.id.listview_artist);
            listView.setAdapter(mSpotifySpotifyArtistAdapter);
            mSpotifySpotifyArtistAdapter.notifyDataSetChanged();
        }


        return rootView;
    }


    public class SearchSpotifyTask extends AsyncTask<String, Void, ArrayList<Artist>>
    {


        @Override
        protected ArrayList<Artist> doInBackground(String... query) {

            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService service = api.getService();
                ArtistsPager artistsPager = service.searchArtists(query[0]);
                return (ArrayList) artistsPager.artists.items;
            }
            catch(RetrofitError error){
                Log.e(LOG_TAG, error.getLocalizedMessage());

            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Artist> spotifyArtists) {

            if (spotifyArtists != null) {

                artists = new ArrayList<ParcelableSpotifyArtist>();

                if (!spotifyArtists.isEmpty()) {

                    for (Artist artist : spotifyArtists) {
                        if (!artist.images.isEmpty()) {
                            artists.add(new ParcelableSpotifyArtist(artist.id, artist.name, artist.images.get(0).url));
                        } else {
                            artists.add(new ParcelableSpotifyArtist(artist.id, artist.name, ""));
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.toast_no_match_found, Toast.LENGTH_SHORT).show();
                }

                mSpotifySpotifyArtistAdapter = new SpotifyArtistAdapter(getActivity().getApplicationContext(), artists);
                ListView listView = (ListView) getActivity().findViewById(R.id.listview_artist);
                listView.setAdapter(mSpotifySpotifyArtistAdapter);
                mSpotifySpotifyArtistAdapter.notifyDataSetChanged();
            } else{
                Toast.makeText(getActivity(), getResources().getString(R.string.toast_no_tracks_found), Toast.LENGTH_SHORT).show();
            }

        }


    }




}
