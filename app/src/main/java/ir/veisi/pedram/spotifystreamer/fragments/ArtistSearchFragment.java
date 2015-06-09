package ir.veisi.pedram.spotifystreamer.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ir.veisi.pedram.spotifystreamer.R;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistSearchFragment extends Fragment {

    public ArtistSearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflating fragment's view to customize it
        View rootView = inflater.inflate(R.layout.fragment_artist_search, container, false);

        // Find reference to search EditText
        final EditText searchEditText = (EditText) rootView.findViewById(R.id.search_artist_edit_text);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = searchEditText.getText().toString();

                // Check for null or empty string to avoid exception thrown because of bad request
                if (!"".equals(query)){
                    SearchForArtist searchTask = new SearchForArtist();
                    searchTask.execute(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return rootView;

    }


    public class SearchForArtist extends AsyncTask<String, Void, Void>{

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
        protected Void doInBackground(String... params) {

            // Nothing to do
            if (params.length == 0) {
                return null;
            }

            String artistName = params[0];

            SpotifyApi api = new SpotifyApi();

            SpotifyService spotify = api.getService();

            Map<String, Object> options = new HashMap<>();
            options.put(SpotifyService.COUNTRY, Locale.getDefault().getCountry());

            // TODO Recover in case of error! e.g. Bad request.

            List<Artist> result = spotify.searchArtists(artistName, options).artists.items;

            for (Artist artist : result) {
                Log.e("TEST", artist.name);
            }

            return null;
        }
    }

}
