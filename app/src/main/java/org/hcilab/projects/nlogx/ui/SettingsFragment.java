package org.hcilab.projects.nlogx.ui;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

//import org.hcilab.projects.nlogx.BuildConfig;
import org.hcilab.projects.nlogx.R;
import org.hcilab.projects.nlogx.misc.Const;
import org.hcilab.projects.nlogx.Entity.Country;
import org.hcilab.projects.nlogx.misc.DatabaseHelper;
import org.hcilab.projects.nlogx.misc.Util;
import org.hcilab.projects.nlogx.service.NotificationHandler;

import java.util.Map;

public class SettingsFragment extends PreferenceFragmentCompat {

	public static final String TAG = SettingsFragment.class.getName();

	private DatabaseHelper dbHelper;
	private BroadcastReceiver updateReceiver;

	private Preference prefStatus;
	private Preference prefBrowse;
	private Preference prefText;
	private Preference prefOngoing;
	private Preference prefSpeechLang;
	private Preference prefAppFilter;
	private SharedPreferences sharedPreferences;
	private Context context;
	private NotificationManager notifManager;
	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
		addPreferencesFromResource(R.xml.preferences);

		PreferenceManager pm = getPreferenceManager();

		sharedPreferences =  pm.getSharedPreferences();
		Log.d("[DEBUG]", "Shared preference from " + this.getClass().getName());
		context = getContext().getApplicationContext();
		logSharedPreferences();
		prefStatus = pm.findPreference(Const.PREF_STATUS);
		if(prefStatus != null) {
			prefStatus.setOnPreferenceClickListener(preference -> {
				startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
				return true;
			});
		}

		prefBrowse = pm.findPreference(Const.PREF_BROWSE);
		if(prefBrowse != null) {
			prefBrowse.setOnPreferenceClickListener(preference -> {
				startActivity(new Intent(getActivity(), BrowseActivity.class));
				return true;
			});
		}

		prefText    = pm.findPreference(Const.PREF_TEXT);
		prefOngoing = pm.findPreference(Const.PREF_ONGOING);

		Preference prefAbout = pm.findPreference(Const.PREF_ABOUT);
		if(prefAbout != null) {
			prefAbout.setOnPreferenceClickListener(preference -> {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("https://github.com/interactionlab/android-notification-log"));
				startActivity(intent);
				return true;
			});
		}

		Preference prefVersion = pm.findPreference(Const.PREF_VERSION);
		if(prefVersion != null) {
			prefVersion.setSummary("org.hcilab.projects.nlogx" + (Const.DEBUG ? " dev" : ""));
			prefVersion.setOnPreferenceClickListener(preference -> {
				createNotification("Test notification", "This is my notification");
				return true;
			});
		}

		prefSpeechLang = pm.findPreference(Const.PREF_SPEECH_LANG);

		if (prefSpeechLang != null){
			String savedSpeechLang = sharedPreferences.getString(Const.PREF_SPEECH_LANG, Const.DEFAULT_LOCALE.toString());
			if (savedSpeechLang.equals("default_lang")){
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString(Const.PREF_SPEECH_LANG, Const.DEFAULT_LOCALE.toString());
				editor.apply();
			}
			prefSpeechLang.setSummary(prefSpeechLang.getKey());

			prefSpeechLang.setOnPreferenceClickListener(preference -> {
				Intent intent = new Intent(getContext(), MainActivity2.class);
				intent.putExtra("sharedPrefName", pm.getSharedPreferencesName());
				intent.putExtra("sharedPrefMode", pm.getSharedPreferencesMode());
				Log.d("[DEBUG]", "sharedPrefName: " + pm.getSharedPreferencesName());
				Log.d("[DEBUG]", "sharedPrefMode: " + pm.getSharedPreferencesName());
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				return true;
			});
		}

		prefAppFilter = pm.findPreference(Const.PREF_APP_FILTER);
		if (prefAppFilter != null) {
			prefAppFilter.setOnPreferenceClickListener(preference -> {
				Intent intent = new Intent(getContext(), AppFilterActivity.class);
				Log.d("[DEBUG]", "Change to AppFilterActivity: ");

				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				return true;
			});
		}


	}


	private void logSharedPreferences() {
		Log.d("[DEBUG]", "===== SharedPreferences Contents =====");
		Map<String, ?> allEntries = sharedPreferences.getAll();
		for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
			Log.d("[DEBUG]", entry.getKey() + ": " + entry.getValue());
		}
		Log.d("[DEBUG]", "=====================================");
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (prefSpeechLang != null) {
			String savedSpeechLang = sharedPreferences.getString(Const.PREF_SPEECH_LANG, Const.DEFAULT_LOCALE.toString());
			if (savedSpeechLang.equals("default_lang")) {
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString(Const.PREF_SPEECH_LANG, Const.DEFAULT_LOCALE.toString());
				editor.apply();
			}
			prefSpeechLang.setSummary(savedSpeechLang);
		}

		try {
			dbHelper = new DatabaseHelper(getActivity());
		} catch (Exception e) {
			if(Const.DEBUG) e.printStackTrace();
		}

		updateReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				update();
			}
		};
	}

	@Override
	public void onResume() {
		super.onResume();
		if (prefSpeechLang != null) {
			String savedSpeechLang = sharedPreferences.getString(Const.PREF_SPEECH_LANG, "NOT SAVED");
			Country country = Const.currentAvailableLocale.get(savedSpeechLang);

            if (country != null)
			{
				prefSpeechLang.setSummary(country.getLocale().getDisplayName());

			}

		}
		if(Util.isNotificationAccessEnabled(getActivity())) {
			prefStatus.setSummary(R.string.settings_notification_access_enabled);
			prefText.setEnabled(true);
			prefOngoing.setEnabled(true);
		} else {
			prefStatus.setSummary(R.string.settings_notification_access_disabled);
			prefText.setEnabled(false);
			prefOngoing.setEnabled(false);
		}

		IntentFilter filter = new IntentFilter();
		filter.addAction(NotificationHandler.BROADCAST);
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(updateReceiver, filter);

		update();
	}

	@Override
	public void onPause() {
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(updateReceiver);
		super.onPause();
	}

	private void update() {
		try {
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			long numRowsPosted = DatabaseUtils.queryNumEntries(db, DatabaseHelper.PostedEntry.TABLE_NAME);
			int stringResource = numRowsPosted == 1 ? R.string.settings_browse_summary_singular : R.string.settings_browse_summary_plural;
			prefBrowse.setSummary(getString(stringResource, numRowsPosted));
		} catch (Exception e) {
			if(Const.DEBUG) e.printStackTrace();
		}
	}

	public void createNotification(String title, String body) {
		final int NOTIFY_ID = 1002;

		// There are hardcoding only for show it's just strings
		String name = "my_package_channel";
		String id = "my_package_channel_1"; // The user-visible name of the channel.
		String description = "my_package_first_channel"; // The user-visible description of the channel.

		Intent intent;
		PendingIntent pendingIntent;
		NotificationCompat.Builder builder;

		if (notifManager == null){
			notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);;

		}


		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			int importance = NotificationManager.IMPORTANCE_HIGH;
			NotificationChannel mChannel = notifManager.getNotificationChannel(id);
			if (mChannel == null) {
				mChannel = new NotificationChannel(id, name, importance);
				mChannel.setDescription(description);
				mChannel.enableVibration(true);
				mChannel.setLightColor(Color.GRAY);
				mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
				notifManager.createNotificationChannel(mChannel);
			}
			builder = new NotificationCompat.Builder(context, id);

			intent = new Intent(context, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

			builder.setContentTitle(title)  // required
					.setSmallIcon(android.R.drawable.ic_popup_reminder) // required
					.setContentText(body)  // required
					.setDefaults(Notification.DEFAULT_ALL)
					.setAutoCancel(true)
					.setContentIntent(pendingIntent)
					.setTicker(title)
					.setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
		} else {

			builder = new NotificationCompat.Builder(getContext());

			intent = new Intent(getContext(), MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

			builder.setContentTitle(title)                           // required
					.setSmallIcon(android.R.drawable.ic_popup_reminder) // required
					.setContentText(body)  // required
					.setDefaults(Notification.DEFAULT_ALL)
					.setAutoCancel(true)
					.setContentIntent(pendingIntent)
					.setTicker(title)
					.setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
					.setPriority(Notification.PRIORITY_HIGH);
		}

		Notification notification = builder.build();
		notifManager.notify(NOTIFY_ID, notification);
	}
}