package org.hcilab.projects.nlogx.ui;

import android.graphics.drawable.Drawable;

public class AppFilter{
    private String packageName;
    private String appName;
    private Drawable icon;
    private boolean isEnabled;



    public AppFilter(String appName, String packageName, Drawable icon, boolean isEnabled) {
        this.isEnabled = isEnabled;
        this.icon = icon;
        this.appName = appName;
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }


    public String getAppName() {
        return appName;
    }

    public Drawable getIcon() {
        return icon;
    }

    
    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
