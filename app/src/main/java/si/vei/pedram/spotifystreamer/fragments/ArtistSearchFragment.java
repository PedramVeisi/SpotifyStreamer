package si.vei.pedram.spotifystreamer.fragments;

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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import retrofit.RetrofitError;
import si.vei.pedram.spotifystreamer.R;
import si.vei.pedram.spotifystreamer.lists.adapters.ArtistsListAdapter;
import si.vei.pedram.spotifystreamer.models.ArtistGist;


/**
 * @author Pedram Veisi
 *         A placeholder fragment containing a simple view.
 */
public class ArtistSearchFragment extends Fragment {

    // Artist search result adapter
    private ArtistsListAdapter mArtistsAdapter;
    private ArrayList<ArtistGist> artists;

    // Used to check if device is rotated. Checking for savedInstanceState is not enough
    // since we have a live search and it can't be checked in afterTextChanged() method
    private boolean rotationFlag = false;

    /**
     * Constructor
     */
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

        final View emptyView = rootView.findViewById(R.id.no_artist_empty_list_mesage_view);
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
                        // This will prevent disabling the live search.
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
                String artistImage = mArtistsAdapter.getItem(position).getImageUrl();

                // Notify the CallBack
                if(artistId != null){
                    ((Callback)getActivity()).onItemSelected(artistId, artistImage);
                }
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
     * AsyncTask to search for artists off the UI thread
     */
    public class SearchForArtist extends AsyncTask<String, Void, List<ArtistGist>> {
        // CAST THE LINEARLAYOUT HOLDING THE MAIN PROGRESS (SPINNER)
        LinearLayout searchProgressBarLinearLeayout = (LinearLayout) getActivity().findViewById(R.id.artist_search_progress_spinner_wrapper);

        @Override
        protected void onPreExecute() {
            // Check if listview is empty. IF it's not, don't show the loading spinner.
            if (mArtistsAdapter.isEmpty()) {
                // Since search is delayed and run from a thread other than the main one, this must be run on UI Thread.
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Show the spinner while loading result
                        searchProgressBarLinearLeayout.setVisibility(View.VISIBLE);
                    }
                });
            }
        }

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
            String country = sharedPrefs.getString(getString(R.string.pref_country_key), getString(R.string.pref_country_default_value));

            // Some countries such as Canada have two codes indicating language (ca-en, ca-fr)
            if (country != null) {
                country = country.split("-")[0];
            }

            options.put(SpotifyService.COUNTRY, country);

            List<Artist> resultArtists;
            artists = new ArrayList<>();

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
                return new ArrayList<>();
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

            // Hide the spinner after loading is done.
            searchProgressBarLinearLeayout.setVisibility(View.GONE);
        }
    }

    public interface Callback {
        /**
         * TopTracksFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(String artistId, String artistImageUrl);
    }

}