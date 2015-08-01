package si.vei.pedram.spotifystreamer.lists.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import si.vei.pedram.spotifystreamer.R;
import si.vei.pedram.spotifystreamer.imagetools.PicassoRoundTransform;
import si.vei.pedram.spotifystreamer.models.TrackGist;

/**
 * Custom adapter to populate the Top Tracks List
 *
 * Created by Pedram Veisi on 10/06/15.
 * @author Pedram Veisi
 */
public class TopTracksListAdapter extends ArrayAdapter<TrackGist> {

    private Context mContext;
    private List<TrackGist> mTracks;

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param tracks   The tracks to represent in the ListView.
     */
    public TopTracksListAdapter(Context context, int resource, List<TrackGist> tracks) {
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

        TrackGist track = mTracks.get(position);

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.trackName.setText(track.getTrackName());

        // Set genre(s)
        viewHolder.trackAlbum.setText(track.getAlbumName());

        if (track.getSmallAlbumThumbnail() != null) {
            // Set the track image (small thumbnail)
            Picasso.with(mContext).load(track.getSmallAlbumThumbnail()).transform(new PicassoRoundTransform()).into(viewHolder.trackAlbumImage);
        } else {
            viewHolder.trackAlbumImage.setImageResource(R.drawable.no_image_available);
        }

        return convertView;
    }

    /*
    * ViewHolder class in order to make the listview scrolling smooth
    */
    static class ViewHolder {
        TextView trackName;
        TextView trackAlbum;
        ImageView trackAlbumImage;
    }
}