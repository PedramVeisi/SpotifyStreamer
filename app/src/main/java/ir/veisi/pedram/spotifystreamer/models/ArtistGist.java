package ir.veisi.pedram.spotifystreamer.models;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by pedram on 12/06/15.
 * This class is used to summarize the returned artist from Spotify and store the data we need.
 */
public class ArtistGist {
    private String mArtistId;
    private String mArtistName;
    private List<Image> mArtistImages;
    private List<String> mArtistGenres;

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

}