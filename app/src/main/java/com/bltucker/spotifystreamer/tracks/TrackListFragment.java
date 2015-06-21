package com.bltucker.spotifystreamer.tracks;


import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bltucker.spotifystreamer.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


public final class TrackListFragment extends Fragment {

    private static final String TOP_TRACKS_LIST_BUNDLE_KEY = "topTracks";

    private static final String ARTIST_ID = "artistId";

    private TrackListAdapter trackListAdapter;

    @InjectView(R.id.track_list_recycler_view)
    RecyclerView trackListRecycler;

    public static TrackListFragment newInstance(String artistId) {
        TrackListFragment fragment = new TrackListFragment();
        Bundle args = new Bundle();
        args.putString(ARTIST_ID, artistId);
        fragment.setArguments(args);
        return fragment;
    }


    public TrackListFragment() {
        this.trackListAdapter = new TrackListAdapter();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_artist_track_list, container, false);
        ButterKnife.inject(this, view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        trackListRecycler.setHasFixedSize(true);
        trackListRecycler.setLayoutManager(layoutManager);
        trackListRecycler.setAdapter(trackListAdapter);

        if(savedInstanceState != null && savedInstanceState.containsKey(TOP_TRACKS_LIST_BUNDLE_KEY)){
            this.trackListAdapter.restoreDataFromBundle(savedInstanceState, TOP_TRACKS_LIST_BUNDLE_KEY);
        } else {
            this.getTracksInBackground();
        }

        return view;
    }


    private void getTracksInBackground(){
        if (getArguments() != null) {
            String artistId = getArguments().getString(ARTIST_ID);
            new GetTrackListTask().execute(artistId);
        }
    }


    class GetTrackListTask extends AsyncTask<String, Void, List<TrackItem>>{

        @Override
        protected List<TrackItem> doInBackground(String... params) {

            Log.d("LOG", "Firing network task to get tracks");

            try{
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotifyService = api.getService();

                Map<String, Object> queryMap = new HashMap<>();
                queryMap.put("country", "US");
                Tracks artistTopTracks = spotifyService.getArtistTopTrack(params[0], queryMap);

                List<TrackItem> tracks = new ArrayList<>(artistTopTracks.tracks.size());
                for(int i = 0; i < artistTopTracks.tracks.size(); i++){
                    Track track = artistTopTracks.tracks.get(i);

                    if(!track.album.images.isEmpty()){

                        String thumbnailUrl = track.album.images.get(track.album.images.size()-1).url;
                        tracks.add(new TrackItem(thumbnailUrl, track.name, track.album.name, track.preview_url));
                    }
                }

                return tracks;

            } catch(Exception ex){
                Log.e("ERROR", Log.getStackTraceString(ex));
                return new ArrayList<>();
            }

        }


        @Override
        protected void onPostExecute(List<TrackItem> trackItems) {
            TrackListFragment.this.trackListAdapter.swapDataSet(trackItems);

            if(trackItems.isEmpty() && !TrackListFragment.this.isDetached()){
                Toast.makeText(TrackListFragment.this.getActivity(), TrackListFragment.this.getString(R.string.no_artist_results), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
