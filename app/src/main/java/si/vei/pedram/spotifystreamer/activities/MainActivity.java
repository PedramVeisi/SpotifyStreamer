package si.vei.pedram.spotifystreamer.activities;

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
import si.vei.pedram.spotifystreamer.fragments.ArtistSearchFragment;
import si.vei.pedram.spotifystreamer.fragments.MusicPlayerFragment;
import si.vei.pedram.spotifystreamer.fragments.TopTracksFragment;
import si.vei.pedram.spotifystreamer.service.MusicService;

/**
 * @author Pedram Veisi
 */
public class MainActivity extends AppCompatActivity implements ArtistSearchFragment.Callback {

    private static final String TOPTRACKSFRAGMENT_TAG = "TTFTAG";
    private final String MUSICPLAYERFRAGMENT_TAG = "MPFTAG";
    private boolean mTwoPane = false;
    public boolean mMusicPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Set a toolbar to replace the action bar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (findViewById(R.id.top_tracks_container) != null) {
            // The top tracks container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
        }

        // Register for local broadcast to update toolbar
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.BROADCAST_MEDIA_PLAYER_PREPARED);
        intentFilter.addAction(MusicService.BROADCAST_SERVICE_STOPPED);

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Show now playing button if music is being played
        MenuItem nowPlayingItem = menu.findItem(R.id.action_now_playing);

        if (mMusicPlaying) {
            nowPlayingItem.setVisible(true);
        } else {
            nowPlayingItem.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
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
            // If we are in two pane mode show the dialog fragment. Otherwise start music player activity
            if (mTwoPane) {
                Bundle arguments = new Bundle();
                arguments.putBoolean(getString(R.string.intent_player_resumed), true);
                arguments.putBoolean(getString(R.string.intent_has_two_pane), mTwoPane);
                MusicPlayerFragment musicPlayerFragment = new MusicPlayerFragment();
                musicPlayerFragment.setArguments(arguments);
                musicPlayerFragment.show(getSupportFragmentManager(), MUSICPLAYERFRAGMENT_TAG);
            } else {
                Intent intent = new Intent(this, MusicPlayerActivity.class);
                intent.setAction(MusicService.ACTION_RESUME_PLAYER);
                startActivity(intent);
            }


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Broadcast receiver
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleBroadcastIntent(intent.getAction());
        }
    };

    /**
     * Handle broadcast messages to update toolbar.
     *
     * @param action
     */
    private void handleBroadcastIntent(String action) {
        // When music player is prepared
        if (action.equalsIgnoreCase(MusicService.BROADCAST_MEDIA_PLAYER_PREPARED)) {
            mMusicPlaying = true;
            invalidateOptionsMenu();
        }
        // When service is stopped
        if (action.equalsIgnoreCase(MusicService.BROADCAST_SERVICE_STOPPED)) {
            mMusicPlaying = false;
            // Close the activity if service is stopped (user clicks on notification close button)
            finish();

        }
    }

    @Override
    public void onItemSelected(String artistId, String artistName, String artistImageUrl) {
        if (mTwoPane) {
            // In two-pane mode, show the top tracks view in this activity by
            // adding or replacing the top tracks fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putString(getString(R.string.intent_artist_id_key), artistId);
            args.putString(getString(R.string.intent_artist_name_key), artistName);
            args.putString(getString(R.string.intent_artist_image_url_key), artistImageUrl);
            args.putBoolean(getString(R.string.intent_music_playing_flag), mMusicPlaying);

            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_tracks_container, fragment, TOPTRACKSFRAGMENT_TAG)
                    .commit();

        } else {
            Intent intent = new Intent(this, TopTracksActivity.class);
            intent.putExtra(getString(R.string.intent_artist_id_key), artistId);
            intent.putExtra(getString(R.string.intent_artist_name_key), artistName);
            intent.putExtra(getString(R.string.intent_artist_image_url_key), artistImageUrl);
            intent.putExtra(getString(R.string.intent_music_playing_flag), mMusicPlaying);
            startActivity(intent);
        }
    }
}