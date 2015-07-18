package com.bltucker.spotifystreamer.tracks;

import android.os.Parcel;
import android.os.Parcelable;

public final class TrackItem implements Parcelable{

    public final String trackThumbnailurl;
    public final String trackTitle;
    public final String trackAlbumTitle;
    public final String previewUrl;


    public TrackItem(String trackThumbnailurl, String trackTitle, String trackAlbumTitle, String previewUrl){

        this.trackThumbnailurl = trackThumbnailurl;
        this.trackTitle = trackTitle;
        this.trackAlbumTitle = trackAlbumTitle;
        this.previewUrl = previewUrl;
    }

    private TrackItem(Parcel parcel){
        this.trackThumbnailurl = parcel.readString();
        this.trackTitle = parcel.readString();
        this.trackAlbumTitle = parcel.readString();
        this.previewUrl = parcel.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.trackThumbnailurl);
        dest.writeString(this.trackTitle);
        dest.writeString(this.trackAlbumTitle);
        dest.writeString(this.previewUrl);
    }

    public static final Parcelable.Creator<TrackItem> CREATOR = new Parcelable.Creator<TrackItem>() {
        public TrackItem createFromParcel(Parcel in) {
            return new TrackItem(in);
        }

        public TrackItem[] newArray(int size) {
            return new TrackItem[size];
        }
    };
}
