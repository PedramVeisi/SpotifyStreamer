package ir.veisi.pedram.spotifystreamer.fragments;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.veisi.pedram.spotifystreamer.R;
import ir.veisi.pedram.spotifystreamer.lists.adapters.TopTracksListAdapter;
import ir.veisi.pedram.spotifystreamer.models.TrackGist;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
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

        // Get artistId from calling activity
        String artistId = getActivity().getIntent().getStringExtra(getResources().getString(R.string.intent_artist_id_name));

        // Instantiate the adapter
        mTracksAdapter = new TopTracksListAdapter(getActivity(), R.layout.list_item_top_tracks, new ArrayList<TrackGist>());

        ListView topTracksListView = (ListView) rootView.findViewById(R.id.artist_top_tracks_listview);
        topTracksListView.setAdapter(mTracksAdapter);

        // To show the empty view when there is no top track
        final View emptyView = (View) rootView.findViewById(R.id.empty_list_message_view);
        topTracksListView.setEmptyView(emptyView);

        // To make sure user won't see "No track found" message before tracks are loaded!
        emptyView.setVisibility(View.GONE);

        // Getting the top tracks off the UI thread.
        GetTopTracks getTopTracks = new GetTopTracks();
        getTopTracks.execute(artistId);

        return rootView;
    }

    public class GetTopTracks extends AsyncTask<String, Void, List<TrackGist>> {

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
        protected List<TrackGist> doInBackground(String... params) {

            // Nothing to do
            if (params.length == 0) {
                return null;
            }

            String artistId = params[0];
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            // Reading Country code from settings
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String countryCode = sharedPrefs.getString(getString(R.string.pref_country_key), getString(R.string.pref_country_default_value));

            // Some countries such as Canada have two codes indicating language (ca-en, ca-fr).
            // To requests artists or tracks language doesn't matter. This line will extract the country
            // code and removes the language part (for Canada result is ca)
            String country = countryCode.split("-")[0];

            // Adding country to search parameters
            Map<String, Object> options = new HashMap<>();
            options.put(SpotifyService.COUNTRY, country);

            List<Track> resultTracks = spotify.getArtistTopTrack(artistId, options).tracks;

            // Shrinking data size by extracting what we want
            List<TrackGist> tracks = new ArrayList<TrackGist>();

            String largeAlbumThumbnailUrl;
            String smallAlbumThumbnailUrl;

            for (Track track : resultTracks){
                // Fill the thumbnail variables with the first image and change them later if wanted sizes exist.
                largeAlbumThumbnailUrl = track.album.images.get(0).url;
                smallAlbumThumbnailUrl =  track.album.images.get(0).url;

                // Get desired thumbnail sizes in case they exist
                for (Image image : track.album.images){
                    if (image.width == getResources().getInteger(R.integer.album_art_large_thumbnail_width)){
                        largeAlbumThumbnailUrl = image.url;
                    }
                    else if(image.width == getResources().getInteger(R.integer.album_art_small_thumbnail_width)){
                        smallAlbumThumbnailUrl = image.url;
                    }
                }

                // Now we have everything. Creating our summarized track
                tracks.add(new TrackGist(track.name, track.album.name, largeAlbumThumbnailUrl, smallAlbumThumbnailUrl, track.preview_url));
            }

            return tracks;
        }

        @Override
        protected void onPostExecute(List<TrackGist> tracks) {
            mTracksAdapter.clear();
            if (tracks.size() != 0) {
                mTracksAdapter.addAll(tracks);
            }
        }
    }

}