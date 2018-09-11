package com.ai.xiaocai.number.manager;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.ai.xiaocai.manager.DoMain;
import com.ai.xiaocai.number.utils.EventResultBean;
import com.ai.xiaocai.number.utils.ReSmartContants;
import com.ai.xiaocai.number.utils.SmartUtil;
import com.ai.xiaocai.utils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Lucien on 2018/6/8.
 */

public class WeChatDataManager {
    private static final String TAG = "XC/WC";
    private String mDeviceId;
    private Handler mHandler;
    private Context mContext;
    private WeChatDateListener mWeChatDateListener;
    private int mAccessTime = 5000;
    private OkHttpClient client;
    private Request requestEvent;
    private Request requestAccess;
    private Request requestVoice;
    private Request requestStatus;

    public void stop() {

    }

    private boolean mDataState = true;
    public void setDataState(boolean b) {
        mDataState  = b;
    }

    public interface WeChatDateListener {
      //  void onVoiceRecieve(List<VoiceResultBean.DataBean> voices);

        void onEvent(String eventStr, String url, String singer, String title);

        void onPoweroffEvent();

        void onVideoEvent(String eventStr, String url, String singer, String title);

    }


    public WeChatDataManager(Context context, Handler handler, WeChatDateListener l) {
        this.mHandler = handler;
        this.mContext = context;
        this.mWeChatDateListener = l;
        doGetDeviceID();
    }


    public String doGetDeviceID() {
        mDeviceId = SmartUtil.getMac();
        LogUtils.d("mDeviceId = " + mDeviceId);
        if (mDeviceId == null || TextUtils.isEmpty(mDeviceId)) {
            mHandler.sendEmptyMessageDelayed(DoMain.MSG_HANDLER_NET_DATA_DEVICE_GET_FAILED, 3000);
        } else {
            doNotifyOnline();
        }
        return mDeviceId;
    }

    public void doPeriodWork() {
        new Thread(mGetNetDataRunnable).start();
    }

    public void doNotifyOnline() {
        new Thread(mDoNotifyOnlineRunnable).start();
    }


    private Runnable mDoNotifyOnlineRunnable = new Runnable() {
        @Override
        public void run() {
            getStatusData();
        }
    };


    private Runnable mGetNetDataRunnable = new Runnable() {
        @Override
        public void run() {
            getAccessData();
            getEventData();
          //  getVoiceData();
            mHandler.sendEmptyMessageDelayed(DoMain.MSG_HANDLER_NET_DATA_DO_PERIOD, mAccessTime);
        }
    };

    private void getStatusData() {
        String url = ReSmartContants.URL + ReSmartContants.URL_DEVICE_STATE + "?" + "equip=" + mDeviceId + "&status=1";

        if (client == null) client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        if (requestStatus == null) requestStatus = new Request.Builder()
                .url(url)
                .build();
        client.newCall(requestStatus).enqueue(mStatusCallback);
    }

    private void getAccessData() {
        String url = ReSmartContants.URL + ReSmartContants.URL_ACCESS;
        if (requestAccess == null) requestAccess = new Request.Builder()
                .url(url)
                .build();
        client.newCall(requestAccess).enqueue(mAccessCallback);
    }

    private void getEventData() {
        String url = ReSmartContants.URL + ReSmartContants.URL_EVENT + "?" + "equip=" + mDeviceId;
        if (requestEvent == null) requestEvent = new Request.Builder()
                .url(url)
                .build();
        client.newCall(requestEvent).enqueue(mEventCallback);
    }

   /* private void getVoiceData() {
        String url = ReSmartContants.URL + ReSmartContants.URL_GET_VOICE + "?" + "equip=" + mDeviceId;
        if (requestVoice == null) requestVoice = new Request.Builder()
                .url(url)
                .build();
        client.newCall(requestVoice).enqueue(mVoiceCallback);
    }*/

    private Callback mStatusCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            LogUtils.w(TAG, "mStatusCallback ", e);
            mHandler.sendEmptyMessageDelayed(DoMain.MSG_HANDLER_NET_DATA_ONLINE_FAILED, mAccessTime);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                String strReponse = response.body().string();
                try {
                    JSONObject reponJson = new JSONObject(strReponse);
                    String reponCode = reponJson.getString("code");
                    reponCode = reponCode.trim();
                    LogUtils.d(TAG,"mStatusCallback " + strReponse);
                    if ("0".equals(reponCode)) {
                        mHandler.sendEmptyMessage(DoMain.MSG_HANDLER_NET_DATA_DEVICE_ONLINE);
                    } else {
                        mHandler.sendEmptyMessageDelayed(DoMain.MSG_HANDLER_NET_DATA_ONLINE_FAILED, mAccessTime);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
    };
    private Callback mAccessCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            LogUtils.i(TAG, "mAsyncCallback ", e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                String strReponse = response.body().string();
                try {
                    JSONObject reponJson = new JSONObject(strReponse);
                    String reponCode = reponJson.getString("code");
                    reponCode = reponCode.trim();
                    LogUtils.d(TAG,"mAccessCallback " + strReponse);
                    if ("0".equals(reponCode)) {
                        String dateStr = reponJson.getString("data");
                        int accessTime = Integer.parseInt(dateStr);
                        mAccessTime = accessTime * 1000;
                    } else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
    };
    private Callback mEventCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            LogUtils.w(TAG, "mEventCallback ", e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                String strReponse = response.body().string();
                try {
                    JSONObject reponJson = new JSONObject(strReponse);
                    String reponCode = reponJson.getString("code");
                    reponCode = reponCode.trim();
                    LogUtils.v(TAG,"mEventCallback " + strReponse);
                    if ("0".equals(reponCode)) {
                        Gson gson = new Gson();
                        EventResultBean eventResultBean = gson.fromJson(strReponse, new TypeToken<EventResultBean>() {
                        }.getType());
                        EventResultBean.DataBean event = eventResultBean.getData();
                        String isGet = event.getIs_get();
                        if ("0".equals(isGet)) {
                            String eventStr = event.getEvent();
                            String url = event.getUrl();
                            String singer = event.getSinger();
                            String title = event.getTitle();
                            String mediaType = event.getType();
                            LogUtils.d(TAG, "STATUS_EVENT  eventStr = " + eventStr);
                            LogUtils.d(TAG, "STATUS_EVENT  mediaType = " + mediaType);
                            LogUtils.d(TAG, "STATUS_EVENT  url = " + url);
                            LogUtils.d(TAG, "STATUS_EVENT  singer = " + singer);
                            LogUtils.d(TAG, "STATUS_EVENT  title = " + title);
                            if ("shutdown".equals(eventStr)) {
                                LogUtils.i("STATUS_EVENT  eventStr = " + eventStr);
                                if (mWeChatDateListener != null)
                                    mWeChatDateListener.onPoweroffEvent();
                                return;
                            }

                            if ("1".equals(mediaType)) {
                                if (mWeChatDateListener != null)
                                    mWeChatDateListener.onEvent(eventStr, url, singer, title);
                            } else if ("2".equals(mediaType)) {
                                if (mWeChatDateListener != null)
                                    mWeChatDateListener.onVideoEvent(eventStr, url, singer, title);
                            } else {

                            }
                        }
                    } else {
                        LogUtils.i("mEventCallback " + strReponse);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
    };
   /* private Callback mVoiceCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            LogUtils.w(TAG, "mVoiceCallback ", e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                String strReponse = response.body().string();
                try {
                    JSONObject reponJson = new JSONObject(strReponse);
                    String reponCode = reponJson.getString("code");
                    reponCode = reponCode.trim();
                    if ("0".equals(reponCode)) {
                        Gson gson = new Gson();
                        VoiceResultBean eventResultBean = gson.fromJson(strReponse, new TypeToken<VoiceResultBean>() {
                        }.getType());
                        int code = eventResultBean.getCode();
                        if (code == 0) {
                            List<VoiceResultBean.DataBean> voices = eventResultBean.getData();
                            if (voices.isEmpty()) return;
                            LogUtils.d(TAG, "STATUS_VOICE strReponse = " + strReponse);
                            if (mWeChatDateListener != null)
                                mWeChatDateListener.onVoiceRecieve(voices);
                        }
                    } else {
                        LogUtils.i("mVoiceCallback " + strReponse);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };*/


}
