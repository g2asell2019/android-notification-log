package org.hcilab.projects.nlogx.service;

import android.app.Notification;
import android.content.Intent;
import android.util.Log;

import org.hcilab.projects.nlogx.R;
import org.hcilab.projects.nlogx.ui.MainActivity;

public class MyForegroundServiceJava extends BaseForegroundServiceJava {

    private int counter = 0;
    private OnCounterChangeListener listener;

    public interface OnCounterChangeListener {
        void onCounterChanged(int newValue);
    }

    public void setOnCounterChangeListener(OnCounterChangeListener listener) {
        this.listener = listener;
    }

    // Example public method
    public void incrementCounter() {
        counter++;
        if (listener != null) {
            listener.onCounterChanged(counter);
        }
    }

    public int getCounter() {
        return counter;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("[DEBUG]", "onStartCommand" );

        if (intent != null && "SPEAK_TEXT".equals(intent.getAction())){
            String text = intent.getStringExtra("text");
            boolean success = TextSpeechSingleton.speak(this, text);
            Log.d("[DEBUG]", "onStartCommand TTS speak result: " + success);
            Log.d("[DEBUG]", "Speak message from foreground service, TTS speak result : " + success);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected Notification serviceNotification() {
        Log.d("[DEBUG]", "serviceNotification: ");
        // Implement as before
        return createNotification(
                "Notification Speaker Running",
                "Listening to notifications...",
                R.mipmap.ic_launcher_alarms_round,
                R.mipmap.ic_launcher_alarms_round,
                MainActivity.class
        );
    }
}