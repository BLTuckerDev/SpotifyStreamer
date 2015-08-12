package com.bltucker.spotifystreamer.tracks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bltucker.spotifystreamer.R;
import com.bltucker.spotifystreamer.playback.PlaybackActivity;
import com.bltucker.spotifystreamer.playback.PlaybackSession;

import java.util.List;

public class TrackListActivity extends Activity implements TrackListFragment.OnFragmentInteractionListener {

    private static final String ARTIST_ID_INTENT_KEY = "artistId";

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
    public void onTrackSelected(TrackItem selectedTrack, List<TrackItem> tracks) {
        PlaybackSession.startNewSession(selectedTrack, tracks);
        PlaybackActivity.launch(this);
    }
}
