package com.bltucker.spotifystreamer;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;


public class ArtistSearchFragment extends Fragment implements TextWatcher {

    private static final String LAST_SEARCH_QUERY_BUNDLE_KEY = "lastSearchQuery";

    @InjectView(R.id.artist_search_field)
    EditText artistSearchField;

    @InjectView(R.id.artist_search_recycler_view)
    RecyclerView artistSearchResultsRecycler;

    private String lastSearchQuery;

    private Runnable nextScheduledSearchRunnable;

    private final Handler scheduledSearchHandler;

    private ArtistSearchResultAdapter searchResultsAdapter;

    public ArtistSearchFragment() {
        scheduledSearchHandler = new Handler(Looper.getMainLooper());
        this.searchResultsAdapter = new ArtistSearchResultAdapter();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(LAST_SEARCH_QUERY_BUNDLE_KEY, lastSearchQuery);
        super.onSaveInstanceState(outState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist_search, container, false);
        ButterKnife.inject(this, view);

        if(savedInstanceState != null && savedInstanceState.containsKey(LAST_SEARCH_QUERY_BUNDLE_KEY)){
            artistSearchField.setText(savedInstanceState.getString(LAST_SEARCH_QUERY_BUNDLE_KEY));
            lastSearchQuery = savedInstanceState.getString(LAST_SEARCH_QUERY_BUNDLE_KEY);
        }

        artistSearchField.addTextChangedListener(this);

        artistSearchResultsRecycler.setHasFixedSize(true);
        artistSearchResultsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        artistSearchResultsRecycler.setAdapter(searchResultsAdapter);

        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.artistSearchField.removeTextChangedListener(this);
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {    }


    @Override
    public void onTextChanged(final CharSequence s, int start, int before, int count) {

        Log.d("LOG", "TEXT CHANGED");
        Log.d("LOG", "LAST QUERY: " + lastSearchQuery);
        Log.d("LOG", "NEW QUERY: " + s.toString());

        if(nextScheduledSearchRunnable != null){
            scheduledSearchHandler.removeCallbacks(nextScheduledSearchRunnable);
        }



        if(!s.toString().equals(lastSearchQuery) && !s.toString().isEmpty()){

            nextScheduledSearchRunnable = new Runnable() {
                @Override
                public void run() {
                    ArtistSearchFragment.this.lastSearchQuery = s.toString();
                    new ArtistSearchTask().execute(s.toString());
                }
            };

            scheduledSearchHandler.postDelayed(nextScheduledSearchRunnable, 300);
        }

    }


    @Override
    public void afterTextChanged(Editable s) {    }


    class ArtistSearchTask extends AsyncTask<String, Void, List<ArtistSearchResult>>{


        @Override
        protected List<ArtistSearchResult> doInBackground(String... params) {

            try{
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotifyService = api.getService();

                ArtistsPager results = spotifyService.searchArtists(params[0]);

                List<ArtistSearchResult> searchResultsList = new ArrayList<>(results.artists.items.size());
                for(int i = 0; i < results.artists.items.size(); i++){

                    Artist artist = results.artists.items.get(i);

                    if(!artist.images.isEmpty()){
                        String thumbnailUrl = artist.images.get(artist.images.size()-1).url;
                        searchResultsList.add(new ArtistSearchResult(artist.id, artist.name, thumbnailUrl));
                    }
                }

                return searchResultsList;

            } catch(Exception ex){
                Log.e("ERROR", Log.getStackTraceString(ex));
                return new ArrayList<>();
            }

        }


        @Override
        protected void onPostExecute(List<ArtistSearchResult> results) {
            ArtistSearchFragment.this.searchResultsAdapter.swapDataSet(results);

            if(results.isEmpty() && !ArtistSearchFragment.this.isDetached()){
                Toast.makeText(ArtistSearchFragment.this.getActivity(), ArtistSearchFragment.this.getString(R.string.no_artist_results), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
