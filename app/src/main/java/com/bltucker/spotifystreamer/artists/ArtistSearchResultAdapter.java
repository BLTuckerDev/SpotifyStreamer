package com.bltucker.spotifystreamer.artists;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bltucker.spotifystreamer.R;
import com.bltucker.spotifystreamer.tracks.TrackListActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

final class ArtistSearchResultAdapter extends RecyclerView.Adapter<ArtistSearchResultAdapter.ViewHolder>{


    private final OnClickListener listener;

    public interface OnClickListener{
        void onClick(View v, String artistId);
    }


    private final List<ArtistSearchResult> searchResults;

    public ArtistSearchResultAdapter(ArtistSearchResultAdapter.OnClickListener listener){
        this.listener = listener;
        this.searchResults = new ArrayList<>(10);
    }


    public void swapDataSet(List<ArtistSearchResult> updatedSearchResults){
        this.searchResults.clear();
        this.searchResults.addAll(updatedSearchResults);
        this.notifyDataSetChanged();
    }


    public void saveDataToBundle(Bundle bundle, String bundleKey) {
        bundle.putParcelableArrayList(bundleKey, new ArrayList<>(searchResults));
    }


    public void restoreDataFromBundle(Bundle bundle, String bundleKey){

        ArrayList<ArtistSearchResult> parcelableArrayList = bundle.getParcelableArrayList(bundleKey);
        this.searchResults.addAll(parcelableArrayList);
        this.notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_search_result_item, parent, false);
        return new ViewHolder(rootView, this.listener);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ArtistSearchResult item = this.searchResults.get(position);
        holder.artistNameTextView.setText(item.artistName);
        Picasso.with(holder.thumbnailImageView.getContext()).load(item.thumbnailUriString).into(holder.thumbnailImageView);
    }


    @Override
    public int getItemCount() {
        return this.searchResults.size();
    }



    public class ViewHolder  extends RecyclerView.ViewHolder{

        public final ImageView thumbnailImageView;
        public final TextView artistNameTextView;

        public ViewHolder(View rootView, final ArtistSearchResultAdapter.OnClickListener clickListener){
            super(rootView);

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String artistId = ArtistSearchResultAdapter.this.searchResults.get(getLayoutPosition()).id;
                    clickListener.onClick(v, artistId);
                }
            });

            this.thumbnailImageView = (ImageView) rootView.findViewById(R.id.artist_search_result_item_thumbnail);
            this.artistNameTextView = (TextView) rootView.findViewById(R.id.artist_search_result_item_title);
        }

    }
}
