package org.hcilab.projects.nlogx.Entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "regex_filters")
public class RegexFilterEntity{
    @PrimaryKey
    @NonNull
    private String packageName;

    private String appName;
    private String regexPattern;
    private String formatedSpeech;

    public RegexFilterEntity(@NonNull String packageName, String appName, String regexPattern, String formatedSpeech) {
        this.packageName = packageName;
        this.appName = appName;
        this.regexPattern = regexPattern;
        this.formatedSpeech = formatedSpeech;
    }

    @NonNull
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(@NonNull String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getRegexPattern() {
        return regexPattern;
    }

    public void setRegexPattern(String regexPattern) {
        this.regexPattern = regexPattern;
    }

    public String getFormatedSpeech() {
        return formatedSpeech;
    }

    public void setFormatedSpeech(String formatedSpeech) {
        this.formatedSpeech = formatedSpeech;
    }
// TODO: Add constructor(s) if needed
    // TODO: Add getters and setters (or make fields public if you're okay with it)
}