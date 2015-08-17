package com.bltucker.spotifystreamer.tracks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.bltucker.spotifystreamer.R;
import com.bltucker.spotifystreamer.SettingsActivity;
import com.bltucker.spotifystreamer.playback.PlaybackActivity;
import com.bltucker.spotifystreamer.playback.PlaybackService;
import com.bltucker.spotifystreamer.playback.PlaybackServiceConnectedEvent;
import com.bltucker.spotifystreamer.playback.PlaybackServiceConnection;
import com.bltucker.spotifystreamer.playback.PlaybackSession;
import com.squareup.otto.Subscribe;

import java.util.List;

public class TrackListActivity extends Activity implements TrackListFragment.OnFragmentInteractionListener {

    private static final String ARTIST_ID_INTENT_KEY = "artistId";

    private MenuItem nowPlayingMenuItem;

    private PlaybackServiceConnection playbackServiceConnection = new PlaybackServiceConnection();

    public static void launch(Context context, String artistId){

        Intent intent = new Intent(context, TrackListActivity.class);
        intent.putExtra(ARTIST_ID_INTENT_KEY, artistId);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);

        if(null == savedInstanceState){

            if(getIntent().getExtras() != null && getIntent().getExtras().containsKey(ARTIST_ID_INTENT_KEY)){
                String artistId = getIntent().getStringExtra(ARTIST_ID_INTENT_KEY);
                getFragmentManager().beginTransaction().replace(R.id.track_list_activity_frame, TrackListFragment.newInstance(artistId)).commit();
            }

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
    protected void onResume() {
        super.onResume();
        this.setNowPlayingMenuItemVisibility();
    }

    @Override
    protected void onDestroy() {
        this.playbackServiceConnection.unbind(this);
        super.onDestroy();
    }

    @Override
    public void onTrackSelected(TrackItem selectedTrack, List<TrackItem> tracks) {
        PlaybackSession.startNewSession(selectedTrack, tracks);
        PlaybackActivity.launch(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_main, menu);
        nowPlayingMenuItem = menu.findItem(R.id.action_now_playing);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_settings){
            SettingsActivity.launch(this);
            return true;
        }

        if(id == R.id.action_now_playing){
            PlaybackActivity.launch(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Subscribe
    public void onPlaybackServiceConnected(PlaybackServiceConnectedEvent event){
        this.setNowPlayingMenuItemVisibility();
    }


    private void setNowPlayingMenuItemVisibility(){
        if(this.playbackServiceConnection.isActive() && this.playbackServiceConnection.getBoundService().isPlaying()){
            nowPlayingMenuItem.setVisible(true);
        }
    }
}
