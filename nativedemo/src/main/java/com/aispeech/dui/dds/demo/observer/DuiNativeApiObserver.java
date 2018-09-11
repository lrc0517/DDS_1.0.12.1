package com.aispeech.dui.dds.demo.observer;

import android.content.Context;

import com.aispeech.ailog.AILog;
import com.aispeech.dui.dsk.duiwidget.NativeApiObserver;

/*
 * 注册NativeApiObserver, 用于客户端响应DUI平台技能配置里的资源调用指令, 同一个NativeApiObserver可以处理多个native api.
 */
public class DuiNativeApiObserver implements NativeApiObserver {

    private String TAG = "DuiNativeApiObserver";

    private Context mContext;

    public DuiNativeApiObserver(Context ctx) {
        mContext = ctx;
    }

    /*
    * onQuery方法执行时，需要调用feedbackNativeApiResult来向DUI平台返回执行结果，表示一个native api执行结束。
    * native api的执行超时时间为10s
    */
    @Override
    public void onQuery(String topic, String data) {
        AILog.i(TAG, "topic: " + topic + ", data: " + data);
    }
}
