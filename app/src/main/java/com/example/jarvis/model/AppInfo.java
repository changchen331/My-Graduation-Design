package com.example.jarvis.model;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * 应用信息
 */
public class AppInfo implements Parcelable {
    public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel in) {
            return new AppInfo(in);
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };

    private String appName; // 应用名称
    private Drawable appIcon; // 应用图标
    private String packageName; // 应用包名
    private ActivityInfo mainActivity; // 应用活动

    public AppInfo() {
    }

    private AppInfo(Parcel in) {
        // 从 Parcel 中读取信息
        appName = in.readString();
        packageName = in.readString();
        mainActivity = in.readParcelable(ActivityInfo.class.getClassLoader());
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public ActivityInfo getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(ActivityInfo mainActivity) {
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public String toString() {
        return "AppInfo{" +
                "appName='" + appName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", appIcon=" + appIcon.toString() + '\'' +
                ", mainActivity=" + mainActivity.name +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(appName); // 写入 appName
        dest.writeString(packageName); // 写入 packageName
        // 如果 mainActivity 不为空，则将其写入
        if (mainActivity != null) dest.writeParcelable(mainActivity, flags);
    }
}
