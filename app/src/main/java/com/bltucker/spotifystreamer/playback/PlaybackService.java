package com.bltucker.spotifystreamer.playback;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;


import com.bltucker.spotifystreamer.EventBus;
import com.bltucker.spotifystreamer.R;
import com.bltucker.spotifystreamer.tracks.TrackItem;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;

public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private static final String LOG_TAG = PlaybackService.class.getSimpleName();

    protected static final String MEDIA_SESSION_NAME = "mediaSession";

    protected static final String PLAY_INTENT = "dev.bltucker.spotifystreamer.notification.play";
    protected static final String PAUSE_INTENT = "dev.bltucker.spotifystreamer.notification.pause";
    protected static final String NEXT_INTENT = "dev.bltucker.spotifystreamer.notification.next";
    protected static final String PREVIOUS_INTENT = "dev.bltucker.spotifystreamer.notification.previous";

    protected static final int PENDING_INTENTS_REQUEST_CODE = 1;

    protected static final int PLAYBACK_CONTROLS_NOTIFICATION_ID = 1;

    public class PlaybackServiceBinder extends Binder {

        public PlaybackService getService(){
            return PlaybackService.this;
        }

    }

    //TODO handle playback session change event
    //TODO lets use the current track change events as well rather relying on the activity to tell us what to do.

    private final IBinder serviceBinder = new PlaybackServiceBinder();

    private MediaPlayer mediaPlayer;
    private boolean isPaused = false;
    private boolean canSeek = false;
    private Uri currentlyPlayingUri;

    private final Handler playbackTimeHandler = new Handler();

    public PlaybackService() {    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.mediaPlayer = new MediaPlayer();

        this.mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        this.mediaPlayer.setOnPreparedListener(this);
        this.mediaPlayer.setOnCompletionListener(this);
        this.mediaPlayer.setOnErrorListener(this);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        this.handlePotentialNotificationIntent(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    private void handlePotentialNotificationIntent(Intent intent){

        if(null == intent){
            return;
        }

        String intentAction = intent.getAction();

        if(null == intentAction){
            return;
        }


        if(intentAction.equals(PLAY_INTENT)){

            this.resumeSong();

        } else if(intentAction.equals(PAUSE_INTENT)){

            this.pauseSong();

        } else if(intentAction.equals(NEXT_INTENT)){

            TrackItem trackItem = PlaybackSession.getCurrentSession().advanceToNextTrack();
            try {
                this.playSong(Uri.parse(trackItem.previewUrl));
            } catch (IOException e) {
                Log.e(LOG_TAG, Log.getStackTraceString(e));
            }

        } else if(intentAction.equals(PREVIOUS_INTENT)){

            TrackItem trackItem = PlaybackSession.getCurrentSession().returnToPreviousTrack();
            try {
                this.playSong(Uri.parse(trackItem.previewUrl));
            } catch (IOException e) {
                Log.e(LOG_TAG, Log.getStackTraceString(e));
            }
        }


    }

    @Override
    public void onDestroy() {
        this.mediaPlayer.release();
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return this.serviceBinder;
    }




    public void playSong(Uri songUri) throws IOException {
        this.mediaPlayer.reset();
        this.isPaused = false;
        this.currentlyPlayingUri = songUri;
        this.mediaPlayer.setDataSource(getApplicationContext(), songUri);
        this.mediaPlayer.prepareAsync();
    }


    public void pauseSong(){
        this.mediaPlayer.pause();
        this.isPaused = true;
    }


    public void resumeSong(){
        this.mediaPlayer.start();
        this.startPlaybackUpdates(this.mediaPlayer);
        this.isPaused = false;
    }


    public void stopSong(){
        this.isPaused = false;
        this.mediaPlayer.stop();
    }

    public void seekTo(int progress) {

        if(this.canSeek){
            this.mediaPlayer.seekTo(progress);
        }

    }

    public boolean isPaused(){
        return this.isPaused;
    }

    public boolean isPlaying(){
        return this.mediaPlayer.isPlaying();
    }


    public Uri getCurrentlyPlayingUri() {
        return currentlyPlayingUri;
    }


    public int getCurrentPlaybackPosition(){
        return this.mediaPlayer.getCurrentPosition();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        TrackItem nextTrack = PlaybackSession.getCurrentSession().advanceToNextTrack();
        try {
            this.canSeek = false;
            this.playSong(Uri.parse(nextTrack.previewUrl));
        } catch (IOException e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
        }
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        Toast.makeText(this, getString(R.string.media_player_error), Toast.LENGTH_LONG).show();
        mp.reset();
        return true;
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        this.canSeek = true;
        this.startPlaybackUpdates(mp);
        this.setupNotificationControls();
    }


    private void startPlaybackUpdates(final MediaPlayer mediaPlayer){

        playbackTimeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (mediaPlayer.isPlaying()) {
                    //update the time and post again
                    EventBus.getEventBus().fireEvent(new PlaybackStatusUpdateEvent(mediaPlayer.getCurrentPosition()));
                    playbackTimeHandler.postDelayed(this, 500);
                }
            }
        }, 500);

    }


    private void setupNotificationControls(){

        final PlaybackService that = this;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(that);

                if(!defaultSharedPreferences.getBoolean(getString(R.string.preference_show_notification_controls_key), true)){
                    return null;
                }

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                PlaybackSession currentSession = PlaybackSession.getCurrentSession();
                TrackItem currentTrack = currentSession.getCurrentTrack();

                Intent blankIntent = new Intent(that, PlaybackService.class);
                PendingIntent contentIntent = PendingIntent.getService(that, PENDING_INTENTS_REQUEST_CODE, blankIntent, 0);


                Notification.Builder builder = null;
                try {
                    builder = new Notification.Builder(that)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(currentTrack.trackAlbumTitle)
                            .setContentText(currentTrack.trackTitle)
                            .setContentIntent(contentIntent)
                            .setLargeIcon(Picasso.with(that).load(currentTrack.trackThumbnailurl).get())
                            .setStyle(new Notification.MediaStyle())
                            .setOngoing(true);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }


                Intent playIntent = new Intent(that, PlaybackService.class);
                playIntent.setAction(PLAY_INTENT);
                Intent pauseIntent = new Intent(that, PlaybackService.class);
                pauseIntent.setAction(PAUSE_INTENT);
                Intent nextIntent = new Intent(that, PlaybackService.class);
                nextIntent.setAction(NEXT_INTENT);
                Intent previousIntent = new Intent(that, PlaybackService.class);
                previousIntent.setAction(PREVIOUS_INTENT);


                PendingIntent pendingPreviousIntent = PendingIntent.getService(that, PENDING_INTENTS_REQUEST_CODE, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent pendingPlayingIntent = PendingIntent.getService(that, PENDING_INTENTS_REQUEST_CODE, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent pausePendingIntent = PendingIntent.getService(that, PENDING_INTENTS_REQUEST_CODE, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent pendingNextIntent = PendingIntent.getService(that, PENDING_INTENTS_REQUEST_CODE, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                builder.addAction(new Notification.Action(android.R.drawable.ic_media_previous, "Previous", pendingPreviousIntent));
                builder.addAction(new Notification.Action(android.R.drawable.ic_media_play, "Play", pendingPlayingIntent));
                builder.addAction(new Notification.Action(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent));
                builder.addAction(new Notification.Action(android.R.drawable.ic_media_next, "Next", pendingNextIntent));

                notificationManager.notify(PlaybackService.PLAYBACK_CONTROLS_NOTIFICATION_ID, builder.build());

                return null;
            }
        }.execute();



    }

}
