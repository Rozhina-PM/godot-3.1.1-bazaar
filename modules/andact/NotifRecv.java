package org.godotengine.godot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.godotengine.godot.AndAct;

public class NotifRecv extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int tag = intent.getIntExtra("tag", 1);
        String message = intent.getStringExtra("message");
        String title = intent.getStringExtra("title");
        String info = intent.getStringExtra("info");
        AndAct._instant_notification(message, title, info, tag, context);
    }
}
