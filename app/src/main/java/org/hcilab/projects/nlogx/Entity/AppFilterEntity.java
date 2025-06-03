package org.hcilab.projects.nlogx.Entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_filter")
public class AppFilterEntity {

    @PrimaryKey
    @NonNull
    private String packageName;

    private String appName;

    private boolean enabled;

    // Constructors
    public AppFilterEntity(@NonNull String packageName, String appName, boolean enabled) {
        this.packageName = packageName;
        this.appName = appName;
        this.enabled = enabled;
    }

    // Getters and Setters
    @NonNull
    public String getPackageName() { return packageName; }
    public String getAppName() { return appName; }
    public boolean isEnabled() { return enabled; }

    public void setPackageName(@NonNull String packageName) { this.packageName = packageName; }
    public void setAppName(String appName) { this.appName = appName; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}

