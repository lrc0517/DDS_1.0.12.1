package com.aispeech.dui.dds.demo.observer;

import android.content.Context;

import com.aispeech.ailog.AILog;
import com.aispeech.dui.dds.DDS;
import com.aispeech.dui.dds.agent.ASREngine;
import com.aispeech.dui.dds.exceptions.DDSNotInitCompleteException;
import com.aispeech.dui.dsk.duiwidget.CommandObserver;

import java.awt.font.TextAttribute;

/*
* 客户端CommandObserver, 用于处理客户端动作的执行以及快捷唤醒中的命令响应.
* 例如在平台配置客户端动作： command://call?phone=$phone$&name=#name#,
* 那么在CommandObserver的onCall方法中会回调topic为"call", data为
 */
public class DuiCommandObserver implements CommandObserver {
    private String TAG = "DuiCommandObserver";

    private Context mContext;

    public DuiCommandObserver(Context context) {
        mContext = context;
    }


    @Override
    public void onCall(String topic, String data) {
        AILog.i(TAG, "topic: " + topic + ", data: " + data);

        switch (topic) {
            case "cmd.demo.start_asrengine":
                startAsrEngine();
                break;
        }
    }

    private void startAsrEngine() {
        try {
            DDS.getInstance().getAgent().getASREngine().startListening(new ASREngine.Callback() {
                @Override
                public void beginningOfSpeech() {

                }

                @Override
                public void endOfSpeech() {

                }

                @Override
                public void bufferReceived(byte[] bytes) {

                }

                @Override
                public void partialResults(String s) {
                    AILog.i(TAG, "partialResults:%! " + s);
                }

                @Override
                public void finalResults(String s) {
                    AILog.i(TAG, "finalResults:%! " + s);
                    try {
                        DDS.getInstance().getAgent().getASREngine().stopListening();
                    } catch (DDSNotInitCompleteException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void error(String s) {

                }

                @Override
                public void rmsChanged(float v) {

                }
            });
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
    }

}
