package com.bltucker.spotifystreamer.tracks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.bltucker.spotifystreamer.R;

public class TrackListActivity extends Activity {

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

        if(getIntent().getExtras() != null && getIntent().getExtras().containsKey(ARTIST_ID_INTENT_KEY)){
            String artistId = getIntent().getStringExtra(ARTIST_ID_INTENT_KEY);
            getFragmentManager().beginTransaction().replace(R.id.track_list_activity_frame, TrackListFragment.newInstance(artistId)).commit();
        }
    }

}
