package com.example.jarvis.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jarvis.R;
import com.example.jarvis.model.AppInfo;

import java.util.Collections;
import java.util.List;

/**
 * 用于在 RecyclerView 中显示应用信息列表的适配器
 * 负责将数据集合中的数据显示在 RecyclerView 上，并处理数据的操作，同时支持点击事件回调
 */
public class AppSelectorAdapter extends RecyclerView.Adapter<AppSelectorAdapter.ViewHolder> {
    private static final String TAG = "AppSelectorAdapter";

    private final List<AppInfo> apps; // 存储应用信息列表

    private OnItemClickListener onItemClickListener; // 定义点击事件的回调接口

    private Integer position = 0; // 记录当前选中 item 的位置

    public AppSelectorAdapter(List<AppInfo> apps) {
        if (apps == null) {
            LogUtil.warning(TAG, "AppSelectorAdapter", "The apps list cannot be null", Boolean.TRUE);
            this.apps = Collections.emptyList();
        } else this.apps = apps;
    }

    /**
     * 设置点击事件回调接口
     *
     * @param onItemClickListener 点击事件的回调接口
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 创建 ViewHolder 实例，用于缓存 item 视图的引用
     *
     * @param parent   父视图组，通常是 RecyclerView 本身
     * @param viewType 视图类型，用于区分不同的 item 布局
     * @return ViewHolder 实例
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 通过 LayoutInflater 加载 item 布局
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.application_selection_item, parent, false);

        // 计算 item 的宽度和边距
        Integer screenWidth = parent.getContext().getResources().getDisplayMetrics().widthPixels; // 获取手机屏幕的宽度
        // RecyclerView 的宽度为 ScreenWidth - LeftPadding - RightPadding
        int recyclerWidth = screenWidth - dpToPx(60, parent.getContext());
        int itemWidth = recyclerWidth / 3;
        int itemMargin = calculateItemMargin(recyclerWidth, itemWidth, apps.size());

        // 设置 item 的布局参数
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
        layoutParams.width = itemWidth - dpToPx(1, parent.getContext());
        layoutParams.setMargins(itemMargin, 0, itemMargin, 0);
        itemView.setLayoutParams(layoutParams);
        return new ViewHolder(itemView);
    }

    /**
     * 将应用信息数据绑定到 item 视图上。
     *
     * @param viewHolder ViewHolder 实例，持有对当前 item 视图的引用
     * @param position   当前 item 在数据集中的位置（索引）
     */
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        // 获取当前位置的应用信息
        AppInfo app = apps.get(position);
        if (app != null) {
            // 设置 app 名称
            TextView appNameView = viewHolder.appName;
            String appName = app.getAppName();
            if (!TextUtils.isEmpty(appName)) appNameView.setText(appName);

            // 设置 app 图标
            ImageView appIconView = viewHolder.appIcon;
            Drawable appIcon = app.getAppIcon();
            if (appIcon != null) appIconView.setImageDrawable(appIcon);

            // 设置点击状态
            viewHolder.itemView.setSelected(position == this.position);
            // 设置点击事件监听器
            viewHolder.itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    // 当前点击位置
                    int currentPosition = viewHolder.getAdapterPosition();
                    // 更新点击状态
                    if (currentPosition != this.position) {
                        notifyItemChanged(this.position); // 取消上个 item 的勾选状态
                        notifyItemChanged(currentPosition); // 通知选中状态变化
                        this.position = currentPosition; // 更新 selectedPosition 为新点击的 position
                    }
                    onItemClickListener.onItemClick(v, currentPosition);
                }
            });
        }
    }

    /**
     * 获取应用信息的总数
     *
     * @return 应用信息的数量
     */
    @Override
    public int getItemCount() {
        return apps != null ? apps.size() : 0;
    }

    /**
     * 将 dp 单位转换为像素单位
     *
     * @param dp      需要转换的 dp 值
     * @param context 上下文对象，用于获取屏幕密度信息
     * @return 转换后的像素值
     */
    private Integer dpToPx(int dp, Context context) {
        // 获取显示度量，用于转换 dp 到像素
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density); // 计算像素值并返回
    }

    /**
     * 根据屏幕宽度和 item 数量计算边距，确保均匀间隔
     *
     * @param recyclerWidth 屏幕宽度（像素）
     * @param itemCount     item 数量
     * @return 计算出的边距值（像素）
     */
    private Integer calculateItemMargin(int recyclerWidth, int itemWidth, int itemCount) {
        // 检查 item 数量是否为 0，如果是，则不需要边距
        if (itemCount == 0) return 0;

        // 计算 item 的跨度数量，确保在 1 到 3 之间
        int itemSpanCount = Math.max(1, Math.min(3, itemCount));

        // 根据列数计算边距
        return switch (itemSpanCount) {
            case 1 ->
                // 1 列时，边距为屏幕宽度与 item 宽度差的 1/2
                    (recyclerWidth - itemWidth) / 2;
            case 2 ->
                // 2 列时，边距为屏幕宽度与 2 倍 item 宽度差的 1/4
                    (recyclerWidth - 2 * itemWidth) / 4;
            default ->
                // 3 列或以上时，边距为屏幕宽度与 3 倍 item 宽度差的 1/6
                    (recyclerWidth - 3 * itemWidth) / 6;
        };
    }

    /**
     * 定义点击事件的回调接口
     */
    public interface OnItemClickListener {
        /**
         * 处理 item 的点击事件
         *
         * @param itemView 被点击的 item 视图
         * @param position 被点击 item 的位置
         */
        void onItemClick(View itemView, int position);
    }

    /**
     * ViewHolder 内部类，用于缓存 item 视图中各个子视图的引用
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView appName; // 应用名称
        ImageView appIcon; // 应用图标

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.application_selection_item_text);
            appIcon = itemView.findViewById(R.id.application_selection_item_image);
        }
    }
}