package ir.veisi.pedram.spotifystreamer.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ir.veisi.pedram.spotifystreamer.R;
import ir.veisi.pedram.spotifystreamer.lists.adapters.TopTracksListAdapter;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;

/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksActivityFragment extends Fragment {

    private TopTracksListAdapter mTracksAdapter;

    public TopTracksActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflating fragment's view to customize it
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        String artistId = getActivity().getIntent().getStringExtra(getResources().getString(R.string.intent_artist_id_name));

        // Instantiate the adapter
        mTracksAdapter = new TopTracksListAdapter(getActivity(), R.layout.list_item_top_tracks, new ArrayList<Track>());

        ListView topTracksListView = (ListView) rootView.findViewById(R.id.artist_top_tracks_listview);
        topTracksListView.setAdapter(mTracksAdapter);

        GetTopTracks getTopTracks = new GetTopTracks();
        getTopTracks.execute(artistId);

        return rootView;
    }

    public class GetTopTracks extends AsyncTask<String, Void, List<Track>> {

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
        protected List<Track> doInBackground(String... params) {

            // Nothing to do
            if (params.length == 0) {
                return null;
            }

            String artistId = params[0];
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            Map<String, Object> options = new HashMap<>();

            // TODO Add country selection to settings
            options.put(SpotifyService.COUNTRY, Locale.getDefault().getCountry());

            List<Track> tracks = spotify.getArtistTopTrack(artistId, options).tracks;
            return tracks;
        }

        @Override
        protected void onPostExecute(List<Track> tracks) {
            mTracksAdapter.clear();
            if (tracks.size() != 0) {
                mTracksAdapter.addAll(tracks);
            }
        }
    }

}