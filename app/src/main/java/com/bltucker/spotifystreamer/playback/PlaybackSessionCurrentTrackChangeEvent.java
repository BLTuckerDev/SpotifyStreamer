package com.bltucker.spotifystreamer.playback;

import com.bltucker.spotifystreamer.tracks.TrackItem;

public class PlaybackSessionCurrentTrackChangeEvent {

    public final TrackItem previousTrack;
    public final TrackItem currentTrack;

    public PlaybackSessionCurrentTrackChangeEvent(TrackItem previousTrack, TrackItem currentTrack){
        this.previousTrack = previousTrack;
        this.currentTrack = currentTrack;
    }

}
