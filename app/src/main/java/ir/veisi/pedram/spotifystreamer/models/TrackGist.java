package ir.veisi.pedram.spotifystreamer.models;

/**
 * Created by pedram on 16/06/15.
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

    public String getTrackName() {
        return mTrackName;
    }

    public String getAlbumName() {
        return mAlbumName;
    }

    public String getLargeAlbumThumbnail() {
        return mLargeAlbumThumbnail;
    }

    public String getSmallAlbumThumbnail() {
        return mSmallAlbumThumbnail;
    }

    public String getPreviewUrl() {
        return mPreviewUrl;
    }

}
