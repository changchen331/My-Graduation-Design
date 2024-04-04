package com.example.jarvis;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jarvis.model.AppInfo;

public class ExtraActivity extends AppCompatActivity {
    private static final String TAG = "ExtraActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra);

        // 从 Intent 中获取用户选择的应用
        AppInfo selectedApp = (AppInfo) getIntent().getParcelableExtra("selected_applications");
        if (selectedApp != null) {
            openApplication(ExtraActivity.this, selectedApp);
        }
    }

    /**
     * 启动指定应用
     *
     * @param context 当前上下文环境
     * @param app     应用信息
     */
    private void openApplication(Context context, AppInfo app) {
        // 获取应用的启动 Intent
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(app.getPackageName());
        // 检查是否成功获取到启动 Intent
        if (intent != null) {
            try {
                // 启动应用
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // 处理无法找到对应的 Activity 的情况
                Log.e(TAG, "Activity not found for package: " + app.getPackageName(), e);
                Toast.makeText(context, "没有权限启动应用。", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 处理无法获取启动 Intent 的情况
            Log.e(TAG, "No launch intent found for package: " + app.getPackageName());
            Toast.makeText(context, "无法启动应用。", Toast.LENGTH_SHORT).show();
        }
    }
}