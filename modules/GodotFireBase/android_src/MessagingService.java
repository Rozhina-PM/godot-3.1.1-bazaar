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

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.godot.game.R;

import java.util.Map;

import org.json.JSONObject;
import org.json.JSONException;

//import org.godotengine.godot.KeyValueStorage;

public class MessagingService extends FirebaseMessagingService {

	private static int NOTIFICATION_REQUEST_ID	= 8001;

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		Utils.d("GodotFireBase", "Message From: " + remoteMessage.getFrom());
		Utils.d("GodotFireBase", "Message From: " + remoteMessage.toString());

		// Check if message contains a data payload.
		if (remoteMessage.getData().size() > 0) {
			Map<String, String> data = remoteMessage.getData();

			Utils.d("GodotFireBase", "Message data payload: " + data.toString());

			//KeyValueStorage.set_context(this);
			handleData(data);
		}

		// Check if message contains a notification payload.
		if (remoteMessage.getNotification() != null) {
			Utils.d("GodotFireBase", 
			"Notification Body: " + remoteMessage.getNotification().getBody());

			sendNotification(remoteMessage.getNotification().getBody(), this, null);
		}
	}

    @Override
    public void onNewToken(String token) {
        Utils.d("GodotFireBase", "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
        Utils.d("GodotFireBase", "Token: " + token);
    }

	private void handleData (Map<String, String> data) {
		// TODO: Perform some action now..!
		// ...

		JSONObject jobject = new JSONObject();

		try {
			for (Map.Entry<String, String> entry : data.entrySet()) {
				jobject.put(entry.getKey(), entry.getValue());
			}
		} catch (JSONException e) { Utils.d("GodotFireBase", "JSONException: parsing, " + e.toString()); }

		if (jobject.length() > 0) {
			//KeyValueStorage.setValue("firebase_notification_data", jobject.toString());
		}
	}

	public static void sendNotification(Bundle bundle, Context context) {
        if (bundle.getString("image_uri") == null || bundle.getString("image_uri").equals("")) {
            sendNotification(bundle.getString("message"), context, bundle.getString("link"));
			return;
        }

		Intent intent = new Intent(context, org.godotengine.godot.Godot.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if(bundle.getString("link") != null && !bundle.getString("link").equals("")) {
			intent.putExtra("link", bundle.getString("link"));
		}

		PendingIntent pendingIntent = PendingIntent.getActivity(
		context, Utils.FIREBASE_NOTIFICATION_REQUEST, intent, PendingIntent.FLAG_ONE_SHOT);

		Uri defaultSoundUri =
		RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Bitmap large_icon;
        //if (bundle.get("large_icon") != null) {
		//	large_icon = Utils.getBitmapFromAsset(context, bundle.getString("large_icon"));
        //} else {
        large_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
        //}

		NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context)        
		.setSmallIcon(R.drawable.icon)
		.setLargeIcon(large_icon)
		.setContentTitle(bundle.getString("title"))
		//.setStyle(new NotificationCompat.BigPictureStyle()
		//		.bigPicture(Utils.getBitmapFromAsset(context, bundle.getString("image_uri"))))
		.setAutoCancel(true)
		.setSound(defaultSoundUri)
		.setContentIntent(pendingIntent);

		NotificationManager notificationManager =
		(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(7002, nBuilder.build());
	}

	public static void sendNotification(String messageBody, Context context, String link) {
		Intent intent = new Intent(context, org.godotengine.godot.Godot.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if(link != null) {
			intent.putExtra("link",link);
		}

		PendingIntent pendingIntent = PendingIntent.getActivity(
		context, Utils.FIREBASE_NOTIFICATION_REQUEST, intent, PendingIntent.FLAG_ONE_SHOT);

		Uri defaultSoundUri =
		RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        //Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);

		// NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context)
		// .setDefaults(Notification.DEFAULT_ALL)		
		// .setContentTitle(context.getString(R.string.godot_project_name_string))
		// .setContentText(messageBody)
        // .setTicker(messageBody)
		// .setAutoCancel(true)
		// .setSound(defaultSoundUri)
		// .setContentIntent(pendingIntent)
		// .setLargeIcon(largeIcon)
		// .setSmallIcon(R.drawable.icon);

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		String NOTIFICATION_CHANNEL_ID = "firebase_channel_id";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "CHANNELNAME", NotificationManager.IMPORTANCE_MAX);
            notificationChannel.setDescription("DESCRIPTION");
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

		Intent godotIntent = new Intent(context, Godot.class);
        godotIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent comebackIntent = PendingIntent.getActivity(context, 1, godotIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

		notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.icon)
                .setLargeIcon(largeIcon)
                .setTicker(messageBody)
                .setContentIntent(comebackIntent)
                .setAutoCancel(true)
                .setContentTitle(context.getString(R.string.godot_project_name_string))
                .setContentText(messageBody)
                .setContentInfo("");

		notificationManager.notify(7002, notificationBuilder.build());
	}
}
