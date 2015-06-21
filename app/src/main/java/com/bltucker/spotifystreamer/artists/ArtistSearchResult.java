package com.bltucker.spotifystreamer.artists;

import android.os.Parcel;
import android.os.Parcelable;

final class ArtistSearchResult implements Parcelable{

    public final String id;
    public final String artistName;
    public final String thumbnailUriString;

    public ArtistSearchResult(String id, String artistName, String thumbnailUriString){
        this.id = id;
        this.artistName = artistName;
        this.thumbnailUriString = thumbnailUriString;
    }

    private ArtistSearchResult(Parcel in) {
        this.id = in.readString();
        this.artistName = in.readString();
        this.thumbnailUriString = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(artistName);
        dest.writeString(thumbnailUriString);
    }

    public static final Parcelable.Creator<ArtistSearchResult> CREATOR = new Parcelable.Creator<ArtistSearchResult>() {
        public ArtistSearchResult createFromParcel(Parcel in) {
            return new ArtistSearchResult(in);
        }

        public ArtistSearchResult[] newArray(int size) {
            return new ArtistSearchResult[size];
        }
    };

}
