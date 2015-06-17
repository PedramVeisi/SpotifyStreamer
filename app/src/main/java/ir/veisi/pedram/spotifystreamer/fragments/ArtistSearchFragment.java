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


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistSearchFragment extends Fragment {

    // Artist search result adapter
    private ArtistsListAdapter mArtistsAdapter;

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
                    // Create a time and wait for the specified delay time then perform the search
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
                else{
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

            List<Artist> resultArtists = spotify.searchArtists(artistName, options).artists.items;
            List<ArtistGist> artists = new ArrayList<ArtistGist>();

            // Extracting required information. Using ArtistModel class, we don't need to pass all the artist data around.
            for (Artist resultArtist : resultArtists){
                ArtistGist artist = new ArtistGist(resultArtist.id, resultArtist.name, resultArtist.images, resultArtist.genres);
                artists.add(artist);
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