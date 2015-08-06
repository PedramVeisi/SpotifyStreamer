package si.vei.pedram.spotifystreamer.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.util.ArrayList;

import si.vei.pedram.spotifystreamer.R;
import si.vei.pedram.spotifystreamer.activities.MusicPlayerActivity;
import si.vei.pedram.spotifystreamer.models.TrackGist;

/**
 * @author Pedram Veisi
 *         Music Player Service
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener {

    // Notification id
    private static final int NOTIFICATION_ID = 1;

    //media player
    private MediaPlayer mPlayer;
    //song list
    private ArrayList<TrackGist> mTrackList;
    //current position
    private int mTrackPosition;

    private boolean mMediaPlayerPrepared = false;

    private final IBinder mMusicBinder = new MusicBinder();

    public MusicService() {
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onCreate();
        //initialize position
        mTrackPosition = 0;
        //create player
        mPlayer = new MediaPlayer();

        // Initialize the player
        initMusicPlayer();

        return START_STICKY;
    }

    /**
     * Set player properties and listeners
     */
    public void initMusicPlayer() {
        mPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // Set class as listener for when the MediaPlayer instance is prepared
        mPlayer.setOnPreparedListener(this);
        // Set class as listener for when a song has completed playback
        mPlayer.setOnCompletionListener(this);
        // Set class as listener for when an error is thrown
        mPlayer.setOnErrorListener(this);

        mPlayer.setOnSeekCompleteListener(this);
    }

    /**
     * @param tracks
     */
    public void setTrackList(ArrayList<TrackGist> tracks) {
        mTrackList = tracks;
    }


    /**
     * Make the player ready to start playing the track
     */
    public void playTrack() {
        mPlayer.reset();
        String trackUrl = mTrackList.get(mTrackPosition).getPreviewUrl();
        Uri trackUri = Uri.parse(trackUrl);

        try {
            mPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        mPlayer.prepareAsync();
    }

    /**
     * Allows us to change the track index
     *
     * @param trackPosition
     */
    public void setTrackPosition(int trackPosition) {
        mTrackPosition = trackPosition;
    }

    public int getTrackPosition() {
        return mTrackPosition;
    }


    public TrackGist getCurrentTrack() {
        return mTrackList.get(mTrackPosition);
    }

    public int getPlayingPosition() {
        return mPlayer.getCurrentPosition();
    }

    public int getTrackDuration() {
        return mPlayer.getDuration();
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    public void pausePlayer() {
        mPlayer.pause();
    }

    public void seekTo(int position) {
        mPlayer.seekTo(position);
    }

    public void startPlayer() {
        mPlayer.start();
    }

    /**
     * Skip to previous track
     */
    public void playPreviousTrack() {
        mTrackPosition--;
        if (mTrackPosition < 0) {
            mTrackPosition = mTrackList.size() - 1;
        }
        playTrack();
    }

    /**
     * Skip to next track
     */
    public void playNextTrack() {
        mTrackPosition++;
        if (mTrackPosition >= mTrackList.size()) {
            mTrackPosition = 0;
        }
        playTrack();
    }

    public boolean isMediaPlayerPrepared() {
        return mMediaPlayerPrepared;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMusicBinder;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mPlayer.getCurrentPosition() > 0){
            mp.reset();
            playNextTrack();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

//        To handle special case of error later
//        switch (what) {
//            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
//                break;
//            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
//                break;
//            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
//                break;
//        }

        mPlayer.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        mMediaPlayerPrepared = true;

        //start playback
        mp.start();

        String trackName = mTrackList.get(mTrackPosition).getTrackName();

        Intent notificationIntent = new Intent(this, MusicPlayerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(trackName)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(trackName);

        Notification notification = builder.getNotification();

        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        mMediaPlayerPrepared = false;
        mPlayer.stop();
        mPlayer.release();
        stopForeground(true);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}
