package com.bltucker.spotifystreamer;

public final class ArtistSearchResult {

    private final String id;
    public final String artistName;
    public final String thumbnailUriString;

    public ArtistSearchResult(String id, String artistName, String thumbnailUriString){
        this.id = id;
        this.artistName = artistName;
        this.thumbnailUriString = thumbnailUriString;
    }

}
