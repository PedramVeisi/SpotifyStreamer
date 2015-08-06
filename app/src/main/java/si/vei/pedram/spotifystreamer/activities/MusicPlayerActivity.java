package si.vei.pedram.spotifystreamer.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import si.vei.pedram.spotifystreamer.R;
import si.vei.pedram.spotifystreamer.fragments.MusicPlayerFragment;
import si.vei.pedram.spotifystreamer.models.TrackGist;

/**
 * Music player activity
 *
 * @author Pedram Veisi
 */
public class MusicPlayerActivity extends AppCompatActivity {

    private final String MUSICPLAYERFRAGMENT_TAG = "MPFTAG";

    private ArrayList<TrackGist> mTrackList;
    private int mTrackPosition;

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

        MusicPlayerFragment musicPlayerFragment = new MusicPlayerFragment();
        musicPlayerFragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction().add(R.id.music_player_container, musicPlayerFragment, MUSICPLAYERFRAGMENT_TAG).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_music_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
