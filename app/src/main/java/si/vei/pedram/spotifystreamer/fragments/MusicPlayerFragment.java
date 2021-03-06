package si.vei.pedram.spotifystreamer.fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    // Data used to play music
    private ArrayList<TrackGist> mTrackList;
    private int mTrackPosition;
    private TrackGist mCurrentTrack;

    // Music service
    private MusicService mMusicService;

    // UI elements
    private ImageButton mPlayButton;
    private ImageButton mForwardButton;
    private ImageButton mRewindButton;
    private ImageButton mNextButton;
    private ImageButton mPreviousButton;

    private SeekBar mTrackSeekbar;
    private ImageView mAlbumArtImageView;
    private TextView mTrackCurrentDuration;
    private TextView mTrackTotalDuration;
    private TextView mAartistNameTextView;
    private TextView mAlbumNameTextView;
    private TextView mTrackNameTextView;

    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();

    private Utilities utils;

    private boolean mHasTwoPanes;
    private boolean mServiceBound = false;
    private boolean mPlayerResumed = false;

    private ShareActionProvider mShareActionProvider;
    private String mTrackShareText;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleBroadcastIntent(intent.getAction());
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_player, container, false);

        // retain this fragment
        setRetainInstance(true);

        setHasOptionsMenu(true);

        mHasTwoPanes = false;

        // Get track list and track position
        Bundle arguments = getArguments();
        if (arguments != null) {
            mTrackList = arguments.getParcelableArrayList(getString(R.string.intent_track_list_key));
            mTrackPosition = arguments.getInt(getString(R.string.intent_selected_track_position));
            mPlayerResumed = arguments.getBoolean(getString(R.string.intent_player_resumed));
            mHasTwoPanes = arguments.getBoolean(getString(R.string.intent_has_two_pane));
        }

        // If app is in two pane mode, music player will be shown as a dialog
        // otherwise, it will be a normal fragment attached to music player activity
        setShowsDialog(mHasTwoPanes);

        // Get UI elements
        mAartistNameTextView = (TextView) rootView.findViewById(R.id.music_player_artist_name_textview);
        mAlbumNameTextView = (TextView) rootView.findViewById(R.id.music_player_album_name_textview);
        mAlbumArtImageView = (ImageView) rootView.findViewById(R.id.music_player_album_art_imageview);
        mTrackNameTextView = (TextView) rootView.findViewById(R.id.music_player_track_name_textview);

        // Seekbar and related labels
        mTrackSeekbar = (SeekBar) rootView.findViewById(R.id.music_player_seekBar);
        mTrackCurrentDuration = (TextView) rootView.findViewById(R.id.music_player_current_time_textview);
        mTrackTotalDuration = (TextView) rootView.findViewById(R.id.music_player_track_total_duration_textview);

        mTrackCurrentDuration.setText(getString(R.string.music_player_seekbar_zero_label));

        // Media Controller Buttons
        mPlayButton = (ImageButton) rootView.findViewById(R.id.music_player_play_pause_button);
        mForwardButton = (ImageButton) rootView.findViewById(R.id.music_player_forward_button);
        mRewindButton = (ImageButton) rootView.findViewById(R.id.music_player_rewind_button);
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

        mRewindButton.setOnClickListener(new View.OnClickListener() {
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

        // Handle starting service. If player is resumes, it means that service is already started.
        // So we will only bind to it. Otherwise we will start the service
        Intent serviceIntent = new Intent(getActivity(), MusicService.class);

        if (!mPlayerResumed && savedInstanceState == null) {
            serviceIntent.setAction(MusicService.ACTION_PLAY);
            serviceIntent.putParcelableArrayListExtra(getString(R.string.intent_track_list_key), mTrackList);
            serviceIntent.putExtra(getString(R.string.intent_selected_track_position), mTrackPosition);
            getActivity().startService(serviceIntent);
        }

        getActivity().bindService(serviceIntent, musicConnection, Context.BIND_AUTO_CREATE);

        // Listener
        mTrackSeekbar.setOnSeekBarChangeListener(this);

        return rootView;
    }

    // Connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            // Get service, update UI and set the flag
            mMusicService = binder.getService();
            updateUi();
            mServiceBound = true;
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
    public void onResume() {
        super.onResume();

        // If we are already connected to the service update the UI. Otherwise service connection will take care of it
        if (mMusicService != null) {
            updateUi();
        }

        // Register for broadcast message from the service in order to update the player
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.BROADCAST_MEDIA_PLAYER_PREPARED);
        intentFilter.addAction(MusicService.BROADCAST_SERVICE_STOPPED);
        intentFilter.addAction(MusicService.BROADCAST_TRACK_CHANGED);
        intentFilter.addAction(MusicService.BROADCAST_TRACK_PAUSED);
        intentFilter.addAction(MusicService.BROADCAST_TRACK_PLAYED);
        intentFilter.addAction(MusicService.BROADCAST_NOTIFICATION_CLOSED);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mServiceBound && getActivity().isFinishing()) {
            // remove message Handler from updating progress bar
            mHandler.removeCallbacks(mUpdateTimeTask);
        }
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        getActivity().unbindService(musicConnection);
        mServiceBound = false;
        super.onDestroy();
    }

    /**
     * Play previous track by calling corresponding method in the service
     */
    private void playPreviousTrack() {
        mMusicService.playPreviousTrack();
    }

    /**
     * Rewind track by calling corresponding method in the service
     */
    private void seekBackward() {
        mMusicService.seekBackward();
    }

    /**
     * Fast forward track by calling corresponding method in the service
     */
    private void seekForward() {
        mMusicService.seekForward();
    }

    /**
     * Play next track by calling corresponding method in the service
     */
    private void playNextTrack() {
        mMusicService.playNextTrack();
    }

    /**
     * Update the track seekbar
     */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 500);
    }

    /**
     * Handle incoming broadcast messages to control music playing.
     *
     * @param action broadcast action
     */
    private void handleBroadcastIntent(String action) {
        if (action.equalsIgnoreCase(MusicService.BROADCAST_MEDIA_PLAYER_PREPARED)) {
            updateProgressBar();
            enableControls();
            mTrackTotalDuration.setText(Integer.toString(mMusicService.getTrackDuration()));
        } else if (action.equalsIgnoreCase(MusicService.BROADCAST_TRACK_CHANGED)) {
            updateUi();
            mHandler.removeCallbacks(mUpdateTimeTask);
        } else if (action.equalsIgnoreCase(MusicService.BROADCAST_TRACK_PLAYED)) {
            mPlayButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_media_pause, null));
        } else if (action.equalsIgnoreCase(MusicService.BROADCAST_TRACK_PAUSED)) {
            mPlayButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_media_play, null));
        } else if (action.equalsIgnoreCase(MusicService.BROADCAST_NOTIFICATION_CLOSED)) {
            getActivity().finish();
        }
    }

    /**
     * Update player UI with any changes in music playing status
     */
    public void updateUi() {
        mCurrentTrack = mMusicService.getCurrentTrack();

        // Invalidate options menu in order to update the toolbar
        getActivity().invalidateOptionsMenu();

        // If app is not in two pane mode setup the share button. Other wise, top tracks fragment will take care of it.
        if (mShareActionProvider != null && !mHasTwoPanes) {
            mTrackShareText = getString(R.string.track_share_text, mCurrentTrack.getTrackName(), mCurrentTrack.getArtistName(), mCurrentTrack.getmExternalUrl());
            mShareActionProvider.setShareIntent(createShareTrackIntent(mTrackShareText));
        }

        mAartistNameTextView.setText(mCurrentTrack.getArtistName());
        mAlbumNameTextView.setText(mCurrentTrack.getAlbumName());
        mTrackNameTextView.setText(mCurrentTrack.getTrackName());

        // Load the album art. In case of error, set the default image.
        Picasso.with(getActivity()).load(mCurrentTrack.getLargeAlbumThumbnail()).error(ResourcesCompat.getDrawable(getResources(), R.drawable.no_image_available, null)).into(mAlbumArtImageView);

        // Disable music controls related to media player (play/pause, rewind, fast forward and seekbar) if the music player is not ready in order to prevent unexpected behaviour. When the music player is ready they will be enabled.
        if (mMusicService.isMediaPlayerPrepared()) {
            enableControls();
            updateProgressBar();
        } else {
            mTrackCurrentDuration.setText(getString(R.string.music_player_seekbar_zero_label));
            mTrackSeekbar.setProgress(0);
            disableControls();
        }
    }

    /**
     * Disable play/pause, rewind, fast forward buttons and seekbar in the UI. This method is used in order to prevent unexpected behaviour when media player is not ready.
     */
    private void disableControls() {
        mPlayButton.setEnabled(false);
        mRewindButton.setEnabled(false);
        mForwardButton.setEnabled(false);
        mTrackSeekbar.setEnabled(false);

        mPlayButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_media_pause_disabled, null));
        mRewindButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_media_rew_disabled, null));
        mForwardButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_media_ff_disabled, null));
    }

    /**
     * Enable media control buttons and seekbar.
     *
     * @see {@link #disableControls()}
     */
    private void enableControls() {
        mPlayButton.setEnabled(true);
        mRewindButton.setEnabled(true);
        mForwardButton.setEnabled(true);
        mTrackSeekbar.setEnabled(true);

        mPlayButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_media_pause, null));

        if (mPlayerResumed && !mMusicService.isPlaying()) {
            mPlayButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_media_play, null));
        }

        mRewindButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_media_rew, null));
        mForwardButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_media_ff, null));
    }

    /**
     * Background Runnable thread to update the seekbar
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
                int progress = utils.getProgressPercentage(currentDuration, totalDuration);
                //Log.d("Progress", ""+progress);
                mTrackSeekbar.setProgress(progress);
            }

            // Running this thread after 1000 milliseconds
            mHandler.postDelayed(this, 500);
        }
    };

    /**
     * Create share intent to set the shareActionbarProvider in toolbar. Track share text is added to the intent.
     * @param trackShareText Share string to set for the shareActionProvider
     * @return the created intent
     */
    private Intent createShareTrackIntent(String trackShareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, trackShareText);
        return shareIntent;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        if (!mHasTwoPanes) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getActivity().getMenuInflater().inflate(R.menu.menu_music_player, menu);

            // Locate MenuItem with ShareActionProvider
            MenuItem menuItem = menu.findItem(R.id.action_share);

            // Fetch and store ShareActionProvider
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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