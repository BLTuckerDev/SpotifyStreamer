package com.bltucker.spotifystreamer.playback;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bltucker.spotifystreamer.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PlaybackFragment extends Fragment {

    private PlaybackFragmentListener fragmentListener;

    @InjectView(R.id.playback_back_button)
    Button backButton;

    @InjectView(R.id.playback_forward_button)
    Button forwardButton;

    @InjectView(R.id.playback_play_button)
    Button playButton;

    @InjectView(R.id.playback_pause_button)
    Button pauseButton;


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


    @OnClick(R.id.playback_back_button)
    public void onBackButtonClick(){
        this.fragmentListener.onBackButtonClick();
    }


    @OnClick(R.id.playback_play_button)
    public void onPlayButtonClick(){
        this.fragmentListener.onPlayButtonClick();
    }


    @OnClick(R.id.playback_pause_button)
    public void onPauseButtonClick(){
        this.fragmentListener.onPauseButtonClick();
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
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        fragmentListener = null;
    }


    public interface PlaybackFragmentListener {
        void onBackButtonClick();
        void onForwardButtonClick();
        void onPlayButtonClick();
        void onPauseButtonClick();
    }

}
