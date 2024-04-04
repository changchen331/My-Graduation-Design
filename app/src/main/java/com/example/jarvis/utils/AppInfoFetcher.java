package com.example.jarvis.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.example.jarvis.model.AppInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 获取应用信息
 */
public class AppInfoFetcher {
    private static final String TAG = "AppInfoFetcher";
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

        // 用于存放应用数据
        List<AppInfo> appList = new ArrayList<>();
        // 获取PackageManager实例
        PackageManager packageManager = context.getPackageManager();

        // 筛选应用
        // 创建一个新的 Intent 对象，设置其动作为 Intent.ACTION_MAIN，表示主动作。
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        // 为 Intent 添加类别 Intent.CATEGORY_LAUNCHER，表示查找可以作为应用启动器的活动。
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        // 使用 PackageManager 的 queryIntentActivities 方法查询所有可以响应此 Intent 的活动。
        List<ResolveInfo> resolves = packageManager.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolves) {
            AppInfo appInfo = new AppInfo();
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            appInfo.setAppName(resolveInfo.loadLabel(packageManager).toString());
            appInfo.setPackageName(activityInfo.packageName);
            appInfo.setAppIcon(activityInfo.loadIcon(packageManager));
            appInfo.setMainActivity(activityInfo);
            appList.add(appInfo);
        }

        Log.v(TAG, String.valueOf(appList.size()));
        return appList;
    }
}
