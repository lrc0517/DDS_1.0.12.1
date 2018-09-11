package com.ai.xiaocai.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.ai.xiaocai.number.manager.WeChatDataManager;
import com.ai.xiaocai.utils.LogUtils;

/**
 * Created by Lucien on 2018/8/22.
 */

public class DoMain {
    private static final String TAG = "XC/DM";
    public static final int MSG_HANDLER_AUTH_FAILED = 0;
    public static final int MSG_HANDLER_UPDATA_OUT_STR = 1;
    public static final int MSG_HANDLER_UPDATA_TIPS = 2;
    public static final int MSG_HANDLER_TIPS_NEED_SHOW = 3;
    public static final int MSG_HANDLER_WAKE_UP_READY = 4;
    public static final int MSG_HANDLER_TTS_COMPLETE = 5;
    public static final int MSG_HANDLER_COMMAND_MESSAGE_VIDEO = 6;
    public static final int MSG_HANDLER_WAKE_UP_SUCCESS = 7;
    public static final int MSG_HANDLER_COMMAND_MESSAGE_VIDEO_STOP = 8;
    public static final int MSG_HANDLER_MESSAGE_MEDIA_LIST = 9;
    public static final int MSG_HANDLER_NET_DATA_DEVICE_GET_FAILED = 10;
    public static final int MSG_HANDLER_NET_DATA_AUTH_FAILED = 11;
    public static final int MSG_HANDLER_NET_DATA_ONLINE_FAILED = 12;
    public static final int MSG_HANDLER_NET_DATA_DEVICE_ONLINE = 13;
    public static final int MSG_HANDLER_NET_DATA_DO_PERIOD = 14;
    public static final int MSG_HANDLER_NET_EVENT_POWEROFF = 15;

    public static final int MSG_HANDLER_NET_DISABLED = 20;
    public static final int MSG_HANDLER_DDS_INIT_COMPLETE = 100;
    public static final int MSG_HANDLER_WLAN_CONFIRM_PASSWORD_FAILED = 400;

    private final DDSResultManager mAIResultManager;
    private final ReminderManager mReminderManager;
    // private  RecordManager mRecordManager;
    private final WeChatDataManager mWeChatDataManager;
    private final Context mContext;
    private final Handler mHandler;
    private DDSManager mDDSManager;

    public DoMain(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        mDDSManager = new DDSManager(context, handler, mDDSListener);

        mAIResultManager = new DDSResultManager(context, handler, mResultManagerListener);
        mReminderManager = new ReminderManager(context);

        // mRecordManager = new RecordManager(context);

        mWeChatDataManager = new WeChatDataManager(context, handler, mWeChatDateListener);

    }

    private DDSManager.DDSListener mDDSListener = new DDSManager.DDSListener() {

        @Override
        public void onShowTips(String tips) {
            Message msg = Message.obtain();
            msg.what = MSG_HANDLER_TIPS_NEED_SHOW;
            msg.obj = tips;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onTTSComplete(boolean isMediaNameTTS,boolean needContinue) {
            LogUtils.d(TAG,"onTTSComplete = "+ isMediaNameTTS+",needContinue = "+needContinue);
            //  mHandler.sendEmptyMessage(MSG_HANDLER_TTS_COMPLETE);
            if (isMediaNameTTS) {
                playMediaData();
            }else {
                mAIResultManager.onTTSend(needContinue);
            }
        }

        @Override
        public void onNativeResult(String nativeApi, String data) {
            try {
                if (mAIResultManager != null) mAIResultManager.setNativeResult(nativeApi, data);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.w(TAG, "onNativeResult", e);
            }
        }

        @Override
        public void onCommandResult(String command, String data) {
            try {
                if (mAIResultManager != null) mAIResultManager.setCommandResult(command, data);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.w(TAG, "onCommandResult", e);
            }
        }

        @Override
        public void onMessageResult(String message, String data) {
            try {
                if (mAIResultManager != null) mAIResultManager.setMessageResult(message, data);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.w(TAG, "onMessageResult", e);
            }
        }

        @Override
        public void onWakeUp() {
            mHandler.sendEmptyMessage(MSG_HANDLER_WAKE_UP_SUCCESS);
            if (mAIResultManager != null) mAIResultManager.pauseMusic();
        }

        @Override
        public void onWakeUpDisabled() {
            if (mAIResultManager != null) mAIResultManager.stop();
            mContext.sendBroadcast(new Intent("action_speech_pray_end"));
        }
    };

    private DDSResultManager.ResultCallBack mResultManagerListener = new DDSResultManager.ResultCallBack() {
        @Override
        public void onCommandCallBack(String command, String strBack) {
            requestTTS(strBack);
        }

        @Override
        public void onCommandPray(boolean pray) {
            if (pray) {
                mContext.sendBroadcast(new Intent("action_speech_pray_start"));
            } else {
                mContext.sendBroadcast(new Intent("action_speech_pray_end"));
            }
        }

        @Override
        public void onCommandMusic(int i) {
            if (mDataState) {
                mAIResultManager.doMusicCommand(i);
            } else {
                if (i == 3) {
                    mContext.sendBroadcast(new Intent("action_speech_music_pre"));
                } else if (i == 4) {
                    mContext.sendBroadcast(new Intent("action_speech_music_next"));
                }
            }
        }

        @Override
        public void onCommandVideo(String linkUrl) {
            Message obtain = Message.obtain();
            obtain.what = MSG_HANDLER_COMMAND_MESSAGE_VIDEO;
            obtain.obj = linkUrl;
            mHandler.sendMessage(obtain);
        }

        @Override
        public void onCommandRest() {
            //if (mDDSManager != null) mDDSManager.changeToRestState();
            Intent intent = new Intent("action_speech_sleep");
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            mContext.sendBroadcast(intent);
        }


        @Override
        public void onMusicControlError(int id) {
            requestTTS("对不起，暂无播放列表。。");
        }

        @Override
        public void onCommandVolume(String volumeTips) {
            requestTTS(volumeTips,false,true);
        }

        @Override
        public void onCommandPraySearch(String title, String type) {
            Intent intent = new Intent("action_speech_music_search");
            intent.putExtra("title", title);
            intent.putExtra("type", type);
            mContext.sendBroadcast(intent);
        }


        @Override
        public void onPlayError() {

        }

        @Override
        public void onPlayComplete() {

        }

        @Override
        public void onLocalMusicPlay(final String str, String url) {
            if (url == null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        requestTTS("对不起，没有找到" + str);
                    }
                }, 1000);
            }
        }

        @Override
        public void onMessageCallback(final String messageBackStr) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestTTS(messageBackStr,false,true);
                }
            }, 2000);
        }

    };


    private WeChatDataManager.WeChatDateListener mWeChatDateListener = new WeChatDataManager.WeChatDateListener() {

      /*  @Override
        public void onVoiceRecieve(List<VoiceResultBean.DataBean> voices) {
            if (mVoices != null) {
                if (voices != null && voices.size() >= 0)
                    mVoices.addAll(voices);
                if (mSpeechEngineManager != null)
                    mSpeechEngineManager.doLocalTTS("您有新的消息！",false);
            } else {
                if (voices != null && voices.size() >= 0)
                    mVoices = voices;
            }


            Intent intent = new Intent("action_broad_cast_wecath_noti");
            intent.putExtra("wecath_list_size", mVoices == null ? 0 : mVoices.size());
            mContext.sendBroadcast(intent);

            //if (mAIResultManager != null) mAIResultManager.playMediaData(voices);
        }*/

        @Override
        public void onEvent(final String eventStr, final String url, final String singer, final String title) {

            if (mDataState && "play".equals(eventStr)) {
                if (mDDSManager != null)
                    mDDSManager.cancel();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (title != null && !TextUtils.isEmpty(title)) {
                            if (singer != null && !TextUtils.isEmpty(singer)) {
                                requestTTS("马上为您播放" + singer + "的" + title, true,false);
                            } else {
                                requestTTS("马上为您播放" + title, true,false);
                            }
                            mEvent = eventStr;
                            mUrl = url;
                        } else {
                            if (mAIResultManager != null)
                                mAIResultManager.playMediaData(eventStr, url);
                        }
                    }
                }, 1000);
            } else if ("pause".equals(eventStr)) {
                if (mAIResultManager != null) mAIResultManager.pauseMusic();
            }
        }

        @Override
        public void onPoweroffEvent() {
            requestTTS("正在关闭小采...");
            mHandler.sendEmptyMessageDelayed(MSG_HANDLER_NET_EVENT_POWEROFF, 2000);
        }

        @Override
        public void onVideoEvent(final String eventStr, final String url, final String singer, final String title) {
            mAIResultManager.cleanMediaData();
            if (mDataState && "play".equals(eventStr)) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (title != null && !TextUtils.isEmpty(title)) {
                            if (singer != null && !TextUtils.isEmpty(singer)) {
                                requestTTS("马上为您播放" + singer + "的" + title, true,false);
                            } else {
                                requestTTS("马上为您播放" + title, true,false);
                            }
                        }
                        Message obtain = Message.obtain();
                        obtain.what = MSG_HANDLER_COMMAND_MESSAGE_VIDEO;
                        obtain.obj = url;
                        mHandler.sendMessage(obtain);
                    }
                });
            } else if ("pause".equals(eventStr)) {
                mHandler.sendEmptyMessage(MSG_HANDLER_COMMAND_MESSAGE_VIDEO_STOP);
            }
        }
    };

    private String mEvent;
    private String mUrl;

    public void playMediaData() {
        if (mAIResultManager != null)
            mAIResultManager.playMediaData(mEvent, mUrl);
    }

    private boolean mDataState = true;

    public void setNetPlayState(boolean b) {
        if (mWeChatDataManager != null) mWeChatDataManager.setDataState(b);
        if (mAIResultManager != null) mAIResultManager.setDataState(b);
        if (mDDSManager != null) mDDSManager.setDataState(b);
        mDataState = b;
    }

    public void addToMediaList(Object obj) {
        if (mAIResultManager != null) mAIResultManager.addPlayList(obj);
    }

    public void onDestroy() {
        if (mDDSManager != null) mDDSManager.onDestory();
        if (mWeChatDataManager != null) mWeChatDataManager.stop();
        if (mAIResultManager != null) mAIResultManager.stop();
        if (mReminderManager != null) mReminderManager.stop();
        //if (mRecordManager != null) mRecordManager.stop();
    }

    public void requestTTS(String ttsStr) {
        requestTTS(ttsStr,false,false);
    }

    public void requestTTS(String ttsStr,boolean isMediaTTS,boolean isNeedContinue) {
        if (mDDSManager != null) mDDSManager.doLocalTTS(ttsStr, isMediaTTS,isNeedContinue);
    }

    public void sendTXT(String txt) {
        if (mDataState && mDDSManager != null) mDDSManager.doSdsTXT(txt);
    }

    public void setVolumedown() {
        if (mAIResultManager != null) mAIResultManager.setVolumeMinDown();
    }

    public void setVolumeUp() {
        if (mAIResultManager != null) mAIResultManager.setVolumeMinUp();
    }

    public void changeWakeState() {
        if (mDDSManager != null) mDDSManager.changeWakeState();
    }

    public void stopAllPlayer() {
        if (mAIResultManager != null) mAIResultManager.pauseMusic();
        if (mDDSManager != null)
            mDDSManager.cancel();
    }

    //+==================+//
    public void doNetDeviceOnline() {
        if (mWeChatDataManager != null) mWeChatDataManager.doNotifyOnline();
    }

    public void doNetDeviceID() {
        if (mWeChatDataManager != null) mWeChatDataManager.doGetDeviceID();
    }

    public void doNetPeriodWork() {
        if (mWeChatDataManager != null) mWeChatDataManager.doPeriodWork();
    }


}
