package org.hcilab.projects.nlogx.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.hcilab.projects.nlogx.R;
import org.hcilab.projects.nlogx.misc.Const;
import org.hcilab.projects.nlogx.misc.DatabaseHelper;
import org.hcilab.projects.nlogx.misc.ExportTask;
import org.hcilab.projects.nlogx.service.MyForegroundServiceJava;
import org.hcilab.projects.nlogx.service.NotificationCollectorMonitorService;
import org.hcilab.projects.nlogx.service.NotificationHandler;

public class MainActivity extends AppCompatActivity {

	private MyForegroundServiceJava myService;
	private boolean mBound = false;

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			MyForegroundServiceJava.LocalBinder binder = (MyForegroundServiceJava.LocalBinder) service;
			myService = (MyForegroundServiceJava) binder.getService();
			mBound = true;

			// Set a callback to receive data from the service
			myService.setOnCounterChangeListener(new MyForegroundServiceJava.OnCounterChangeListener() {
				@Override
				public void onCounterChanged(int newValue) {
					runOnUiThread(() -> {
						// Update UI with new counter value
						Toast.makeText(MainActivity.this, "Counter: " + newValue, Toast.LENGTH_SHORT).show();
					});
				}
			});
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, MyForegroundServiceJava.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mBound) {
			myService.setOnCounterChangeListener(null); // Remove listener!
			unbindService(mConnection);
			mBound = false;
		}
	}

	// Example: Call a public method from the service (e.g., on a button click)
	public void onIncrementButtonClick(View view) {
		if (mBound && myService != null) {
			myService.incrementCounter();
		}
	}

	// Example: Get data from the service
	public void onShowCounterButtonClick(View view) {
		if (mBound && myService != null) {
			int current = myService.getCounter();
			Toast.makeText(this, "Counter is: " + current, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.startService(new Intent(this, NotificationCollectorMonitorService.class));
		ContextCompat.startForegroundService(this,
				new Intent(this, MyForegroundServiceJava.class)
		);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		long itemId = item.getItemId();
		if (itemId == R.id.menu_delete){
			confirm();
			return true;
		} else if (itemId == R.id.menu_export) {
			export();
			return true;
		} else if (itemId == R.id.text_speech) {
			Intent intent = new Intent(this.getApplicationContext(), MainActivity2.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.getBaseContext().startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	private void confirm() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
		builder.setTitle(R.string.dialog_delete_header);
		builder.setMessage(R.string.dialog_delete_text);
		builder.setNegativeButton(R.string.dialog_delete_no, (dialogInterface, i) -> {});
		builder.setPositiveButton(R.string.dialog_delete_yes, (dialogInterface, i) -> truncate());
		builder.show();
	}

	private void truncate() {
		try {
			DatabaseHelper dbHelper = new DatabaseHelper(this);
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.execSQL(DatabaseHelper.SQL_DELETE_ENTRIES_POSTED);
			db.execSQL(DatabaseHelper.SQL_CREATE_ENTRIES_POSTED);
			db.execSQL(DatabaseHelper.SQL_DELETE_ENTRIES_REMOVED);
			db.execSQL(DatabaseHelper.SQL_CREATE_ENTRIES_REMOVED);
			Intent local = new Intent();
			local.setAction(NotificationHandler.BROADCAST);
			LocalBroadcastManager.getInstance(this).sendBroadcast(local);
		} catch (Exception e) {
			if(Const.DEBUG) e.printStackTrace();
		}
	}

	private void export() {
		if(!ExportTask.exporting) {
			ExportTask exportTask = new ExportTask(this, findViewById(android.R.id.content));
			exportTask.execute();
		}
	}

}