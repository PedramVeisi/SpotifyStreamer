package si.vei.pedram.spotifystreamer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import si.vei.pedram.spotifystreamer.R;
import si.vei.pedram.spotifystreamer.fragments.ArtistSearchFragment;
import si.vei.pedram.spotifystreamer.fragments.TopTracksFragment;

/**
 * @author Pedram Veisi
 */
public class MainActivity extends AppCompatActivity implements ArtistSearchFragment.Callback {

    private static final String TOPTRACKSFRAGMENT_TAG = "TTFTAG";
    private boolean mTwoPane;

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            // Start SettingsActivity from menu
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(String artistId, String artistImageUrl) {
        if (mTwoPane) {
            // In two-pane mode, show the top tracks view in this activity by
            // adding or replacing the top tracks fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putString(getString(R.string.artist_id_key), artistId);
            args.putString(getString(R.string.artist_image_url_key), artistImageUrl);

            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_tracks_container, fragment, TOPTRACKSFRAGMENT_TAG)
                    .commit();

        } else {
            Intent intent = new Intent(this, TopTracksActivity.class);
            intent.putExtra(getString(R.string.artist_id_key), artistId);
            intent.putExtra(getString(R.string.artist_image_url_key), artistImageUrl);
            startActivity(intent);
        }
    }
}