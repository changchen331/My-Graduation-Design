package com.example.jarvis.utils;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jarvis.R;
import com.example.jarvis.model.AppInfo;

import java.util.List;


/**
 * RecyclerView 适配器
 * 将数据集合中的数据显示在 RecyclerView 上，同时处理数据的增删改查等操作，并确保视图的更新与数据源保持同步。
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private final List<AppInfo> apps; //应用信息列表
    private int selectedPosition = 0; //被选择 itemView 的位置
    private OnItemClickListener onItemClickListener; //定义回调接口的成员变量

    public RecyclerViewAdapter(List<AppInfo> apps) {
        this.apps = apps;
    }

    /**
     * 设置回调接口实例的方法
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 创建新的 item 视图持有者（ViewHolder），在 RecyclerView 需要显示一个新 item 时被调用
     *
     * @param parent   通常是 RecyclerView 本身。LayoutInflater 会将新的 item 视图作为 parent 的子视图进行添加。
     * @param viewType 用于区分不同的 item 类型。
     * @return ViewHolder 实例
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //创建新的 item 视图（itemView）
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.application_selection_item, parent, false);
        return new ViewHolder(itemView);
    }

    /**
     * 将数据绑定到 ViewHolder 持有的视图上
     *
     * @param viewHolder holder 持有对当前 item 视图的引用
     * @param position   当前 item 在数据集中的位置（索引）
     */
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @SuppressLint("RecyclerView") int position) {
        AppInfo app = apps.get(position); //获取被点击视图的应用信息
        viewHolder.appName.setText(app.getAppName()); //设置应用名称
        viewHolder.appIcon.setImageDrawable(app.getAppIcon()); //设置应用图标

        //设置 itemView 是否选中（若选中则更改背景颜色）
        viewHolder.itemView.setSelected(position == selectedPosition); //设置选中状态
    }

    /**
     * 获取应用信息总数
     *
     * @return 应用信息总数
     */
    @Override
    public int getItemCount() {
        if (apps != null) return apps.size();
        return 0;
    }

    /**
     * 回调接口 OnItemClickListener
     * 用于组件间通信
     */
    public interface OnItemClickListener {
        /**
         * 对 RecyclerView 中项（item）的点击事件监听
         *
         * @param itemView 项视图
         * @param position 被点击项的位置
         */
        void onItemClick(View itemView, int position);
    }

    /**
     * ViewHolder内部静态类
     * 用于缓存 item 视图中各个子视图的引用
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView appName; //应用名称
        ImageView appIcon; //应用图标

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.application_selection_item_image);
            appName = itemView.findViewById(R.id.application_selection_item_text);

            //设置点击事件
            //当点击 itemView 时,触发 onClick 方法
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    //更新 selectedPosition（若选中则更改背景颜色）
                    if (selectedPosition != getAdapterPosition()) {
                        notifyItemChanged(selectedPosition); //先取消上个 item 的勾选状态
                        selectedPosition = getAdapterPosition(); //更新 selectedPosition
                        notifyItemChanged(selectedPosition); //设置新 item 的勾选状态
                    }
                    onItemClickListener.onItemClick(v, selectedPosition); //回调函数
                }
            });
        }
    }
}

