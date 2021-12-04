package org.godotengine.godot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;

public class UnbiasedTime {
    private static long lastTimestamp = 0;
    private static long lastUptime = 0;
    private static long lastOffset = 0;
    private static boolean usingDeviceTime = false;

    private static long getTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    private static long calculateOffset(long ts1, long ts2, long up1, long up2, long off1) {
        long offset;
        long uptimeElapsed = up2 - up1;
        long lastRealtime = ts1 - off1;
        if (uptimeElapsed > 0) {
            offset = ts2 - (lastRealtime + uptimeElapsed);
            usingDeviceTime = false;
        } else {
            offset = lastOffset;
            usingDeviceTime = true;
        }
        return offset;
    }

    private static long getUptime() {
        return SystemClock.elapsedRealtime() / 1000;
    }

    private static long getOffset() {
        long timestamp = UnbiasedTime.getTimestamp();
        long uptime = UnbiasedTime.getUptime();
        return UnbiasedTime.calculateOffset(lastTimestamp, timestamp, lastUptime, uptime, lastOffset);
    }

    private static void readPreferences(Context activity) {
        SharedPreferences prefs = activity.getSharedPreferences("unbiasedTime", 0);
        lastTimestamp = prefs.getLong("timestamp", UnbiasedTime.getTimestamp());
        lastUptime = prefs.getLong("uptime", UnbiasedTime.getUptime());
        lastOffset = prefs.getLong("offset", 0);
    }

    private static void writePreferences(Context activity) {
        SharedPreferences prefs = activity.getSharedPreferences("unbiasedTime", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("timestamp", UnbiasedTime.getTimestamp());
        editor.putLong("uptime", UnbiasedTime.getUptime());
        editor.putLong("offset", UnbiasedTime.getOffset());
        editor.commit();
    }

    public static void vtcOnSessionStart(Context activity) {
        UnbiasedTime.readPreferences(activity);
    }

    public static void vtcOnSessionEnd(Context activity) {
        UnbiasedTime.writePreferences(activity);
    }

    public static long vtcTimestampOffset() {
        return UnbiasedTime.getOffset();
    }

    public static long vtcUptime() {
        return UnbiasedTime.getUptime();
    }

    public static boolean vtcUsingDeviceTime() {
        return usingDeviceTime;
    }
}
