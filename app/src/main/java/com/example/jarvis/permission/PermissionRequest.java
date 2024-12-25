package com.example.jarvis.permission;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.jarvis.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 封装权限申请
 */
public class PermissionRequest extends AppCompatActivity {
    private static final String TAG = "PermissionRequest";
    private static final Integer REQUEST_PERMISSION_CODE = 1;  // 默认请求权限的 requestCode 为 1

    private PermissionListener mListener;

    /**
     * 请求申请权限
     * 默认请求权限的 requestCode 为 1
     *
     * @param permissions        要申请的权限数组
     * @param permissionListener 权限申请结果监听者
     */
    public void requestRuntimePermission(Context context, String[] permissions, PermissionListener permissionListener) {
        mListener = permissionListener;

        // 存放 permissions 中当前未被授予的权限
        List<String> permissionList = new ArrayList<>();

        // 遍历权限数组，检测所需权限是否已被授予，若该权限尚未授予，添加到 permissionList 中
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                if (!permissionList.contains(permission)) permissionList.add(permission);
        }

        if (!permissionList.isEmpty())
            ActivityCompat.requestPermissions((Activity) context, permissionList.toArray(new String[0]), REQUEST_PERMISSION_CODE); // 有权限尚未授予，去授予权限
        else if (mListener != null) {
            //权限都被授予了
            PermissionRequest.this.mListener.onGranted();  //'权限都被授予了'回调
            LogUtil.info(TAG, "requestRuntimePermission", "权限都授予了", Boolean.TRUE);
        }
    }

    /**
     * 申请权限结果返回
     *
     * @param requestCode  请求码
     * @param permissions  所有申请的权限集合
     * @param grantResults 权限申请的结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CODE) {
            // 有权限申请
            if (grantResults.length > 0) {
                // 存储被用户拒绝的权限
                List<String> deniedPermissionList = getStrings(permissions, grantResults);

                if (deniedPermissionList.isEmpty() && mListener != null) {
                    // 没有被拒绝的权限
                    mListener.onGranted();
                    LogUtil.info(TAG, "onRequestPermissionsResult", "权限都授予了", Boolean.TRUE);
                } else if (mListener != null) {
                    // 有被拒绝的权限
                    mListener.onDenied(deniedPermissionList);
                    LogUtil.info(TAG, "onRequestPermissionsResult", "有权限被拒绝了", Boolean.TRUE);
                }
            }
        }
    }

    /**
     * 获取被拒绝的权限
     *
     * @param permissions  所有申请的权限集合
     * @param grantResults 权限申请的结果
     * @return 被拒绝的权限申请集合
     */
    private static List<String> getStrings(String[] permissions, int[] grantResults) {
        List<String> deniedPermissionList = new ArrayList<>();

        // 有权限被拒绝，分类出被拒绝的权限
        for (int i = 0; i < grantResults.length; i++) {
            String permission = permissions[i];
            int grantResult = grantResults[i];
            if (grantResult != PackageManager.PERMISSION_GRANTED && !deniedPermissionList.contains(permission))
                deniedPermissionList.add(permission);
        }
        return deniedPermissionList;
    }
}
