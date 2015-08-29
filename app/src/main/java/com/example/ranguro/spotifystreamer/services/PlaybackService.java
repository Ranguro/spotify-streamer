package com.example.ranguro.spotifystreamer.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.example.ranguro.spotifystreamer.classes.ParcelableSpotifyTrack;
import com.example.ranguro.spotifystreamer.ui.PlayerActivityFragment;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Randall on 15/08/2015.
 */
//Used as a guide for implementation on PlaybackService and Controllers.
//http://code.tutsplus.com/tutorials/create-a-music-player-on-android-user-controls--mobile-22787 - User Control
//http://code.tutsplus.com/tutorials/create-a-music-player-on-android-song-playback--mobile-22778 - Playback Service


public class PlaybackService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnSeekCompleteListener{

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private MediaSession mediaSession;
    private MediaController mediaController;
    private MediaSessionManager mediaManager;

    private final IBinder musicBind = new PlaybackBinder();
    private static final int NOTIFY_ID=1;

    private final Handler handler = new Handler();
    public static final String BROADCAST_ACTION = "com.example.ranguro.spotifystreamer.seekprogress";

    public static boolean trackEnded;

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";


    private ArrayList<ParcelableSpotifyTrack> playlist;

    private int playlistPosition;

    private boolean isPaused = true;
    private Intent seekIntent;
    private int mediaMax;
    private int mediaPosition;


    @Override
    public void onCreate() {

        super.onCreate();
        seekIntent = new Intent(BROADCAST_ACTION);
        mediaPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mediaPlayer.reset();

        if (mediaManager == null){
            initMediaSessions();
        }
        handleMediaIntent(intent);
        //Register
        registerReceiver(seekbarReceiver, new IntentFilter(PlayerActivityFragment.BROADCAST_SEEKBAR));



        playlist = intent.getParcelableArrayListExtra("playlist");
        playlistPosition = intent.getIntExtra("position",0);

        playTrack();

        return START_STICKY;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initMediaSessions(){
        mediaSession = new MediaSession(getApplicationContext(), "simple player session");
        mediaController= new MediaController(getApplicationContext(), mediaSession.getSessionToken());
        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                //mediaPlayer.start();
                buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                mediaPlayer.stop();
                buildNotification(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT));
            }

            @Override
            public void onPause() {
                super.onPause();
                mediaPlayer.pause();
                buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                mediaPlayer.stop();
                buildNotification(generateAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS));
            }
        });
    }


    private void handleMediaIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        if (action.equalsIgnoreCase(ACTION_PLAY)) {
            mediaController.getTransportControls().play();
        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
            mediaController.getTransportControls().pause();
        } else if (action.equalsIgnoreCase(ACTION_NEXT)) {
            mediaController.getTransportControls().skipToNext();
        } else if (action.equalsIgnoreCase(ACTION_PREVIOUS)) {
            mediaController.getTransportControls().skipToPrevious();
        }else if (action.equalsIgnoreCase(ACTION_STOP)) {
            mediaController.getTransportControls().stop();
        }
    }

    private Notification.Action generateAction(int icon, String title, String intentAction){
        Intent intent = new Intent(getApplicationContext(), PlaybackService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(),1,intent,0);
        return new Notification.Action.Builder(icon,title,pendingIntent).build();

    }

    private void buildNotification(Notification.Action action){
        Notification.MediaStyle style = new Notification.MediaStyle();

        Intent intent = new Intent(getApplicationContext(), PlaybackService.class);
        intent.setAction(ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(),1,intent,0);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("Now Playing")
                .setContentText(playlist.get(playlistPosition).artistName).setDeleteIntent(pendingIntent)
                .setStyle(style);
        builder.addAction(generateAction(android.R.drawable.ic_media_previous,"Previous", ACTION_PREVIOUS));
        builder.addAction(action);
        builder.addAction(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT));
        style.setShowActionsInCompactView(0, 1, 2);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,builder.build());
    }


    private void setUpHandler() {
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 10);
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        @Override
        public void run() {
            logMediaPosition();

            handler.postDelayed(this, 10);

        }
    };

    private void logMediaPosition() {
        if(mediaPlayer.isPlaying()){
            mediaPosition = mediaPlayer.getCurrentPosition();
            mediaMax = mediaPlayer.getDuration();
            seekIntent.putExtra("media_max", String.valueOf(mediaMax));
            seekIntent.putExtra("counter", String.valueOf(mediaPosition));
            sendBroadcast(seekIntent);
        }

    }

    public void playTrack(){

        mediaPlayer.reset();


        // Set up the MediaPlayer data source using the strAudioLink value
        if (!mediaPlayer.isPlaying()) {
            ParcelableSpotifyTrack track = playlist.get(playlistPosition);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            try {
                mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(track.previewUrl));
            } catch (Exception e) {
                Log.e("MUSIC SERVICE", "Error setting data source", e);
            }
            mediaPlayer.prepareAsync();
        }
        setUpHandler();
    }

    public void stopTrack()
    {
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
    }





    // --Receive seekbar position if it has been changed by the user in the
    // activity
    private BroadcastReceiver seekbarReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateSeekPos(intent);
        }
    };

    // Update seek position from Activity
    public void updateSeekPos(Intent intent) {
        int seekPos = intent.getIntExtra("seekpos", 0);
        if (mediaPlayer.isPlaying()) {
            handler.removeCallbacks(sendUpdatesToUI);
            mediaPlayer.seekTo(seekPos);
            setUpHandler();
        }

    }




    @Override
    public boolean onUnbind(Intent intent) {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaSession.release();
        return super.onUnbind(intent);

    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        mediaPlayer.start();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null){
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }

        stopForeground(true);

        unregisterReceiver(seekbarReceiver);


        handler.removeCallbacks(sendUpdatesToUI);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }



    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //resetButtonPlayStopBroadcast();
        trackEnded = true;
        stopTrack();
        stopSelf();

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        mediaPlayer.reset();
        return false;
    }

    public int getCurrentPlaylistPosition() {
        return playlistPosition;
    }

    public void setPlaylist(ArrayList<ParcelableSpotifyTrack> playlist) {
        this.playlist = playlist;
    }

    public void setPosition(int position) {
        playlistPosition = position;

    }

    public void resume() {
        mediaPlayer.start();
    }

    public void pauseTrack() {
        mediaPlayer.pause();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

        if (!mediaPlayer.isPlaying()){
            mediaPlayer.start();
        }

    }

    public class PlaybackBinder extends Binder {
        public PlaybackService getService() {
            return PlaybackService.this;
        }
    }


    public class MusicBinder extends Binder {
        PlaybackService getService() {
            return PlaybackService.this;
        }
    }


    // Cancel Notification
    private void cancelNotification() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) getSystemService(ns);
        notificationManager.cancel(NOTIFY_ID);
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    public class UrlToImageTask extends AsyncTask<String,Void,Bitmap>{

        @Override
        protected Bitmap doInBackground(String...url) {

            return getBitmapFromURL(url[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

        }
    }

}
