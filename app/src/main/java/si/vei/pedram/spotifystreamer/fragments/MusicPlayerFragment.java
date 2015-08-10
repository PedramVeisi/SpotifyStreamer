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

    private ArrayList<TrackGist> mTrackList;
    private int mTrackPosition;
    private Intent mPlayIntent;
    private MusicService mMusicService;

    private ImageButton mPlayButton;
    private ImageButton mForwardButton;
    private ImageButton mRewindButton;
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

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
        setHasOptionsMenu(true);
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
            mPlayerResumed = arguments.getBoolean(getString(R.string.intent_player_resumed));
            hasTwoPanes = arguments.getBoolean(getString(R.string.intent_has_two_pane));
        }

        setShowsDialog(hasTwoPanes);

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

        // Listeners
        mTrackSeekbar.setOnSeekBarChangeListener(this);

        mPlayIntent = new Intent(getActivity(), MusicService.class);

        if (!mPlayerResumed) {
            mPlayIntent.setAction(MusicService.ACTION_PLAY);
            mPlayIntent.putParcelableArrayListExtra(getString(R.string.intent_track_list_key), mTrackList);
            mPlayIntent.putExtra(getString(R.string.intent_selected_track_position), mTrackPosition);
            getActivity().startService(mPlayIntent);
        }

        getActivity().bindService(mPlayIntent, musicConnection, Context.BIND_AUTO_CREATE);

        return rootView;
    }

    // Connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
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
            getActivity().unbindService(musicConnection);
            mServiceBound = false;
        }
    }

    //play previous
    private void playPreviousTrack() {
        mMusicService.playPreviousTrack();
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
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 500);
    }

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

    public void updateUi() {

        mCurrentTrack = mMusicService.getCurrentTrack();

        mTrackShareText = getString(R.string.track_share_text, mCurrentTrack.getTrackName(), mCurrentTrack.getArtistName(), mCurrentTrack.getPreviewUrl());
        mShareActionProvider.setShareIntent(createShareTrackIntent(mTrackShareText));

        mAartistNameTextView.setText(mCurrentTrack.getArtistName());
        mAlbumNameTextView.setText(mCurrentTrack.getAlbumName());
        mTrackNameTextView.setText(mCurrentTrack.getTrackName());

        // Load the album art
        Picasso.with(getActivity()).load(mCurrentTrack.getLargeAlbumThumbnail()).error(ResourcesCompat.getDrawable(getResources(), R.drawable.no_image_available, null)).into(mAlbumArtImageView);

        if (mMusicService.isMediaPlayerPrepared()) {
            enableControls();
            updateProgressBar();
        } else {
            mTrackCurrentDuration.setText(getString(R.string.music_player_seekbar_zero_label));
            mTrackSeekbar.setProgress(0);
            disableControls();
        }
    }

    private void disableControls() {
        mPlayButton.setEnabled(false);
        mRewindButton.setEnabled(false);
        mForwardButton.setEnabled(false);
        mTrackSeekbar.setEnabled(false);

        mPlayButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_media_pause_disabled, null));
        mRewindButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_media_rew_disabled, null));
        mForwardButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_media_ff_disabled, null));
    }

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

    private Intent createShareTrackIntent(String trackShareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, trackShareText);
        return shareIntent;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getActivity().getMenuInflater().inflate(R.menu.menu_music_player, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
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