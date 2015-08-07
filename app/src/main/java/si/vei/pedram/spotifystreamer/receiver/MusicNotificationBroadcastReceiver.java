package si.vei.pedram.spotifystreamer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import si.vei.pedram.spotifystreamer.service.MusicService;

/**
 * Created by pedram on 06/08/15.
 */
public class MusicNotificationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(
                android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {

            Intent intent1 = new Intent(MusicService.ACTION_PAUSE);
            intent1.setClass(context,
                    MusicService.class);
            // send an intent to our MusicService to telling it to pause the
            // audio
            context.startService(intent1);

        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {

            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(
                    Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                return;

            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    Intent intentPlay = new Intent(MusicService.ACTION_PLAY);
                    intentPlay.setClass(context, MusicService.class);
                    context.startService(intentPlay);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    Intent intentPause = new Intent(MusicService.ACTION_PAUSE);
                    intentPause.setClass(context, MusicService.class);
                    context.startService(intentPause);
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    Intent intentNext = new Intent(MusicService.ACTION_NEXT);
                    intentNext.setClass(context,
                            MusicService.class);
                    context.startService(intentNext);

                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    Intent intentPrev = new Intent(MusicService.ACTION_PREVIOUS);
                    intentPrev.setClass(context,
                            MusicService.class);
                    context.startService(intentPrev);

                    break;
                default:
                    break;
            }
        } else {
            if (intent.getAction().equals(MusicService.ACTION_PLAY)) {
                Intent intentPlay = new Intent(MusicService.ACTION_PLAY);
                intentPlay.setClass(context, MusicService.class);
                context.startService(intentPlay);
            } else if (intent.getAction().equals(MusicService.ACTION_PAUSE)) {
                Intent intentPause = new Intent(MusicService.ACTION_PAUSE);
                intentPause.setClass(context, MusicService.class);
                context.startService(intentPause);
            } else if (intent.getAction().equals(MusicService.ACTION_NEXT)) {
                Intent intentNext = new Intent(MusicService.ACTION_NEXT);
                intentNext.setClass(context, MusicService.class);
                context.startService(intentNext);
            } else if (intent.getAction().equals(MusicService.ACTION_CLOSE_NOTIFICATION)) {
                Intent intentCloseNotification = new Intent(MusicService.ACTION_CLOSE_NOTIFICATION);
                intentCloseNotification.setClass(context, MusicService.class);
                context.startService(intentCloseNotification);
            } else if (intent.getAction().equals(MusicService.ACTION_PREVIOUS)) {
                Intent intentPrev = new Intent(MusicService.ACTION_PREVIOUS);
                intentPrev.setClass(context, MusicService.class);
                context.startService(intentPrev);
            }
        }
    }

    public String ComponentName() {
        return this.getClass().getName();
    }

}
