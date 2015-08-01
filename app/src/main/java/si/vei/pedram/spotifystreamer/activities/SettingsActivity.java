package si.vei.pedram.spotifystreamer.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import si.vei.pedram.spotifystreamer.fragments.SettingsFragment;

/**
 * Created by Pedram Veisi on 16/06/15.
 *
 *  @author Pedram Veisi
 */
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}