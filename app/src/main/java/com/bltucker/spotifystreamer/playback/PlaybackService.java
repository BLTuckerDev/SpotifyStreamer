package com.bltucker.spotifystreamer.playback;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;


import java.io.IOException;

public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    public class PlaybackServiceBinder extends Binder {

        public PlaybackService getService(){
            return PlaybackService.this;
        }

    }


    private final IBinder serviceBinder = new PlaybackServiceBinder();

    private MediaPlayer mediaPlayer;

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
        this.mediaPlayer.setDataSource(getApplicationContext(), songUri);
        this.mediaPlayer.prepareAsync();
    }


    public void pauseSong(){
        this.mediaPlayer.pause();
    }


    public void resumeSong(){
        this.mediaPlayer.start();
    }


    public boolean isPlaying(){
        return this.mediaPlayer.isPlaying();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }
}
