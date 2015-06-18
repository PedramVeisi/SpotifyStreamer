package ir.veisi.pedram.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by pedram on 12/06/15.
 * This class is used to summarize the returned artist from Spotify and store the data we need.
 */
public class ArtistGist implements Parcelable {

    private String mArtistId;
    private String mArtistName;
    private List<Image> mArtistImages;
    private List<String> mArtistGenres;

    // Read data from Parcel. Constructor auto-generated using Parcelable Generator Plugin
    // https://github.com/mcharmas/android-parcelable-intellij-plugin
    private ArtistGist(Parcel in) {
        this.mArtistId = in.readString();
        this.mArtistName = in.readString();
        this.mArtistImages = new ArrayList<Image>();
        in.readList(this.mArtistImages, List.class.getClassLoader());
        this.mArtistGenres = in.createStringArrayList();
    }

    public ArtistGist(String artistId, String artistName, List<Image> artistImages, List<String> artistGenres){
        this.mArtistId = artistId;
        this.mArtistName = artistName;
        this.mArtistImages = artistImages;
        this.mArtistGenres = artistGenres;
    }

    // Returns artist's ID
    public String getId() {
        return mArtistId;
    }

    // Returns artist's name
    public String getName() {
        return mArtistName;
    }

    // Returns list of artist's images
    public List<Image> getImages() {
        return mArtistImages;
    }

    // Returns list of genres
    public List<String> getGenres() {
        return mArtistGenres;
    }

    // Create a Parcel and save data in it.
    // Following code auto-generated using Parcelable Generator Plugin
    // https://github.com/mcharmas/android-parcelable-intellij-plugin
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mArtistId);
        dest.writeString(this.mArtistName);
        dest.writeList(this.mArtistImages);
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