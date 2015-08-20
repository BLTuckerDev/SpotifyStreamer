package com.bltucker.spotifystreamer.playback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ShareActionProvider;

import com.bltucker.spotifystreamer.R;
import com.bltucker.spotifystreamer.SettingsActivity;
import com.bltucker.spotifystreamer.tracks.TrackItem;

public class PlaybackActivity extends Activity implements PlaybackServiceConnectionProvider {

    private ShareActionProvider shareActionProvider;

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_playback, menu);

        MenuItem shareItem = menu.findItem(R.id.menu_item_share);
        shareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
        this.updateShareProviderIntent();

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_settings){
            SettingsActivity.launch(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateShareProviderIntent(){

        if(shareActionProvider == null){
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
