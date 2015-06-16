package ir.veisi.pedram.spotifystreamer.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import ir.veisi.pedram.spotifystreamer.fragments.SettingsFragment;

/**
 * Created by pedram on 16/06/15.
 */
public class SettingsActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}