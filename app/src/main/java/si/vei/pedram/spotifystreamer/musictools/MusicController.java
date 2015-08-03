package si.vei.pedram.spotifystreamer.musictools;

import android.content.Context;
import android.widget.MediaController;

/**
 * This class is created to override the hide method of MediaController in order to prevent it from hiding after 3 seconds
 *
 * @author Pedram Veisi
 */
public class MusicController extends MediaController {

    public MusicController(Context c) {
        super(c);
    }

    public void hide() {
    }

}