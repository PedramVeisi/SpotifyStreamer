package ir.veisi.pedram.spotifystreamer.models;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by pedram on 12/06/15.
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

    public String getId() {
        return mArtistId;
    }

    public String getName() {
        return mArtistName;
    }

    public List<Image> getImages() {
        return mArtistImages;
    }

    public List<String> getGenres() {
        return mArtistGenres;
    }

}