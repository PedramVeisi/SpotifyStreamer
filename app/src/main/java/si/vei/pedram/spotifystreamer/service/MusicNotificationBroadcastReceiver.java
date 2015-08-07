package si.vei.pedram.spotifystreamer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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

//        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
//
//            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(
//                    Intent.EXTRA_KEY_EVENT);
//            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
//                return;
//
//            switch (keyEvent.getKeyCode()) {
//                case KeyEvent.KEYCODE_HEADSETHOOK:
//                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
//                    Intent intentPlayPause = new Intent(MusicService.ACTION_PLAY_PAUSE);
//                    intentPlayPause.setClass(context,
//                            MusicService.class);
//                    context.startService(intentPlayPause);
//                case KeyEvent.KEYCODE_MEDIA_PLAY:
//                    break;
//                case KeyEvent.KEYCODE_MEDIA_PAUSE:
//                    break;
//                case KeyEvent.KEYCODE_MEDIA_NEXT:
//                    Intent intentNext = new Intent(MusicService.ACTION_NEXT);
//                    intentNext.setClass(context,
//                            MusicService.class);
//                    context.startService(intentNext);
//
//                    break;
//                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
//                    Intent intentPrev = new Intent(MusicService.ACTION_PREVIOUS);
//                    intentPrev.setClass(context,
//                            MusicService.class);
//                    context.startService(intentPrev);
//
//                    break;
//                default:
//                    break;
//            }
        } else {
            if (intent.getAction().equals(MusicService.ACTION_PLAY_PAUSE)) {
                Intent intentPlayPause = new Intent(MusicService.ACTION_PLAY_PAUSE);
                intentPlayPause.setClass(context, MusicService.class);
                context.startService(intentPlayPause);
            } else if (intent.getAction().equals(MusicService.ACTION_NEXT)) {
                Intent intentNext = new Intent(MusicService.ACTION_NEXT);
                intentNext.setClass(context, MusicService.class);
                context.startService(intentNext);
            } else if (intent.getAction().equals(MusicService.ACTION_CLOSE_NOTIFICATION)) {
                Intent i = new Intent(context, MusicService.class);
                context.stopService(i);
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
