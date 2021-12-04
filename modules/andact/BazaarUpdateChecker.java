package org.godotengine.godot;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.content.Intent;
import android.util.Log;
import com.farsitel.bazaar.IUpdateCheckService;

public class BazaarUpdateChecker {

    private Activity activity = null;
    private IUpdateCheckService updateService;
    private UpdateServiceConnection updateServiceConnection;

    private long versionCode = -1;

    class UpdateServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder boundService) {
            updateService = IUpdateCheckService.Stub.asInterface((IBinder) boundService);
            try {
                versionCode = updateService.getVersionCode(activity.getPackageName());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            updateService = null;
        }
    }

    public void init(Activity activity) {

        this.activity = activity;

        updateServiceConnection = new UpdateServiceConnection();
        Intent i = new Intent(
                "com.farsitel.bazaar.service.UpdateCheckService.BIND");
        i.setPackage("com.farsitel.bazaar");

        activity.bindService(i, updateServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void release() {
        try {
            activity.unbindService(updateServiceConnection);
        } catch (IllegalArgumentException e) {
            // Somehow we've already been unbound. This is a non-fatal error.
        }
        updateServiceConnection = null;
    }

    public long getVersionCode() {
        return versionCode;
    }
}
