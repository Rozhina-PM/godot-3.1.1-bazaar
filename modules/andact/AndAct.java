package org.godotengine.godot;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Rect;
import android.content.ContextWrapper;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.View;
import android.view.KeyEvent;
import androidx.core.content.FileProvider;

import androidx.core.app.NotificationCompat;

import android.util.Log;
import android.content.Intent;
import android.content.ActivityNotFoundException;

import com.godot.game.R;

import java.util.Calendar;
import java.io.File;
import java.lang.Long;

import org.godotengine.godot.BazaarUpdateChecker;
import org.godotengine.godot.UnbiasedTime;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerClient.InstallReferrerResponse;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;

public class AndAct extends Godot.SingletonBase {
    private Activity activity = null; // The main activity of the game
    private final static int NOTIFICATION_TAG = 100;
    private BazaarUpdateChecker bazaarUpdateChecker;
    private InstallReferrerClient referrerClient;
    private static int instanceId = 0;
    private static boolean startWithNotif = false;
    private static int lastNotifTag = 0;


    public void init(int _instanceId) {
        instanceId = _instanceId;
        if (startWithNotif) {
            GodotLib.calldeferred(instanceId, "_on_start_with_notification", new Object[] { lastNotifTag });
        }
    }

    public static void on_start_with_notification(int tag) {
        startWithNotif = true;
        lastNotifTag = tag;
    }

    /**
     * start_activity method
     *
     * @param String uri
     * @param String package
     * @param String action 0: ACTION_MAIN, 1: ACTION_VIEW, 2: ACTION_EDIT
     */
    public void start_activity(final String uri, final String pkg, final int action) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                switch (action) {
                    case 0:
                        intent.setAction(Intent.ACTION_MAIN);
                        break;
                    case 1:
                        intent.setAction(Intent.ACTION_VIEW);
                        break;
                    case 2:
                        intent.setAction(Intent.ACTION_EDIT);
                        break;
                }
                intent.setData(Uri.parse(uri));
                intent.setPackage(pkg);
                try {
                    activity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    return;
                }
            }
        });
    }

    /**
     * instant_notification method
     *
     * @param String message
     * @param String title
     * @param String info
     * @param int    tag
     */
    public void instant_notification(String message, String title, String info) {
        _instant_notification(message, title, info, -1, activity.getApplicationContext());
    }

    /**
     * deffered_notification method
     *
     * @param String message
     * @param String title
     * @param String info
     * @param int    tag
     * @param int    interval
     */
    public void deffered_notification(String message, String title, String info, int tag, int interval) {
        Intent intent = new Intent(activity.getApplicationContext(), org.godotengine.godot.NotifRecv.class);
        intent.putExtra("tag", tag);
        intent.putExtra("message", message);
        intent.putExtra("title", title);
        intent.putExtra("info", info);
        intent.putExtra("notification", "notification");
        intent.putExtra("notification", "notificationsalam");
        intent.putExtra("notificationTag", tag);

        PendingIntent notificationIntent = PendingIntent.getBroadcast(activity, tag, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, interval);

        AlarmManager notificationAlarm = (AlarmManager) activity.getSystemService(activity.ALARM_SERVICE);
        notificationAlarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), notificationIntent);
    }

    /**
     * cancel_notification method
     */
    public void cancel_notification(int tag) {
        Intent intent = new Intent(activity.getApplicationContext(), org.godotengine.godot.NotifRecv.class);
        PendingIntent notificationIntent = PendingIntent.getBroadcast(activity, tag, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager notificationAlarm = (AlarmManager) activity.getSystemService(activity.ALARM_SERVICE);
        notificationAlarm.cancel(notificationIntent);
    }


    public void cancel_all_notifications() {

        NotificationManager notificationManager = (NotificationManager) activity.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
	    notificationManager.cancelAll();
    }

    /**
     * apk_version_name method
     */
    public String apk_version_name() {

        try {
            return activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * apk_version_code method
     */
    public int apk_version_code() {
        try {
            return activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            return -1;
        }
    }

    public int bazaar_apk_version_code() {
        return (int) bazaarUpdateChecker.getVersionCode();
    }

    public void share_app(String title, String body) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
		activity.startActivity(Intent.createChooser(sharingIntent, "اشتراک گذاری"));
    }

    public void share_pic(String path, String title, String subject, String text)
    {
        File f = new File(path);

        Uri uri;
        try {
            uri = FileProvider.getUriForFile(activity, activity.getPackageName(), f);
        } catch (IllegalArgumentException e) {
            Log.e("", "The selected file can't be shared: " + path);
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivity(Intent.createChooser(shareIntent, title));
    }


    /**
     * unbiased_time_now method
     */
    public String unbiased_time_offset() {
        return Long.toString(UnbiasedTime.vtcTimestampOffset());
    }

    /**
     * Retrieve install referrer
     */
    public String install_referrer() {
	try {
		ReferrerDetails response = referrerClient.getInstallReferrer();
		return response.getInstallReferrer();
        } catch (Exception e) {
            return "";
        }
    }

    public static void _instant_notification(String message, String title, String info, int tag, Context context) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "andact_channel_id";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "CHANNELNAME", NotificationManager.IMPORTANCE_MAX);
            notificationChannel.setDescription("DESCRIPTION");
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Intent godotIntent = new Intent(context, Godot.class);
        godotIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        godotIntent.putExtra("notification", "notification");
        godotIntent.putExtra("notificationTag", tag);

        PendingIntent comebackIntent = PendingIntent.getActivity(context, tag, godotIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.icon)
                .setLargeIcon(largeIcon)
                .setTicker(message)
                .setContentIntent(comebackIntent)
                .setContentTitle(title)
                .setContentText(message)
                .setContentInfo(info);

	    notificationBuilder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(NOTIFICATION_TAG, notificationBuilder.build());
    }

    public int get_visible_height() {
        Rect rectangle = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);

        return rectangle.bottom;
    }

    public int get_visible_width() {
        Rect rectangle = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);

        return rectangle.right;
    }

    public void show_webview(String url) {
		Intent myIntent = new Intent(activity, WebViewAct.class);
		myIntent.putExtra("url", url); //Optional parameters
		activity.startActivity(myIntent);
    }

    static public Godot.SingletonBase initialize(Activity activity) {
        return new AndAct(activity);
    }

    public AndAct(Activity activity) {
        registerClass("AndAct", new String[]{
                "init",
                "start_activity",
                "instant_notification",
                "deffered_notification",
		        "cancel_all_notifications",
                "cancel_notification",
                "apk_version_name",
                "apk_version_code",
                "unbiased_time_offset",
                "bazaar_apk_version_code",
                "share_app",
                "share_pic",
                "install_referrer",
                "get_visible_height",
                "get_visible_width",
                "show_webview"
        });

        this.activity = activity;

        bazaarUpdateChecker = new BazaarUpdateChecker();
        bazaarUpdateChecker.init(activity);

        UnbiasedTime.vtcOnSessionStart(activity.getApplicationContext());

	referrerClient = InstallReferrerClient.newBuilder(activity).build();
	referrerClient.startConnection(new InstallReferrerStateListener() {
	    @Override
	    public void onInstallReferrerSetupFinished(int responseCode) {
		switch (responseCode) {
		    case InstallReferrerResponse.OK:
			// Connection established.
			break;
		    case InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
			// API not available on the current Play Store app.
			break;
		    case InstallReferrerResponse.SERVICE_UNAVAILABLE:
			// Connection couldn't be established.
			break;
		}
	    }

	    @Override
	    public void onInstallReferrerServiceDisconnected() {
		// Try to restart the connection on the next request to
		// Google Play by calling the startConnection() method.
	    }
	});
    }

    protected void onMainDestroy() {
        bazaarUpdateChecker.release();
        UnbiasedTime.vtcOnSessionEnd(activity.getApplicationContext());
    }

    protected void onMainPause() {
        UnbiasedTime.vtcOnSessionEnd(activity.getApplicationContext());
    }

    protected void onMainResume() {
        UnbiasedTime.vtcOnSessionStart(activity.getApplicationContext());
    }
}
