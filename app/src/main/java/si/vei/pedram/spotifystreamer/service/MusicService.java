package si.vei.pedram.spotifystreamer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import si.vei.pedram.spotifystreamer.R;
import si.vei.pedram.spotifystreamer.activities.MusicPlayerActivity;
import si.vei.pedram.spotifystreamer.models.TrackGist;

/**
 * Music Player Service
 *
 * @author Pedram Veisi
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener {

    // Service actions
    public static final String ACTION_PLAY = "music_service.action_play";
    public static final String ACTION_PAUSE = "music_service.action_pause";
    public static final String ACTION_NEXT = "music_service.action_next";
    public static final String ACTION_PREVIOUS = "music_service.action_previous";
    public static final String ACTION_CLOSE_NOTIFICATION = "music_service.action_close_notification";
    public static final String ACTION_RESUME_PLAYER = "music_service.resume_player";

    // Local broadast messages
    public static final String BROADCAST_TRACK_PAUSED = "music_service.broadcast_pause";
    public static final String BROADCAST_TRACK_PLAYED = "music_service.broadcast_play";
    public static final String BROADCAST_TRACK_CHANGED = "music_service.broadcast_track_change";
    public static final String BROADCAST_SERVICE_STOPPED = "music_service.broadcast_service_stopped";
    public static final String BROADCAST_MEDIA_PLAYER_PREPARED = "music_service.broadcast_media_player_prepared";
    public static final String BROADCAST_NOTIFICATION_CLOSED = "music_service.broadcast_notification_closed";

    // Notification id
    private static final int NOTIFICATION_ID = 1;

    private int mSeekForwardTime;
    private int mSeekBackwardTime;
    ; // 3000 milliseconds

    //media player
    private MediaPlayer mPlayer;
    // Track list
    private ArrayList<TrackGist> mTrackList;
    // Current track position
    private int mTrackPosition;

    private boolean mMediaPlayerPrepared = false;
    private boolean mPlaybackPaused = false;

    private final IBinder mMusicBinder = new MusicBinder();
    private NotificationManager mNotificationManager;

    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mSeekForwardTime = getResources().getInteger(R.integer.music_fast_forward_time); // 3000 milliseconds
        mSeekBackwardTime = getResources().getInteger(R.integer.music_rewind_time);

        // Initialize the player
        initMusicPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_STICKY;
    }

    /**
     * Handle intent actions
     *
     * @param intent
     */
    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        // Get track list and track position from intent if exist
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
            } else {
                playTrack();
            }
        }
        if (action.equalsIgnoreCase(ACTION_PAUSE)) {
            pausePlayer();
        }
        if (action.equalsIgnoreCase(ACTION_NEXT)) {
            playNextTrack();
        }
        if (action.equalsIgnoreCase(ACTION_PREVIOUS)) {
            playPreviousTrack();
        }
        if (action.equalsIgnoreCase(ACTION_CLOSE_NOTIFICATION)) {
            broadcast(BROADCAST_NOTIFICATION_CLOSED);
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

        // In order to play music while device is locked
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

        String trackUrl = getCurrentTrack().getPreviewUrl();
        Uri trackUri = Uri.parse(trackUrl);

        try {
            mPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);

        }
        buildNotification();
        mPlayer.prepareAsync();
    }

    /**
     * Return current track
     *
     * @return current track which is being played
     */
    public TrackGist getCurrentTrack() {
        return mTrackList.get(mTrackPosition);
    }

    /**
     * Return track's playing position
     *
     * @return track's playing position
     */
    public int getPlayingPosition() {
        return mPlayer.getCurrentPosition();
    }

    /**
     * Return track duration
     *
     * @return track duration
     */
    public int getTrackDuration() {
        return mPlayer.getDuration();
    }

    /**
     * Return a boolean indicating playing status
     *
     * @return playing status
     */
    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    /**
     * Return a boolean indicating pause status
     *
     * @return pause status
     */
    public boolean isPaused() {
        return mPlaybackPaused;
    }

    /**
     * Pause player, set flags, update notification and broadcast a message indicating track being paused
     */
    public void pausePlayer() {
        mPlayer.pause();
        mPlaybackPaused = true;
        buildNotification();
        broadcast(BROADCAST_TRACK_PAUSED);
    }

    /**
     * Seeks to desired position
     *
     * @param position
     */
    public void seekTo(int position) {
        mPlayer.seekTo(position);
    }

    /**
     * Start player
     */
    public void startPlayer() {
        mPlayer.start();
        mPlaybackPaused = false;
        buildNotification();
        broadcast(BROADCAST_TRACK_PLAYED);
    }

    /**
     * Skip to previous track
     */
    public void playPreviousTrack() {
        mTrackPosition--;
        if (mTrackPosition < 0) {
            mTrackPosition = mTrackList.size() - 1;
        }
        broadcast(BROADCAST_TRACK_CHANGED);
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
        broadcast(BROADCAST_TRACK_CHANGED);
        playTrack();
    }

    /**
     * Fast forward track
     */
    public void seekForward() {
        int currentPosition = getPlayingPosition();
        int totalDuration = getTrackDuration();
        if (currentPosition + mSeekForwardTime <= totalDuration) {
            seekTo(getPlayingPosition() + mSeekForwardTime);
        } else {
            seekTo(totalDuration);
        }
    }

    /**
     * Rewind track
     */
    public void seekBackward() {
        int currentPosition = getPlayingPosition();
        if (currentPosition - mSeekBackwardTime >= 0) {
            seekTo(currentPosition - mSeekBackwardTime);
        } else {
            seekTo(0);
        }
    }

    /**
     * Return a boolean indication media player preparation status
     *
     * @return media player preparation status
     */
    public boolean isMediaPlayerPrepared() {
        return mMediaPlayerPrepared;
    }

    private void buildNotification() {

        // Reading notification settings from settings
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean notificationOn = sharedPrefs.getBoolean(getString(R.string.pref_notification_setting_key), true);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (!notificationOn) {
            mNotificationManager.cancel(NOTIFICATION_ID);
            return;
        }

        TrackGist currentTrack = mTrackList.get(mTrackPosition);

        String trackName = currentTrack.getTrackName();
        String albumName = currentTrack.getAlbumName();

        // Set layouts for simple and expanded notifications
        RemoteViews simpleContentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.music_player_notification);
        RemoteViews expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.music_player_big_notification);

        // Handle clicks on notification (resume player)
        Intent notificationIntent = new Intent(this, MusicPlayerActivity.class);
        notificationIntent.setAction(MusicService.ACTION_RESUME_PLAYER);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        // Set notification properties and build it
        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_music)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(trackName).build();

        // set listeners for views
        setListeners(simpleContentView);
        setListeners(expandedView);

        // Set small and big notifications
        notification.contentView = simpleContentView;
        notification.bigContentView = expandedView;

        // If notification is being created for the first time
        if (!isPlaying() && !isPaused()) {
            // Set a default image to notification and wait for Picasso to load the album art from the internet
            notification.contentView.setImageViewResource(R.id.notification_album_art_imageview, R.drawable.default_album_art);
            notification.bigContentView.setImageViewResource(R.id.notification_album_art_imageview, R.drawable.default_album_art);
        }

        // Load album art into image view in small and big notifications
        Picasso.with(this).load(currentTrack.getSmallAlbumThumbnail()).into(simpleContentView, R.id.notification_album_art_imageview, NOTIFICATION_ID, notification);
        Picasso.with(this).load(currentTrack.getSmallAlbumThumbnail()).into(expandedView, R.id.notification_album_art_imageview, NOTIFICATION_ID, notification);

        // Handle play and pause buttons on the notification
        if (isPaused()) {
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

        // Set track info on the notification
        notification.contentView.setTextViewText(R.id.notification_track_name_textview, trackName);
        notification.contentView.setTextViewText(R.id.notification_album_name_textview, albumName);
        notification.bigContentView.setTextViewText(R.id.notification_track_name_textview, trackName);
        notification.bigContentView.setTextViewText(R.id.notification_album_name_textview, albumName);

        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(NOTIFICATION_ID, notification);
        //startForeground(NOTIFICATION_ID, notification);
    }


    /**
     * Set listener for notification views
     *
     * @param view
     */
    public void setListeners(RemoteViews view) {
        Intent previousIntent = setAction(this, ACTION_PREVIOUS);
        Intent notificationCloseIntent = setAction(this, ACTION_CLOSE_NOTIFICATION);
        Intent pauseIntent = setAction(this, ACTION_PAUSE);
        Intent nextIntent = setAction(this, ACTION_NEXT);
        Intent playIntent = setAction(this, ACTION_PLAY);

        PendingIntent previousPendingIntent = PendingIntent.getService(getApplicationContext(), 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.notification_previous_button, previousPendingIntent);

        PendingIntent notificationClosePendingIntent = PendingIntent.getService(getApplicationContext(), 0, notificationCloseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.notification_close_button, notificationClosePendingIntent);

        PendingIntent pausePendingIntent = PendingIntent.getService(getApplicationContext(), 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.notification_pause_button, pausePendingIntent);

        PendingIntent nextPendingIntent = PendingIntent.getService(getApplicationContext(), 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.notification_next_button, nextPendingIntent);

        PendingIntent playPendingIntent = PendingIntent.getService(getApplicationContext(), 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.notification_play_button, playPendingIntent);
    }

    /**
     * Create an intent and set the required action
     *
     * @param context
     * @param action
     * @return prepared intent
     */
    public Intent setAction(Context context, String action) {
        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(action);
        return intent;
    }

    /**
     * Broadcast a message
     *
     * @param message
     */
    private void broadcast(String message) {
        Intent intent = new Intent(message);
        // You can also include some extra data.
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
        mPlayer.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        mMediaPlayerPrepared = true;
        broadcast(BROADCAST_MEDIA_PLAYER_PREPARED);

        //start playback
        mp.start();
    }

    @Override
    public void onDestroy() {
        if (mNotificationManager != null) {
            // Cancel the current notification
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
        if (mMediaPlayerPrepared) {
            mPlayer.stop();
            mPlayer.release();
        }
        mMediaPlayerPrepared = false;
        broadcast(BROADCAST_SERVICE_STOPPED);
        super.onDestroy();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    /**
     * Binder class
     */
    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}

