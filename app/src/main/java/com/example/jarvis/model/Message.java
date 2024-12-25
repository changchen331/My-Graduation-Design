package com.example.jarvis.model;

import androidx.annotation.NonNull;

/**
 * 额外对话信息
 */
public class Message {
    public static final int TYPE_RECEIVED = 0; // 接收
    public static final int TYPE_SENT = 1; // 发送

    private String content; // 消息内容
    private Integer type; // 消息类型

    public Message(String content, int type) {
        this.content = content;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @NonNull
    @Override
    public String toString() {
        return "Message{" + "content='" + content + '\'' + ", type=" + type + '}';
    }
}
