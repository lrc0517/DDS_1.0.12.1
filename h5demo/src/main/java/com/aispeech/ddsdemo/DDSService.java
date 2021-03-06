package com.aispeech.ddsdemo;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.aispeech.dui.dds.DDS;
import com.aispeech.dui.dds.DDSAuthListener;
import com.aispeech.dui.dds.DDSConfig;
import com.aispeech.dui.dds.DDSInitListener;
import com.aispeech.dui.dds.agent.MessageObserver;
import com.aispeech.dui.dds.auth.AuthType;
import com.aispeech.dui.dds.exceptions.DDSNotInitCompleteException;
import com.aispeech.dui.dsk.duiwidget.CommandObserver;
import com.aispeech.dui.dsk.duiwidget.NativeApiObserver;
import com.aispeech.dui.dsk.duiwidget.TextWidget;

import org.json.JSONObject;

public class DDSService extends Service {
    public static final String TAG = "DDSService";

    public DDSService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();
        return super.onStartCommand(intent, flags, startId);
    }


    private void init() {

        DDS.getInstance().setDebugMode(2);//在调试时可以打开sdk调试日志，在发布版本时，请关闭
        Log.d(TAG, "Start DDS");
        DDS.getInstance().init(getApplicationContext(), createConfig(), new DDSInitListener() {
            @Override
            public void onInitComplete(boolean isFull) {
                Log.d(TAG, "onInitComplete");
                if (isFull) {
                    sendBroadcast(new Intent("ddsdemo.intent.action.init_complete"));
                    try {
                        //开唤醒，调用后才能唤醒
                        DDS.getInstance().getAgent().getWakeupEngine().enableWakeup();
                    } catch (DDSNotInitCompleteException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(int what, final String msg) {
                Log.e(TAG, "Init onError: " + what + ", error: " + msg);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }, new DDSAuthListener() {
            @Override
            public void onAuthSuccess() {
                Log.d(TAG, "onAuthSuccess");
                sendBroadcast(new Intent("ddsdemo.intent.action.auth_success"));
            }

            @Override
            public void onAuthFailed(final String errId, final String error) {
                Log.e(TAG, "onAuthFailed: " + errId + ", error:" + error);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "授权错误:" + errId + ":\n" + error + "\n请查看手册处理", Toast
                                .LENGTH_SHORT).show();
                    }
                });
                sendBroadcast(new Intent("ddsdemo.intent.action.auth_failed"));
            }
        });


        DDS.getInstance().getAgent().subscribe("sys.dialog.start", new MessageObserver() {
            @Override
            public void onMessage(String message, String data) {
                if ("sys.dialog.start".equals(message)) {
                }
            }
        });

        // 注册command,这个command需要在dui平台的技能上添加
        DDS.getInstance().getAgent().subscribe(new String[]{"open_window"}, commandObserver);
        // 注销
//        DDS.getInstance().getAgent().unSubscribe(commandObserver);

        // 注册本地api，这个本地api也需要在对平台的技能上添加
        DDS.getInstance().getAgent().subscribe(new String[]{"query_battery"}, nativeApiObserver);
        // 注销
//        DDS.getInstance().getAgent().unSubscribe(nativeApiObserver);


    }


    private NativeApiObserver nativeApiObserver = new NativeApiObserver() {
        @Override
        public void onQuery(final String nativeApi, final String data) {
            if (nativeApi.equals("query_battery")) {
                try {
                    JSONObject jsonData = new JSONObject(data);
                    String intentName = jsonData.optString("intentName");
                    // 执行查询电量操作
                    String battery = "电量剩余10%";
                    DDS.getInstance().getAgent().feedbackNativeApiResult(nativeApi, new TextWidget().setText(battery));
                } catch (Exception e) {

                }
            }
        }
    };

    private CommandObserver commandObserver = new CommandObserver() {
        @Override
        public void onCall(final String command, final String data) {
            if (command.equals("open_the_window")) {
                try {
                    JSONObject jsonData = new JSONObject(data);
                    String intentName = jsonData.optString("intentName");
                    String w = jsonData.optString("w");
                    // TODO 依据w的值，执行打开窗户操作
                } catch (Exception e) {

                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        DDS.getInstance().getAgent().unSubscribe(commandObserver);
        DDS.getInstance().getAgent().unSubscribe(nativeApiObserver);
        DDS.getInstance().release();
        System.exit(0);
    }

    private DDSConfig createConfig() {

        DDSConfig config = new DDSConfig();


        config.addConfig(DDSConfig.K_PRODUCT_ID, "278573265");  // TODO 填写自己的产品ID
        config.addConfig(DDSConfig.K_USER_ID, "2874863675@qq.com");  // TODO 填写真是的用户ID
        config.addConfig(DDSConfig.K_ALIAS_KEY, "xiaocai");   // TODO 填写产品的发布分支
        config.addConfig(DDSConfig.K_AUTH_TYPE, AuthType.PROFILE);
        //bf031387a13bbf031387a13b5b3b27c1
        config.addConfig(DDSConfig.K_API_KEY, "bf031387a13bbf031387a13b5b3b27c1");  // TODO 填写API KEY
        config.addConfig(DDSConfig.K_DUICORE_ZIP, "duicore.zip");
        config.addConfig(DDSConfig.K_CUSTOM_ZIP, "product.zip");
       // config.addConfig(DDSConfig.K_USE_UPDATE_NOTIFICATION, "false");//系统更新通知
      //  config.addConfig(DDSConfig.K_USE_UPDATE_DUICORE, "false");//热更新
        config.addConfig(DDSConfig.K_MIC_TYPE, "5");
        config.addConfig(DDSConfig.K_AEC_MODE, "external");

        //TTS
        config.addConfig(DDSConfig.K_STREAM_TYPE, AudioManager.STREAM_MUSIC);//
        //ASR
        config.addConfig(DDSConfig.K_ASR_ENABLE_PUNCTUATION, "true");//识别标点
        config.addConfig(DDSConfig.K_ASR_ENABLE_TONE, "true");//识别声调
        Log.i(TAG, "config->" + config.toString());
        return config;
    }


}