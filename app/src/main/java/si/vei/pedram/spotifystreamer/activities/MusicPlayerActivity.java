package si.vei.pedram.spotifystreamer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ShareActionProvider;

import java.util.ArrayList;

import si.vei.pedram.spotifystreamer.R;
import si.vei.pedram.spotifystreamer.fragments.MusicPlayerFragment;
import si.vei.pedram.spotifystreamer.models.TrackGist;
import si.vei.pedram.spotifystreamer.service.MusicService;

/**
 * Music player activity
 *
 * @author Pedram Veisi
 */
public class MusicPlayerActivity extends AppCompatActivity {

    private final String MUSICPLAYERFRAGMENT_TAG = "MPFTAG";

    private ArrayList<TrackGist> mTrackList;
    private int mTrackPosition;
    private boolean mPlayerResumed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        if (getIntent().getExtras() != null) {
            // Get track list from calling activity
            mTrackList = getIntent().getParcelableArrayListExtra(getString(R.string.intent_track_list_key));

            // Get track position in the list
            mTrackPosition = getIntent().getExtras().getInt(getString(R.string.intent_selected_track_position));
        }

        // Handle resuming from now playing button or notification
        if (getIntent().getAction() != null) {
            if (getIntent().getAction().equalsIgnoreCase(MusicService.ACTION_RESUME_PLAYER)) {
                mPlayerResumed = true;
            }
        }

        // Set a toolbar to replace the action bar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Display back button on toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FragmentManager fm = getSupportFragmentManager();

        // Create the music player fragment and add it to the activity
        // using a fragment transaction.
        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList(getString(R.string.intent_track_list_key), mTrackList);
        arguments.putInt(getString(R.string.intent_selected_track_position), mTrackPosition);
        arguments.putBoolean(getString(R.string.intent_player_resumed), mPlayerResumed);

        MusicPlayerFragment musicPlayerFragment = new MusicPlayerFragment();
        musicPlayerFragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction().add(R.id.music_player_container, musicPlayerFragment, MUSICPLAYERFRAGMENT_TAG).commit();
    }

}