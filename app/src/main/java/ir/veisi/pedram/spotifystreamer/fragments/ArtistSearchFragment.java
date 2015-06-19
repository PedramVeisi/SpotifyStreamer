package ir.veisi.pedram.spotifystreamer.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import ir.veisi.pedram.spotifystreamer.R;
import ir.veisi.pedram.spotifystreamer.activities.TopTracksActivity;
import ir.veisi.pedram.spotifystreamer.lists.adapters.ArtistsListAdapter;
import ir.veisi.pedram.spotifystreamer.models.ArtistGist;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import retrofit.RetrofitError;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistSearchFragment extends Fragment {

    // Artist search result adapter
    private ArtistsListAdapter mArtistsAdapter;
    private ArrayList<ArtistGist> artists;

    // Used to check if device is rotated. Checking for savedInstanceState is not enough
    // since we have a live search and it can't be checked in afterTextChanged() method
    private boolean rotationFlag = false;

    public ArtistSearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflating fragment's view to customize it
        View rootView = inflater.inflate(R.layout.fragment_artist_search, container, false);

        // Instantiate the adapter
        mArtistsAdapter = new ArtistsListAdapter(getActivity(), R.layout.list_item_artists, new ArrayList<ArtistGist>());

        // Reference to the listview
        ListView artistsListView = (ListView) rootView.findViewById(R.id.artist_search_result_listview);
        artistsListView.setAdapter(mArtistsAdapter);

        final View emptyView = (View) rootView.findViewById(R.id.empty_list_message_view);
        artistsListView.setEmptyView(emptyView);

        // To make sure user won't see "No artist found" message before searching anything!
        emptyView.setVisibility(View.GONE);

        // If user is rotating the screen, get the track data from savedInstanceState
        if (savedInstanceState != null) {
            artists = savedInstanceState.getParcelableArrayList(getString(R.string.state_artists));
            mArtistsAdapter.clear();
            mArtistsAdapter.addAll(artists);
            rotationFlag = true;
        }

        // Find reference to search EditText
        final EditText searchEditText = (EditText) rootView.findViewById(R.id.search_artist_edit_text);

        // Implementing live search
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            // Using a timer to delay search. It will help preventing multiple API calls while user is still typing
            Timer timer = new Timer();
            // Search delay time in ms
            private final long delay = getResources().getInteger(R.integer.artist_search_delay);

            @Override
            public void afterTextChanged(Editable s) {
                final String query = searchEditText.getText().toString();

                // Check for null or empty string to avoid exception thrown because of bad request.
                // With empty string the listview will be cleared.
                if (!"".equals(query)) {
                    if (rotationFlag) {
                        rotationFlag = false;
                    } else {
                        // Create a timer and wait for the specified delay time then perform the search
                        timer.cancel();
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                SearchForArtist searchTask = new SearchForArtist();
                                searchTask.execute(query);
                            }
                        }, delay);
                    }
                } else {
                    // If the search box is empty cancel the timer to prevent delayed listview population,
                    // clear the adapter to empty the list (if not already)
                    // and don't show the "No Artist Found" message.
                    timer.cancel();
                    mArtistsAdapter.clear();
                    emptyView.setVisibility(View.GONE);
                }
            }
        });

        // Start TopTracksActivity for each artist
        artistsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String artistId = mArtistsAdapter.getItem(position).getId();
                Intent intent = new Intent(getActivity(), TopTracksActivity.class);
                intent.putExtra(getString(R.string.intent_artist_id), artistId);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Saving state in case user is rotating the device
        outState.putParcelableArrayList(getString(R.string.state_artists), artists);
    }

    /*
     *  AsyncTask to search for artists off the UI thread
     */
    public class SearchForArtist extends AsyncTask<String, Void, List<ArtistGist>> {
        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected List<ArtistGist> doInBackground(String... params) {

            // Nothing to do
            if (params.length == 0) {
                return null;
            }

            String artistName = params[0];
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            Map<String, Object> options = new HashMap<>();

            // Reading Country code from settings

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String countryCode = sharedPrefs.getString(getString(R.string.pref_country_key), getString(R.string.pref_country_default_value));

            // Some countries such as Canada have two codes indicating language (ca-en, ca-fr)
            String country = countryCode.split("-")[0];

            options.put(SpotifyService.COUNTRY, country);

            List<Artist> resultArtists = null;
            artists = new ArrayList<ArtistGist>();

            try {
                resultArtists = spotify.searchArtists(artistName, options).artists.items;
            } catch (RetrofitError e) {
                e.printStackTrace();
                // If there is any error, show a toast and return the artists which is an empty list.
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), getString(R.string.error_loading_results), Toast.LENGTH_LONG).show();
                    }
                });
                return new ArrayList<ArtistGist>();
            }

            if (resultArtists != null) {
                // Extracting required information. Using ArtistModel class, we don't need to pass all the artist data around.
                for (Artist resultArtist : resultArtists) {
                    String artistImage = null;
                    if (resultArtist.images.size() != 0) {
                        artistImage = resultArtist.images.get(0).url;
                    }
                    ArtistGist artist = new ArtistGist(resultArtist.id, resultArtist.name, artistImage, resultArtist.genres);
                    artists.add(artist);
                }

            }

            return artists;
        }

        @Override
        protected void onPostExecute(List<ArtistGist> artists) {
            mArtistsAdapter.clear();
            if (artists.size() != 0) {
                mArtistsAdapter.addAll(artists);
            }
        }
    }
}