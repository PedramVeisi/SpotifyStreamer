package ir.veisi.pedram.spotifystreamer.lists.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import ir.veisi.pedram.spotifystreamer.R;
import ir.veisi.pedram.spotifystreamer.imagetools.PicassoRoundTransform;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by pedram on 10/06/15.
 */
public class TopTracksListAdapter extends ArrayAdapter<Track> {

    private Context mContext;
    private List<Track> mTracks;

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param tracks  The tracks to represent in the ListView.
     */
    public TopTracksListAdapter(Context context, int resource, List<Track> tracks) {
        super(context, resource, tracks);
        this.mContext = context;
        this.mTracks = tracks;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_top_tracks, parent, false);

            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.trackName = (TextView) convertView.findViewById(R.id.track_name_textview);
            viewHolder.trackAlbum = (TextView) convertView.findViewById(R.id.track_album_textview);
            viewHolder.trackAlbumImage = (ImageView) convertView.findViewById(R.id.track_album_cover_imageview);
            convertView.setTag(viewHolder);
        }

        Track track = mTracks.get(position);

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.trackName.setText(track.name);

        // Set genre(s)
        viewHolder.trackAlbum.setText(track.album.name);



        // Set track image
        if (track.album.images.size() != 0) {
            // Set the track image
            Picasso.with(mContext).load(track.album.images.get(0).url).transform(new PicassoRoundTransform()).into(viewHolder.trackAlbumImage);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView trackName;
        TextView trackAlbum;
        ImageView trackAlbumImage;
    }
}