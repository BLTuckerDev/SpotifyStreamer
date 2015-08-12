package com.bltucker.spotifystreamer.playback;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bltucker.spotifystreamer.EventBus;
import com.bltucker.spotifystreamer.R;
import com.bltucker.spotifystreamer.tracks.TrackItem;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PlaybackFragment extends DialogFragment {
    //TODO we need a scroll view to handle horizontal playback
//TODO when we rotate we need to check on the state of the play button and store that!

    private static final String LOG_TAG = PlaybackFragment.class.getSimpleName();

    private PlaybackFragmentListener fragmentListener;

    @InjectView(R.id.playback_back_button)
    ImageButton backButton;

    @InjectView(R.id.playback_forward_button)
    ImageButton forwardButton;

    @InjectView(R.id.playback_play_button)
    ImageButton playButton;

    @InjectView(R.id.playback_pause_button)
    ImageButton pauseButton;

    @InjectView(R.id.playback_progress_bar)
    ProgressBar playbackProgressBar;

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
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playback, container, false);
        ButterKnife.inject(this, view);

        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.updateFragmentUI(PlaybackSession.getCurrentSession().getCurrentTrack());
    }


    @OnClick(R.id.playback_back_button)
    public void onBackButtonClick(){
        this.fragmentListener.onBackButtonClick();
    }


    @OnClick(R.id.playback_play_button)
    public void onPlayButtonClick(){
        this.fragmentListener.onPlayButtonClick();
        this.pauseButton.setVisibility(View.VISIBLE);
        this.playButton.setVisibility(View.INVISIBLE);
    }


    @OnClick(R.id.playback_pause_button)
    public void onPauseButtonClick(){
        this.fragmentListener.onPauseButtonClick();
        this.pauseButton.setVisibility(View.INVISIBLE);
        this.playButton.setVisibility(View.VISIBLE);
    }


    @OnClick(R.id.playback_forward_button)
    public void onForwardButtonClick(){
        this.fragmentListener.onForwardButtonClick();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            fragmentListener = (PlaybackFragmentListener) activity;
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
        String currentPlaybackTime = String.format("0:%02d", event.getCurrentTrackPosition() / 1000);
        currentPlaybackTimeTextView.setText(currentPlaybackTime);
    }


    private void updateFragmentUI(TrackItem currentTrack){
        playbackProgressBar.setProgress(0);
        Picasso.with(getActivity()).load(currentTrack.trackNowPlayingImageUrl).into(albumArtImageView);
        artistNameTextView.setText(currentTrack.trackArtistName);
        albumTitleTextView.setText(currentTrack.trackAlbumTitle);
        songTitleTextView.setText(currentTrack.trackTitle);
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if(fragmentListener != null){
            this.fragmentListener.onFragmentDismissed();
        }
    }





    public interface PlaybackFragmentListener {
        void onBackButtonClick();
        void onForwardButtonClick();
        void onPlayButtonClick();
        void onPauseButtonClick();
        void onFragmentDismissed();
    }

}
