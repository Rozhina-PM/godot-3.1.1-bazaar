/**
 * Copyright 2017 FrogSquare. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package org.godotengine.godot;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.os.Bundle;

import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;

public class Analytics {

	private String affiliation = null;
	private int current_level = -1;
	private int coins = -1;

	public static Analytics getInstance(Activity p_activity) {
		if (mInstance == null) {
			synchronized (Analytics.class) {
				mInstance = new Analytics(p_activity);
			}
		}

		return mInstance;
	}

	public Analytics (Activity p_activity) {
		activity = p_activity;
	}

	public void init (FirebaseApp fireBaseApp) {
		mFirebaseApp = fireBaseApp;
		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);

		Utils.d("GodotFireBase", "Firebase Analytics initialized..!");
	}

	public void set_extras(final String _affiliation, final int _current_level, final int _coins) {
		affiliation = _affiliation;
		current_level = _current_level;
		coins = _coins;
	}

	public void send_achievement(final String id) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, id);

		send_to_firebase(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle);
	}

	public void send_group(final String groupID) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.GROUP_ID, groupID);

		send_to_firebase(FirebaseAnalytics.Event.JOIN_GROUP, bundle);
	}

	public void send_level_up(final String character, final int level) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.CHARACTER, character);
		bundle.putInt(FirebaseAnalytics.Param.LEVEL, level);
		bundle.putDouble(FirebaseAnalytics.Param.VALUE, level);

		send_to_firebase(FirebaseAnalytics.Event.LEVEL_UP, bundle);
	}

	public void send_app_open(final int sessions, final int sessions_after_update) {
		Bundle bundle = new Bundle();
		bundle.putInt("sessions", sessions);
		bundle.putInt("sessions_after_update", sessions_after_update);

		send_to_firebase(FirebaseAnalytics.Event.APP_OPEN, bundle);
	}

	public void send_purchase(final String sku, final int amount) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.COUPON, sku);
		bundle.putDouble(FirebaseAnalytics.Param.VALUE, amount);
		bundle.putString(FirebaseAnalytics.Param.CURRENCY, "IRR");

		send_to_firebase(FirebaseAnalytics.Event.ECOMMERCE_PURCHASE, bundle);
	}

	public void send_score(final String character, final int level, final int score) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.CHARACTER, character);
		bundle.putInt(FirebaseAnalytics.Param.LEVEL, level);
		bundle.putInt(FirebaseAnalytics.Param.SCORE, score);

		send_to_firebase(FirebaseAnalytics.Event.POST_SCORE, bundle);
	}

	public void send_content(final String content_type, final String item_id) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, content_type);
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, item_id);

		send_to_firebase(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
	}

	public void send_share() {
		// TODO

		//SHARE

		/**
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, id);

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
		mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle);
		Utils.d("GodotFireBase", "Sending Achievement: " + id);
		**/
	}

	public void earn_currency(final String currency_name, final int value) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.VIRTUAL_CURRENCY_NAME, currency_name);
		bundle.putInt(FirebaseAnalytics.Param.VALUE, value);

		send_to_firebase(FirebaseAnalytics.Event.EARN_VIRTUAL_CURRENCY, bundle);
	}

	public void spend_currency(final String item_name, final String currency_name, final int value) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, item_name);
		bundle.putString(FirebaseAnalytics.Param.VIRTUAL_CURRENCY_NAME, currency_name);
		bundle.putInt(FirebaseAnalytics.Param.VALUE, value);

		send_to_firebase(FirebaseAnalytics.Event.SPEND_VIRTUAL_CURRENCY, bundle);
	}

	public void send_tutorial_begin() {
		Bundle bundle = new Bundle();
		//bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, id);

		send_to_firebase(FirebaseAnalytics.Event.TUTORIAL_BEGIN, bundle);
	}

	public void send_tutorial_complete() {
		Bundle bundle = new Bundle();
		//bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, id);

		send_to_firebase(FirebaseAnalytics.Event.TUTORIAL_COMPLETE, bundle);
	}

	public void send_events(String eventName, Dictionary keyValues) {

		// Generate bundle out of keyValues
		Bundle bundle = new Bundle();
		Utils.putAllInDict(bundle, keyValues);

		// Dispatch event
		send_to_firebase(eventName, bundle);
	}

	public void send_custom(final String key, final String value) {
		Bundle bundle = new Bundle();
		bundle.putString(key, value);

		send_to_firebase("appEvent", bundle);
	}

	public void send_to_firebase(String eventName, Bundle bundle) {
		add_extras(bundle);

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
		mFirebaseAnalytics.logEvent(eventName, bundle);

		Utils.d("GodotFireBase", "Sending:App:Event:[" + eventName + "]:" + bundle.toString() + "");
	}

	private void add_extras(Bundle bundle) {
		if (affiliation != null)
			bundle.putString(FirebaseAnalytics.Param.AFFILIATION, affiliation);
		if (coins != -1)
			bundle.putDouble(FirebaseAnalytics.Param.TAX, coins);
		if (current_level != -1)
			bundle.putLong(FirebaseAnalytics.Param.LEVEL, current_level);
	}

	private static Context context;
	private static Activity activity = null;
	private static Analytics mInstance = null;

	private FirebaseApp mFirebaseApp = null;
	private FirebaseAnalytics mFirebaseAnalytics = null;
}
