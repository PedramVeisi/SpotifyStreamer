package si.vei.pedram.spotifystreamer.activities;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import si.vei.pedram.spotifystreamer.R;
import si.vei.pedram.spotifystreamer.fragments.MusicPlayerFragment;
import si.vei.pedram.spotifystreamer.fragments.TopTracksFragment;
import si.vei.pedram.spotifystreamer.service.MusicService;

/**
 * @author Pedram Veisi
 */
public class TopTracksActivity extends AppCompatActivity {

    private final String MUSICPLAYERFRAGMENT_TAG = "MPFTAG";
    private boolean mMusicPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_top_tracks);

        // Set a toolbar to replace the action bar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (savedInstanceState == null) {
            // Create the top tracks fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(getString(R.string.intent_artist_id_key), getIntent().getStringExtra(getString(R.string.intent_artist_id_key)));
            arguments.putString(getString(R.string.intent_artist_image_url_key), getIntent().getStringExtra(getString(R.string.intent_artist_image_url_key)));

            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.top_tracks_container, fragment)
                    .commit();
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.BROADCAST_MEDIA_PLAYER_PREPARED);
        intentFilter.addAction(MusicService.BROADCAST_SERVICE_STOPPED);

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem nowPlayingItem = menu.findItem(R.id.action_now_playing);

        if (mMusicPlaying) {
            nowPlayingItem.setVisible(true);
        } else {
            nowPlayingItem.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // Start SettingsActivity from menu
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            return true;
        }

        if (id == R.id.action_now_playing) {
            Intent intent = new Intent(this, MusicPlayerActivity.class);
            intent.setAction(MusicService.ACTION_RESUME_PLAYER);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleBroadcastIntent(intent.getAction());
        }
    };

    private void handleBroadcastIntent(String action) {
        if (action.equalsIgnoreCase(MusicService.BROADCAST_MEDIA_PLAYER_PREPARED)) {
            mMusicPlaying = true;
            invalidateOptionsMenu();
        }
        if (action.equalsIgnoreCase(MusicService.BROADCAST_SERVICE_STOPPED)) {
            mMusicPlaying = false;
            invalidateOptionsMenu();
        }
    }

}
