package si.vei.pedram.spotifystreamer.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
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
public class MusicPlayerFragment extends DialogFragment implements SeekBar.OnSeekBarChangeListener {

    private ArrayList<TrackGist> mTrackList;
    private int mTrackPosition;
    private Intent mPlayIntent;
    private MusicService mMusicService;

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

    private Utilities utils;

    private TextView mAartistNameTextView;
    private TextView mAlbumNameTextView;
    private ImageView mAlbumArtImageView;
    private TextView mTrackNameTextView;
    private TrackGist mCurrentTrack;

    private Context mContext;

    private boolean mServiceBound = false;

//    public static MusicPlayerFragment newInstance(Context context, ArrayList<TrackGist> trackList, int trackPosition) {
//        MusicPlayerFragment fragment = new MusicPlayerFragment();
//
//        Bundle arguments = new Bundle();
//        arguments.putParcelableArrayList(context.getString(R.string.intent_track_list_key), trackList);
//        arguments.putInt(context.getString(R.string.intent_selected_track_position), trackPosition);
//
//        return fragment;
//    }

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_player, container, false);

        boolean hasTwoPanes = false;

        // Get track list and track position
        Bundle arguments = getArguments();
        if (arguments != null) {
            mTrackList = arguments.getParcelableArrayList(getString(R.string.intent_track_list_key));
            mTrackPosition = arguments.getInt(getString(R.string.intent_selected_track_position));
            hasTwoPanes = arguments.getBoolean(getString(R.string.intent_has_two_pane));
        }

        setShowsDialog(hasTwoPanes);

        // Get UI elements
        mAartistNameTextView = (TextView) rootView.findViewById(R.id.music_player_artist_name_textview);
        mAlbumNameTextView = (TextView) rootView.findViewById(R.id.music_player_album_name_textview);
        mAlbumArtImageView = (ImageView) rootView.findViewById(R.id.music_player_album_art_imageview);
        mTrackNameTextView = (TextView) rootView.findViewById(R.id.music_player_track_name_textview);

        mCurrentTrack = mTrackList.get(mTrackPosition);

        // Set UI elements for the current track
        mAartistNameTextView.setText(mCurrentTrack.getArtistName());
        mAlbumNameTextView.setText(mCurrentTrack.getAlbumName());
        mTrackNameTextView.setText(mCurrentTrack.getTrackName());

        // Load the album art
        Picasso.with(getActivity()).load(mCurrentTrack.getLargeAlbumThumbnail()).into(mAlbumArtImageView);

        // Seekbar and related labels
        mTrackSeekbar = (SeekBar) rootView.findViewById(R.id.music_player_seekBar);
        mTrackCurrentDuration = (TextView) rootView.findViewById(R.id.music_player_current_time_textview);
        mTrackTotalDuration = (TextView) rootView.findViewById(R.id.music_player_track_total_duration_textview);

        mTrackCurrentDuration.setText(getString(R.string.music_player_seekbar_zero_label));
        mTrackTotalDuration.setText(getString(R.string.music_player_seekbar_total_duration));

        // Media Controller Buttons
        mPlayButton = (ImageButton) rootView.findViewById(R.id.music_player_play_pause_button);
        mForwardButton = (ImageButton) rootView.findViewById(R.id.music_player_forward_button);
        mBackwardButton = (ImageButton) rootView.findViewById(R.id.music_player_rewind_button);
        mNextButton = (ImageButton) rootView.findViewById(R.id.music_player_next_track_button);
        mPreviousButton = (ImageButton) rootView.findViewById(R.id.music_player_previous_track_button);

        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPreviousTrack();
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMusicService.isPlaying()) {
                    mMusicService.pausePlayer();
                    mPlayButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_media_play, null));
                } else {
                    mMusicService.startPlayer();
                    mPlayButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_media_pause, null));
                }
            }
        });

        mBackwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBackward();
            }
        });

        mForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekForward();
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextTrack();
            }
        });

        utils = new Utilities();

        // Listeners
        mTrackSeekbar.setOnSeekBarChangeListener(this);

        mPlayIntent = new Intent(getActivity(), MusicService.class);
        mPlayIntent.setAction(MusicService.ACTION_PLAY);
        mPlayIntent.putParcelableArrayListExtra(getString(R.string.intent_track_list_key), mTrackList);
        mPlayIntent.putExtra(getString(R.string.intent_selected_track_position), mTrackPosition);

        getActivity().startService(mPlayIntent);

        getActivity().bindService(mPlayIntent, musicConnection, Context.BIND_AUTO_CREATE);
        mServiceBound = true;

        return rootView;
    }

    // Connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            mMusicService = binder.getService();

            mMusicService.setTrackList(mTrackList);
            mMusicService.setTrackPosition(mTrackPosition);

            // Since we want to play a track every time the service starts, we will call playTrack method to initialize the player and set the track
            mMusicService.playTrack();
            updateProgressBar();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(mMusicService.getString(R.string.state_current_track), mCurrentTrack);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mServiceBound && getActivity().isFinishing()) {
            // remove message Handler from updating progress bar
            mHandler.removeCallbacks(mUpdateTimeTask);
            getActivity().unbindService(musicConnection);
            mServiceBound = false;
        }
    }

    //play previous
    private void playPreviousTrack() {
        mMusicService.playPreviousTrack();

        mTrackPosition = mMusicService.getTrackPosition();
        mCurrentTrack = mMusicService.getCurrentTrack();

        mAartistNameTextView.setText(mCurrentTrack.getArtistName());
        mAlbumNameTextView.setText(mCurrentTrack.getAlbumName());
        mTrackNameTextView.setText(mCurrentTrack.getTrackName());

        // Load the album art
        Picasso.with(getActivity()).load(mCurrentTrack.getLargeAlbumThumbnail()).into(mAlbumArtImageView);

        mTrackCurrentDuration.setText(getString(R.string.music_player_seekbar_zero_label));

        // Reset seekbar
        mTrackSeekbar.setProgress(0);

        // TODO Spotify API allows to play 30 second samples, so the total duration is 30. For a real app this should be changed to get the duration from music service (after the file is loaded. Simply calling duration won't work here)
        mTrackTotalDuration.setText(getString(R.string.music_player_seekbar_total_duration));

    }

    private void seekBackward() {
        mMusicService.seekBackward();
    }

    private void seekForward() {
        mMusicService.seekForward();
    }

    //play next
    private void playNextTrack() {
        mMusicService.playNextTrack();

        mTrackPosition = mMusicService.getTrackPosition();
        mCurrentTrack = mMusicService.getCurrentTrack();

        mAartistNameTextView.setText(mCurrentTrack.getArtistName());
        mAlbumNameTextView.setText(mCurrentTrack.getAlbumName());
        mTrackNameTextView.setText(mCurrentTrack.getTrackName());

        // Load the album art
        Picasso.with(getActivity()).load(mCurrentTrack.getLargeAlbumThumbnail()).into(mAlbumArtImageView);

        mTrackCurrentDuration.setText(getString(R.string.music_player_seekbar_zero_label));

        // Reset seekbar
        mTrackSeekbar.setProgress(0);

        // TODO Spotify API allows to play 30 second samples, so the total duration is 30. For a real app this should be changed to get the duration from music service (after the file is loaded. Simply calling duration won't work here)
        mTrackTotalDuration.setText(getString(R.string.music_player_seekbar_total_duration));

    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 500);
    }

//    public void updateUI() {
//        mCurrentTrack = mMusicService.getCurrentTrack();
//        mAartistNameTextView.setText(mCurrentTrack.getArtistName());
//        mAlbumNameTextView.setText(mCurrentTrack.getAlbumName());
//        mTrackNameTextView.setText(mCurrentTrack.getTrackName());
//
//        // Load the album art
//        Picasso.with(getActivity()).load(mCurrentTrack.getLargeAlbumThumbnail()).into(mAlbumArtImageView);
//
//        mTrackCurrentDuration.setText(getString(R.string.music_player_seekbar_zero_label));
//    }

    public static void changeButton() {
    }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {

            if (mMusicService.isMediaPlayerPrepared()) {
                // Update the seekbar only if music is playing
                // This condition will prevent calling getDuration method when player is not ready
                long totalDuration = mMusicService.getTrackDuration();
                long currentDuration = mMusicService.getPlayingPosition();

                // Displaying Total Duration time
                mTrackTotalDuration.setText("" + utils.milliSecondsToTimer(totalDuration));
                // Displaying time completed playing
                mTrackCurrentDuration.setText("" + utils.milliSecondsToTimer(currentDuration));

                // Updating progress bar
                int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
                //Log.d("Progress", ""+progress);
                mTrackSeekbar.setProgress(progress);
            }

            // Running this thread after 1000 milliseconds
            mHandler.postDelayed(this, 500);
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateProgressBar();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mMusicService.getTrackDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mMusicService.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }
}