package com.example.jarvis.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jarvis.R;
import com.example.jarvis.model.Message;

import java.util.Collections;
import java.util.List;

/**
 * 用于在 RecyclerView 中显示气泡对话的适配器
 * 负责将数据集合中的数据显示在 RecyclerView 上，并处理数据的操作，同时支持点击事件回调
 */
public class DialogueInfoAdapter extends RecyclerView.Adapter<DialogueInfoAdapter.ViewHolder> {
    private static final String TAG = "DialogueInfoAdapter";
    private final List<Message> messages; // 存储对话信息列表

    private ButtonClickListener buttonClickListener; // 点击按钮的回调接口
    private OnItemClickListener onItemClickListener; // 点击事件的回调接口

    public DialogueInfoAdapter(List<Message> messages) {
        if (messages == null) {
            LogUtil.warning(TAG, "DialogueInfoAdapter", "The list of messages cannot be null", Boolean.TRUE);
            this.messages = Collections.emptyList();
        } else this.messages = messages;
    }

    /**
     * 设置按钮点击事件回调接口，允许外部定义如何处理按钮点击事件
     *
     * @param buttonClickListener 传入的按钮点击事件的回调接口
     */
    public void setButtonClickListener(ButtonClickListener buttonClickListener) {
        this.buttonClickListener = buttonClickListener;
    }

    /**
     * 设置 item 点击事件回调接口，允许外部定义如何处理 item 点击事件
     *
     * @param onItemClickListener 传入的 item 点击事件的回调接口
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
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.extra_dialog_item, parent, false);
        return new ViewHolder(itemView);
    }

    /**
     * 将应用信息数据绑定到 item 视图上，根据消息类型显示不同的布局
     *
     * @param viewHolder ViewHolder 实例，持有对当前 item 视图的引用
     * @param position   当前 item 在数据集中的位置（索引）
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        // 获取当前位置的对话信息
        Message message = messages.get(position);
        // 消息绑定
        viewHolder.bind(message, position);
    }

    /**
     * 获取应用信息的总数，即消息列表的大小
     *
     * @return 消息列表的大小
     */
    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * 定义 ButtonClickListener 接口在适配器内部，用于处理按钮点击事件
     */
    public interface ButtonClickListener {
        /**
         * 当按钮被点击时，调用此方法，并传递被点击 item 的位置信息
         *
         * @param position 被点击 item 的位置
         */
        void onButtonClick(int position);
    }

    /**
     * 定义点击事件的回调接口，用于处理 item 的点击事件
     */
    public interface OnItemClickListener {
        /**
         * 处理 item 的点击事件，传递被点击 item 的视图和位置信息
         *
         * @param itemView 被点击的 item 视图
         * @param position 被点击 item 的位置
         */
        void onItemClick(View itemView, int position);
    }

    /**
     * ViewHolder 内部类，用于缓存 item 视图中各个子视图的引用
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout extraDialogLeft; // 问题对话框
        TextView extraDialogLeftText; // 问题文本显示框
        LinearLayout extraDialogRight; // 回复对话框
        TextView extraDialogRightText; // 回复文本显示框
        ImageButton extraDialogRightEdit; // 回复文本修改按钮

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            extraDialogLeft = itemView.findViewById(R.id.extra_dialog_left);
            extraDialogLeftText = itemView.findViewById(R.id.extra_dialog_left_text);
            extraDialogRight = itemView.findViewById(R.id.extra_dialog_right);
            extraDialogRightText = itemView.findViewById(R.id.extra_dialog_right_text);
            extraDialogRightEdit = itemView.findViewById(R.id.extra_dialog_right_edit);
        }

        void bind(Message message, int position) {
            // 根据消息类型显示对应的布局
            boolean isReceived = message.getType() == Message.TYPE_RECEIVED;
            extraDialogLeft.setVisibility(isReceived ? View.VISIBLE : View.GONE);
            extraDialogLeftText.setText(message.getContent());
            extraDialogRight.setVisibility(isReceived ? View.GONE : View.VISIBLE);
            extraDialogRightText.setText(message.getContent());

            // 设置按钮的点击监听器
            if (!isReceived && buttonClickListener != null) {
                extraDialogRightEdit.setOnClickListener(v -> buttonClickListener.onButtonClick(position));
            } else extraDialogRightEdit.setOnClickListener(null);

            // 设置文本的点击事件，当文本被点击时调用 onItemClickListener 的 onItemClick 方法
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(itemView, position);
                }
            });
        }
    }
}
