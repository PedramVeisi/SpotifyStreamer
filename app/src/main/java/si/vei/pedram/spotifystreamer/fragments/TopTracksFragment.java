package si.vei.pedram.spotifystreamer.fragments;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.RetrofitError;
import si.vei.pedram.spotifystreamer.R;
import si.vei.pedram.spotifystreamer.lists.adapters.TopTracksListAdapter;
import si.vei.pedram.spotifystreamer.models.TrackGist;

/**
 * A placeholder fragment containing a simple view.
 *
 * @author Pedram Veisi
 */
public class TopTracksFragment extends Fragment {

    private TopTracksListAdapter mTracksAdapter;
    private ArrayList<TrackGist> tracks = new ArrayList<TrackGist>();
    private String artistId;
    private String artistImageUrl;
    private GetTopTracks getTopTracks;

    /**
     * Constructor
     */
    public TopTracksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflating fragment's view to customize it
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        // Get artistId
        Bundle arguments = getArguments();
        if (arguments != null) {
            artistId = arguments.getString(getString(R.string.artist_id_key));
            artistImageUrl = arguments.getString(getString(R.string.artist_image_url_key));
        }

        if (artistImageUrl != null) {
            // Set header image view
            ImageView headerImageView = (ImageView) rootView.findViewById(R.id.top_tracks_header_imageview);
            Picasso.with(getActivity()).load(artistImageUrl).into(headerImageView);
        }

        // Instantiate the adapter
        mTracksAdapter = new TopTracksListAdapter(getActivity(), R.layout.list_item_top_tracks, new ArrayList<TrackGist>());

        ListView topTracksListView = (ListView) rootView.findViewById(R.id.artist_top_tracks_listview);
        topTracksListView.setAdapter(mTracksAdapter);

        // To show the empty view when there is no top track
        View emptyView = (View) rootView.findViewById(R.id.empty_top_tracks_list_message_view);
        topTracksListView.setEmptyView(emptyView);

        // To make sure user won't see "No track found" message before tracks are loaded!
        emptyView.setVisibility(View.GONE);

        // If user is rotating the screen, get the track data from savedInstanceState
        if (savedInstanceState != null) {
            tracks = savedInstanceState.getParcelableArrayList(getString(R.string.state_tracks));
            mTracksAdapter.clear();
            mTracksAdapter.addAll(tracks);
        } else {
            // Getting the top tracks off the UI thread.
            getTopTracks = new GetTopTracks();
            getTopTracks.execute(artistId);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Saving state in case user is rotating the device
        outState.putParcelableArrayList(getString(R.string.state_tracks), tracks);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Cancel AsyncTask if fragment is changed
        getTopTracks.cancel(true);
    }

    public class GetTopTracks extends AsyncTask<String, Void, ArrayList<TrackGist>> {
        @Override
        protected ArrayList<TrackGist> doInBackground(String... params) {

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

            List<Track> resultTracks;

            try {
                resultTracks = spotify.getArtistTopTrack(artistId, options).tracks;
            } catch (RetrofitError e) {
                e.printStackTrace();
                // If there is any error, show a toast and return an empty list.
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), getString(R.string.error_loading_results), Toast.LENGTH_LONG).show();
                    }
                });
                // Return an empty list
                return new ArrayList<TrackGist>();
            }

            String largeAlbumThumbnailUrl;
            String smallAlbumThumbnailUrl;

            ArrayList<TrackGist> trackGists = new ArrayList<TrackGist>();

            if (resultTracks != null) {
                for (Track track : resultTracks) {
                    // Fill the thumbnail variables with the first image and change them later if wanted sizes exist.
                    largeAlbumThumbnailUrl = track.album.images.get(0).url;
                    smallAlbumThumbnailUrl = track.album.images.get(0).url;

                    // Get desired thumbnail sizes in case they exist
                    for (Image image : track.album.images) {
                        if (image.width == getResources().getInteger(R.integer.album_art_large_thumbnail_width)) {
                            largeAlbumThumbnailUrl = image.url;
                        } else if (image.width == getResources().getInteger(R.integer.album_art_small_thumbnail_width)) {
                            smallAlbumThumbnailUrl = image.url;
                        }
                    }

                    // Now we have everything. Creating our summarized track
                    trackGists.add(new TrackGist(track.name, track.album.name, largeAlbumThumbnailUrl, smallAlbumThumbnailUrl, track.preview_url));
                }
            }

            return trackGists;
        }

        @Override
        protected void onPostExecute(ArrayList<TrackGist> trackGists) {
            if (isCancelled())
                return;
            else {
                tracks = trackGists;
                mTracksAdapter.clear();
                if (trackGists.size() != 0) {
                    mTracksAdapter.addAll(trackGists);
                }
            }
        }
    }
}