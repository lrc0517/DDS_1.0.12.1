package com.aispeech.dui.dds.demo;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaRecorder.AudioSource;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.ai.xiaocai.R;
import com.aispeech.dui.dds.DDS;
import com.aispeech.dui.dds.DDSAuthListener;
import com.aispeech.dui.dds.DDSConfig;
import com.aispeech.dui.dds.DDSInitListener;
import com.aispeech.dui.dds.agent.MessageObserver;
import com.aispeech.dui.dds.auth.AuthType;
import com.aispeech.dui.dds.demo.observer.DuiCommandObserver;
import com.aispeech.dui.dds.demo.observer.DuiNativeApiObserver;
import com.aispeech.dui.dds.exceptions.DDSNotInitCompleteException;
import com.aispeech.dui.dsk.duiwidget.CommandObserver;
import com.aispeech.dui.dsk.duiwidget.NativeApiObserver;
import com.aispeech.dui.dsk.duiwidget.TextWidget;

import org.json.JSONObject;

/*
*
* 参见Android SDK集成文档: https://www.dui.ai/docs/operation/#/ct_common_Andriod_SDK
*/
public class DDSService extends Service {
    public static final String TAG = "DDSService";

    private static String[] commands;
    private static String[] apis;
    private static String[] messages;

    private DuiCommandObserver mCommandObserver;
    private DuiNativeApiObserver mApiObserver;

    public DDSService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        commands = getResources().getStringArray(R.array.demo_actions);
        apis = getResources().getStringArray(R.array.demo_apis);
        messages = getResources().getStringArray(R.array.demo_messages);
        mCommandObserver = new DuiCommandObserver(getApplicationContext());
        mApiObserver = new DuiNativeApiObserver(getApplication());
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();
        return super.onStartCommand(intent, flags, startId);
    }

    private DDSInitListener mInitListener = new DDSInitListener() {
        @Override
        public void onInitComplete(boolean isFull) {
            Log.d(TAG, "onInitComplete");
            if (isFull) {
                sendBroadcast(new Intent("ddsdemo.intent.action.init_complete"));
                try {
                    // 注册CommandObserver,用于处理DUI平台技能配置里的客户端动作指令, 同一个CommandObserver可以处理多个commands.
                    DDS.getInstance().getAgent().subscribe(commands, mCommandObserver);
                    // 注册NativeApiObserver, 用于客户端响应DUI平台技能配置里的资源调用指令, 同一个NativeApiObserver可以处理多个native api.
                    DDS.getInstance().getAgent().subscribe(apis, mApiObserver);
                    // 开唤醒，调用后才能唤醒
                    DDS.getInstance().getAgent().getWakeupEngine().enableWakeup();
                } catch (DDSNotInitCompleteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onError(int what, final  String msg) {
            Log.e(TAG, "Init onError: " + what + ", error: " + msg);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private DDSAuthListener  mAuthListener = new DDSAuthListener() {
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
                    Toast.makeText(getApplicationContext(),
                            "授权错误:" + errId + ":\n" + error + "\n请查看手册处理", Toast.LENGTH_SHORT).show();
                }
            });
            sendBroadcast(new Intent("ddsdemo.intent.action.auth_failed"));
        }
    };

    private void init() {
        DDS.getInstance().setDebugMode(2); //在调试时可以打开sdk调试日志，在发布版本时，请关闭
        Log.d(TAG, "Start DDS");
        DDS.getInstance().init(getApplicationContext(), createConfig(), mInitListener, mAuthListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DDS.getInstance().getAgent().unSubscribe(mCommandObserver);
        DDS.getInstance().getAgent().unSubscribe(mApiObserver);
        DDS.getInstance().release();
    }

    private DDSConfig createConfig() {

        DDSConfig config = new DDSConfig();

        // 基础配置项
        config.addConfig(DDSConfig.K_PRODUCT_ID, "278573265"); // 产品ID
        config.addConfig(DDSConfig.K_USER_ID, "2874863675@qq.com");  // 用户ID
        config.addConfig(DDSConfig.K_ALIAS_KEY, "xiaocai");   // 产品的发布分支
        config.addConfig(DDSConfig.K_AUTH_TYPE, AuthType.PROFILE); //授权方式, 支持思必驰账号授权和profile文件授权
        config.addConfig(DDSConfig.K_API_KEY, "bf031387a13bbf031387a13b5b3b27c1");  // 产品授权秘钥，服务端生成，用于产品授权
        //config.addConfig("MINIMUM_STORAGE", (long)  200 * 1024 * 1024); // SDK需要的最小存储空间的配置，对于/data分区较小的机器可以配置此项，同时需要把内核资源放在其他位置

//        // 资源更新配置项
//        // 参考可选内置资源包文档: https://www.dui.ai/docs/operation/#/ct_ziyuan
        config.addConfig(DDSConfig.K_DUICORE_ZIP, "duicore.zip"); // 预置在指定目录下的DUI内核资源包名, 避免在线下载内核消耗流量, 推荐使用
        config.addConfig(DDSConfig.K_CUSTOM_ZIP, "product.zip"); // 预置在指定目录下的DUI产品配置资源包名, 避免在线下载产品配置消耗流量, 推荐使用
        config.addConfig(DDSConfig.K_USE_UPDATE_DUICORE, "false"); //设置为false可以关闭dui内核的热更新功能，可以配合内置dui内核资源使用
        config.addConfig(DDSConfig.K_USE_UPDATE_NOTIFICATION, "false"); // 是否使用内置的资源更新通知栏
//
//        // 录音配置项
//        config.addConfig(DDSConfig.K_RECORDER_MODE, "internal"); //录音机模式：external（使用外置录音机，需主动调用拾音接口）、internal（使用内置录音机，DDS自动录音）
//        //config.addConfig(DDSConfig.K_IS_REVERSE_AUDIO_CHANNEL, "false"); // 录音机通道是否反转，默认不反转
//        //config.addConfig(DDSConfig.K_AUDIO_SOURCE, AudioSource.DEFAULT);
//        //config.addConfig(DDSConfig.K_AUDIO_BUFFER_SIZE, (16000 * 1 * 16 * 100 / 1000));
//
//        // TTS配置项
        config.addConfig(DDSConfig.K_STREAM_TYPE, AudioManager.STREAM_MUSIC); // 内置播放器的STREAM类型
//        config.addConfig(DDSConfig.K_TTS_MODE, "internal"); // TTS模式：external（使用外置TTS引擎，需主动注册TTS请求监听器）、internal（使用内置DUI TTS引擎）
//        config.addConfig(DDSConfig.K_CUSTOM_TIPS, "{\"71304\":\"请讲话\",\"71305\":\"不知道你在说什么\",\"71308\":\"咱俩还是聊聊天吧\"}"); // 指定对话错误码的TTS播报。若未指定，则使用产品配置。
//
//        //唤醒配置项
//        config.addConfig(DDSConfig.K_WAKEUP_ROUTER, "dialog"); //唤醒路由：partner（将唤醒结果传递给partner，不会主动进入对话）、dialog（将唤醒结果传递给dui，会主动进入对话）
//
//        //识别配置项
//        config.addConfig(DDSConfig.K_AUDIO_COMPRESS, "false"); //是否开启识别音频压缩
        config.addConfig(DDSConfig.K_ASR_ENABLE_PUNCTUATION, "false"); //识别是否开启标点
//        config.addConfig(DDSConfig.K_ASR_ROUTER, "dialog"); //识别路由：partner（将识别结果传递给partner，不会主动进入语义）、dialog（将识别结果传递给dui，会主动进入语义）
//        config.addConfig(DDSConfig.K_VAD_TIMEOUT, 5000); // VAD静音检测超时时间，默认8000毫秒
        config.addConfig(DDSConfig.K_ASR_ENABLE_TONE, "true"); // 识别结果的拼音是否带音调
//
//        // 调试配置项
//        config.addConfig(DDSConfig.K_WAKEUP_DEBUG, "true"); // 用于唤醒音频调试, 开启后在 "/sdcard/Android/data/包名/cache" 目录下会生成唤醒音频
//        config.addConfig(DDSConfig.K_VAD_DEBUG, "true"); // 用于过vad的音频调试, 开启后在 "/sdcard/Android/data/包名/cache" 目录下会生成过vad的音频
//        config.addConfig("ASR_DEBUG", "true"); // 用于识别音频调试, 开启后在 "/sdcard/Android/data/包名/cache" 目录下会生成识别音频
//        config.addConfig("TTS_DEBUG", "true");  // 用于tts音频调试, 开启后在 "/sdcard/Android/data/包名/cache/tts/" 目录下会自动生成tts音频

        config.addConfig(DDSConfig.K_MIC_TYPE, "5");
        config.addConfig(DDSConfig.K_AEC_MODE, "external");
        Log.i(TAG, "config->" + config.toString());
        return config;
    }


}
