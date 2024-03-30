package com.example.jarvis.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.example.jarvis.model.AppInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取应用信息
 */
public class AppInfoFetcher {
    private final Context context;

    public AppInfoFetcher(Context context) {
        this.context = context;
    }

    @SuppressLint("QueryPermissionsNeeded")
    public List<AppInfo> getAllInstalledApps() {
        if (context == null) {
            // 提供一个空列表，因为没有有效的 context 来获取应用信息
            return Collections.emptyList();
        }

        List<AppInfo> appList = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\."); // 正则表达式（匹配'.'）

        // 获取所有应用包信息
        PackageManager packageManager = context.getPackageManager();
        try {
            List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES);

            // 过滤应用包信息（提取需要的应用信息）
            for (PackageInfo aPackage : packages) {
                if (aPackage != null) {
                    Matcher matcher = pattern.matcher(aPackage.applicationInfo.loadLabel(packageManager).toString());
                    if (!matcher.find()) {
                        // 应用名称不包含‘.’，录入信息
                        AppInfo appInfo = new AppInfo();
                        appInfo.setAppName(aPackage.applicationInfo.loadLabel(packageManager).toString());
                        appInfo.setPackageName(aPackage.packageName);
                        appInfo.setClassName(aPackage.applicationInfo.className);
                        appInfo.setVersionName(aPackage.versionName);
                        Drawable appIcon = aPackage.applicationInfo.loadIcon(packageManager);
                        if (appIcon != null) {
                            appInfo.setAppIcon(appIcon);
                        }
                        if ((aPackage.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                            // 非系统应用
                            appInfo.setSystemApp(false);
                        }
                        appList.add(appInfo);
                    }
                }
            }
        } catch (Exception e) {
            // 处理获取应用信息时可能发生的异常
            Log.e("getAllInstalledApps", "Failed to get installed apps", e);
        }

        return appList;
    }
}
