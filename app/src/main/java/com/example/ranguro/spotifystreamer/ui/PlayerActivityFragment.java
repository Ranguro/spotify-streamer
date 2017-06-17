package com.example.ranguro.spotifystreamer.ui;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ranguro.spotifystreamer.R;
import com.example.ranguro.spotifystreamer.classes.ParcelableSpotifyTrack;
import com.example.ranguro.spotifystreamer.classes.Utils;
import com.example.ranguro.spotifystreamer.services.PlaybackService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerActivityFragment extends DialogFragment implements SeekBar.OnSeekBarChangeListener {

    private final String LOG_TAG = PlayerActivityFragment.class.getSimpleName();

    public static final String KEY_TRACK = "track";
    public static final String KEY_CURRENT_POSITION = "current position";

    private PlaybackService playbackService;
    private Intent playbackIntent;
    private Intent intent;

    private boolean broadcastIsRegistered;


    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
    }

    private boolean playbackBound = false;


    private ArrayList<ParcelableSpotifyTrack> spotifyTrackList;

    public static final String BROADCAST_SEEKBAR = "com.example.ranguro.spotifystreamer.sendseekbar";

    private boolean trackIsPaused = false;
    private int currentPosition;

    private TextView artistName;
    private TextView albumName;
    private ImageView albumArtwork;
    private TextView trackName;
    private TextView startDuration;
    private TextView endDuration;
    private SeekBar trackProgress;
    private ImageButton playPreviousTrackBtn;
    private ImageButton playTrackBtn;
    private ImageButton playNextTrackBtn;
    private ParcelableSpotifyTrack spotifyTrack;

    public PlayerActivityFragment() {


    }

    static PlayerActivityFragment newInstance(ArrayList<ParcelableSpotifyTrack> topTracksList, int position) {
        PlayerActivityFragment playerActivityFragment = new PlayerActivityFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList(KEY_TRACK, topTracksList);
        args.putInt(KEY_CURRENT_POSITION, position);
        playerActivityFragment.setArguments(args);
        return playerActivityFragment;
    }


    @Override
    public void onStart() {
        super.onStart();
        intent = new Intent(BROADCAST_SEEKBAR);
        playbackIntent = new Intent(getActivity(), PlaybackService.class);
        playbackIntent.putParcelableArrayListExtra("playlist", spotifyTrackList);
        playbackIntent.putExtra("position", currentPosition);
        playbackIntent.putExtra("preview_url", spotifyTrackList.get(currentPosition).previewUrl);
        if (!trackIsPaused) {
            getActivity().startService(playbackIntent);
        }
        getActivity().bindService(playbackIntent, playbackConnection, Context.BIND_AUTO_CREATE);
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spotifyTrackList = getArguments().getParcelableArrayList(KEY_TRACK);
        if (savedInstanceState == null){
            currentPosition = getArguments().getInt(KEY_CURRENT_POSITION);
        }else{
            currentPosition = savedInstanceState.getInt("playlist_position");
        }

        if(savedInstanceState != null) {
            trackIsPaused = savedInstanceState.getBoolean("state");
        }
        playbackIntent = new Intent(getActivity(), PlaybackService.class);
        playbackIntent.putParcelableArrayListExtra("playlist", spotifyTrackList);
        playbackIntent.putExtra("position", currentPosition);
        playbackIntent.putExtra("preview_url", spotifyTrackList.get(currentPosition).previewUrl);
        if (!trackIsPaused) {
            getActivity().startService(playbackIntent);
        }
        getActivity().bindService(playbackIntent, playbackConnection, Context.BIND_AUTO_CREATE);


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("state", trackIsPaused);

        outState.putInt("track_progress", trackProgress.getProgress());
        outState.putInt("playlist_position", currentPosition);

        outState.putInt("track_max_progress", trackProgress.getMax());

        outState.putString("duration", (String) startDuration.getText());
        outState.putString("duration", (String) startDuration.getText());
    }

    //connect to the service
    private ServiceConnection playbackConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlaybackService.PlaybackBinder binder = (PlaybackService.PlaybackBinder)service;
            playbackService = binder.getService();
            playbackBound = true;
            getActivity().registerReceiver(broadcastReceiver, new IntentFilter(
                    PlaybackService.UPDATE_UI));
            getActivity().registerReceiver(broadcastReceiver, new IntentFilter(
                    PlaybackService.RESET_PLAY_BUTTON));
            playbackService.playTrack(spotifyTrackList.get(currentPosition).previewUrl);
            broadcastIsRegistered=true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (broadcastIsRegistered){
                getActivity().unregisterReceiver(broadcastReceiver);
                broadcastIsRegistered = false;
            }
            playbackBound = false;
        }
    };



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        spotifyTrack = spotifyTrackList.get(currentPosition);

        artistName = (TextView) rootView.findViewById(R.id.artist_name);
        albumName = (TextView) rootView.findViewById(R.id.album_name);
        albumArtwork = (ImageView) rootView.findViewById(R.id.album_artwork);
        trackName = (TextView) rootView.findViewById(R.id.track_name);
        trackProgress = (SeekBar) rootView.findViewById(R.id.track_progress);
        startDuration = (TextView) rootView.findViewById(R.id.start_duration);
        endDuration = (TextView) rootView.findViewById(R.id.end_duration);
        playPreviousTrackBtn = (ImageButton) rootView.findViewById(R.id.play_previous_track_btn);
        playTrackBtn = (ImageButton) rootView.findViewById(R.id.play_track_btn);
        playNextTrackBtn = (ImageButton) rootView.findViewById(R.id.play_next_track_btn);

        trackProgress.setOnSeekBarChangeListener(this);

        updatePlayerScreen();

        if (savedInstanceState != null){
            trackProgress.setMax(savedInstanceState.getInt("track_max_progress"));
            trackProgress.setProgress(savedInstanceState.getInt("track_progress"));
            startDuration.setText(savedInstanceState.getString("duration"));
            trackIsPaused = savedInstanceState.getBoolean("state");
            if (trackIsPaused){
                playTrackBtn.setImageResource(android.R.drawable.ic_media_play);
            }else{
                playTrackBtn.setImageResource(android.R.drawable.ic_media_pause);
            }
        }



        playPreviousTrackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPreviousTrack();
            }
        });

        playTrackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playStopTrack();
            }
        });

        playNextTrackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextTrack();
            }
        });


        return rootView;
    }


    @Override
    public void onPause() {
        super.onPause();
        if (broadcastIsRegistered){
            getActivity().unbindService(playbackConnection);
            getActivity().unregisterReceiver(broadcastReceiver);
            broadcastIsRegistered = false;
        }
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent serviceIntent) {

            if(serviceIntent.getAction().equals(playbackService.UPDATE_UI)){
                updateUI(serviceIntent);
            }
            else if (serviceIntent.getAction().equals(playbackService.RESET_PLAY_BUTTON)){
                playTrackBtn.setImageResource(android.R.drawable.ic_media_play);
                trackIsPaused = true;
            }

        }

    };





    private void updateUI(Intent serviceIntent) {
        String counter = serviceIntent.getStringExtra("counter");
        String mediaMax = serviceIntent.getStringExtra("media_max");
        int maxDuration = Integer.parseInt(mediaMax);
        int seekProgress = Integer.parseInt(counter);

        startDuration.setText(getTimeString(seekProgress));
        trackProgress.setProgress(seekProgress);
        trackProgress.setMax(maxDuration);


    }


    private void updatePlayerScreen(){

        spotifyTrack = spotifyTrackList.get(currentPosition);

        artistName.setText(spotifyTrack.artistName);
        albumName.setText(spotifyTrack.albumName);

        if (!spotifyTrack.albumImageUrl.isEmpty()){
            Picasso.with(getActivity().getApplicationContext()).load(spotifyTrack.albumImageUrl)
                    .resize(800, 800).into(albumArtwork);
        }

        trackName.setText(spotifyTrack.name);
        trackProgress.setProgress(0);
        startDuration.setText("0:00");
        playTrackBtn.setImageResource(android.R.drawable.ic_media_pause);
    }

    private void playPreviousTrack() {
        playbackService.stopTrack();

        if(currentPosition != 0){
            currentPosition -= 1;
        }
        updatePlayerScreen();
        playbackService.setPosition(currentPosition);
        if (Utils.isNetworkAvailable(getActivity())) {
            playbackService.startMediaPlayer(spotifyTrack.previewUrl);
        }else{
            Toast.makeText(getActivity(), R.string.toast_no_internet, Toast.LENGTH_SHORT).show();
        }
        trackIsPaused = false;
    }



    private void playStopTrack() {
        if (trackIsPaused){
            if (trackProgress.getMax() == trackProgress.getProgress()){
                playTrackBtn.setImageResource(android.R.drawable.ic_media_pause);
                trackIsPaused = false;
                if (!broadcastIsRegistered){
                    getActivity().registerReceiver(broadcastReceiver, new IntentFilter(
                            PlaybackService.UPDATE_UI));
                    broadcastIsRegistered = true;
                }
                trackProgress.setProgress(0);
                playbackService.playTrack(spotifyTrack.previewUrl);

            }
            if (trackProgress.getProgress() != 0){
                playTrackBtn.setImageResource(android.R.drawable.ic_media_pause);
                playbackService.resume(trackProgress.getProgress());
                trackIsPaused = false;
            }
            else{
                playTrackBtn.setImageResource(android.R.drawable.ic_media_pause);
                trackIsPaused = false;
                if (!broadcastIsRegistered){
                    getActivity().registerReceiver(broadcastReceiver, new IntentFilter(
                            PlaybackService.UPDATE_UI));
                    broadcastIsRegistered = true;
                }
                playbackService.playTrack(spotifyTrack.previewUrl);
            }
        }else{
            playTrackBtn.setImageResource(android.R.drawable.ic_media_play);
            playbackService.pauseTrack();
            trackIsPaused = true;
        }
    }
    private void playNextTrack() {
        playbackService.stopTrack();
        if(currentPosition < spotifyTrackList.size()-1){
            currentPosition += 1;
        }
        trackIsPaused = false;
        playbackService.setPosition(currentPosition);
        updatePlayerScreen();
        if (Utils.isNetworkAvailable(getActivity())) {
            playbackService.startMediaPlayer(spotifyTrack.previewUrl);
        }else{
            Toast.makeText(getActivity(), R.string.toast_no_internet, Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (fromUser) {
            Log.d(TAG, "onProgressChanged: " + getTimeString(progress));
            startDuration.setText(getTimeString(progress));
            Log.d(TAG, "onProgressChanged: " + progress);
            int seekPos = seekBar.getProgress();

            intent.putExtra("seekpos", seekPos);
            getActivity().sendBroadcast(intent);
        }
    }



    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        playbackService.pauseTrack();
        playTrackBtn.setImageResource(android.R.drawable.ic_media_play);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (!trackIsPaused) {
            playbackService.resume(seekBar.getProgress());
            playTrackBtn.setImageResource(android.R.drawable.ic_media_pause);
        }
    }



    //Snippet from
    // http://stackoverflow.com/questions/5418644/android-mediaplayer-based-on-seekbar-progress-update-the-elapsed-time-of-audio
    // to display elapsed time
    private String getTimeString(int millis) {
        StringBuffer buf = new StringBuffer();

        int minutes = ( millis % (1000*60*60) ) / (1000 * 60);
        int seconds = ( ( millis % (1000*60*60) ) % (1000*60) ) / 1000;

        buf

                .append(String.format("%2d", minutes))
                .append(":")
                .append(String.format("%02d", seconds));

        return buf.toString();
    }


}
