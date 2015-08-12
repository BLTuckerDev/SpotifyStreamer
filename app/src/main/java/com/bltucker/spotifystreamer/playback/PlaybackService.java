package com.bltucker.spotifystreamer.playback;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;


import com.bltucker.spotifystreamer.EventBus;
import com.bltucker.spotifystreamer.R;
import com.bltucker.spotifystreamer.tracks.TrackItem;

import java.io.IOException;

public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private static final String LOG_TAG = PlaybackService.class.getSimpleName();

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
    public IBinder onBind(Intent intent) {
        return this.serviceBinder;
    }

    //TODO do we need to dispose in onUnbind?

    public void playSong(Uri songUri) throws IOException {
        this.mediaPlayer.reset();
        this.isPaused = false;
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

    public boolean isPaused(){
        return this.isPaused;
    }

    public boolean isPlaying(){
        return this.mediaPlayer.isPlaying();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        TrackItem nextTrack = PlaybackSession.getCurrentSession().advanceToNextTrack();
        try {
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
        this.startPlaybackUpdates(mp);
    }


    private void startPlaybackUpdates(final MediaPlayer mediaPlayer){

        playbackTimeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if(mediaPlayer.isPlaying()){
                    //update the time and post again
                    EventBus.getEventBus().fireEvent(new PlaybackStatusUpdateEvent(mediaPlayer.getCurrentPosition()));
                    playbackTimeHandler.postDelayed(this, 500);
                }
            }
        }, 500);

    }
}
