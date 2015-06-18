package ir.veisi.pedram.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by pedram on 16/06/15.
 * This class is used to summarize the returned track from Spotify and store the data we need.
 */
public class TrackGist implements Parcelable {

    private String mTrackName;
    private String mAlbumName;
    private String mLargeAlbumThumbnail;
    private String mSmallAlbumThumbnail;
    private String mPreviewUrl;

    // Read data from Parcel. Constructor auto-generated using Parcelable Generator Plugin
    // https://github.com/mcharmas/android-parcelable-intellij-plugin
    private TrackGist(Parcel in) {
        this.mTrackName = in.readString();
        this.mAlbumName = in.readString();
        this.mLargeAlbumThumbnail = in.readString();
        this.mSmallAlbumThumbnail = in.readString();
        this.mPreviewUrl = in.readString();
    }

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

    // Create a Parcel and save data in it.
    // Following code auto-generated using Parcelable Generator Plugin
    // https://github.com/mcharmas/android-parcelable-intellij-plugin
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mTrackName);
        dest.writeString(this.mAlbumName);
        dest.writeString(this.mLargeAlbumThumbnail);
        dest.writeString(this.mSmallAlbumThumbnail);
        dest.writeString(this.mPreviewUrl);
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
