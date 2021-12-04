package org.godotengine.godot;

import android.app.Activity;
import ir.metrix.sdk.MetrixClient;
import ir.metrix.sdk.MetrixConfig;
import ir.metrix.sdk.OnSessionIdListener;
import ir.metrix.sdk.OnReceiveUserIdListener;
import java.lang.Long;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;

public class Metrix extends Godot.SingletonBase {
    private Activity activity = null; // The main activity of the game

    private MetrixConfig metrixConfig = null;
    private MetrixClient instance = null;
    private String metrixUserId;
    private String metrixSessionId;

    static public Godot.SingletonBase initialize(Activity activity) {
        return new Metrix(activity);
    }

    public Metrix(Activity activity) {
        registerClass("Metrix", new String[]{
		"getMetrixUserId",
		"getMetrixSessionId",
		"getMetrixSessionNum",
		"customEvent",
		"revenueEvent"
        });

        this.activity = activity;

	// Read https://docs.metrix.ir/sdk/android/
	//
	
	metrixConfig = new MetrixConfig(this.activity.getApplication(), "METRIX_KEY");

	metrixConfig.setLocationListening(false);
	metrixConfig.setEventUploadThreshold(50);
	metrixConfig.setEventUploadMaxBatchSize(100);
	metrixConfig.setEventMaxCount(1000);
	metrixConfig.setEventUploadPeriodMillis(30000);
	metrixConfig.setSessionTimeoutMillis(1800000);
	metrixConfig.enableLogging(true);
	metrixConfig.setLogLevel(Log.VERBOSE);
	metrixConfig.setFlushEventsOnClose(true); // false


	metrixConfig.setOnReceiveUserIdListener(new OnReceiveUserIdListener() {
            @Override
            public void onReceiveUserId(String id) {

		metrixUserId = id;
            }
        });

	metrixConfig.setOnSessionIdListener(new OnSessionIdListener() {
            @Override
            public void onReceiveSessionId(String id) {
            	metrixSessionId = id;
            }
        });

		ir.metrix.sdk.Metrix.onCreate(metrixConfig);
		instance = ir.metrix.sdk.Metrix.getInstance();
    }

    public String getMetrixUserId() {
	    return metrixUserId;
    }

    public String getMetrixSessionId() {
	    return metrixSessionId;
    }

    public String getMetrixSessionNum() {
	    return Long.toString(ir.metrix.sdk.Metrix.getInstance().getSessionNum());
    }

	public void customEvent(String slug, String test) {
		Map<String, String> attributes = new HashMap<>();
		attributes.put("test", test);

		Map<String, Double> metrics = new HashMap<>();

		ir.metrix.sdk.Metrix.getInstance().newEvent(slug, attributes, metrics);
    }

	public void revenueEvent(String slug, float amount, String orderId) {
		ir.metrix.sdk.Metrix.getInstance().newRevenue(slug, (double) amount, ir.metrix.sdk.MetrixCurrency.IRR, orderId);
	}

    protected void onMainDestroy() {
    }

    protected void onMainPause() {
    }

    protected void onMainResume() {
    }
}
