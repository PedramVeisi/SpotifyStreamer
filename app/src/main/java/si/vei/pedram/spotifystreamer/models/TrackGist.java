package si.vei.pedram.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class is used to summarize the returned track from Spotify and store the data we need.
 * Created by Pedram Veisi on 16/06/15.
 * @author Pedram Veisi
 *
 */

public class TrackGist implements Parcelable {

    private String mTrackName;
    private String mArtistName;
    private String mAlbumName;
    private String mLargeAlbumThumbnail;
    private String mSmallAlbumThumbnail;
    private String mPreviewUrl;
    private String mExternalUrl;

    /**
     * Constructor
     *
     * Auto-generated using Parcelable Generator Plugin to read data from Parcel.
     * https://github.com/mcharmas/android-parcelable-intellij-plugin
     * @param in
     */
    private TrackGist(Parcel in) {
        this.mTrackName = in.readString();
        this.mArtistName = in.readString();
        this.mAlbumName = in.readString();
        this.mLargeAlbumThumbnail = in.readString();
        this.mSmallAlbumThumbnail = in.readString();
        this.mPreviewUrl = in.readString();
        this.mExternalUrl = in.readString();
    }

    /**
     * Constructor
     *  @param trackName Name of Track
     * @param albumName Album of Track
     * @param largeAlbumThumbnail Large album thumbnail for the track
     * @param smallAlbumThumbnail Small album thumbnail for the track
     * @param previewUrl Preview URL of the track
     * @param externalUrl
     */
    public TrackGist(String trackName, String artistName, String albumName, String largeAlbumThumbnail, String smallAlbumThumbnail, String previewUrl, String externalUrl) {
        this.mTrackName = trackName;
        this.mArtistName = artistName;
        this.mAlbumName = albumName;
        this.mLargeAlbumThumbnail = largeAlbumThumbnail;
        this.mSmallAlbumThumbnail = smallAlbumThumbnail;
        this.mPreviewUrl = previewUrl;
        this.mExternalUrl = externalUrl;
    }


    /**
     * @return Track's name
     */
    public String getTrackName() {
        return mTrackName;
    }

    /**
     * @return Artist's name
     */
    public String getArtistName(){
        return mArtistName;
    }

    /**
     * @return Track's album
     */
    public String getAlbumName() {
        return mAlbumName;
    }

    /**
     * @return Large album thumbnail
     */
    public String getLargeAlbumThumbnail() {
        return mLargeAlbumThumbnail;
    }

    /**
     * @return Small album thumbnail
     */
    public String getSmallAlbumThumbnail() {
        return mSmallAlbumThumbnail;
    }

    /**
     * @return Preview url for the track
     */
    public String getPreviewUrl() {
        return mPreviewUrl;
    }

    /**
     * @return Preview url for the track
     */
    public String getmExternalUrl() {
        return mExternalUrl;
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
        dest.writeString(this.mTrackName);
        dest.writeString(this.mArtistName);
        dest.writeString(this.mAlbumName);
        dest.writeString(this.mLargeAlbumThumbnail);
        dest.writeString(this.mSmallAlbumThumbnail);
        dest.writeString(this.mPreviewUrl);
        dest.writeString(this.mExternalUrl);
    }

    public static final Parcelable.Creator<TrackGist> CREATOR = new Parcelable.Creator<TrackGist>() {
        public TrackGist createFromParcel(Parcel source) {
            return new TrackGist(source);
        }

        public TrackGist[] newArray(int size) {
            return new TrackGist[size];
        }
    };
}
