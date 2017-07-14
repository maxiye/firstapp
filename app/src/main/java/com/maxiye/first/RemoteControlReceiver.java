package com.maxiye.first;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.widget.Toast;

/**线控
 * Created by Administrator on 2017-06-14.
 */

public class RemoteControlReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())){
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                Toast.makeText(context,"Pressed play btn", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
