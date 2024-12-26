package com.example.jarvis.utils;

import android.content.Context;
import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ChatToGPTUtil {
    private static final String TAG = "ChatToGPTUtil";

    public static final String URL = "https://api.openai.com/v1/chat/completions";
    public static final Integer MESSAGE_CODE = 331;

    private ChatToGPTUtil() {
        // 私有构造函数防止实例化
    }

    /**
     * 获取 起始问题
     *
     * @param context context
     * @param scene   场景
     * @param handler 用于在主线程上处理响应的 Handler
     */
    public static void getStart(Context context, String scene, Handler handler) {
        // 构建请求体
        JSONObject overallJson = new JSONObject();
        try {
            overallJson.put("model", "gpt-4o");
            overallJson.put("temperature", 0.7);

            JSONArray messages = new JSONArray();
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a helpful assistant.");
            messages.put(systemMessage);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", getContent_firstQuestion(context, scene));
            messages.put(userMessage);
            overallJson.put("messages", messages);

            JSONObject responseFormat = new JSONObject();
            responseFormat.put("type", "text");
            overallJson.put("response_format", responseFormat);
            // 测试
            LogUtil.debug(TAG, "getStart", overallJson.toString(), Boolean.FALSE);
        } catch (JSONException e) {
            LogUtil.error(TAG, "getStart", "JSON 构建失败", e);
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(overallJson.toString(), JSON);

        // 发送请求
        HttpRequestsUtil.postSync(URL, requestBody, context, handler, MESSAGE_CODE);
    }

    /**
     * 获取 总结
     *
     * @param context context
     * @param slots   槽位
     * @param handler 用于在主线程上处理响应的 Handler
     */
    public static void getQuestion(Context context, JSONObject slots, Handler handler) {
        // 构建请求体
        JSONObject overallJson = new JSONObject();
        try {
            overallJson.put("model", "gpt-4o");

            JSONArray messages = new JSONArray();
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a helpful assistant.");
            messages.put(systemMessage);
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", "");
            messages.put(userMessage);
            overallJson.put("messages", messages);

            overallJson.put("temperature", 0.7);

            JSONObject responseFormat = new JSONObject();
            responseFormat.put("type", "text");
            overallJson.put("response_format", responseFormat);
        } catch (JSONException e) {
            LogUtil.error(TAG, "getConclusion", "JSON 提取失败", e);
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(overallJson.toString(), JSON);

        // 发送请求
        HttpRequestsUtil.postSync(URL, requestBody, context, handler, MESSAGE_CODE);
    }

    /**
     * 获取 总结
     *
     * @param context context
     * @param slots   槽位
     * @param handler 用于在主线程上处理响应的 Handler
     */
    public static void getConclusion(Context context, JSONObject slots, Handler handler) {
        // 构建请求体
        JSONObject overallJson = new JSONObject();
        try {
            overallJson.put("model", "gpt-4o");

            JSONArray messages = new JSONArray();
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a helpful assistant.");
            messages.put(systemMessage);
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", getContent_getConclusion(slots));
            messages.put(userMessage);
            overallJson.put("messages", messages);

            overallJson.put("temperature", 0.7);

            JSONObject responseFormat = new JSONObject();
            responseFormat.put("type", "text");
            overallJson.put("response_format", responseFormat);
        } catch (JSONException e) {
            LogUtil.error(TAG, "getConclusion", "JSON 提取失败", e);
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(overallJson.toString(), JSON);

        // 发送请求
        HttpRequestsUtil.postSync(URL, requestBody, context, handler, MESSAGE_CODE);
    }

    private static String getContent_firstQuestion(Context context, String scene) {
        String contentHead = "你是一个负责信息抽取的机器人。在 " + scene + " 场景下，当用户想使用app进行服务预约时，通常需要填写以下信息（我们称每种信息为一个“槽位”）：\n\n";
        String contentBody = SlotUtil.getSlots(context, scene) + "\n\n";
        String contentTail = "我们希望通过人工智能，帮助老年人在app中进行服务预约时填写这些槽位。\n请随机选择一个槽位对用户进行提问，你的提问需要易懂，情感表达亲切，更口语化。\n在你的回答中，请只给出问题，不要生成无关的信息。";
        return contentHead + contentBody + contentTail;
    }

    private static String getContent_getConclusion(JSONObject slots) {
        String contentHead = "给定槽位填充\"" + slots.toString() + "\",";
        String contentTail = "生成一段友好的提示，以展示用户已经填写的信息，并请求用户确认。提示需符合口语习惯，是一个连贯的句子，比如“您确定是在北京朝阳预定xx日期xx内容的xx嘛，如果有什么不对可以随时联系我修改。";
        return contentHead + contentTail;
    }
}
