package com.example.jarvis.utils;

import static com.example.jarvis.utils.HttpRequests.postAsync;
import static com.example.jarvis.utils.SlotUtil.getSlots;

import android.content.Context;

import com.example.jarvis.model.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ChatToGPT {
    private static final String TAG = "ChatToGPT";
    private static final String url = "https://api.openai.com/v1/chat/completions";

    /**
     * 获取 起始问题
     *
     * @param context context
     * @param scene   场景
     * @return Message 对象
     */
    public static Message getStart(Context context, String scene) {
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
            LogUtil.debug(TAG, "getStart", overallJson.toString(), Boolean.TRUE);
        } catch (JSONException e) {
            LogUtil.error(TAG, "getStart", "JSON 构建失败", e);
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(overallJson.toString(), JSON);

        Message response = postAsync(url, requestBody, true, context);
        LogUtil.info(TAG, "getStart", response.getContent(), Boolean.TRUE);

        try {
            JSONObject jsonObject = new JSONObject(response.getContent());
            JSONArray choices = jsonObject.getJSONArray("choices");
            response.setContent(choices.getJSONObject(0).getString("text"));
        } catch (JSONException e) {
            LogUtil.error(TAG, "getStart", "", e);
        }

        return response;
    }

    /**
     * 获取 总结
     *
     * @param slots 槽位
     * @return Message 对象
     */
    public static Message getConclusion(Context context, JSONObject slots) {
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

        Message response = postAsync(url, requestBody, true, context);
        LogUtil.info(TAG, "getConclusion", response.getContent(), Boolean.TRUE);

        try {
            JSONObject jsonObject = new JSONObject(response.getContent());
            JSONArray choices = jsonObject.getJSONArray("choices");
            response.setContent(choices.getJSONObject(0).getString("text"));
        } catch (JSONException e) {
            LogUtil.error(TAG, "getConclusion", "", e);
        }

        return response;
    }

    private static String getContent_firstQuestion(Context context, String scene) {
        String contentHead = "你是一个负责信息抽取的机器人。在 " + scene + " 场景下，当用户想使用app进行服务预约时，通常需要填写以下信息（我们称每种信息为一个“槽位”）：\n\n";
        String contentBody = getSlots(context, scene) + "\n\n";
        String contentTail = "我们希望通过人工智能，帮助老年人在app中进行服务预约时填写这些槽位。\n请随机选择一个槽位对用户进行提问，你的提问需要易懂，情感表达亲切，更口语化。\n在你的回答中，请只给出问题，不要生成无关的信息。";
        return contentHead + contentBody + contentTail;
    }

    private static String getContent_getConclusion(JSONObject slots) {
        String contentHead = "给定槽位填充\"" + slots.toString() + "\",";
        String contentTail = "生成用户友好的一段文字，以展示用户已经填写的信息，并请求用户确认。";
        return contentHead + contentTail;
    }
}
