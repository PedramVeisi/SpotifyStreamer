package ir.veisi.pedram.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * This class is used to summarize the returned artist from Spotify and store the data we need.
 * Created by Pedram Veisi on 12/06/15.
 *
 * @author Pedram Veisi
 */
public class ArtistGist implements Parcelable {

    private String mArtistId;
    private String mArtistName;
    private String mArtistImageUrl;
    private List<String> mArtistGenres;

    /**
     * Constructor
     * <p/>
     * Read data from Parcel. Constructor auto-generated using Parcelable Generator Plugin
     * https://github.com/mcharmas/android-parcelable-intellij-plugin
     *
     * @param in
     */
    private ArtistGist(Parcel in) {
        this.mArtistId = in.readString();
        this.mArtistName = in.readString();
        this.mArtistImageUrl = in.readString();
        this.mArtistGenres = in.createStringArrayList();
    }

    /**
     * Constructor
     *
     * @param artistId
     * @param artistName
     * @param artistImageUrl
     * @param artistGenres
     */
    public ArtistGist(String artistId, String artistName, String artistImageUrl, List<String> artistGenres) {
        this.mArtistId = artistId;
        this.mArtistName = artistName;
        this.mArtistImageUrl = artistImageUrl;
        this.mArtistGenres = artistGenres;
    }

    /**
     * @return Artist's ID
     */
    public String getId() {
        return mArtistId;
    }

    /**
     * @return Artist's name
     */
    public String getName() {
        return mArtistName;
    }

    /**
     * @return Artist's images URL
     */
    public String getImageUrl() {
        return mArtistImageUrl;
    }

    /**
     * @return List of genres
     */
    public List<String> getGenres() {
        return mArtistGenres;
    }

    // Create a Parcel and save data in it.
    // Following code auto-generated using Parcelable Generator Plugin
    // https://github.com/mcharmas/android-parcelable-intellij-plugin

    /**
     * {@inheritDoc}
     */
    @Override
    public int describeContents() {
        return 0;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mArtistId);
        dest.writeString(this.mArtistName);
        dest.writeString(this.mArtistImageUrl);
        dest.writeStringList(this.mArtistGenres);
    }

    public static final Parcelable.Creator<ArtistGist> CREATOR = new Parcelable.Creator<ArtistGist>() {
        public ArtistGist createFromParcel(Parcel source) {
            return new ArtistGist(source);
        }

        public ArtistGist[] newArray(int size) {
            return new ArtistGist[size];
        }
    };
}