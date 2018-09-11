package com.ai.xiaocai;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;

import com.ai.xiaocai.manager.DoMain;
import com.ai.xiaocai.utils.LogUtils;
import com.ai.xiaocai.utils.NetworkUtil;
import com.hanks.htextview.typer.TyperTextView;

public class XiaocaiService extends Service {
    private static final String TAG = "XC/Service";
    private DoMain mDoMain;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DoMain.MSG_HANDLER_TIPS_NEED_SHOW:
                    mHandler.removeMessages(DoMain.MSG_HANDLER_TTS_COMPLETE);
                    String str = (String) msg.obj;
                    if (isShowTips && str != null && mTtvContent != null) {
                        if (str.length() > 40)
                            str = str.substring(0, 40) + "...";
                        mTtvContent.animateText(str);
                        mAlertDialog.show();
                        mHandler.sendEmptyMessageDelayed(DoMain.MSG_HANDLER_TTS_COMPLETE, 5000);
                    }
                    break;
                case DoMain.MSG_HANDLER_TTS_COMPLETE:
                    mHandler.removeMessages(DoMain.MSG_HANDLER_TTS_COMPLETE);
                    mAlertDialog.dismiss();
                    break;

                case DoMain.MSG_HANDLER_NET_DATA_DEVICE_GET_FAILED:
                    if (mDoMain != null) mDoMain.doNetDeviceID();
                    break;
                case DoMain.MSG_HANDLER_NET_DATA_ONLINE_FAILED:
                    if (mDoMain != null) mDoMain.doNetDeviceOnline();
                    break;
                case DoMain.MSG_HANDLER_NET_DATA_DEVICE_ONLINE:
                    if (mDoMain != null) mDoMain.doNetPeriodWork();
                    break;
                case DoMain.MSG_HANDLER_NET_DATA_DO_PERIOD:
                    if (mDoMain != null) mDoMain.doNetPeriodWork();
                    break;
                case DoMain.MSG_HANDLER_NET_DISABLED:
                    if (mDoMain != null && !NetworkUtil.isWifiConnected(getApplicationContext()))
                        mDoMain.requestTTS("没有找到熟悉的网络，请帮我连网.");
                    break;
                case DoMain.MSG_HANDLER_NET_EVENT_POWEROFF:
                    LogUtils.i("MSG_HANDLER_NET_EVENT_POWEROFF");
                    XiaocaiService.this.sendBroadcast(new Intent("app_shut_down"));
                    break;

                case DoMain.MSG_HANDLER_WLAN_CONFIRM_PASSWORD_FAILED:
                    LogUtils.i("MSG_HANDLER_WLAN_CONFIRM_PASSWORD");
                    if (mDoMain != null && !NetworkUtil.isWifiConnected(getApplicationContext()))
                        mDoMain.requestTTS("对不起，密码错误，请重新配网。。。");
                    break;
                case DoMain.MSG_HANDLER_MESSAGE_MEDIA_LIST:
                    if (mDoMain != null)
                        mDoMain.addToMediaList(msg.obj);
                    break;

                //LXQ 2018.7.27
                case DoMain.MSG_HANDLER_COMMAND_MESSAGE_VIDEO:
                    // initVideoUI();
                    Intent in = new Intent("action_speech_play_new_video");
                    in.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                    sendBroadcast(in);

                    final String videoUrl = (String) msg.obj;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //   playCommandVideo(videoUrl);
                            String extension = MimeTypeMap.getFileExtensionFromUrl(videoUrl);
                            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                            Intent mediaIntent = new Intent(Intent.ACTION_VIEW);
                            mediaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mediaIntent.setDataAndType(Uri.parse(videoUrl), mimeType);
                            startActivity(mediaIntent);
                        }
                    }, 1500);
                    break;
                case DoMain.MSG_HANDLER_COMMAND_MESSAGE_VIDEO_STOP:
                    Intent inte = new Intent("action_speech_play_new_video");
                    inte.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                    sendBroadcast(inte);
                    break;
            }
        }
    };
    private View mView;
    private TyperTextView mTtvContent;
    private AlertDialog mAlertDialog;


    private boolean isShowTips = true;

    private void setTipsShowState(boolean show) {
        if (!show) mAlertDialog.dismiss();
        isShowTips = show;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mDoMain == null) mDoMain = new DoMain(this, mHandler);
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(getApplicationContext(), R.style.AppTheme_Dailog);
        mView = View
                .inflate(this, R.layout.custom_dialog, null);
        mTtvContent = (TyperTextView) mView.findViewById(R.id.ttv_dialog_content);
        mbuilder.setView(mView);
        mAlertDialog = mbuilder.create();
        mAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction("speech_action_weather_request");
        intentFilter.addAction("action_speech_translate_start");
        intentFilter.addAction("action_speech_translate_end");
        intentFilter.addAction("action_update_speech_tone");
        intentFilter.addAction("action_update_speech_wakeup_words");
        intentFilter.addAction("action_speech_chat_recieve");
        intentFilter.addAction("action_speech_chat_speak_start");
        intentFilter.addAction("action_speech_chat_speak_end");
        intentFilter.addAction("action_speech_luancher_music");
        intentFilter.addAction("action_speech_play_pause");
        intentFilter.addAction("action_speech_daogao_on");
        intentFilter.addAction("action_speech_daogao_off");
        intentFilter.addAction("alarm_killed");
        intentFilter.addAction("action_speech_sleep");
        intentFilter.addAction("wifi_ap_opend");
        intentFilter.addAction("action_speech_connect_wifi");
        intentFilter.addAction("wlan_confirm_password");
        intentFilter.addAction("action_speech_send_txt");
        intentFilter.addAction("action_speech_vulome_action_down");
        intentFilter.addAction("action_speech_vulome_action_up");
        intentFilter.addAction("action_speech_pray_empty");
        intentFilter.addAction("com.fota.info.update");
        registerReceiver(mBroadcastReceiver, intentFilter);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        unregisterReceiver(mBroadcastReceiver);
        mDoMain.onDestroy();
        super.onDestroy();
    }

    //+======================================================+//
    private boolean mIsConnected = false;
    private int mStatus = 1;
    private int mLevel = 0;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.d("ACTION = " + intent.getAction());

            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_NEW_STATE, 0);
                LogUtils.v(TAG, "wifiState = " + wifiState);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLING: //0
                        break;
                    case WifiManager.WIFI_STATE_DISABLED://1
                        break;

                    case WifiManager.WIFI_STATE_ENABLING://2
                        break;
                    case WifiManager.WIFI_STATE_ENABLED://3
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN://4
                        break;
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (parcelableExtra != null) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    NetworkInfo.State state = networkInfo.getState();
                    boolean isConnected = state == NetworkInfo.State.CONNECTED;
                    LogUtils.i("isConnected:" + isConnected);
                    if (isConnected && !mIsConnected) {
                        if (mDoMain != null) mDoMain.requestTTS("联网已成功，快来和我聊天吧。");
                    } else if (!isConnected && mIsConnected) {
                        if (mDoMain != null) mDoMain.requestTTS("网络已断开。");
                    }
                    mIsConnected = isConnected;
                }
            } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction())) {

                int linkWifiResult = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                Bundle extras = intent.getExtras();
                SupplicantState wifiState = (SupplicantState) extras.get(WifiManager.EXTRA_NEW_STATE);
                //SupplicantState wifiState = ;
                //intent.getExtra(WifiManager.EXTRA_NEW_STATE, -1);
                LogUtils.i("linkWifiResult = " + linkWifiResult + ",wifiState = " + wifiState);
                if (linkWifiResult == WifiManager.ERROR_AUTHENTICATING) {

                }

            } else if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {

                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                boolean present = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
                LogUtils.v(TAG, "level = " + level + ",present = " + present + ",status = " + status);
                if (status == BatteryManager.BATTERY_STATUS_FULL) {
                    /*if (mStatus != status) {
                        if (mUISpeechControl != null) mUISpeechControl.batteryPowerFull();
                    }*/
                } else if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                    if (level == 100 && mLevel != level) {
                        if (mDoMain != null) mDoMain.requestTTS("电池已充满。");
                    }
                } else {
                    if (level == 20 && mLevel != level) {
                        if (mDoMain != null) mDoMain.requestTTS("剩余电量，百分之二十");
                    } else if (level == 10 && mLevel != level) {
                        if (mDoMain != null) mDoMain.requestTTS("剩余电量，百分之十");
                    } else if (level == 5 && mLevel != level) {
                        if (mDoMain != null) mDoMain.requestTTS("剩余电量，百分之五");
                    } else {
                    }
                }
                mStatus = status;
                mLevel = level;
            } else if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {
                if (mDoMain != null) mDoMain.requestTTS("正在充电");
            } else if (Intent.ACTION_BATTERY_LOW.equals(intent.getAction())) {
                if (mDoMain != null) mDoMain.requestTTS("电池电量低");
            } else if (Intent.ACTION_BATTERY_OKAY.equals(intent.getAction())) {
                if (mDoMain != null) mDoMain.requestTTS("电量已恢复");
            } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {

            } else if ("speech_action_weather_request".equals(intent.getAction())) {
                if (mDoMain != null) mDoMain.sendTXT("今天天气");
            } else if ("action_speech_translate_start".equals(intent.getAction())) {
                //remove 2018.7.14 if (mUISpeechControl != null) mUISpeechControl.translateStart();
            } else if ("action_speech_translate_end".equals(intent.getAction())) {
                //remove 2018.7.14 //if (mUISpeechControl != null) mUISpeechControl.translateEnd();
            } else if ("action_update_speech_tone".equals(intent.getAction())) {
                //int speechTone = Settings.System.getInt(getContentResolver(), "speech_tone", 0);
                //LogUtils.v(TAG, "speechTone = " + speechTone);
                //FIXME modify speaker
            } else if ("action_speech_chat_recieve".equals(intent.getAction())) {
                //if (mUISpeechControl != null) mUISpeechControl.playVoiceMessage();
                if (mDoMain != null) mDoMain.sendTXT("头条新闻");
            } else if ("action_speech_chat_speak_start".equals(intent.getAction())) {
                //if (mUISpeechControl != null) mUISpeechControl.voiceSpeakStart();
            } else if ("action_speech_chat_speak_end".equals(intent.getAction())) {
                //if (mUISpeechControl != null)mUISpeechControl.voiceSpeakEnd();
            } else if ("action_speech_play_pause".equals(intent.getAction())) {
                if (mDoMain != null)
                    mDoMain.stopAllPlayer();
            } else if ("action_speech_sleep".equals(intent.getAction())) {
                if (mDoMain != null)
                    mDoMain.changeWakeState();
            } else if ("wifi_ap_opend".equals(intent.getAction())) {
                if (mDoMain != null) mDoMain.requestTTS("热点已开启，请用手机帮我配网吧。");
            } else if ("action_speech_connect_wifi".equals(intent.getAction())) {
                if (mDoMain != null) mDoMain.requestTTS("正在开启配网功能，请稍后。。。");
            } else if ("wlan_confirm_password".equals(intent.getAction())) {
                mHandler.sendEmptyMessageDelayed(mDoMain.MSG_HANDLER_WLAN_CONFIRM_PASSWORD_FAILED, 10000);
            } else if ("action_speech_send_txt".equals(intent.getAction())) {
                if (mDoMain != null) mDoMain.sendTXT("播放音乐");
            } else if ("action_speech_vulome_action_down".equals(intent.getAction())) {
                if (mDoMain != null) mDoMain.setVolumedown();
            } else if ("action_speech_vulome_action_up".equals(intent.getAction())) {
                if (mDoMain != null) mDoMain.setVolumeUp();
            } else if ("action_speech_pray_empty".equals(intent.getAction())) {
                if (mDoMain != null) mDoMain.requestTTS("没有找到灵修资源，请插入SD卡或连接网络。");
            } else if ("com.fota.info.update".equals(intent.getAction())) {
                if (mDoMain != null) mDoMain.requestTTS("更新已可以用，请重启设备更新，更新时电量需大于30%。");
            } else if ("action_speech_daogao_on".equals(intent.getAction())) {
                setTipsShowState(false);
                if (mDoMain != null) mDoMain.setNetPlayState(false);
            } else if ("action_speech_daogao_off".equals(intent.getAction())) {
                setTipsShowState(true);
                if (mDoMain != null) mDoMain.setNetPlayState(true);
            }
            //action_speech_play_pauseing
        }
    };

}
