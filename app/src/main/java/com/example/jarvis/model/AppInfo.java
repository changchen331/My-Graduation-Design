package com.example.jarvis.model;

import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * 应用信息
 */
public class AppInfo {
    public String appName = ""; //应用名称
    public String packageName = ""; //应用包名（用于启动应用）
    public String versionName = ""; //版本名称
    public Drawable appIcon = null; //应用图标
    // 注意：Drawable不能直接转换为String来表示图标，需要将其转换为Bitmap然后保存为文件
    public boolean isSystemApp = true; //是否为系统应用

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
                ", versionName='" + versionName + '\'' +
                ", appIcon=" + appIcon.toString() + '\'' +
                ", isSystemApp=" + isSystemApp +
                '}';
    }

    public void print() {
        Log.v("appInfo", "Name:" + appName + " Package:" + packageName);
        Log.v("appInfo", "Name:" + appName + " versionName:" + versionName);
        Log.v("appInfo", "Name:" + appName + " isSystemApp:" + isSystemApp);
    }
}
