package si.vei.pedram.spotifystreamer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import si.vei.pedram.spotifystreamer.R;
import si.vei.pedram.spotifystreamer.models.TrackGist;
import si.vei.pedram.spotifystreamer.service.MusicService;

/**
 * Music player activity
 *
 * @author Pedram Veisi
 */
public class MusicPlayerActivity extends ActionBarActivity {

    private MusicService mMusicService;
    private Intent mPlayIntent;
    private boolean mMusicBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        // Get track list from calling activity
        ArrayList<TrackGist> trackList = getIntent().getParcelableArrayListExtra(getString(R.string.intent_track_list_key));

        // Get track position in the list
        int trackPosition = getIntent().getExtras().getInt(getString(R.string.intent_selected_track_position));

        // Set a toolbar to replace the action bar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Display back button on toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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
