package com.bltucker.spotifystreamer.tracks;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
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


public final class TrackListFragment extends Fragment implements TrackListAdapter.OnClickListener {


    public interface OnFragmentInteractionListener {
        void onTrackSelected(TrackItem selectedTrack, List<TrackItem> tracks);
    }

    private static final String TOP_TRACKS_LIST_BUNDLE_KEY = "topTracks";
    private static final String LAST_SCROLL_SCROLL_POSITION_BUNDLE_KEY = "scrollPosition";

    private static final String ARTIST_ID = "artistId";

    private TrackListAdapter trackListAdapter;
    private OnFragmentInteractionListener fragmentInteractionListener;

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
        this.trackListAdapter = new TrackListAdapter(this);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LinearLayoutManager layoutManager = (LinearLayoutManager) this.trackListRecycler.getLayoutManager();

        if(layoutManager != null){
            outState.putInt(LAST_SCROLL_SCROLL_POSITION_BUNDLE_KEY, layoutManager.findFirstCompletelyVisibleItemPosition());
        }

        trackListAdapter.saveDataToBundle(outState, TOP_TRACKS_LIST_BUNDLE_KEY);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            fragmentInteractionListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_artist_track_list, container, false);
        ButterKnife.inject(this, view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        trackListRecycler.setHasFixedSize(true);
        trackListRecycler.setLayoutManager(layoutManager);
        trackListRecycler.setAdapter(trackListAdapter);

        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null && savedInstanceState.containsKey(TOP_TRACKS_LIST_BUNDLE_KEY)){
            this.trackListAdapter.restoreDataFromBundle(savedInstanceState, TOP_TRACKS_LIST_BUNDLE_KEY);

            if(savedInstanceState.containsKey(LAST_SCROLL_SCROLL_POSITION_BUNDLE_KEY)){
                trackListRecycler.scrollToPosition(savedInstanceState.getInt(LAST_SCROLL_SCROLL_POSITION_BUNDLE_KEY));
            }

        } else {
            this.getTracksInBackground();
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        fragmentInteractionListener = null;
    }

    private void getTracksInBackground(){
        if (getArguments() != null) {
            String artistId = getArguments().getString(ARTIST_ID);
            new GetTrackListTask().execute(artistId);
        }
    }


    @Override
    public void onClick(TrackItem selectedTrack, List<TrackItem> tracks) {

        if(fragmentInteractionListener != null){
            fragmentInteractionListener.onTrackSelected(selectedTrack, tracks);
        }
    }

    class GetTrackListTask extends AsyncTask<String, Void, List<TrackItem>>{

        @Override
        protected List<TrackItem> doInBackground(String... params) {

            try{
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotifyService = api.getService();

                Map<String, Object> queryMap = new HashMap<>();
                Context context = TrackListFragment.this.getActivity();

                if(context != null){

                    String countryCodePreference = PreferenceManager.getDefaultSharedPreferences(context)
                            .getString(context.getString(R.string.preference_country_code_key), "US");

                    queryMap.put("country", countryCodePreference);
                } else {
                    queryMap.put("country", "US");
                }

                Tracks artistTopTracks = spotifyService.getArtistTopTrack(params[0], queryMap);

                List<TrackItem> tracks = new ArrayList<>(artistTopTracks.tracks.size());
                for(int i = 0; i < artistTopTracks.tracks.size(); i++){
                    Track track = artistTopTracks.tracks.get(i);

                    if(!track.album.images.isEmpty()){

                        String thumbnailUrl = track.album.images.get(track.album.images.size()-1).url;
                        String nowPlayingUrl = track.album.images.get(0).url;
                        String externalUrl = track.external_urls.get("spotify");
                        tracks.add(new TrackItem(thumbnailUrl, nowPlayingUrl, track.name, track.album.name, track.artists.get(0).name, track.preview_url, externalUrl));
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
