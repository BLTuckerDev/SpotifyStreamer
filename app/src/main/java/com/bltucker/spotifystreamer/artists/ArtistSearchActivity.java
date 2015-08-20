package com.bltucker.spotifystreamer.artists;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ShareActionProvider;

import com.bltucker.spotifystreamer.EventBus;
import com.bltucker.spotifystreamer.R;
import com.bltucker.spotifystreamer.SettingsActivity;
import com.bltucker.spotifystreamer.playback.PlaybackActivity;
import com.bltucker.spotifystreamer.playback.PlaybackFragment;
import com.bltucker.spotifystreamer.playback.PlaybackService;
import com.bltucker.spotifystreamer.playback.PlaybackServiceConnectedEvent;
import com.bltucker.spotifystreamer.playback.PlaybackServiceConnection;
import com.bltucker.spotifystreamer.playback.PlaybackServiceConnectionProvider;
import com.bltucker.spotifystreamer.playback.PlaybackSession;
import com.bltucker.spotifystreamer.tracks.TrackItem;
import com.bltucker.spotifystreamer.tracks.TrackListActivity;
import com.bltucker.spotifystreamer.tracks.TrackListFragment;
import com.squareup.otto.Subscribe;

import java.util.List;


public class ArtistSearchActivity extends Activity implements ArtistSearchFragment.OnFragmentInteractionListener,
        TrackListFragment.OnFragmentInteractionListener, PlaybackServiceConnectionProvider {

    private static final String LOG_TAG = ArtistSearchActivity.class.getSimpleName();

    private boolean twoPaneMode = false;
    private MenuItem nowPlayingMenuItem;
    private MenuItem shareMenuItem;

    private ShareActionProvider shareActionProvider;

    private PlaybackServiceConnection playbackServiceConnection = new PlaybackServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getEventBus().register(this);

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
    protected void onResume() {
        super.onResume();
        this.setNowPlayingMenuItemVisibility();

        if(PlaybackSession.getCurrentSession() != null){
            this.setSharingMenuItemVisibility();
        }

    }


    @Override
    protected void onDestroy() {
        EventBus.getEventBus().unregister(this);
        this.playbackServiceConnection.unbind(this);
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_main, menu);
        nowPlayingMenuItem = menu.findItem(R.id.action_now_playing);
        shareMenuItem = menu.findItem(R.id.menu_item_share);

        shareActionProvider = (ShareActionProvider) shareMenuItem.getActionProvider();
        this.updateShareProviderIntent();

        this.setNowPlayingMenuItemVisibility();

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
            playbackFragment.setStartedInDialogMode(true);
            playbackFragment.show(getFragmentManager(), "playback");
            this.setSharingMenuItemVisibility();
            this.updateShareProviderIntent();
        } else {
            PlaybackActivity.launch(this);
        }

    }

    @Subscribe
    public void onPlaybackServiceConnected(PlaybackServiceConnectedEvent event){
        this.setNowPlayingMenuItemVisibility();
    }


    private void setNowPlayingMenuItemVisibility(){

        if(this.nowPlayingMenuItem == null){
            return;
        }

        if(this.playbackServiceConnection.isActive() && !twoPaneMode && (this.playbackServiceConnection.getBoundService().isPlaying() || this.playbackServiceConnection.getBoundService().isPaused())){
            nowPlayingMenuItem.setVisible(true);
        }
    }

    private void setSharingMenuItemVisibility(){

        if(this.shareMenuItem != null && twoPaneMode){
            shareMenuItem.setVisible(true);
        }

    }

    private void updateShareProviderIntent(){

        if(shareActionProvider == null){
            return;
        }

        PlaybackSession session = PlaybackSession.getCurrentSession();

        if(session == null){
            return;
        }

        TrackItem currentTrack = PlaybackSession.getCurrentSession().getCurrentTrack();

        if(currentTrack == null){
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.list_to_this));
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.putExtra(Intent.EXTRA_TEXT, currentTrack.externalShareUrl);

        this.shareActionProvider.setShareIntent(shareIntent);
    }

    @Override
    public PlaybackServiceConnection getPlaybackServiceConnection() {
        return this.playbackServiceConnection;
    }

}
