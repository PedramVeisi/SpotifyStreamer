package ir.veisi.pedram.spotifystreamer.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import ir.veisi.pedram.spotifystreamer.R;

public class SettingsFragment extends PreferenceFragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_general);
    }
}
