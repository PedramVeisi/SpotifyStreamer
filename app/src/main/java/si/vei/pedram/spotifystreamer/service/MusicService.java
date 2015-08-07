package si.vei.pedram.spotifystreamer.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

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

    public static final String ACTION_PLAY = "si.vei.spotifystreamer.mediaplayer.action_play";
    public static final String ACTION_PAUSE = "si.vei.spotifystreamer.mediaplayer.action_pause";
    //public static final String ACTION_REWIND = "action_rewind";
    //public static final String ACTION_FAST_FORWARD = "action_fast_foward";
    public static final String ACTION_NEXT = "si.vei.spotifystreamer.mediaplayer.action_next";
    public static final String ACTION_PREVIOUS = "si.vei.spotifystreamer.mediaplayer.action_previous";
    public static final String ACTION_STOP = "si.vei.spotifystreamer.mediaplayer.action_stop";
    public static final String ACTION_CLOSE_NOTIFICATION = "si.vei.spotifystreamer.mediaplayer.action_close_notification";


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
    private Handler mTrackChangeHandler;
    private Handler mPlayPauseHandler;
    private RemoteControlClient remoteControlClient;
    private ComponentName remoteComponentName;
    private AudioManager audioManager;
    private boolean mPlaybackPaused = false;

    public MusicService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // Initialize the player
        initMusicPlayer();

        RegisterRemoteClient();

        handleIntent(intent);

//        mTrackChangeHandler = new Handler(new Handler.Callback() {
//            @Override
//            public boolean handleMessage(Message msg) {
//                buildNotification();
//                try {
//                    playTrack();
//                    MusicPlayerFragment.updateUI();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                return false;
//            }
//        });
//
//        mPlayPauseHandler = new Handler(new Handler.Callback() {
//            @Override
//            public boolean handleMessage(Message msg) {
//                String message = (String) msg.obj;
//                if (mPlayer == null)
//                    return false;
//                if (message.equalsIgnoreCase("Play")) {
//                    remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
//                    mPlayer.start();
//                } else if (message.equalsIgnoreCase("Pause")) {
//                    remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
//                    mPlayer.pause();
//                }
//                buildNotification();
//                try {
//                    MusicPlayerFragment.changeButton();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                Log.d("TAG", "TAG Pressed: " + message);
//                return false;
//            }
//        });

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
            if (mPlaybackPaused) {
                startPlayer();
                buildNotification();
            } else {
                playTrack();
            }
        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
            pausePlayer();
            buildNotification();
        } else if (action.equalsIgnoreCase(ACTION_NEXT)) {
            playNextTrack();
        } else if (action.equalsIgnoreCase(ACTION_CLOSE_NOTIFICATION)) {
            stopSelf();
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
     * Make the player ready to start playing the track
     */
    public void playTrack() {
        mPlayer.reset();

        mPlaybackPaused = false;
        mMediaPlayerPrepared = false;

        String trackUrl = mTrackList.get(mTrackPosition).getPreviewUrl();
        Uri trackUri = Uri.parse(trackUrl);

        try {
            mPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        buildNotification();
        remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        mPlayer.prepareAsync();
    }

    private void RegisterRemoteClient() {
        remoteComponentName = new ComponentName(getApplicationContext(), new MusicNotificationBroadcastReceiver().ComponentName());
        try {
            if (remoteControlClient == null) {
                audioManager.registerMediaButtonEventReceiver(remoteComponentName);
                Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                mediaButtonIntent.setComponent(remoteComponentName);
                PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
                remoteControlClient = new RemoteControlClient(mediaPendingIntent);
                audioManager.registerRemoteControlClient(remoteControlClient);
            }
            remoteControlClient.setTransportControlFlags(
                    RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                            RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                            RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE |
                            RemoteControlClient.FLAG_KEY_MEDIA_STOP |
                            RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
                            RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        mPlaybackPaused = true;
    }

    public void seekTo(int position) {
        mPlayer.seekTo(position);
    }

    public void startPlayer() {
        mPlayer.start();
        mPlaybackPaused = false;
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

    private void buildNotification() {

        TrackGist currentTrack = mTrackList.get(mTrackPosition);

        String trackName = currentTrack.getTrackName();
        String albumName = currentTrack.getAlbumName();
        RemoteViews simpleContentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.music_player_notification);
        RemoteViews expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.music_player_big_notification);

        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_music)
                .setContentTitle(trackName).build();

        setListeners(simpleContentView);
        setListeners(expandedView);

        notification.contentView = simpleContentView;
        notification.bigContentView = expandedView;

        try {
            // Set a default image to notification and wait for Picasso to load the album art from the internet
            notification.contentView.setImageViewResource(R.id.notification_album_art_imageview, R.drawable.default_album_art);
            //notification.bigContentView.setImageViewResource(R.id.notification_album_art_imageview, R.drawable.default_album_art);

            Picasso.with(this).load(currentTrack.getSmallAlbumThumbnail()).into(simpleContentView, R.id.notification_album_art_imageview, NOTIFICATION_ID, notification);
            Picasso.with(this).load(currentTrack.getSmallAlbumThumbnail()).into(expandedView, R.id.notification_album_art_imageview, NOTIFICATION_ID, notification);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mPlaybackPaused) {
            notification.contentView.setViewVisibility(R.id.notification_pause_button, View.GONE);
            notification.contentView.setViewVisibility(R.id.notification_play_button, View.VISIBLE);

            notification.bigContentView.setViewVisibility(R.id.notification_pause_button, View.GONE);
            notification.bigContentView.setViewVisibility(R.id.notification_play_button, View.VISIBLE);
        } else {
            notification.contentView.setViewVisibility(R.id.notification_pause_button, View.VISIBLE);
            notification.contentView.setViewVisibility(R.id.notification_play_button, View.GONE);

            notification.bigContentView.setViewVisibility(R.id.notification_pause_button, View.VISIBLE);
            notification.bigContentView.setViewVisibility(R.id.notification_play_button, View.GONE);
        }

        notification.contentView.setTextViewText(R.id.notification_track_name_textview, trackName);
        notification.contentView.setTextViewText(R.id.notification_album_name_textview, albumName);
        notification.bigContentView.setTextViewText(R.id.notification_track_name_textview, trackName);
        notification.bigContentView.setTextViewText(R.id.notification_album_name_textview, albumName);

        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        startForeground(NOTIFICATION_ID, notification);
    }

    public void setListeners(RemoteViews view) {
        Intent previous = new Intent(ACTION_PREVIOUS);
        Intent delete = new Intent(ACTION_CLOSE_NOTIFICATION);
        Intent pause = new Intent(ACTION_PAUSE);
        Intent next = new Intent(ACTION_NEXT);
        Intent play = new Intent(ACTION_PLAY);

        PendingIntent pPrevious = PendingIntent.getBroadcast(getApplicationContext(), 0, previous, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.notification_previous_button, pPrevious);

        PendingIntent pDelete = PendingIntent.getBroadcast(getApplicationContext(), 0, delete, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.notification_close_button, pDelete);

        PendingIntent pPause = PendingIntent.getBroadcast(getApplicationContext(), 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.notification_pause_button, pPause);

        PendingIntent pNext = PendingIntent.getBroadcast(getApplicationContext(), 0, next, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.notification_next_button, pNext);

        PendingIntent pPlay = PendingIntent.getBroadcast(getApplicationContext(), 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.notification_play_button, pPlay);
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
    }

    @Override
    public void onDestroy() {
        mMediaPlayerPrepared = false;
        mPlayer.stop();
        mPlayer.release();
        stopForeground(true);
        super.onDestroy();
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
