package si.vei.pedram.spotifystreamer.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import si.vei.pedram.spotifystreamer.R;
import si.vei.pedram.spotifystreamer.models.TrackGist;

/**
 * A placeholder fragment containing a simple view.
 */
public class MusicPlayerFragment extends Fragment {

    private ArrayList<TrackGist> mTrackList;
    private int mTrackPosition;

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
}
