package com.example.ranguro.spotifystreamer.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.example.ranguro.spotifystreamer.classes.ParcelableSpotifyTrack;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

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

    public static final String UPDATE_UI = "com.example.ranguro.spotifystreamer.services.UPDATE_UI";
    public static final String RESET_PLAY_BUTTON = "com.example.ranguro.spotifystreamer.services.RESET_PLAY_BUTTON";




    private String playingTrackUrl;

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";


    private ArrayList<ParcelableSpotifyTrack> playlist;

    private int playlistPosition;

    private Intent seekIntent;
    private int mediaMax;
    private int mediaPosition;


    @Override
    public void onCreate() {

        super.onCreate();
        seekIntent = new Intent(UPDATE_UI);
        mediaPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mediaManager == null) {
            Log.d(TAG, "onStartCommand: ");
            initMediaSessions();
        }
        handleMediaIntent(intent);

        setUpHandler();

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
        notificationManager.notify(1, builder.build());
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
            seekIntent.setAction(UPDATE_UI);
            seekIntent.putExtra("media_max", String.valueOf(mediaMax));
            seekIntent.putExtra("counter", String.valueOf(mediaPosition));
            sendBroadcast(seekIntent);
        }

    }

    public void startMediaPlayer(String previewUrl){
        playingTrackUrl = previewUrl;
        mediaPlayer.reset();
        // Set up the MediaPlayer data source using the strAudioLink value
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(playingTrackUrl));
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(this);
    }

    public void playTrack(String previewUrl) {

        //Track changed.
        if(mediaPlayer != null){
            if (!previewUrl.equals(playingTrackUrl)) {
                startMediaPlayer(previewUrl);
            } else {
                //Track has not changed.
                if (!mediaPlayer.isPlaying()) {

                    if (mediaPlayer.getCurrentPosition() >= mediaPlayer.getDuration()){
                        resume(0);
                    }
                }
            }
        }
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
        System.out.println("Pos: " + seekPos);
        if (mediaPlayer.isPlaying()) {
            handler.removeCallbacks(sendUpdatesToUI);
            mediaPlayer.seekTo(seekPos);
            setUpHandler();
        }

    }




    @Override
    public boolean onUnbind(Intent intent) {

        handler.removeCallbacks(sendUpdatesToUI);
        mediaSession.release();
        return super.onUnbind(intent);

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if(!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(sendUpdatesToUI);
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        mediaPlayer.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }


    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        Intent resetPlayButtonIntent = new Intent(RESET_PLAY_BUTTON);
        sendBroadcast(resetPlayButtonIntent);
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

    public void resume(int progress) {
        mediaPlayer.seekTo(progress);
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

}
