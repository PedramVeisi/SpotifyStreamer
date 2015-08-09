package si.vei.pedram.spotifystreamer.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
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

    public static final String ACTION_PLAY = "music_service.action_play";
    public static final String ACTION_PAUSE = "music_service.action_pause";
    public static final String ACTION_NEXT = "music_service.action_next";
    public static final String ACTION_PREVIOUS = "music_service.action_previous";
    public static final String ACTION_CLOSE_NOTIFICATION = "music_service.action_close_notification";

    public static final String BROADCAST_TRACK_PAUSED = "music_service.broadcast_pause";
    public static final String BROADCAST_TRACK_PLAYED = "music_service.broadcast_play";
    public static final String BROADCAST_TRACK_CHANGED = "music_service.broadcast_track_change";
    public static final String BROADCAST_SERVICE_STOPPED = "music_service.broadcast_service_stopped";
    public static final String BROADCAST_MEDIA_PLAYER_PREPARED = "music_service.broadcast_media_player_prepared";
    public static final String BROADCAST_NOTIFICATION_CLOSED = "music_service.broadcast_notification_closed";

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

    private final IBinder mMusicBinder = new MusicBinder();
    private boolean mPlaybackPaused = false;

    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the player
        initMusicPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_STICKY;
    }

    private void handleIntent(Intent intent) {

        if (intent == null)
            return;

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
            stopMusicService();
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

        //new UpdateMetadata(mCurrentTrack, mRemoteControlClient).execute(mCurrentTrack.getLargeAlbumThumbnail());
        //UpdateMetadata();

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

    public boolean isPaused() {
        return mPlaybackPaused;
    }

    public void pausePlayer() {
        mPlayer.pause();
        mPlaybackPaused = true;
        buildNotification();
        broadcast(BROADCAST_TRACK_PAUSED);
    }

    public void seekTo(int position) {
        mPlayer.seekTo(position);
    }

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
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(trackName).build();

        setListeners(simpleContentView);
        setListeners(expandedView);

        notification.contentView = simpleContentView;
        notification.bigContentView = expandedView;


        // If notification is being created for the first time
        if (!isPlaying() && !isPaused()) {
            // Set a default image to notification and wait for Picasso to load the album art from the internet
            notification.contentView.setImageViewResource(R.id.notification_album_art_imageview, R.drawable.default_album_art);
            notification.bigContentView.setImageViewResource(R.id.notification_album_art_imageview, R.drawable.default_album_art);
        }

        Picasso.with(this).load(currentTrack.getSmallAlbumThumbnail()).into(simpleContentView, R.id.notification_album_art_imageview, NOTIFICATION_ID, notification);
        Picasso.with(this).load(currentTrack.getSmallAlbumThumbnail()).into(expandedView, R.id.notification_album_art_imageview, NOTIFICATION_ID, notification);

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

        notification.contentView.setTextViewText(R.id.notification_track_name_textview, trackName);
        notification.contentView.setTextViewText(R.id.notification_album_name_textview, albumName);
        notification.bigContentView.setTextViewText(R.id.notification_track_name_textview, trackName);
        notification.bigContentView.setTextViewText(R.id.notification_album_name_textview, albumName);

        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        startForeground(NOTIFICATION_ID, notification);
    }

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

    public Intent setAction(Context context, String action) {
        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(action);
        return intent;
    }

    private void broadcast(String message) {
        Intent intent = new Intent(message);
        // You can also include some extra data.
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void stopMusicService() {
        stopSelf();
        stopForeground(true);
        broadcast(BROADCAST_SERVICE_STOPPED);
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
        broadcast(BROADCAST_MEDIA_PLAYER_PREPARED);

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

//    private class UpdateMetadata extends AsyncTask<String, Void, Bitmap> {
//        private final RemoteControlClient remoteControlClient;
//        TrackGist currentTrack;
//
//        public UpdateMetadata(TrackGist currentTrack, RemoteControlClient remoteControlClient) {
//            this.currentTrack = currentTrack;
//            this.remoteControlClient = remoteControlClient;
//        }
//
//        protected Bitmap doInBackground(String... urls) {
//            String albumArtUrl = urls[0];
//            Bitmap albumArt = null;
//            try {
//                InputStream in = new java.net.URL(albumArtUrl).openStream();
//                albumArt = BitmapFactory.decodeStream(in);
//            } catch (Exception e) {
//                Log.e("Error", e.getMessage());
//                e.printStackTrace();
//            }
//            return albumArt;
//        }
//
//        protected void onPostExecute(Bitmap result) {
//            Log.e("TAG", "1");
//            if (remoteControlClient == null) {
//                Log.e("TAG", "2");
//                return;
//            }
//            RemoteControlClient.MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
//            metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, currentTrack.getAlbumName());
//            metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, currentTrack.getArtistName());
//            metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, currentTrack.getTrackName());
//            Bitmap defualtAlbumArt = null;
//            if (result == null) {
//                defualtAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.default_album_art);
//                Log.e("TAG", "3");
//            }
//            metadataEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, defualtAlbumArt);
//            metadataEditor.apply();
//        }
//    }


//    private void UpdateMetadata() {
//        if (mRemoteControlClient == null)
//            return;
//
//        RemoteControlClient.MetadataEditor metadataEditor = mRemoteControlClient.editMetadata(true);
//        metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, mCurrentTrack.getAlbumName());
//        metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, mCurrentTrack.getArtistName());
//        metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, mCurrentTrack.getTrackName());
//
//        //TODO Get album art from the Internet
//        Bitmap albumArt = BitmapFactory.decodeResource(getResources(), R.drawable.default_album_art);
//
//        metadataEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, albumArt);
//
//        metadataEditor.apply();
//    }


    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}

