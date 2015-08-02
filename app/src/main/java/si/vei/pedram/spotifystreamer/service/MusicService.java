package si.vei.pedram.spotifystreamer.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

import java.util.ArrayList;

import si.vei.pedram.spotifystreamer.models.TrackGist;

/**
 * @author Pedram Veisi
 * Music Player Service
 */
public class MusicService extends Service  implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener{

    //media player
    private MediaPlayer mPlayer;
    //song list
    private ArrayList<TrackGist> mTrackList;
    //current position
    private int mTrackPosition;

    @Override
    public void onCreate() {
        super.onCreate();
        //initialize position
        mTrackPosition = 0;
        //create player
        mPlayer = new MediaPlayer();

        // Initialize the player
        initMusicPlayer();
    }

    public MusicService() {
    }

    /**
     * Set player properties and listeners
     */
    public void initMusicPlayer(){
        mPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // Set class as listener for when the MediaPlayer instance is prepared
        mPlayer.setOnPreparedListener(this);
        // Set class as listener for when a song has completed playback
        mPlayer.setOnCompletionListener(this);
        // Set class as listener for when an error is thrown
        mPlayer.setOnErrorListener(this);
    }

    public void setSongsList(ArrayList<TrackGist> tracks){
        mTrackList = tracks;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
}
