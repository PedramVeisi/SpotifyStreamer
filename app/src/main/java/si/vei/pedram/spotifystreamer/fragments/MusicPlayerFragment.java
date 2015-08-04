package si.vei.pedram.spotifystreamer.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import si.vei.pedram.spotifystreamer.R;
import si.vei.pedram.spotifystreamer.Utilities;
import si.vei.pedram.spotifystreamer.models.TrackGist;
import si.vei.pedram.spotifystreamer.service.MusicService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MusicPlayerFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    private ArrayList<TrackGist> mTrackList;
    private int mTrackPosition;
    private Intent mPlayIntent;
    private MusicService mMusicService;
    private boolean mMusicBound;

    private boolean mFragmentPaused = false;
    private boolean mPlaybackPaused = true;

    private Handler handler = new Handler();

    private ImageButton mPlayButton;
    private ImageButton mForwardButton;
    private ImageButton mBackwardButton;
    private ImageButton mNextButton;
    private ImageButton mPreviousButton;


    private SeekBar mTrackSeekbar;
    private TextView mTrackCurrentDuration;
    private TextView mTrackTotalDuration;

    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();
    ;

    private Utilities utils;
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds

    private TextView mAartistNameTextView;
    private TextView mAlbumNameTextView;
    private ImageView mAlbumArtImageView;
    private TextView mTrackNameTextView;

    public MusicPlayerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_player, container, false);

        // Get track list and track position
        Bundle arguments = getArguments();
        if (arguments != null) {
            mTrackList = arguments.getParcelableArrayList(getString(R.string.intent_track_list_key));
            mTrackPosition = arguments.getInt(getString(R.string.intent_selected_track_position));
        }

        // Get UI elements
        mAartistNameTextView = (TextView) rootView.findViewById(R.id.music_player_artist_name_textview);
        mAlbumNameTextView = (TextView) rootView.findViewById(R.id.music_player_album_name_textview);
        mAlbumArtImageView = (ImageView) rootView.findViewById(R.id.music_player_album_art_imageview);
        mTrackNameTextView = (TextView) rootView.findViewById(R.id.music_player_track_name_textview);

        TrackGist currentTrack = mTrackList.get(mTrackPosition);

        mAartistNameTextView.setText(currentTrack.getArtistName());
        mAlbumNameTextView.setText(currentTrack.getAlbumName());
        mTrackNameTextView.setText(currentTrack.getTrackName());

        Picasso.with(getActivity()).load(currentTrack.getLargeAlbumThumbnail()).into(mAlbumArtImageView);

        mPlayButton = (ImageButton) rootView.findViewById(R.id.music_player_play_pause_button);
        mForwardButton = (ImageButton) rootView.findViewById(R.id.music_player_forward_button);
        mBackwardButton = (ImageButton) rootView.findViewById(R.id.music_player_rewind_button);
        mNextButton = (ImageButton) rootView.findViewById(R.id.music_player_next_track_button);
        mPreviousButton = (ImageButton) rootView.findViewById(R.id.music_player_previous_track_button);

        utils = new Utilities();

        // Listeners
        mTrackSeekbar.setOnSeekBarChangeListener(this); // Important

        return rootView;
    }

    // Connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            mMusicService = binder.getService();
            //pass list
            mMusicService.setTrackList(mTrackList);
            mMusicService.setTrackPosition(mTrackPosition);
            mMusicBound = true;

            // Since we want to play a track every time the service starts, we will call playTrack method to initialize the player and set the track
            mMusicService.playTrack();
            if (mPlaybackPaused) {
                mPlaybackPaused = false;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMusicBound = false;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        // Start and bind the service when activity starts
        if (mPlayIntent == null) {
            mPlayIntent = new Intent(getActivity(), MusicService.class);
            getActivity().bindService(mPlayIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(mPlayIntent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFragmentPaused) {
            mFragmentPaused = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mFragmentPaused = true;
    }

    @Override
    public void onDestroy() {
        getActivity().unbindService(musicConnection);
        getActivity().stopService(mPlayIntent);
        mMusicService = null;
        super.onDestroy();
    }

    //play next
    private void playNextTrack() {
        mMusicService.playNextTrack();
        if (mPlaybackPaused) {
            mPlaybackPaused = false;
        }
    }

    //play previous
    private void playPreviousTrack() {
        mMusicService.playPreviousTrack();
        if (mPlaybackPaused) {
            mPlaybackPaused = false;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}