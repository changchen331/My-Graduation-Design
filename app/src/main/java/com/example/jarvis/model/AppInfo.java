package com.example.jarvis.model;

import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * 应用信息
 */
public class AppInfo {
    private String appName = ""; //应用名称
    private String packageName = ""; //应用包名（用于启动应用）
    private String className = ""; //应用类名（用于启动应用）
    private String versionName = ""; //版本名称
    private Drawable appIcon = null; //应用图标
    private boolean isSystemApp = true; //是否为系统应用

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public boolean isSystemApp() {
        return isSystemApp;
    }

    public void setSystemApp(boolean systemApp) {
        isSystemApp = systemApp;
    }

    @NonNull
    @Override
    public String toString() {
        return "AppInfo{" +
                "appName='" + appName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", className='" + className + '\'' +
                ", versionName='" + versionName + '\'' +
                ", appIcon=" + appIcon.toString() + '\'' +
                ", isSystemApp=" + isSystemApp +
                '}';
    }

    public void print() {
        Log.v("appInfo", "应用名称: " + appName + " -> 应用包名: " + packageName);
        Log.v("appInfo", "应用名称: " + appName + " -> 应用包名: " + className);
        Log.v("appInfo", "应用名称: " + appName + " -> 版本号: " + versionName);
        Log.v("appInfo", "应用名称: " + appName + " -> 是否为系统应用: " + isSystemApp);
    }
}
