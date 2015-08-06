package si.vei.pedram.spotifystreamer.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat.MediaStyle;
import android.util.Log;

import java.util.ArrayList;

import si.vei.pedram.spotifystreamer.R;
import si.vei.pedram.spotifystreamer.models.TrackGist;

/**
 * @author Pedram Veisi
 *         Music Player Service
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener {

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_REWIND = "action_rewind";
    public static final String ACTION_FAST_FORWARD = "action_fast_foward";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";

    private int seekForwardTime = 3000; // 3000 milliseconds
    private int seekBackwardTime = 3000; // 3000 milliseconds

    // Notification id
    private static final int NOTIFICATION_ID = 1;

    //media player
    private MediaPlayer mPlayer;
    //song list
    private ArrayList<TrackGist> mTrackList;
    //current position
    private int mTrackPosition;

    private boolean mMediaPlayerPrepared = false;

    private MediaSessionCompat mSession;
    private MediaControllerCompat mController;

    private final IBinder mMusicBinder = new MusicBinder();
    private TrackGist mCurrentTrack;

    public MusicService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Initialize the player
        initMusicPlayer();

        handleIntent(intent);
        return START_STICKY;
    }

    private void handleIntent(Intent intent) {

        if (intent.getExtras() != null) {
            mTrackList = intent.getParcelableArrayListExtra(getString(R.string.intent_track_list_key));
            mTrackPosition = intent.getIntExtra(getString(R.string.intent_selected_track_position), 0);
        }
        if (intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();

        if (action.equalsIgnoreCase(ACTION_PLAY)) {
            mController.getTransportControls().play();
        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
            mController.getTransportControls().pause();
        } else if (action.equalsIgnoreCase(ACTION_FAST_FORWARD)) {
            mController.getTransportControls().fastForward();
        } else if (action.equalsIgnoreCase(ACTION_REWIND)) {
            mController.getTransportControls().rewind();
        } else if (action.equalsIgnoreCase(ACTION_PREVIOUS)) {
            mController.getTransportControls().skipToPrevious();
        } else if (action.equalsIgnoreCase(ACTION_NEXT)) {
            mController.getTransportControls().skipToNext();
        } else if (action.equalsIgnoreCase(ACTION_STOP)) {
            mController.getTransportControls().stop();
        }
    }

    /**
     * Set player properties and listeners
     */
    public void initMusicPlayer() {
        if (mPlayer == null) {
            //create player
            mPlayer = new MediaPlayer();
        }

        ComponentName mRemoteControlResponder = new ComponentName(getPackageName(),
                MusicIntentReceiver.class.getName());

        mSession = new MediaSessionCompat(getApplicationContext(), "MusicPlayerSession", mRemoteControlResponder, null);
        try {
            mController = new MediaControllerCompat(getApplicationContext(), mSession.getSessionToken());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

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

        mSession.setCallback(new MediaSessionCompat.Callback() {
                                 @Override
                                 public void onPlay() {
                                     super.onPlay();
                                     startPlayer();
                                     buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
                                 }

                                 @Override
                                 public void onPause() {
                                     super.onPause();
                                     pausePlayer();
                                     buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
                                 }

                                 @Override
                                 public void onSkipToNext() {
                                     super.onSkipToNext();
                                     playNextTrack();
                                     //Change media here
                                     buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
                                 }

                                 @Override
                                 public void onSkipToPrevious() {
                                     super.onSkipToPrevious();
                                     playPreviousTrack();
                                     //Change media here
                                     buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
                                 }

                                 @Override
                                 public void onFastForward() {
                                     super.onFastForward();
                                     seekForward();
                                     //Manipulate current media here
                                 }

                                 @Override
                                 public void onRewind() {
                                     super.onRewind();
                                     seekBackward();
                                     //Manipulate current media here
                                 }

                                 @Override
                                 public void onStop() {
                                     super.onStop();
                                     stopSelf();
                                     //Stop media player here
                                     NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                     notificationManager.cancel(1);
                                     Intent intent = new Intent(getApplicationContext(), MusicService.class);
                                     stopService(intent);
                                 }

                                 @Override
                                 public void onSeekTo(long pos) {
                                     super.onSeekTo(pos);
                                 }
                             }
        );
    }


    /**
     * Make the player ready to start playing the track
     */
    public void playTrack() {
        mPlayer.reset();
        mMediaPlayerPrepared = false;
        String trackUrl = mTrackList.get(mTrackPosition).getPreviewUrl();
        Uri trackUri = Uri.parse(trackUrl);

        try {
            mPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        mPlayer.prepareAsync();
    }

    public void setTrackList(ArrayList<TrackGist> trackList) {
        mTrackList = trackList;
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

    public void seekForward() {
        int currentPosition = getPlayingPosition();
        int totalDuration = getTrackDuration();
        if (currentPosition + seekForwardTime <= totalDuration) {
            seekTo(getPlayingPosition() + seekForwardTime);
        } else {
            seekTo(totalDuration);
        }
    }

    public void seekBackward() {
        int currentPosition = getPlayingPosition();
        if (currentPosition - seekBackwardTime >= 0) {
            seekTo(currentPosition - seekBackwardTime);
        } else {
            seekTo(0);
        }
    }

    public boolean isMediaPlayerPrepared() {
        return mMediaPlayerPrepared;
    }

    private void buildNotification(NotificationCompat.Action action) {

        TrackGist currentTrack = mTrackList.get(mTrackPosition);

        MediaStyle style = new MediaStyle();

        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        intent.setAction(ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(currentTrack.getTrackName())
                .setContentText(currentTrack.getArtistName())
                .setDeleteIntent(pendingIntent)
                .setStyle(style);

        builder.addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS));
        builder.addAction(generateAction(android.R.drawable.ic_media_rew, "Rewind", ACTION_REWIND));
        builder.addAction(action);
        builder.addAction(generateAction(android.R.drawable.ic_media_ff, "Fast Foward", ACTION_FAST_FORWARD));
        builder.addAction(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT));
        style.setShowActionsInCompactView(0, 1, 2, 3, 4);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMusicBinder;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mPlayer.getCurrentPosition() > 0) {
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

//        String trackName = mTrackList.get(mTrackPosition).getTrackName();
//
//        Intent notificationIntent = new Intent(this, MusicPlayerActivity.class);
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
//                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        Notification.Builder builder = new Notification.Builder(this);
//
//        builder.setContentIntent(pendInt)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setTicker(trackName)
//                .setOngoing(true)
//                .setContentTitle("Playing")
//                .setContentText(trackName);
//
//        Notification notification = builder.getNotification();
//
//        startForeground(NOTIFICATION_ID, notification);
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
