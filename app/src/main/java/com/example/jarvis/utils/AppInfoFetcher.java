package com.example.jarvis.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.example.jarvis.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * ✔ 工具类用于获取设备上所有已安装应用的信息。
 */
public class AppInfoFetcher {
    private static final String TAG = "AppInfoFetcher";

    /**
     * 获取设备上所有已安装应用的名称列表。
     *
     * @param context 应用上下文，必须非空。
     * @return 已安装应用的名称列表。
     */
    public static List<AppInfo> getAllInstalledApps(Context context) {
        if (context == null) {
            Log.e(TAG, "Context cannot be null.");
            return new ArrayList<>();
        }

        // 获取应用信息
        List<AppInfo> installedApps = new ArrayList<>(); // 应用数据列表
        try {
            // 获取 PackageManager 实例
            PackageManager pm = context.getPackageManager();
            // 筛选应用
            // 创建一个 Intent 对象
            Intent intent = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolves = pm.queryIntentActivities(intent, 0);// 查询所有可以响应此 Intent 的活动
            for (ResolveInfo resolveInfo : resolves) {
                // 存入信息
                ActivityInfo activityInfo = resolveInfo.activityInfo;
                AppInfo appInfo = new AppInfo();
                appInfo.setAppName(resolveInfo.loadLabel(pm).toString());
                appInfo.setPackageName(activityInfo.packageName);
                appInfo.setAppIcon(activityInfo.loadIcon(pm));
                appInfo.setMainActivity(activityInfo);
                installedApps.add(appInfo);
            }
        } catch (Exception e) {
            // 如果获取应用列表过程中发生异常，记录错误日志
            Log.e(TAG, "Error fetching installed app names", e);
        }

        return installedApps;
    }
}
