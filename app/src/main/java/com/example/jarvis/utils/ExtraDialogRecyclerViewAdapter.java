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

import java.util.List;

public class ExtraDialogRecyclerViewAdapter extends RecyclerView.Adapter<ExtraDialogRecyclerViewAdapter.ViewHolder> {
    private List<Message> messages; // 存储对话信息列表
    private ButtonClickListener buttonClickListener; // 定义点击按钮的回调接口

    public ExtraDialogRecyclerViewAdapter(List<Message> messages) {
        this.messages = messages;
    }

    public void setButtonClickListener(ButtonClickListener buttonClickListener) {
        this.buttonClickListener = buttonClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 通过 LayoutInflater 加载 item 布局
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.extra_dialog_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 获取当前位置的对话信息
        Message message = messages.get(position);
        if (message.getType() == Message.TYPE_RECEIVED) {
            holder.extraDialogLeftText.setText(message.getContent());
            holder.extraDialogLeft.setVisibility(View.VISIBLE);
            holder.extraDialogRight.setVisibility(View.GONE);
        } else {
            holder.extraDialogRightText.setText(message.getContent());
            holder.extraDialogLeft.setVisibility(View.GONE);
            holder.extraDialogRight.setVisibility(View.VISIBLE);
            // 设置按钮的点击监听器
            holder.extraDialogRightEdit.setOnClickListener(v -> buttonClickListener.onButtonClick(position));
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    // 定义 ButtonClickListener 接口在适配器内部
    public interface ButtonClickListener {
        void onButtonClick(int position);
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
    }
}
