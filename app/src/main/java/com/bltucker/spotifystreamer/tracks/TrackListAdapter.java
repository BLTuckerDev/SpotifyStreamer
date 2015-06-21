package com.bltucker.spotifystreamer.tracks;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bltucker.spotifystreamer.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

final class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.ViewHolder> {

    private final List<TrackItem> tracks;

    public TrackListAdapter(){
        this.tracks = new ArrayList<>(10);
    }


    public void swapDataSet(List<TrackItem> tracks){
        this.tracks.clear();
        this.tracks.addAll(tracks);
        this.notifyDataSetChanged();
    }


    public void saveDataToBundle(Bundle bundle, String bundleKey){
        bundle.putParcelableArrayList(bundleKey, new ArrayList<TrackItem>(this.tracks));
    }


    public void restoreDataFromBundle(Bundle savedInstanceState, String topTracksListBundleKey) {
        ArrayList<TrackItem> parcelableArrayList = savedInstanceState.getParcelableArrayList(topTracksListBundleKey);
        this.tracks.addAll(parcelableArrayList);
        this.notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_item, parent, false);
        return new ViewHolder(rootView);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        TrackItem item = this.tracks.get(position);

        holder.trackTitleTextView.setText(item.trackTitle);
        holder.trackAlbumTextView.setText(item.trackAlbumTitle);
        Picasso.with(holder.trackThumbnailImageView.getContext()).load(item.trackThumbnailurl).into(holder.trackThumbnailImageView);
    }


    @Override
    public int getItemCount() {
        return this.tracks.size();
    }





    public class ViewHolder extends RecyclerView.ViewHolder{

        public final ImageView trackThumbnailImageView;
        public final TextView trackTitleTextView;
        public final TextView trackAlbumTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            this.trackThumbnailImageView = (ImageView) itemView.findViewById(R.id.track_item_thumbnail);
            this.trackTitleTextView = (TextView) itemView.findViewById(R.id.track_item_track_title);
            this.trackAlbumTextView = (TextView) itemView.findViewById(R.id.track_item_album_title);
        }
    }
}
