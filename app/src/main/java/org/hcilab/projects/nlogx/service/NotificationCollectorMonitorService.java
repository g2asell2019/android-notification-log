package org.hcilab.projects.nlogx.service;


import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by xinghui on 9/20/16.
 * <p>
 * calling this in your Application's onCreate
 * startService(new Intent(this, NotificationCollectorMonitorService.class));
 * <p>
 * BY THE WAY Don't Forget to Add the Service to the AndroidManifest.xml File.
 * <service android:name=".NotificationCollectorMonitorService"/>
 */
public class NotificationCollectorMonitorService extends Service {

    /**
     * {@link Log#isLoggable(String, int)}
     * <p>
     * IllegalArgumentException is thrown if the tag.length() > 23.
     */
    private static final String TAG = "NotifiCollectorMonitor";

    @Override
    public void onCreate() {
        super.onCreate();
        ensureCollectorRunning();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void ensureCollectorRunning() {
        ComponentName collectorComponent = new ComponentName(this, /*NotificationListenerService Inheritance*/ NotificationListener.class);
        Log.d(TAG, "ensureCollectorRunning collectorComponent: " + collectorComponent);
        
        boolean collectorRunning = isServiceRunning(collectorComponent);
        if (collectorRunning) {
            Log.d(TAG, "ensureCollectorRunning: collector is running");
            return;
        }
        Log.d(TAG, "ensureCollectorRunning: collector not running, reviving...");
        reviveCollector();


        collectorRunning = isServiceRunning(collectorComponent);

        if (collectorRunning) {
            Log.d(TAG, "ensureCollectorRunning: after reviving, collector is running");
        }
        else {
            Log.d(TAG, "ensureCollectorRunning: after reviving, collector not running ");

        }
    }

    private void reviveCollector() {
        ComponentName thisComponent = new ComponentName(this, /*getClass()*/ NotificationListener.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }
    private boolean isServiceRunning(ComponentName collectorComponent){
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean collectorRunning = false;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            Log.v(TAG, "ensureCollectorRunning running service: " + service.service);
            if (service.service.equals(collectorComponent)) {
                collectorRunning = true;
            }
        }
        return collectorRunning;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}