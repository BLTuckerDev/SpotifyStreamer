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
import com.bltucker.spotifystreamer.playback.PlaybackActivity;
import com.bltucker.spotifystreamer.playback.PlaybackSession;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

final class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.ViewHolder> {

    public interface OnClickListener{
        void onClick(TrackItem selectedTrack, List<TrackItem> tracks);
    }

    private final OnClickListener listener;
    private final List<TrackItem> tracks;

    public TrackListAdapter(OnClickListener listener){
        this.tracks = new ArrayList<>(10);
        this.listener = listener;
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
        return new ViewHolder(rootView, listener);
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

        public ViewHolder(View itemView, final OnClickListener listener) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<TrackItem> tracks = TrackListAdapter.this.tracks;
                    TrackItem selectedTrack = tracks.get(getLayoutPosition());
                    listener.onClick(selectedTrack, tracks);
                }
            });

            this.trackThumbnailImageView = (ImageView) itemView.findViewById(R.id.track_item_thumbnail);
            this.trackTitleTextView = (TextView) itemView.findViewById(R.id.track_item_track_title);
            this.trackAlbumTextView = (TextView) itemView.findViewById(R.id.track_item_album_title);
        }
    }
}
