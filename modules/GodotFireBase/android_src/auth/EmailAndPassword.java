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

package org.godotengine.godot.auth;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.godot.game.R;

import org.json.JSONObject;
import org.json.JSONException;

import org.godotengine.godot.Utils;

public class EmailAndPassword {

	public static EmailAndPassword getInstance (Activity p_activity) {
		if (mInstance == null) {
			mInstance = new EmailAndPassword(p_activity);
		}

		return mInstance;
	}

	public EmailAndPassword(Activity p_activity) {
		activity = p_activity;
	}

	public void init() {
		// Initialize listener.
		// ...

		mAuth = FirebaseAuth.getInstance();

		mAuthListener = new FirebaseAuth.AuthStateListener() {

			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
				FirebaseUser user = firebaseAuth.getCurrentUser();
				if (user != null) {
					Utils.d("GodotFireBase", "E&P:onAuthStateChanged:signed_in:" + user.getUid());
					successSignIn(user);
				} else {
					// User is signed out
					Utils.d("GodotFireBase", "E&P:onAuthStateChanged:signed_out");
					successSignOut();
				}

				// update user details;
			}
		};
	}

	public void createAccount(final String email, final String password) {
		Utils.d("GodotFireBase", "E&P:CreateAccount:" + email);

		mAuth.createUserWithEmailAndPassword(email, password)
		.addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				Utils.d("GodotFireBase", "E&P:CreateUserWithEmail:onComplete:" + task.isSuccessful());

				// If sign in fails, display a message to the user. If sign in succeeds
				// the auth state listener will be notified and logic to handle the
				// signed in user can be handled in the listener.

				if (!task.isSuccessful()) {
					Utils.d("GodotFireBase", "E&P:CreateAccount:Error");
				}
			}
		});
	}

	public void signIn(final String email, final String password) {

	}

	public void signOut() {
		mAuth.signOut();
	}

	private void successSignIn(FirebaseUser user) {
		Utils.d("GodotFireBase", "E&P:SignIn:Success");

		try {
			currentEPUser.put("name", user.getDisplayName());
			currentEPUser.put("email_id", user.getEmail());
			currentEPUser.put("photo_uri", user.getPhotoUrl());
		} catch(JSONException e) { Utils.d("GodotFireBase", "E&P:JSON:Parse:Error"); }

		// Utils.callScriptFunc("Auth", "login", "true");
	}

	private void successSignOut() {

		// Utils.callScriptFunc("Auth", "login", "false");
	}

	private void sendEmailVerification() {

	}

	public void onStart() {
		mAuth.addAuthStateListener(mAuthListener);
	}

	public void onStop() {
		if (mAuthListener != null) { mAuth.removeAuthStateListener(mAuthListener); }
	}

	private static Activity activity = null;
	private static EmailAndPassword mInstance = null;

	private FirebaseAuth mAuth;
	private FirebaseAuth.AuthStateListener mAuthListener;

	private JSONObject currentEPUser = new JSONObject();
}
