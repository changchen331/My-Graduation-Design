package com.example.jarvis.utils;


import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpRequestsUtil {
    private static final String TAG = "HttpRequestsUtil";
    public static final OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).build();

    private HttpRequestsUtil() {
        // 私有构造函数防止实例化
    }

    /**
     * 发送 GET 异步请求
     *
     * @param url 要请求的 URL 字符串
     */
    public static void getAsync(String url) {
        // GET 异步请求
        Request request = new Request.Builder().url(url).get().build();
        // 请求的 call 对象
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            // 失败的请求
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtil.warning(TAG, "getAsync", "发送请求失败", Boolean.TRUE);
            }

            // 结束的回调
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful())
                    LogUtil.info(TAG, "getAsync_onResponse", responseBody, Boolean.TRUE);
                else LogUtil.warning(TAG, "getAsync_onResponse", responseBody, Boolean.TRUE);
            }
        });
    }

    /**
     * 发送 GET 同步请求
     *
     * @param url         要请求的 URL 字符串
     * @param handler     用于在主线程上处理响应的 Handler
     * @param messageCode 信息标识
     */
    public static void getSync(String url, Handler handler, Integer messageCode) {
        // GET 同步请求
        Request request = new Request.Builder().url(url).get().build();
        // 请求的 call 对象
        new Thread() {
            @Override
            public void run() {
                super.run();
                // 请求的 call 对象
                Call call = client.newCall(request);
                try (Response response = call.execute()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    if (response.isSuccessful())
                        LogUtil.info(TAG, "getSync", responseBody, Boolean.TRUE);
                    else LogUtil.warning(TAG, "getSync", responseBody, Boolean.TRUE);

                    // 通过Handler传递消息
                    Message message = Message.obtain();
                    message.what = messageCode;
                    message.obj = responseBody;
                    handler.sendMessage(message);
                } catch (IOException e) {
                    LogUtil.error(TAG, "getSync", "发送请求失败", e);
                }
            }
        }.start();
    }

    /**
     * 发送 POST 异步请求
     *
     * @param url         要请求的 URL 字符串
     * @param requestBody 请求体
     */
    public static void postAsync(String url, RequestBody requestBody) {
        if (requestBody == null) {
            LogUtil.warning(TAG, "postAsync", "请求体为空", Boolean.TRUE);
            return;
        }

        // POST 异步请求
        Request request = new Request.Builder().url(url).post(requestBody).build();
        // 请求的 call 对象
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            // 失败的请求
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtil.warning(TAG, "postAsync_onFailure", "发送请求失败", Boolean.TRUE);
            }

            // 结束的回调
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful())
                    LogUtil.info(TAG, "postAsync_onResponse", responseBody, Boolean.TRUE);
                else LogUtil.warning(TAG, "postAsync_onResponse", responseBody, Boolean.TRUE);
            }
        });
    }

    /**
     * 发送 POST 同步请求-1
     *
     * @param url         要请求的 URL 字符串
     * @param requestBody 请求体
     * @param handler     用于在主线程上处理响应的 Handler
     * @param messageCode 信息标识
     */
    public static void postSync(String url, RequestBody requestBody, Handler handler, Integer messageCode) {
        if (requestBody == null) {
            LogUtil.warning(TAG, "postSync", "请求体为空", Boolean.TRUE);
            return;
        }

        // POST 同步请求
        Request request = new Request.Builder().url(url).post(requestBody).build();
        new Thread() {
            @Override
            public void run() {
                super.run();
                // 请求的 call 对象
                Call call = client.newCall(request);
                try (Response response = call.execute()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    if (response.isSuccessful())
                        LogUtil.info(TAG, "postSync", responseBody, Boolean.TRUE);
                    else LogUtil.warning(TAG, "postSync", responseBody, Boolean.TRUE);

                    // 通过Handler传递消息
                    Message message = Message.obtain();
                    message.what = messageCode;
                    message.obj = responseBody;
                    handler.sendMessage(message);
                } catch (IOException e) {
                    LogUtil.error(TAG, "postSync", "发送请求失败", e);
                }
            }
        }.start();
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
