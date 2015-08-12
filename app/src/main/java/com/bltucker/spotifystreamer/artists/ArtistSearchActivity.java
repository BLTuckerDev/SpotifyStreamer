package com.bltucker.spotifystreamer.artists;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.bltucker.spotifystreamer.R;
import com.bltucker.spotifystreamer.playback.PlaybackActivity;
import com.bltucker.spotifystreamer.playback.PlaybackFragment;
import com.bltucker.spotifystreamer.playback.PlaybackService;
import com.bltucker.spotifystreamer.playback.PlaybackServiceConnection;
import com.bltucker.spotifystreamer.playback.PlaybackSession;
import com.bltucker.spotifystreamer.tracks.TrackItem;
import com.bltucker.spotifystreamer.tracks.TrackListActivity;
import com.bltucker.spotifystreamer.tracks.TrackListFragment;

import java.io.IOException;
import java.util.List;


public class ArtistSearchActivity extends Activity implements ArtistSearchFragment.OnFragmentInteractionListener,
        TrackListFragment.OnFragmentInteractionListener, PlaybackFragment.PlaybackFragmentListener {

    private static final String LOG_TAG = ArtistSearchActivity.class.getSimpleName();

    private boolean twoPaneMode = false;

    private PlaybackServiceConnection playbackServiceConnection = new PlaybackServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(findViewById(R.id.main_activity_track_list_fragment) != null){

            twoPaneMode = true;
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


    @Override
    public void onArtistSelected(String artistId) {

        if(twoPaneMode){
            getFragmentManager().beginTransaction().replace(R.id.main_activity_track_list_fragment, TrackListFragment.newInstance(artistId)).commit();
        } else {
            TrackListActivity.launch(this, artistId);
        }


    }


    @Override
    public void onTrackSelected(TrackItem selectedTrack, List<TrackItem> tracks) {

        PlaybackSession.startNewSession(selectedTrack, tracks);

        if(twoPaneMode){
            PlaybackFragment playbackFragment = PlaybackFragment.newInstance();
            playbackFragment.show(getFragmentManager(), "playback");
        } else {
            PlaybackActivity.launch(this);
        }

    }


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
        this.playbackServiceConnection.getBoundService().stopSong();
    }


    private void playTrack(TrackItem track){
        try {
            this.playbackServiceConnection.getBoundService().playSong(Uri.parse(track.previewUrl));
        } catch (IOException e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
        }
    }
}
