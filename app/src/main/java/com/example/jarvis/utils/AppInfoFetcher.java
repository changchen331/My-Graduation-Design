package com.example.jarvis.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.example.jarvis.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class AppInfoFetcher {
    private final Context context;

    public AppInfoFetcher(Context context) {
        this.context = context;
    }

    @SuppressLint("QueryPermissionsNeeded")
    public ArrayList<AppInfo> getAllInstalledApps() {
        //用来存储获取的应用信息数据
        ArrayList<AppInfo> appList = new ArrayList<>();

        //获取所有应用包信息
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES);

        //提取应用包信息
        for (PackageInfo aPackage : packages) {
            AppInfo appInfo = new AppInfo();
            appInfo.setAppName(aPackage.applicationInfo.loadLabel(packageManager).toString());
            appInfo.setPackageName(aPackage.packageName);
            appInfo.setVersionName(aPackage.versionName);
            appInfo.setAppIcon(aPackage.applicationInfo.loadIcon(packageManager));
            if ((aPackage.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                //非系统应用
                appInfo.setSystemApp(false);
            }
            appList.add(appInfo);
        }

        return appList;
    }
}
