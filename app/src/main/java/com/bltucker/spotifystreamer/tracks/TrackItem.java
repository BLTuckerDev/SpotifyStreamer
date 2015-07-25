package com.bltucker.spotifystreamer.tracks;

import android.os.Parcel;
import android.os.Parcelable;

public final class TrackItem implements Parcelable{

    public final String trackThumbnailurl;
    public final String trackNowPlayingImageUrl;
    public final String trackTitle;
    public final String trackAlbumTitle;
    public final String previewUrl;
    public final String trackArtistName;


    public TrackItem(String trackThumbnailurl, String trackNowPlayingImageUrl, String trackTitle, String trackAlbumTitle, String artistName, String previewUrl){

        this.trackThumbnailurl = trackThumbnailurl;
        this.trackNowPlayingImageUrl = trackNowPlayingImageUrl;
        this.trackTitle = trackTitle;
        this.trackAlbumTitle = trackAlbumTitle;
        this.trackArtistName = artistName;
        this.previewUrl = previewUrl;
    }

    private TrackItem(Parcel parcel){
        this.trackThumbnailurl = parcel.readString();
        this.trackNowPlayingImageUrl = parcel.readString();
        this.trackTitle = parcel.readString();
        this.trackAlbumTitle = parcel.readString();
        this.trackArtistName = parcel.readString();
        this.previewUrl = parcel.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.trackThumbnailurl);
        dest.writeString(this.trackNowPlayingImageUrl);
        dest.writeString(this.trackTitle);
        dest.writeString(this.trackAlbumTitle);
        dest.writeString(this.trackArtistName);
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
