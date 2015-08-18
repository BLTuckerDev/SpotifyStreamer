package com.bltucker.spotifystreamer.playback;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.bltucker.spotifystreamer.EventBus;
import com.bltucker.spotifystreamer.R;
import com.bltucker.spotifystreamer.tracks.TrackItem;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PlaybackFragment extends DialogFragment {
    //TODO we need a scroll view to handle horizontal playback
//TODO when we rotate we need to check on the state of the play button and store that!

    private static final String LOG_TAG = PlaybackFragment.class.getSimpleName();

    private PlaybackServiceConnectionProvider fragmentListener;
    private PlaybackServiceConnection playbackServiceConnection;
    private ShareActionProvider shareActionProvider;

    @InjectView(R.id.playback_back_button)
    ImageButton backButton;

    @InjectView(R.id.playback_forward_button)
    ImageButton forwardButton;

    @InjectView(R.id.playback_play_button)
    ImageButton playButton;

    @InjectView(R.id.playback_pause_button)
    ImageButton pauseButton;

    @InjectView(R.id.playback_progress_bar)
    SeekBar playbackProgressBar;

    @InjectView(R.id.playback_current_time)
    TextView currentPlaybackTimeTextView;

    @InjectView(R.id.playback_album_art)
    ImageView albumArtImageView;

    @InjectView(R.id.playback_album_title)
    TextView albumTitleTextView;

    @InjectView(R.id.playback_artist_name)
    TextView artistNameTextView;

    @InjectView(R.id.playback_song_title)
    TextView songTitleTextView;


    public static PlaybackFragment newInstance() {
        PlaybackFragment fragment = new PlaybackFragment();
        return fragment;
    }


    public PlaybackFragment() {
        this.setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_playback, menu);

        MenuItem shareItem = menu.findItem(R.id.menu_item_share);
        shareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
        this.updateShareProviderIntent();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playback, container, false);
        ButterKnife.inject(this, view);

        playbackProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentPlaybackTimeTextView.setText(PlaybackFragment.this.getFormattedPlaybackTimeFromMilliseconds(seekBar.getProgress()));
                }
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }


            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if (playbackServiceConnectionIsReady()) {
                    playbackServiceConnection.getBoundService().seekTo(seekBar.getProgress());
                }
            }
        });

        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TrackItem currentTrack = PlaybackSession.getCurrentSession().getCurrentTrack();
        this.updateFragmentUI(currentTrack);
    }


    @OnClick(R.id.playback_back_button)
    public void onBackButtonClick(){
        if(this.playbackServiceConnectionIsReady()){
            this.playTrack(PlaybackSession.getCurrentSession().returnToPreviousTrack());
        }
    }


    @OnClick(R.id.playback_play_button)
    public void onPlayButtonClick(){

        if(this.playbackServiceConnectionIsReady()){

            PlaybackService playbackService = this.playbackServiceConnection.getBoundService();

            if(playbackService.isPaused()){
                playbackService.resumeSong();
            } else {
                this.playTrack(PlaybackSession.getCurrentSession().getCurrentTrack());
            }

        }

        this.pauseButton.setVisibility(View.VISIBLE);
        this.playButton.setVisibility(View.INVISIBLE);
    }


    @OnClick(R.id.playback_pause_button)
    public void onPauseButtonClick(){

        if(this.playbackServiceConnectionIsReady()){
            this.playbackServiceConnection.getBoundService().pauseSong();
        }

        this.pauseButton.setVisibility(View.INVISIBLE);
        this.playButton.setVisibility(View.VISIBLE);
    }


    @OnClick(R.id.playback_forward_button)
    public void onForwardButtonClick(){
        if(this.playbackServiceConnectionIsReady()){
            this.playTrack(PlaybackSession.getCurrentSession().advanceToNextTrack());
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            fragmentListener = (PlaybackServiceConnectionProvider) activity;
            playbackServiceConnection = fragmentListener.getPlaybackServiceConnection();
            EventBus.getEventBus().register(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getEventBus().unregister(this);
        fragmentListener = null;

        if(this.playbackServiceConnection.isActive() && this.playbackServiceConnection.getBoundService().isPaused()){
            this.playbackServiceConnection.getBoundService().dismissNotifications();
        }

        playbackServiceConnection = null;
    }


    private boolean playbackServiceConnectionIsReady(){
        return this.playbackServiceConnection != null && this.playbackServiceConnection.isActive();
    }



    private void playTrack(TrackItem track){
        try {

            if(this.playbackServiceConnectionIsReady()){
                this.playbackServiceConnection.getBoundService().playSong(Uri.parse(track.previewUrl));
                this.updateShareProviderIntent();
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
        }
    }

    @Subscribe
    public void onPlaybackServiceConnected(PlaybackServiceConnectedEvent event){
        TrackItem currentTrack = PlaybackSession.getCurrentSession().getCurrentTrack();

        boolean playing = this.playbackServiceConnection.getBoundService().isPlaying();
        boolean paused = this.playbackServiceConnection.getBoundService().isPaused();

        if(!playing && !paused){
                //start fresh
            this.playTrack(currentTrack);

            this.pauseButton.setVisibility(View.VISIBLE);
            this.playButton.setVisibility(View.INVISIBLE);

        } else if(playing && !paused) {
            //currently playing a song
            Uri currentlyPlayingUri = this.playbackServiceConnection.getBoundService().getCurrentlyPlayingUri();
            if(!currentlyPlayingUri.equals(Uri.parse(currentTrack.previewUrl))){
                this.playTrack(currentTrack);
            }

            this.pauseButton.setVisibility(View.VISIBLE);
            this.playButton.setVisibility(View.INVISIBLE);

        } else if(!playing && paused){

            //song is currently paused
            Uri currentlyPlayingUri = this.playbackServiceConnection.getBoundService().getCurrentlyPlayingUri();

            if(!currentlyPlayingUri.equals(Uri.parse(currentTrack.previewUrl))){
                this.playTrack(currentTrack);
                this.pauseButton.setVisibility(View.VISIBLE);
                this.playButton.setVisibility(View.INVISIBLE);
            } else {
                this.pauseButton.setVisibility(View.INVISIBLE);
                this.playButton.setVisibility(View.VISIBLE);
                int currentPlaybackPosition = this.playbackServiceConnection.getBoundService().getCurrentPlaybackPosition();
                playbackProgressBar.setProgress(currentPlaybackPosition);
                String currentPlaybackTime = this.getFormattedPlaybackTimeFromMilliseconds(currentPlaybackPosition);
                currentPlaybackTimeTextView.setText(currentPlaybackTime);
            }

        }


    }

    @Subscribe
    public void onNewPlaybackSessionStarted(NewPlaybackSessionStartedEvent event){
        TrackItem currentTrack = PlaybackSession.getCurrentSession().getCurrentTrack();
        this.updateFragmentUI(currentTrack);
    }

    @Subscribe
    public void onPlaybackSessionTrackChangeEvent(PlaybackSessionCurrentTrackChangeEvent event){
        this.updateFragmentUI(event.getCurrentTrack());
    }


    @Subscribe
    public void onPlaybackStatusUpdateEvent(PlaybackStatusUpdateEvent event){
        playbackProgressBar.setProgress(event.getCurrentTrackPosition());
        String currentPlaybackTime = this.getFormattedPlaybackTimeFromMilliseconds(event.getCurrentTrackPosition());
        currentPlaybackTimeTextView.setText(currentPlaybackTime);
    }


    private String getFormattedPlaybackTimeFromMilliseconds(int milliseconds){
        String currentPlaybackTime = String.format("0:%02d", milliseconds / 1000);
        return currentPlaybackTime;
    }

    private void updateFragmentUI(TrackItem currentTrack){
        playbackProgressBar.setProgress(0);
        Picasso.with(getActivity()).load(currentTrack.trackNowPlayingImageUrl).into(albumArtImageView);
        artistNameTextView.setText(currentTrack.trackArtistName);
        albumTitleTextView.setText(currentTrack.trackAlbumTitle);
        songTitleTextView.setText(currentTrack.trackTitle);
    }


    private void updateShareProviderIntent(){

        if(shareActionProvider == null){
            return;
        }

        TrackItem currentTrack = PlaybackSession.getCurrentSession().getCurrentTrack();

        if(currentTrack == null){
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Listen to this!");
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.putExtra(Intent.EXTRA_TEXT, currentTrack.externalShareUrl);

        this.shareActionProvider.setShareIntent(shareIntent);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if(this.playbackServiceConnectionIsReady()){
            this.playbackServiceConnection.getBoundService().stopSong();
        }
    }

}
