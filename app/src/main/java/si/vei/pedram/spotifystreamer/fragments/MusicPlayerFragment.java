package si.vei.pedram.spotifystreamer.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import si.vei.pedram.spotifystreamer.R;
import si.vei.pedram.spotifystreamer.models.TrackGist;
import si.vei.pedram.spotifystreamer.service.MusicService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MusicPlayerFragment extends Fragment {

    private ArrayList<TrackGist> mTrackList;
    private int mTrackPosition;

    private MusicService mMusicService;
    private Intent mPlayIntent;
    private boolean mMusicBound = false;

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

        return rootView;
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get service
            mMusicService = binder.getService();
            //pass list
            mMusicService.setTrackList(mTrackList);
            mMusicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMusicBound = false;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        if(mPlayIntent == null){
            mPlayIntent = new Intent(getActivity(), MusicService.class);
            getActivity().getApplicationContext().bindService(mPlayIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().getApplicationContext().startService(mPlayIntent);
        }
    }

    @Override
    public void onDestroy() {
        getActivity().getApplicationContext().stopService(mPlayIntent);
        mMusicService = null;
        super.onDestroy();
    }

}
