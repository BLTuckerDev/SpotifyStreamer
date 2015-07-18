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

    private static final String SONG_LIST_INTENT_KEY = "songs";
    private static final String SELECTED_TRACK = "selectedTrack";

    private static final String LOG_TAG = PlaybackActivity.class.getSimpleName();


    public static void launch(Context context, TrackItem selectedTrack, List<TrackItem> tracks){
        Intent intent = new Intent(context, PlaybackActivity.class);
        intent.putParcelableArrayListExtra(SONG_LIST_INTENT_KEY, new ArrayList<>(tracks));
        intent.putExtra(SELECTED_TRACK, selectedTrack);
        context.startActivity(intent);
    }


    private PlaybackServiceConnection playbackServiceConnection = new PlaybackServiceConnection();

    private List<TrackItem> trackList;
    private TrackItem currentTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        if(null == savedInstanceState){
            if(isValidIntent()){
                this.trackList = getIntent().getExtras().getParcelableArrayList(SONG_LIST_INTENT_KEY);
                this.currentTrack = (TrackItem) getIntent().getExtras().get(SELECTED_TRACK);
                getFragmentManager().beginTransaction().replace(R.id.playback_activity_frame_container, PlaybackFragment.newInstance()).commit();
            }
        }

    }

    private boolean isValidIntent(){

        if(getIntent().getExtras() == null){
            return false;
        }

        boolean containsTrackList = getIntent().getExtras().containsKey(SONG_LIST_INTENT_KEY);
        boolean containsSelectedTrack = getIntent().getExtras().containsKey(SELECTED_TRACK);

        return containsTrackList && containsSelectedTrack;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_playback, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackButtonClick() {

        int indexOfPreviousTrack = this.trackList.indexOf(currentTrack) - 1;

        if(indexOfPreviousTrack < 0){
            indexOfPreviousTrack = this.trackList.size() -1;
        }

        TrackItem previousTrack = this.trackList.get(indexOfPreviousTrack);
        this.playTrack(previousTrack);
    }


    @Override
    public void onForwardButtonClick() {

        int indexOfNextTrack = this.trackList.indexOf(currentTrack) + 1;

        if(indexOfNextTrack >= this.trackList.size()){
            indexOfNextTrack = 0;
        }

        this.playTrack(this.trackList.get(indexOfNextTrack));
    }


    @Override
    public void onPlayButtonClick() {
        PlaybackService playbackService = this.playbackServiceConnection.getBoundService();

        if(playbackService.isPlaying()){
            playbackService.resumeSong();
        } else {
            this.playTrack(currentTrack);
        }
    }


    @Override
    public void onPauseButtonClick() {
        this.playbackServiceConnection.getBoundService().pauseSong();
    }


    private void playTrack(TrackItem track){
        //send info to the fragment.

        try {
            this.playbackServiceConnection.getBoundService().playSong(Uri.parse(track.previewUrl));
            this.currentTrack = track;

        } catch (IOException e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
        }
    }


}
