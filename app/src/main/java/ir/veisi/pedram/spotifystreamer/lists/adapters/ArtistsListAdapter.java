package ir.veisi.pedram.spotifystreamer.lists.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import ir.veisi.pedram.spotifystreamer.R;
import ir.veisi.pedram.spotifystreamer.datamodels.ArtistGist;
import ir.veisi.pedram.spotifystreamer.imagetools.PicassoRoundTransform;

/**
 * Created by pedram on 10/06/15.
 */
public class ArtistsListAdapter extends ArrayAdapter<ArtistGist> {

    private Context mContext;
    private List<ArtistGist> mArtists;

    /**
     * Constructor
     *  @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param artists  The artists to represent in the ListView.
     */
    public ArtistsListAdapter(Context context, int resource, ArrayList<ArtistGist> artists) {
        super(context, resource, artists);
        this.mContext = context;
        this.mArtists = artists;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_artists, parent, false);

            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.artistName = (TextView) convertView.findViewById(R.id.artist_name_textview);
            viewHolder.genres = (TextView) convertView.findViewById(R.id.artist_genre_textview);
            viewHolder.artistImage = (ImageView) convertView.findViewById(R.id.artist_imageview);
            convertView.setTag(viewHolder);
        }

        ArtistGist artist = mArtists.get(position);

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.artistName.setText(artist.getName());

        // Set genre(s)
        if(artist.getGenres().size() != 0){
            String delim = "";
            StringBuilder sb = new StringBuilder();
            for (String genre : artist.getGenres()){
                // Make the first letter upper case.
                genre = genre.substring(0, 1).toUpperCase() + genre.substring(1);
                sb.append(delim).append(genre);
                delim = ", ";
            }

            String allGenres = sb.toString();
            viewHolder.genres.setText(allGenres);
        }

        // Set artist image
        if (artist.getImages().size() != 0) {
            // Set the artist image
            Picasso.with(mContext).load(artist.getImages().get(0).url).transform(new PicassoRoundTransform()).into(viewHolder.artistImage);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView artistName;
        TextView genres;
        ImageView artistImage;
    }
}