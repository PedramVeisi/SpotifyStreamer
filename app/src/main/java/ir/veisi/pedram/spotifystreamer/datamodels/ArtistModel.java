package ir.veisi.pedram.spotifystreamer.datamodels;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by pedram on 12/06/15.
 */
public class ArtistModel {
    private String mArtistId;
    private String mArtistName;
    private List<Image> mArtistImages;
    private List<String> mArtistGenres;

    public String getId() {
        return mArtistId;
    }

    public void setId(String artistId) {
        this.mArtistId = artistId;
    }

    public String getName() {
        return mArtistName;
    }

    public void setName(String artistName) {
        this.mArtistName = artistName;
    }

    public List<Image> getImages() {
        return mArtistImages;
    }

    public void setImages(List<Image> artistImages) {
        this.mArtistImages = artistImages;
    }

    public List<String> getGenres() {
        return mArtistGenres;
    }

    public void setGenres(List<String> artistGenres) {
        this.mArtistGenres = artistGenres;
    }
}