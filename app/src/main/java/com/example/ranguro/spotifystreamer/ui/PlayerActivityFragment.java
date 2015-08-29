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

import com.example.ranguro.spotifystreamer.R;
import com.example.ranguro.spotifystreamer.classes.ParcelableSpotifyTrack;
import com.example.ranguro.spotifystreamer.services.PlaybackService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

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

    private boolean broadcastIsRegistered ;

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
    }

    private boolean playbackBound = false;
    private boolean trackPlaying = false;

    private ArrayList<ParcelableSpotifyTrack> spotifyTrackList;

    public static final String BROADCAST_SEEKBAR = "com.example.ranguro.spotifystreamer.sendseekbar";

    private boolean trackIsPaused = true;
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
    private boolean trackEnded;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spotifyTrackList = getArguments().getParcelableArrayList(KEY_TRACK);
        currentPosition = getArguments().getInt(KEY_CURRENT_POSITION);
        intent = new Intent(BROADCAST_SEEKBAR);
        if(playbackIntent==null){
            playbackIntent = new Intent(getActivity(), PlaybackService.class);
            playbackIntent.putParcelableArrayListExtra("playlist", spotifyTrackList);
            playbackIntent.putExtra("position", currentPosition);
            getActivity().bindService(playbackIntent, playbackConnection, Context.BIND_AUTO_CREATE);
            playbackIntent.setAction(PlaybackService.ACTION_PLAY);
        }

    }


    //connect to the service
    private ServiceConnection playbackConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlaybackService.PlaybackBinder binder = (PlaybackService.PlaybackBinder)service;
            playbackService = binder.getService();
            playbackService.setPlaylist(spotifyTrackList);
            playbackService.setPosition(currentPosition);
            playbackBound = true;
            getActivity().registerReceiver(broadcastReceiver, new IntentFilter(
                    PlaybackService.BROADCAST_ACTION));
            broadcastIsRegistered=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            getActivity().unregisterReceiver(broadcastReceiver);
            broadcastIsRegistered = false;
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

        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(
                PlaybackService.BROADCAST_ACTION));
        broadcastIsRegistered = true;
        getActivity().startService(playbackIntent);
        trackIsPaused = false;
        playTrackBtn.setImageResource(android.R.drawable.ic_media_pause);


        return rootView;
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(
                PlaybackService.BROADCAST_ACTION));
        broadcastIsRegistered = false;
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent serviceIntent) {
            updateUI(serviceIntent);
        }
    };


    private void updateUI(Intent serviceIntent) {
        String counter = serviceIntent.getStringExtra("counter");
        String mediaMax = serviceIntent.getStringExtra("media_max");
        int maxDuration = Integer.parseInt(mediaMax);
        int seekProgress = Integer.parseInt(counter);

        startDuration.setText(getTimeString(seekProgress));
        trackProgress.setMax(maxDuration);
        trackProgress.setProgress(seekProgress);

    }

    private void stopPlayerService(){
        if (broadcastIsRegistered) {
            try {
                getActivity().unregisterReceiver(broadcastReceiver);
                broadcastIsRegistered = false;
            } catch (Exception e) {
                Log.d(LOG_TAG,e.getLocalizedMessage());

            }
        }

        try{
            getActivity().stopService(playbackIntent);
        }catch (Exception e){
            Log.d(LOG_TAG, e.getLocalizedMessage());
        }
        trackPlaying = false;
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
        playTrackBtn.setImageResource(android.R.drawable.ic_media_play);
    }

    private void playPreviousTrack() {

        if(currentPosition != 0){
            currentPosition -= 1;
        }
        updatePlayerScreen();
        trackIsPaused = true;
        stopPlayerService();
        playbackService.stopTrack();
        playbackService.setPosition(currentPosition);
    }



    private void playStopTrack() {
        if (trackIsPaused){
            if (trackProgress.getProgress() != 0){
                playTrackBtn.setImageResource(android.R.drawable.ic_media_pause);
                playbackService.resume();
                trackIsPaused = false;
            }
            else{
                playTrackBtn.setImageResource(android.R.drawable.ic_media_pause);
                trackIsPaused = false;
                getActivity().registerReceiver(broadcastReceiver, new IntentFilter(
                        PlaybackService.BROADCAST_ACTION));
                broadcastIsRegistered = true;
                playbackService.playTrack();
            }
        }else{
            playTrackBtn.setImageResource(android.R.drawable.ic_media_play);
            playbackService.pauseTrack();
            trackIsPaused = true;
        }
    }
    private void playNextTrack(){

        if(currentPosition < spotifyTrackList.size()-1){
            currentPosition += 1;
        }
        trackIsPaused = true;
        playbackService.stopTrack();
        playbackService.setPosition(currentPosition);
        getActivity().unregisterReceiver(broadcastReceiver);
        broadcastIsRegistered = false;
        updatePlayerScreen();
    }



    @Override
    public void onDestroy() {

        super.onDestroy();
        if  (broadcastIsRegistered){
            getActivity().unregisterReceiver(broadcastReceiver);
            broadcastIsRegistered = true;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (fromUser) {
            int seekPos = trackProgress.getProgress();
            intent.putExtra("seekpos", seekPos);
            getActivity().sendBroadcast(intent);
        }
    }



    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

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
