package ir.veisi.pedram.spotifystreamer.models;

/**
 * Created by pedram on 16/06/15.
 * This class is used to summarize the returned track from Spotify and store the data we need.
 */
public class TrackGist {

    String mTrackName;
    String mAlbumName;
    String mLargeAlbumThumbnail;
    String mSmallAlbumThumbnail;
    String mPreviewUrl;

    public TrackGist(String trackName, String albumName, String largeAlbumThumbnail, String smallAlbumThumbnail, String previewUrl) {
        this.mTrackName = trackName;
        this.mAlbumName = albumName;
        this.mLargeAlbumThumbnail = largeAlbumThumbnail;
        this.mSmallAlbumThumbnail = smallAlbumThumbnail;
        this.mPreviewUrl = previewUrl;
    }

    // Returns track's name
    public String getTrackName() {
        return mTrackName;
    }

    // Returns track's album
    public String getAlbumName() {
        return mAlbumName;
    }

    // Returns large album thumbnail
    public String getLargeAlbumThumbnail() {
        return mLargeAlbumThumbnail;
    }

    // Returns small album thumbnail
    public String getSmallAlbumThumbnail() {
        return mSmallAlbumThumbnail;
    }

    // Returns preview url for the track
    public String getPreviewUrl() {
        return mPreviewUrl;
    }

}
