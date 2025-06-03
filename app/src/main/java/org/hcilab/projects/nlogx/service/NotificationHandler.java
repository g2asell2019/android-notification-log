package org.hcilab.projects.nlogx.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.hcilab.projects.nlogx.misc.Const;
import org.hcilab.projects.nlogx.misc.DatabaseHelper;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

/*
* Issues with the Current Implementation
No User Control: There's no check for whether the user has enabled TTS in the app settings before speaking

Always Speaking: TTS is triggered for every notification regardless of importance or context

No Filtering: The text is spoken before any content filtering is applied (e.g., sensitive information)

Threading Issues: TTS initialization happens on the main thread, which could cause delays

Toast Spam: Showing toasts about language settings every time a notification is processed could be annoying

No Error Handling: There's no handling of TTS errors during speaking
* */
public class NotificationHandler {

	public static final String BROADCAST = "org.hcilab.projects.nlogx.update";
	public static final String LOCK = "lock";

	private Context context;
	private SharedPreferences sp;

	private final BroadcastSender broadcastSender;

	public NotificationHandler(Context context) {
		this(context, new LocalBroadcastSender(context));

	}
	// Constructor with injectable BroadcastSender for testing
	public NotificationHandler(Context context, BroadcastSender broadcastSender) {
		this.context = context;
		this.broadcastSender = broadcastSender;
		sp = PreferenceManager.getDefaultSharedPreferences(context);
	}


	public void handlePosted(StatusBarNotification sbn) {
		if(sbn.isOngoing() && !sp.getBoolean(Const.PREF_ONGOING, false)) {
			if(Const.DEBUG) System.out.println("posted ongoing!");
			return;
		}
		boolean text = sp.getBoolean(Const.PREF_TEXT, true);
		NotificationObject no = new NotificationObject(context, sbn, text, -1);
		log(DatabaseHelper.PostedEntry.TABLE_NAME, DatabaseHelper.PostedEntry.COLUMN_NAME_CONTENT, no.toString());

		// Check if TTS is enabled in settings
		//boolean ttsEnabled = sp.getBoolean(Const.PREF_TTS_ENABLED, false);
		//if (ttsEnabled) {
			// Use the singleton TTS instance
//			boolean success = MyTell.speak(context, no.getText());
//			Log.d("[DEBUG]", "TTS speak result: " + success);
		//}

		Intent intent = new Intent(context, MyForegroundServiceJava.class);
		intent.setAction("SPEAK_TEXT");
		intent.putExtra("text", no.getText());
		ContextCompat.startForegroundService(context, intent);
	}

	public void handleRemoved(StatusBarNotification sbn, int reason) {
		if(sbn.isOngoing() && !sp.getBoolean(Const.PREF_ONGOING, false)) {
			if(Const.DEBUG) System.out.println("removed ongoing!");
			return;
		}
		NotificationObject no = new NotificationObject(context, sbn, false, reason);
		log(DatabaseHelper.RemovedEntry.TABLE_NAME, DatabaseHelper.RemovedEntry.COLUMN_NAME_CONTENT, no.toString());
	}

	private void log(String tableName, String columnName, String content) {
		try {
			if(content != null) {
				synchronized (LOCK) {
					DatabaseHelper dbHelper = new DatabaseHelper(context);
					SQLiteDatabase db = dbHelper.getWritableDatabase();
					ContentValues values = new ContentValues();
					values.put(columnName, content);
					db.insert(tableName, "null", values);
					db.close();
					dbHelper.close();
				}

				Intent local = new Intent();
				local.setAction(BROADCAST);
				broadcastSender.sendBroadcast(local);  // Use the injected broadcastSender
			}
		} catch (Exception e) {
			if(Const.DEBUG) e.printStackTrace();
		}
	}

}
