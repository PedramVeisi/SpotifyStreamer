package si.vei.pedram.spotifystreamer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import si.vei.pedram.spotifystreamer.service.MusicService;

/**
 * Created by pedram on 06/08/15.
 */
public class MusicNotificationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        // Pause music in case headphone is unplugged
        if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            Intent pauseIntent = new Intent(MusicService.ACTION_PAUSE);
            pauseIntent.setClass(context,
                    MusicService.class);
            // send an intent to our MusicService to telling it to pause the
            // audio
            context.startService(pauseIntent);

        }
    }

    public String ComponentName() {
        return this.getClass().getName();
    }

}
