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
import android.widget.ImageView;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import si.vei.pedram.spotifystreamer.R;
import si.vei.pedram.spotifystreamer.models.TrackGist;
import si.vei.pedram.spotifystreamer.musictools.MusicController;
import si.vei.pedram.spotifystreamer.service.MusicService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MusicPlayerFragment extends Fragment implements MediaPlayerControl {

    private ArrayList<TrackGist> mTrackList;
    private int mTrackPosition;
    private Intent mPlayIntent;
    private MusicController mMusicController;
    private MusicService mMusicService;
    private boolean mMusicBound;

    private boolean mFragmentPaused = false;
    private boolean mPlaybackPaused = true;

    private Handler handler = new Handler();

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
        TextView artistNameTextView = (TextView) rootView.findViewById(R.id.music_player_artist_name_textview);
        TextView albumNameTextView = (TextView) rootView.findViewById(R.id.music_player_album_name_textview);
        ImageView albumArtImageView = (ImageView) rootView.findViewById(R.id.music_player_album_art_imageview);
        TextView trackNameTextView = (TextView) rootView.findViewById(R.id.music_player_track_name_textview);

        TrackGist currentTrack = mTrackList.get(mTrackPosition);

        artistNameTextView.setText(currentTrack.getArtistName());
        albumNameTextView.setText(currentTrack.getAlbumName());
        trackNameTextView.setText(currentTrack.getTrackName());

        Picasso.with(getActivity()).load(currentTrack.getLargeAlbumThumbnail()).into(albumArtImageView);

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
            if(mPlaybackPaused){
                // Set controller
                setController();
                mPlaybackPaused = false;
            }
            mMusicController.show(0);
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
        if(mFragmentPaused){
            setController();
            mFragmentPaused = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mFragmentPaused = true;
    }

    @Override
    public void onStop() {
        mMusicController.hide();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        getActivity().unbindService(musicConnection);
        getActivity().stopService(mPlayIntent);
        mMusicService = null;
        super.onDestroy();
    }

    /**
     * Set the music controller
     */
    private void setController() {

        if (mMusicController == null) {
            mMusicController = new MusicController(getActivity());
        }

        // Set listeners for the controller
        mMusicController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextTrack();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPreviousTrack();
            }
        });

        mMusicController.setMediaPlayer(this);
        mMusicController.setAnchorView(getView().findViewById(R.id.player_controller_container));
        mMusicController.setEnabled(true);
        mMusicController.requestFocus();
    }

    //play next
    private void playNextTrack() {
        mMusicService.playNextTrack();
        if(mPlaybackPaused){
            setController();
            mPlaybackPaused = false;
        }
        mMusicController.show(0);
    }

    //play previous
    private void playPreviousTrack() {
        mMusicService.playPreviousTrack();
        if(mPlaybackPaused){
            setController();
            mPlaybackPaused=false;
        }
        mMusicController.show(0);
    }

    @Override
    public void start() {
        mMusicService.startPlayer();
    }

    @Override
    public void pause() {
        mPlaybackPaused = true;
        mMusicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (mMusicService != null && mMusicBound && mMusicService.isPlaying()) {
            return mMusicService.getTrackDuration();
        } else {
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        if (mMusicService != null && mMusicBound && mMusicService.isPlaying()) {
            return mMusicService.getPlayingPosition();
        } else {
            return 0;
        }
    }


    @Override
    public void seekTo(int pos) {
        mMusicService.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        if (mMusicService != null && mMusicBound) {
            return mMusicService.isPlaying();
        }
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

}
