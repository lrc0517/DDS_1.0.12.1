package com.ai.xiaocai.manager;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.text.TextUtils;

import com.ai.xiaocai.R;
import com.ai.xiaocai.number.utils.ReSmartContants;
import com.ai.xiaocai.number.utils.SmartUtil;
import com.ai.xiaocai.utils.LogUtils;
import com.aispeech.dui.dds.DDS;
import com.aispeech.dui.dds.DDSAuthListener;
import com.aispeech.dui.dds.DDSConfig;
import com.aispeech.dui.dds.DDSInitListener;
import com.aispeech.dui.dds.agent.MessageObserver;
import com.aispeech.dui.dds.agent.TTSEngine;
import com.aispeech.dui.dds.auth.AuthType;
import com.aispeech.dui.dds.exceptions.DDSNotInitCompleteException;
import com.aispeech.dui.dsk.duiwidget.CommandObserver;
import com.aispeech.dui.dsk.duiwidget.NativeApiObserver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Lucien on 2018/8/22.
 */

public class DDSManager {



    public interface DDSListener {

        void onShowTips(String tips);

        void onTTSComplete(boolean isMediaNameTTS, boolean needContinueTTS);

        void onNativeResult(String nativeApi, String data);

        void onCommandResult(String command, String data);

        void onMessageResult(String message, String data);

        void onWakeUp();

        void onWakeUpDisabled();

    }

    private static final String TAG = "XC/DDS";
    private Handler mHandler;
    private Context mContext;
    private final AudioManager mAm;
    private boolean isAuthed = false;
    private final DDSListener mDDSListener;

    public DDSManager(Context context, Handler handler,DDSListener l) {
        this.mContext = context;
        this.mHandler = handler;
        this.mDDSListener = l;
        mAm = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        init();
    }

    public void doAuth() {
        try {
            DDS.getInstance().doAuth();
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
    }

    private String mDeviceID;

    public String getDeviceId() {
        if (mDeviceID == null)
            mDeviceID = SmartUtil.getMac();
        return mDeviceID;
    }

    private boolean mIsMediaNameTTS = false;
    private boolean mNeedContinueTTS = false;
    public void doLocalTTS(String txt, boolean isMediaName,boolean isNeedContinue) {
        mIsMediaNameTTS = isMediaName;
        mNeedContinueTTS = isNeedContinue;
        if (txt == null) return;
        try {
            DDS.getInstance().getAgent().getTTSEngine().shutup("100");
            DDS.getInstance().getAgent().getTTSEngine().speak(txt, 1, "100", AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
            if (mDDSListener != null) mDDSListener.onShowTips(txt);
            postChatInfo(false, getDeviceId(), txt);
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
    }

    public void doSdsTXT(String s) {
        try {
            DDS.getInstance().getAgent().sendText(s);
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
    }

    private boolean mDataState = true;
    public void setDataState(boolean b) {
        mDataState = b;
    }


    private boolean isSleep = false;
    private boolean isChanging = false;

    public void changeWakeState() {
        if (isChanging) {
            return;
        }
        isChanging = true;
        try {
            isSleep = !isSleep;
            final String[] sleepTip = mContext.getResources().getStringArray(isSleep ? R.array.sleep_tips : R.array.wakeup_tips);
            final int num = (int) (Math.random() * sleepTip.length);
            DDS.getInstance().getAgent().avatarClick(" ");
            DDS.getInstance().getAgent().stopDialog();
            final String tts = sleepTip[num];
            if (isSleep) {
                DDS.getInstance().getAgent().getWakeupEngine().disableWakeup();
                if (mDDSListener!=null)mDDSListener.onWakeUpDisabled();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doLocalTTS(tts, false,false);
                    }
                }, 1000);
            } else {
                DDS.getInstance().getAgent().getWakeupEngine().enableWakeup();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doLocalTTS(tts, false,true);
                    }
                }, 1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.w(TAG, "changeWakeState Error", e);
            isChanging = false;
        }
    }

    public void changeToRestState() {
        try {
            isSleep = true;
            DDS.getInstance().getAgent().getWakeupEngine().disableWakeup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void cancel() {
        isChanging = false;
        try {
            requestFocus();
            DDS.getInstance().getAgent().getTTSEngine().shutup("100");
            DDS.getInstance().getAgent().stopDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDestory() {
        DDS.getInstance().getAgent().unSubscribe(commandObserver);
        DDS.getInstance().getAgent().unSubscribe(nativeApiObserver);
        DDS.getInstance().getAgent().unSubscribe(messageObserver);
        DDS.getInstance().release();
        System.exit(0);
    }


    private void init() {
        DDS.getInstance().init(mContext.getApplicationContext(), createConfig(), new DDSInitListener() {
            @Override
            public void onInitComplete(boolean isFull) {
                LogUtils.d(TAG, "onInitComplete isFull = " + isFull);
                doAuth();
                if (isFull) {
                    try {
                        mHandler.sendEmptyMessageDelayed(DoMain.MSG_HANDLER_NET_DISABLED, 20000);
                        DDS.getInstance().getAgent().getTTSEngine().setSpeaker("qianranc");
                        doLocalTTS("我已经开机了", false,false);
                        DDS.getInstance().getAgent().getWakeupEngine().enableWakeup();
                        DDS.getInstance().getAgent().getTTSEngine().setListener(mTtsListener);
                       /* DDS.getInstance().getAgent().getWakeupEngine().updateCommandWakeupWord(
                                new String[]{"打开灯效", "关闭灯效","打开投影","关闭投影",
                                        "我要灵修", "我要祷告","开始灵修","开始祷告",
                                        "结束灵修", "结束祷告","灵修结束","祷告结束"
                                },//1.actions 命令唤醒词指令, 为string数组, 不为null
                                new String[]{"打开灯效","关闭灯效","打开投影","关闭投影",
                                        "我要灵修", "我要祷告","开始灵修","开始祷告",
                                        "结束灵修", "结束祷告","灵修结束","祷告结束"
                                },//2.words 命令唤醒词, 为string数组, 不为null
                                new String[]{"da kai deng xiao",
                                        "guan bi deng xiao",
                                        "da kai tou ying",
                                        "guan bi tou ying",
                                        "wo yao ling xiu",
                                        "wo yao dao gao",
                                        "kai shi ling xiu",
                                        "kai shi dao gao",
                                        "jie shu ling xiu",
                                        "jie shu dao gao",
                                        "ling xiu jie shu",
                                        "dao gao jie shu"
                                },//3.pinyins 命令唤醒词的拼音, 形如：ni hao xiao chi, 为string数组, 不为null
                                new String[]{"0.25","0.25","0.25","0.25","0.25","0.25","0.25","0.25","0.25","0.25","0.25","0.25"},//4.thresholds 命令唤醒词的阈值, 形如：0.120(取值范围：0-1)。为string数组, 不为null
                                new String[][]{{"好的"},{"好的"},{"好的"},{"好的"},
                                        {""},{""},{""},{""},
                                        {""},{""},{""},{""}
                                }//5.greetings 命令唤醒词的欢迎语, 为string二维数组, 不为null, 每维对应一个唤醒词的欢迎语
                        );*/

                      /*  DDS.getInstance().getAgent().getWakeupEngine().updateShortcutWakeupWord(
                                new String[]{"再见","拜拜","休息吧"},//1.words 打断唤醒词, 为string数组, 不为null
                                new String[]{"zai jian","bai bai","xiu xi ba"},//2.pinyins 打断唤醒词的拼音, 形如：ni hao xiao chi, 为string数组, 不为null
                                new String[]{"0.08","0.08","0.08"}//3.thresholds 打断唤醒词的阈值, 形如：0.120(取值范围：0-1) 为string数组, 不为null
                        );*/
                    } catch (DDSNotInitCompleteException e) {
                        LogUtils.w(TAG, "onInitComplete Error", e);
                        init();
                    }
                } else {
                    DDS.getInstance().release();
                    init();
                }
            }

            @Override
            public void onError(int what, String msg) {
                LogUtils.i("on init Error what = " + what + ",msg = " + msg);
                init();
            }
        }, new DDSAuthListener() {
            @Override
            public void onAuthSuccess() {
                LogUtils.d(TAG, "onAuthSuccess");
            }

            @Override
            public void onAuthFailed(String errId, String error) {
                LogUtils.w("DDS onAuthFailed: " + errId + ", error:" + error);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doAuth();
                    }
                },5000);

            }
        });

        DDS.getInstance().getAgent().subscribe(new String[]{
                "sys.dialog.start",
                "sys.dialog.end",
                "sys.wakeup.result",
                "context.input.text",
                "context.output.text",
                "context.widget.media",
                "context.widget.custom",
                "context.widget.web",
                "context.widget.content",
                "context.widget.list"
        }, messageObserver);


        DDS.getInstance().getAgent().subscribe(new String[]{
                "open_window",
                "device_help",
                "device_light",
                "device_shadow",
                "device_battery",
                "device_rest",
                "local_music_play",
                "local_music_pray",
                "local_music_pray_search",
                "online_music_pray_search",
                "local_music_control",
                "custom_media_video",
                "alarm_localhook_local_alarm_delete",
                "alarm.query.action",
                "Timer"
        }, commandObserver);


        DDS.getInstance().getAgent().subscribe(new String[]{
                "query_battery",
                "alarm.set",
                "alarm.list",
                "alarm.delete",
                /*闹钟*/
                "alarm_localhook_local_alarm_add_single",
                "alarm_localhook_local_add_time",
                "alarm_localhook_local_alarm_add_repeat",

                "alarm_localhook_local_alarm_add_timer",
                "alarm_localhook_local_alarm_add_timer_by_action",
                "settings.volume.inc",
                "settings.volume.dec",
                "settings.volume.set",
                "settings.mutemode.open",
                "settings.wifi.open",
                "settings.wifi.close"
        }, nativeApiObserver);
    }

    private DDSConfig createConfig() {

        DDSConfig config = new DDSConfig();

        // 基础配置项
        config.addConfig(DDSConfig.K_PRODUCT_ID, "278573265"); // 产品ID
        config.addConfig(DDSConfig.K_USER_ID, "2874863675@qq.com");  // 用户ID
        config.addConfig(DDSConfig.K_ALIAS_KEY, "prod");   // 产品的发布分支
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
        config.addConfig("CLOSE_TIPS", "true");
        LogUtils.d("config->" + config.toString());
        return config;
    }

    //+===============================================================+//
    private boolean requestFocus() {
        // Request audio focus for playback
        int result = mAm.requestAudioFocus(afChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                //Pause playback
                //mediaPlayer.pause();
                try {
                    DDS.getInstance().getAgent().getTTSEngine().shutup("100");
                } catch (DDSNotInitCompleteException e) {
                    e.printStackTrace();
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                //Resume playback
                // mediaPlayer.resume ();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // mAm.unregisterMediaButtonEventReceiver(RemoteControlReceiver);
                mAm.abandonAudioFocus(afChangeListener);
                try {
                    DDS.getInstance().getAgent().getTTSEngine().shutup("100");
                } catch (DDSNotInitCompleteException e) {
                    e.printStackTrace();
                }
                //Stop playback
                // mediaPlayer.stop();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {

            }
        }
    };

    //+===============================================================+//

    private NativeApiObserver nativeApiObserver = new NativeApiObserver() {
        @Override
        public void onQuery(final String nativeApi, final String data) {
            mDDSListener.onNativeResult(nativeApi, data);
        }
    };

    private CommandObserver commandObserver = new CommandObserver() {
        @Override
        public void onCall(final String command, final String data) {
            mDDSListener.onCommandResult(command, data);
        }
    };

    private MessageObserver messageObserver = new MessageObserver() {
        @Override
        public void onMessage(String message, String data) {
            LogUtils.i("message = " + message + ",data = " + data);
            if ("context.input.text".equals(message)) {
                try {
                    if (data.contains("text")) {
                        JSONObject jsonObject = new JSONObject(data);
                        String text = jsonObject.getString("text");
                        postChatInfo(true, getDeviceId(), text);
                        String type = jsonObject.getString("type");
                        if ("command".equals(type)) {
                            doSdsTXT(text);
                        }
                    }
                    //FIXME post to net server
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if ("context.output.text".equals(message)) {
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    String text = jsonObject.getString("text");
                    postChatInfo(false, getDeviceId(), text);
                    //FIXME post to net server
                    if (text != null && !TextUtils.isEmpty(text)) {
                        mDDSListener.onShowTips(text);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if ("sys.wakeup.result".equals(message)) {
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    String greeting = jsonObject.getString("greeting");
                    if (requestFocus()) {
                        // mIsWakesound = true;
                        // doLocalTTS(greeting);
                        DDS.getInstance().getAgent().avatarClick(greeting);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtils.w(TAG, "Wake error", e);
                }
            } else if ("sys.dialog.start".equals(message)) {
                mHandler.removeMessages(DoMain.MSG_HANDLER_MESSAGE_MEDIA_LIST);
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    String reason = jsonObject.getString("reason");
                    if ("wakeup.major".equals(reason)) {
                        requestFocus();
                        if (mDDSListener != null) mDDSListener.onWakeUp();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }  else {
                mDDSListener.onMessageResult(message, data);
            }
        }
    };


    private TTSEngine.Callback mTtsListener = new TTSEngine.Callback() {
        @Override
        public void beginning(String s) {

        }

        @Override
        public void received(byte[] bytes) {

        }

        @Override
        public void end(String s, int i) {
            LogUtils.v(TAG, "end s = " + s + ",i = " + i);
            if (mDDSListener!=null)mDDSListener.onTTSComplete(mIsMediaNameTTS,mNeedContinueTTS);
            mIsMediaNameTTS = false;
            mNeedContinueTTS = false;
            isChanging = false;
        }

        @Override
        public void error(String s) {
            mIsMediaNameTTS = false;
            isChanging = false;
        }
    };

    private static OkHttpClient okHttpClient;
    public void postChatInfo(boolean isInput, String deviceID, String content) {

        LogUtils.v(TAG, "isInput " + isInput);
        LogUtils.v(TAG, "deviceID " + deviceID);
        LogUtils.v(TAG, "content " + content);

        if (deviceID == null || content == null || TextUtils.isEmpty(content)) {
            return;
        }

        if (okHttpClient == null) okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        //MediaType  设置Content-Type 标头中包含的媒体类型值
        FormBody formBody = new FormBody.Builder()
                .add("equip", deviceID)
                .add("content", content)
                .add("status", isInput ? "3" : "2")
                .build();

        Request request = new Request.Builder()
                .url(ReSmartContants.URL + ReSmartContants.URL_CHAT)//请求的url
                .post(formBody)
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(mOkHttpCallBacl);
    }

    private static Callback mOkHttpCallBacl = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            LogUtils.w("post Chat onFailure " + e);
            e.printStackTrace();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            LogUtils.v("post Chat response = " + response.body().string());
        }
    };

}
