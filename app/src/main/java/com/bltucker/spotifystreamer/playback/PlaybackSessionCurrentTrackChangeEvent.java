package com.bltucker.spotifystreamer.playback;

import com.bltucker.spotifystreamer.tracks.TrackItem;

public class PlaybackSessionCurrentTrackChangeEvent {

    private final TrackItem previousTrack;
    private final TrackItem currentTrack;

    public PlaybackSessionCurrentTrackChangeEvent(TrackItem previousTrack, TrackItem currentTrack){
        this.previousTrack = previousTrack;
        this.currentTrack = currentTrack;
    }


    public TrackItem getPreviousTrack() {
        return previousTrack;
    }


    public TrackItem getCurrentTrack() {
        return currentTrack;
    }
}
