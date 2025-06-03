package org.hcilab.projects.nlogx.service;

import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class LocalBroadcastSender implements BroadcastSender {
    private final LocalBroadcastManager broadcastManager;

    public LocalBroadcastSender(Context context) {
        broadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @Override
    public void sendBroadcast(Intent intent) {
        broadcastManager.sendBroadcast(intent);
    }
}