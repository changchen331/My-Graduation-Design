package com.example.jarvis.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.jarvis.R;
import com.example.jarvis.model.Message;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpRequests {
    private static final String TAG = "HttpRequests";
    private static final OkHttpClient client;

    static {
        client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).build();
    }

    // 发送 GET 异步请求
    public static Message getAsync(String url) {
        Message message = new Message(Message.TYPE_RECEIVED);

        // GET 请求
        Request request = new Request.Builder().url(url).get().build();
        // 请求的 call 对象
        Call call = client.newCall(request);
        // 异步请求
        call.enqueue(new Callback() {
            // 失败的请求
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                message.setContent("");
                LogUtil.warning(TAG, "getAsync", "发送请求失败", Boolean.TRUE);
            }

            // 结束的回调
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                message.setContent(responseBody);

                if (response.isSuccessful())
                    LogUtil.info(TAG, "getAsync_onResponse", responseBody, Boolean.TRUE);
                else LogUtil.warning(TAG, "getAsync_onResponse", "请求失败", Boolean.TRUE);
            }
        });

        return message;
    }

    // 发送 POST 异步请求
    public static Message postAsync(String url, RequestBody requestBody) {
        Message message = new Message(Message.TYPE_RECEIVED);

        if (requestBody == null) {
            message.setContent("");
            LogUtil.warning(TAG, "postAsync", "请求体为空", Boolean.TRUE);
            return message;
        }

        // POST 请求
        Request request = new Request.Builder().url(url).post(requestBody).build();
        // 请求的 call 对象
        Call call = client.newCall(request);
        // 异步请求
        call.enqueue(new Callback() {
            // 失败的请求
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                message.setContent("");
                LogUtil.warning(TAG, "postAsync", "发送请求失败", Boolean.TRUE);
            }

            // 结束的回调
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                message.setContent(responseBody);

                if (response.isSuccessful())
                    LogUtil.info(TAG, "postAsync_onResponse", responseBody, Boolean.TRUE);
                else LogUtil.warning(TAG, "postAsync_onResponse", "请求失败", Boolean.TRUE);
            }
        });

        return message;
    }

    // 发送 POST 异步请求
    public static Message postAsync(String url, RequestBody requestBody, Boolean needHeader, Context context) {
        Message message = new Message(Message.TYPE_RECEIVED);

        if (requestBody == null) {
            message.setContent("");
            LogUtil.warning(TAG, "postAsync", "请求体为空", Boolean.TRUE);
            return message;
        }

        // POST 请求
        Request request;
        if (needHeader) {
            String api_Key = context.getString(R.string.gpt_4o);
            request = new Request.Builder().url(url).post(requestBody).addHeader("Authorization", "Bearer " + api_Key).build();
        } else return postAsync(url, requestBody);
        // 请求的 call 对象
        Call call = client.newCall(request);
        // 异步请求
        call.enqueue(new Callback() {
            // 失败的请求
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                message.setContent("");
                LogUtil.warning(TAG, "postAsync", "发送请求失败", Boolean.TRUE);
            }

            // 结束的回调
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                message.setContent(responseBody);

                if (response.isSuccessful())
                    LogUtil.info(TAG, "postAsync_onResponse", responseBody, Boolean.TRUE);
                else LogUtil.warning(TAG, "postAsync_onResponse", "请求失败", Boolean.TRUE);
            }
        });

        return message;
    }

    //发送 PUT 请求
//    public static String put(String url, String json) throws IOException {
//        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//        RequestBody requestBody = RequestBody.create(json, JSON);
//        Request request = new Request.Builder().url(url).put(requestBody).build();
//
//        try (Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//
//            if (response.body() != null) {
//                return response.body().string();
//            }
//        }
//        return "";
//    }

    //发送 DEL 请求
//    public static String del(String url) throws IOException {
//        Request request = new Request.Builder().url(url).delete().build();
//
//        try (Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//
//            if (response.body() != null) {
//                return response.body().string();
//            }
//        }
//        return "";
//    }
}
