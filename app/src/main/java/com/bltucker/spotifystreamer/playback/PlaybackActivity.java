package com.bltucker.spotifystreamer.playback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bltucker.spotifystreamer.R;

public class PlaybackActivity extends Activity implements PlaybackServiceConnectionProvider {

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
    public PlaybackServiceConnection getPlaybackServiceConnection() {
        return this.playbackServiceConnection;
    }
}
