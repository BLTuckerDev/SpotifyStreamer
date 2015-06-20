package com.bltucker.spotifystreamer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public final class ArtistSearchResultAdapter extends RecyclerView.Adapter<ArtistSearchResultAdapter.ViewHolder>{

    private final List<ArtistSearchResult> searchResults;

    public ArtistSearchResultAdapter(){
        this.searchResults = new ArrayList<>(10);
    }


    public void swapDataSet(List<ArtistSearchResult> updatedSearchResults){
        this.searchResults.clear();
        this.searchResults.addAll(updatedSearchResults);
        this.notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_search_result_item, parent, false);
        return new ViewHolder(rootView);
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


        public ViewHolder(View rootView){
            super(rootView);

            this.thumbnailImageView = (ImageView) rootView.findViewById(R.id.artist_search_result_item_thumbnail);
            this.artistNameTextView = (TextView) rootView.findViewById(R.id.artist_search_result_item_title);
        }
    }
}
