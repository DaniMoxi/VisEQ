/*
 Copyright (c) 2012, Spotify AB
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Neither the name of Spotify AB nor the names of its contributors may 
 be used to endorse or promote products derived from this software 
 without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL SPOTIFY AB BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Sets a high priority on the service by bringing it to foreground.
 * Also acts like a bridge to the LibSpotify methods.
 * 
 * Should be rules for when to go to foreground. Wakelock is also needed.
 */
package com.lsu.vizeq;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import com.lsu.vizeq.R;

public class SpotifyService extends Service {
	private NotificationManager mNotificationManager;
	private static int NOTIFICATION = 29213;
	private final IBinder mBinder = new LocalBinder();
	private WifiLock mWifiLock;
	private static boolean libLoaded = false;

	static interface LoginDelegate {
		void onLogin();

		void onLoginFailed(String message);

	}

	static interface PlayerUpdateDelegate {
		void onPlayerPositionChanged(float pos);

		void onEndOfTrack();

		void onPlayerPause();

		void onPlayerPlay();

		void onTrackStarred();

		void onTrackUnStarred();
	}

	public class LocalBinder extends Binder {
		SpotifyService getService() {
			return SpotifyService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		System.loadLibrary("spotify");
		System.loadLibrary("spotifywrapper");


		
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			throw new RuntimeException("Storage card not available");
		if (libLoaded == false) 
		{
			Log.d("init", "spotify");
			LibSpotifyWrapper.init(LibSpotifyWrapper.class.getClassLoader(), Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.lsu.vizeq");
			libLoaded = true;
		}

		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// Display a notification about us starting. We put an icon in the
		// status bar.
		showNotification();
		
		mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
		mWifiLock.acquire();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		mNotificationManager.cancel(NOTIFICATION);
		
		mWifiLock.release();

		// Tell the user we stopped.
		Toast.makeText(this, "The local service has stopped", Toast.LENGTH_SHORT).show();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private void showNotification() {
		//Notification notification = new Notification(R.drawable.player_next_album, "VizEQ is playing...", System.currentTimeMillis());
		//Intent notificationIntent = new Intent(this, PlayerActivity.class);
		//PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		//notification.setLatestEventInfo(this, "VizEQ", "Party!", pendingIntent);
		//startForeground(NOTIFICATION, notification);
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.launch)
		        .setContentTitle("VizEQ")
		        .setContentText("VizEQ");
		Intent resultIntent = new Intent(this, PlayerActivity.class);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(PlayerActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
			    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			// mId allows you to update the notification later on.
		int mId = 1;
			mNotificationManager.notify(mId, mBuilder.build());
	}

	public void login(String email, String password, LoginDelegate loginDelegate) {
		LibSpotifyWrapper.loginUser(email, password, loginDelegate);

	}

	public void togglePlay(String uri, PlayerUpdateDelegate playerPositionDelegate) {
		LibSpotifyWrapper.togglePlay(uri, playerPositionDelegate);
	}
	
	public void playNext(String uri, PlayerUpdateDelegate playerPositionDelegate) {
		LibSpotifyWrapper.playNext(uri, playerPositionDelegate);
	}

	public void seek(float position) {
		LibSpotifyWrapper.seek(position);
	}

	public void star() {
		LibSpotifyWrapper.star();
	}
	
	public void unStar() {
		LibSpotifyWrapper.unstar();
	}

	public void destroy() {
		LibSpotifyWrapper.destroy();
		
	}

}