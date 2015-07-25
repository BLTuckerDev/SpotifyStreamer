package com.bltucker.spotifystreamer.playback;

import com.bltucker.spotifystreamer.EventBus;
import com.bltucker.spotifystreamer.tracks.TrackItem;

import java.util.ArrayList;
import java.util.List;

public final class PlaybackSession {

    private static PlaybackSession currentSession;

    public static PlaybackSession getCurrentSession(){
        return currentSession;
    }


    public static void startNewSession(TrackItem selectedTrackItem, List<TrackItem> trackList){

        if(currentSession != null){
            EventBus.getEventBus().unregister(currentSession);
        }

        currentSession = new PlaybackSession(selectedTrackItem, trackList);

        EventBus.getEventBus().register(currentSession);

        EventBus.getEventBus().fireEvent(new NewPlaybackSessionStartedEvent());

    }

    private TrackItem currentTrack;
    private final List<TrackItem> trackList;


    private PlaybackSession(TrackItem selectedTrackItem, List<TrackItem> trackList){
        this.currentTrack = selectedTrackItem;
        this.trackList = new ArrayList<>(trackList);
    }


    private void setCurrentTrack(TrackItem track){
        EventBus.getEventBus().fireEvent(new PlaybackSessionCurrentTrackChangeEvent(this.currentTrack, track));
        this.currentTrack = track;
    }


    public TrackItem getCurrentTrack() {
        return currentTrack;
    }


    public TrackItem advanceToNextTrack(){

        int indexOfNextTrack = this.trackList.indexOf(currentTrack) + 1;

        if(indexOfNextTrack >= this.trackList.size()){
            indexOfNextTrack = 0;
        }

        this.setCurrentTrack(this.trackList.get(indexOfNextTrack));

        return this.currentTrack;
    }


    public TrackItem returnToPreviousTrack(){

        int indexOfPreviousTrack = this.trackList.indexOf(currentTrack) - 1;

        if(indexOfPreviousTrack < 0){
            indexOfPreviousTrack = this.trackList.size() -1;
        }

        this.setCurrentTrack(this.trackList.get(indexOfPreviousTrack));

        return this.currentTrack;
    }

}
