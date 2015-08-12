package com.bltucker.spotifystreamer.playback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.bltucker.spotifystreamer.R;
import com.bltucker.spotifystreamer.tracks.TrackItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class PlaybackActivity extends Activity implements PlaybackFragment.PlaybackFragmentListener{

    private static final String LOG_TAG = PlaybackActivity.class.getSimpleName();

    public static void launch(Context context){
        Intent intent = new Intent(context, PlaybackActivity.class);
        context.startActivity(intent);
    }

    private PlaybackServiceConnection playbackServiceConnection = new PlaybackServiceConnection();

    //region ActivityLifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        if(null == savedInstanceState){
            getFragmentManager().beginTransaction().replace(R.id.playback_activity_frame_container, PlaybackFragment.newInstance()).commit();
        }

    }


    @Override
    protected void onStart() {
        super.onStart();

        Intent playbackServiceIntent = new Intent(this, PlaybackService.class);
        bindService(playbackServiceIntent, playbackServiceConnection, Context.BIND_AUTO_CREATE);
        startService(playbackServiceIntent);
    }


    @Override
    protected void onDestroy() {
        this.playbackServiceConnection.unbind(this);
        super.onDestroy();
    }

    //endregion

    //region FragmentListenerMethods

    @Override
    public void onBackButtonClick() {
        this.playTrack(PlaybackSession.getCurrentSession().returnToPreviousTrack());
    }


    @Override
    public void onForwardButtonClick() {
        this.playTrack(PlaybackSession.getCurrentSession().advanceToNextTrack());
    }


    @Override
    public void onPlayButtonClick() {
        PlaybackService playbackService = this.playbackServiceConnection.getBoundService();

        if(playbackService.isPaused()){
            playbackService.resumeSong();
        } else {
            this.playTrack(PlaybackSession.getCurrentSession().getCurrentTrack());
        }
    }


    @Override
    public void onPauseButtonClick() {
        this.playbackServiceConnection.getBoundService().pauseSong();
    }


    @Override
    public void onFragmentDismissed() {
        //nothing to do.
    }

    //endregion


    private void playTrack(TrackItem track){
        try {
            this.playbackServiceConnection.getBoundService().playSong(Uri.parse(track.previewUrl));
        } catch (IOException e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
        }
    }

}
