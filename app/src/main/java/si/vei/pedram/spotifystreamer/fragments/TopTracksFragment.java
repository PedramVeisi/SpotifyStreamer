package si.vei.pedram.spotifystreamer.fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import si.vei.pedram.spotifystreamer.activities.MusicPlayerActivity;
import si.vei.pedram.spotifystreamer.lists.adapters.TopTracksListAdapter;
import si.vei.pedram.spotifystreamer.models.TrackGist;
import si.vei.pedram.spotifystreamer.service.MusicService;

/**
 * A placeholder fragment containing top tracks for a given artist
 *
 * @author Pedram Veisi
 */
public class TopTracksFragment extends Fragment {

    private final String MUSICPLAYERFRAGMENT_TAG = "MPFTAG";

    private TopTracksListAdapter mTracksAdapter;

    private MusicService mMusicService;

    private ArrayList<TrackGist> tracks = new ArrayList();
    private GetTopTracks getTopTracks;

    private ShareActionProvider mShareActionProvider;
    private String mTrackShareText;

    private boolean mHasTwoPanes;
    private boolean mMusicPlaying;

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

        String artistImageUrl = null;
        String artistName = null;
        String artistId = null;

        // Get artistId, artist name, artist image url and music playing flag
        Bundle arguments = getArguments();
        if (arguments != null) {
            artistId = arguments.getString(getString(R.string.intent_artist_id_key));
            artistImageUrl = arguments.getString(getString(R.string.intent_artist_image_url_key));
            artistName = arguments.getString(getString(R.string.intent_artist_name_key));
            mMusicPlaying = arguments.getBoolean(getString(R.string.intent_music_playing_flag));
        }

        // Set artist name as subtitle in toolbar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(artistName);

        // Set artists image on top of top tracks fragment
        final ImageView headerImageView = (ImageView) rootView.findViewById(R.id.top_tracks_header_imageview);
        if (artistImageUrl != null) {
            // Set header image view
            Picasso.with(getActivity()).load(artistImageUrl).into(headerImageView);
        } else {
            // Hide the image view if there is no image
            headerImageView.setVisibility(View.GONE);
        }

        // Instantiate the adapter
        mTracksAdapter = new TopTracksListAdapter(getActivity(), R.layout.list_item_top_tracks, new ArrayList<TrackGist>());

        ListView topTracksListView = (ListView) rootView.findViewById(R.id.artist_top_tracks_listview);
        topTracksListView.setAdapter(mTracksAdapter);

        // The resource value for R.bool.has_two_panes is different on tablets and phones.
        mHasTwoPanes = getResources().getBoolean(R.bool.has_two_panes);

        // Indicate that fragment can access and update the toolbar
        setHasOptionsMenu(true);

        topTracksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<TrackGist> trackList = (ArrayList<TrackGist>) mTracksAdapter.getTracks();
                if (mHasTwoPanes) {
                    MusicPlayerFragment fragment = new MusicPlayerFragment();
                    // Create the music player fragment and add it to the activity
                    // using a fragment transaction.
                    Bundle arguments = new Bundle();
                    arguments.putParcelableArrayList(getString(R.string.intent_track_list_key), trackList);
                    arguments.putInt(getString(R.string.intent_selected_track_position), position);
                    arguments.putBoolean(getString(R.string.intent_has_two_pane), mHasTwoPanes);

                    MusicPlayerFragment musicPlayerFragment = new MusicPlayerFragment();
                    musicPlayerFragment.setArguments(arguments);
                    musicPlayerFragment.show(getActivity().getSupportFragmentManager(), MUSICPLAYERFRAGMENT_TAG);

                    mMusicPlaying = true;

                    // Call on CreateOptionsMenu and update toolbar
                    getActivity().invalidateOptionsMenu();

                } else {
                    Intent intent = new Intent(getActivity(), MusicPlayerActivity.class);
                    intent.putParcelableArrayListExtra(getString(R.string.intent_track_list_key), (ArrayList<TrackGist>) mTracksAdapter.getTracks());
                    intent.putExtra(getString(R.string.intent_selected_track_position), position);
                    intent.putExtra(getString(R.string.intent_has_two_pane), mHasTwoPanes);
                    startActivity(intent);
                }
            }
        });

        // To show the empty view when there is no top track
        View emptyView = rootView.findViewById(R.id.empty_top_tracks_list_message_view);
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

        // Register for local broadcast to update toolbar and shareText
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.BROADCAST_TRACK_CHANGED);
        intentFilter.addAction(MusicService.BROADCAST_MEDIA_PLAYER_PREPARED);
        intentFilter.addAction(MusicService.BROADCAST_SERVICE_STOPPED);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, intentFilter);

        // Bind to service in order to get the current playing track from the service
        Intent serviceIntent = new Intent(getActivity(), MusicService.class);
        getActivity().bindService(serviceIntent, musicConnection, Context.BIND_AUTO_CREATE);

        return rootView;
    }

    // Connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            mMusicService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Saving state in case user is rotating the device
        outState.putParcelableArrayList(getString(R.string.state_tracks), tracks);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Show share action button if app is in two pane mode an music is being played. Music player fragment will take care of this on phones
        if (mHasTwoPanes && mMusicPlaying) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getActivity().getMenuInflater().inflate(R.menu.menu_top_tracks, menu);

            // Locate MenuItem with ShareActionProvider
            MenuItem menuItem = menu.findItem(R.id.action_share);

            // Make item visible
            menuItem.setVisible(true);

            // Fetch and store ShareActionProvider
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        }
    }

    /**
     * Create share intent to set the shareActionbarProvider in toolbar. Track share text is added to the intent.
     *
     * @param trackShareText Share string to set for the shareActionProvider
     * @return the created intent
     */
    private Intent createShareTrackIntent(String trackShareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, trackShareText);
        return shareIntent;
    }

    @Override
    public void onPause() {
        // Cancel AsyncTask if fragment is changed
        if (getTopTracks != null) {
            getTopTracks.cancel(true);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
        getActivity().unbindService(musicConnection);
        super.onDestroy();
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleBroadcastIntent(intent.getAction());
        }
    };

    /**
     * Handle incoming broadcast messages
     *
     * @param action broadcast action
     */
    private void handleBroadcastIntent(String action) {
        // Update share text in case media player is ready (a song is being player) or track is changed.
        if (action.equalsIgnoreCase(MusicService.BROADCAST_TRACK_CHANGED) || action.equalsIgnoreCase(MusicService.BROADCAST_MEDIA_PLAYER_PREPARED)) {
            if (mShareActionProvider != null) {
                TrackGist currentTrack = mMusicService.getCurrentTrack();
                mTrackShareText = getString(R.string.track_share_text, currentTrack.getTrackName(), currentTrack.getArtistName(), currentTrack.getPreviewUrl());
                mShareActionProvider.setShareIntent(createShareTrackIntent(mTrackShareText));
            }
            getActivity().invalidateOptionsMenu();
        }
        // If service is stopped (user clicks on notification close button) finish the activity
        if (action.equalsIgnoreCase(MusicService.BROADCAST_SERVICE_STOPPED)) {
            getActivity().finish();
        }
    }

    /**
     * Background thread to get list of top tracks for each artist
     */
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
                return new ArrayList<>();
            }

            String largeAlbumThumbnailUrl;
            String smallAlbumThumbnailUrl;
            String artistsString = "";

            ArrayList<TrackGist> trackGists = new ArrayList<>();

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
                    trackGists.add(new TrackGist(track.name, track.artists.get(0).name, track.album.name, largeAlbumThumbnailUrl, smallAlbumThumbnailUrl, track.preview_url, track.external_urls.get("spotify")));
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