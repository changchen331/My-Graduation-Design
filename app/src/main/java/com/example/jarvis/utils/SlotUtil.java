package com.example.jarvis.utils;

import android.content.Context;

import com.example.jarvis.R;

public class SlotUtil {
    private static final String TAG = "SlotUtil";

    private SlotUtil() {
        // 私有构造函数防止实例化
    }

    public static String getSlots(Context context, String scene) {
        return switch (scene) {
            case "KTV团购" -> context.getResources().getString(R.string.ktv_tuan2_gou4);
            case "上门家政服务" ->
                    context.getResources().getString(R.string.shang4_men2_jia1_zheng4_fu2_wu4);
            case "医院" -> context.getResources().getString(R.string.yi1_yuan4);
            case "在线买药" -> context.getResources().getString(R.string.zai4_xian4_mai3_yao4);
            case "家店修理" -> context.getResources().getString(R.string.jia1_dian4_xiu1_li3);
            case "度假村" -> context.getResources().getString(R.string.du4_jia4_cun1);
            case "快递" -> context.getResources().getString(R.string.kuai4_di4);
            case "戏曲演出订票" ->
                    context.getResources().getString(R.string.xi4_qv3_yan3_chu1_men2_piao4);
            case "手机维修" -> context.getResources().getString(R.string.shou3_ji1_wei2_xiu1);
            case "打车" -> context.getResources().getString(R.string.da3_che1);
            case "播放有声书" ->
                    context.getResources().getString(R.string.bo1_fang4_you3_sheng1_shu1);
            case "播放电视剧" ->
                    context.getResources().getString(R.string.bo1_fang4_dian4_shi4_jv4);
            case "查询路线" -> context.getResources().getString(R.string.cha2_xun2_lu4_xian4);
            case "民宿预定" -> context.getResources().getString(R.string.min2_su4_yu4_ding4);
            case "生鲜采购" -> context.getResources().getString(R.string.sheng1_xian1_cai3_gou4);
            case "看牙" -> context.getResources().getString(R.string.kan4_ya2);
            case "网络购物" -> context.getResources().getString(R.string.wang3_luo4_gou4_wu4);
            case "羽毛球馆" -> context.getResources().getString(R.string.yu3_mao2_qiu2_guan3);
            case "血压记录" -> context.getResources().getString(R.string.xue3_ya1_ji4_lu4);
            case "订购外卖" -> context.getResources().getString(R.string.ding4_gou4_wai4_mai4);
            case "记账" -> context.getResources().getString(R.string.ji4_zhang4);
            case "购买景点门票" ->
                    context.getResources().getString(R.string.gou4_mai3_jing3_dian3_men2_piao4);
            case "购买火车票" ->
                    context.getResources().getString(R.string.gou4_mai3_huo3_che1_piao4);
            case "跟团游预定" ->
                    context.getResources().getString(R.string.gen1_tuan2_you2_yu4_ding4);
            case "陪诊服务" -> context.getResources().getString(R.string.pei2_zhen3_fu2_wu4);
            case "预定电影票" ->
                    context.getResources().getString(R.string.yu4_ding4_dian4_ying3_piao4);
            case "预定邮轮船票" ->
                    context.getResources().getString(R.string.yu4_ding4_you2_lun2_chuan2_piao4);
            case "预约体检" -> context.getResources().getString(R.string.yu4_yue1_ti3_jian3);
            case "预约餐厅" -> context.getResources().getString(R.string.yu4_yue1_can1_ting1);
            default -> "未知的场景";
        };
    }
}
