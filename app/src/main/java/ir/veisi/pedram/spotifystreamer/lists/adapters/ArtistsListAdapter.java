package ir.veisi.pedram.spotifystreamer.lists.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ir.veisi.pedram.spotifystreamer.R;
import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by pedram on 10/06/15.
 */
public class ArtistsListAdapter extends ArrayAdapter<Artist> {

    private Context mContext;
    private  List<Artist> mArtists;

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

        View rowView = convertView;

        // TODO Add ViewHolder

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_item_artists, parent, false);
        }
        else{
            Artist artist = mArtists.get(position);

            TextView artistName = (TextView) rowView.findViewById(R.id.artist_name_textview);
            ImageView artistImage = (ImageView) rowView.findViewById(R.id.artist_imageview);

            artistName.setText(artist.name);
            //artistImage.setImag(artist.images.get(0));
        }

        return rowView;
    }
}
