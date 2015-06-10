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
import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by pedram on 10/06/15.
 */
public class ArtistsListAdapter extends ArrayAdapter<Artist> {

    private Context mContext;
    private List<Artist> mArtists;

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param artists  The artists to represent in the ListView.
     */
    public ArtistsListAdapter(Context context, int resource, List<Artist> artists) {
        super(context, resource, artists);
        this.mContext = context;
        this.mArtists = artists;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // TODO Add ViewHolder

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_artists, parent, false);
        }

        Artist artist = mArtists.get(position);

        TextView artistName = (TextView) convertView.findViewById(R.id.artist_name_textview);
        artistName.setText(artist.name);

        if (artist.images.size() != 0) {
            ImageView artistImage = (ImageView) convertView.findViewById(R.id.artist_imageview);

            //TODO Move to AsyncTask
            Picasso.with(mContext).load(artist.images.get(0).url).transform(new PicassoRoundTransform()).into(artistImage);

        }

        return convertView;
    }
}