package com.bltucker.spotifystreamer.playback;

public final class PlaybackStatusUpdateEvent {

    private final int currentTrackPosition;

    public PlaybackStatusUpdateEvent(int currentTrackPosition){
        this.currentTrackPosition = currentTrackPosition;
    }


    public int getCurrentTrackPosition() {
        return currentTrackPosition;
    }
}
