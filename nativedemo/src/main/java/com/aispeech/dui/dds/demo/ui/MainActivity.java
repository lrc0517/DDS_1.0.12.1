package com.aispeech.dui.dds.demo.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import com.ai.xiaocai.R;
import com.aispeech.ailog.AILog;
import com.aispeech.dui.dds.DDS;
import com.aispeech.dui.dds.DDSConfig;
import com.aispeech.dui.dds.agent.MessageObserver;
import com.aispeech.dui.dds.demo.DDSService;
import com.aispeech.dui.dds.demo.ui.adapter.DialogAdapter;
import com.aispeech.dui.dds.demo.bean.MessageBean;
import com.aispeech.dui.dds.demo.widget.InputField;
import com.aispeech.dui.dds.exceptions.DDSNotInitCompleteException;
import com.aispeech.dui.dds.update.DDSUpdateListener;
import com.aispeech.dui.dds.utils.PrefUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements InputField.Listener {
    public static final String TAG = "MainActivity";

    private InputField inputField;
    private RecyclerView mRecyclerView;
    private DialogAdapter mDialogAdapter;  // 各种UI控件的实现在DialogAdapter类里
    private LinkedList<MessageBean> mMessageList;
    private Handler mHandler = new Handler();
    private DuiMessageObserver mMessageObserver;
    MyReceiver receiver;
    private boolean isActivityShowing = false;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction("ddsdemo.intent.action.init_complete");
        receiver = new MyReceiver();
        registerReceiver(receiver, filter);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View titleView = inflater.inflate(R.layout.action_bar_title, null);
        actionBar.setCustomView(titleView, lp);

        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);

        inputField = (InputField) this.findViewById(R.id.input_field);
        inputField.setListener(this);

        mRecyclerView = (RecyclerView) this.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mMessageList = new LinkedList<>();

        mDialogAdapter = new DialogAdapter(mMessageList);
        mRecyclerView.setAdapter(mDialogAdapter);

        mMessageObserver = new DuiMessageObserver();

    }


    @Override
    protected void onStart() {
        isActivityShowing = true;
        try {
            DDS.getInstance().getUpdater().update(ddsUpdateListener);
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
        DDS.getInstance().getAgent().subscribe("sys.resource.updated", resourceUpdatedMessageObserver);
        super.onStart();
    }

    @Override
    protected void onStop() {
        AILog.d(TAG, "onStop() " + this.hashCode());
        isActivityShowing = false;
        if (dialog != null) {
            dialog.dismiss();
        }
        DDS.getInstance().getAgent().unSubscribe(resourceUpdatedMessageObserver);
        super.onStop();
    }


    @Override
    protected void onResume() {
        DDS.getInstance().getAgent().subscribe(new String[]{
                "sys.resource.updated",
                "sys.dialog.state",
                "context.output.text",
                "context.input.text",
                "avatar.silence",
                "avatar.listening",
                "avatar.understanding",
                "avatar.speaking",
                "context.widget.content",
                "context.widget.list",
                "context.widget.web",
                "context.widget.media"}, mMessageObserver);

        inputField.getAvatarView().go();
        sendHiMessage();
        enableWakeup();

        super.onResume();
    }

    @Override
    protected void onPause() {
        DDS.getInstance().getAgent().unSubscribe(mMessageObserver);

        inputField.toIdle();
        disableWakeup();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        inputField.destroy();
        stopService();
    }

    private void stopService() {
        Intent intent = new Intent(MainActivity.this, DDSService.class);
        stopService(intent);
    }

    void enableWakeup() {
        try {
            DDS.getInstance().getAgent().getWakeupEngine().enableWakeup();
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
    }

    void disableWakeup() {
        try {
            DDS.getInstance().getAgent().stopDialog();
            DDS.getInstance().getAgent().getWakeupEngine().disableWakeup();
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
    }

    public void sendHiMessage() {
        String[] wakeupWords = new String[0];
        String minorWakeupWord = null;
        try {
            wakeupWords = DDS.getInstance().getAgent().getWakeupEngine().getWakeupWords();
            minorWakeupWord = DDS.getInstance().getAgent().getWakeupEngine().getMinorWakeupWord();
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
        String hiStr = "";
        if (wakeupWords != null && minorWakeupWord != null) {
            hiStr = getString(R.string.hi_str2, wakeupWords[0], minorWakeupWord);
        } else if (wakeupWords != null && wakeupWords.length == 2) {
            hiStr = getString(R.string.hi_str2, wakeupWords[0], wakeupWords[1]);
        } else if (wakeupWords != null && wakeupWords.length > 0) {
            hiStr = getString(R.string.hi_str, wakeupWords[0]);
        }
        if (!TextUtils.isEmpty(hiStr)) {
            MessageBean bean = new MessageBean();
            bean.setText(hiStr);
            bean.setType(MessageBean.TYPE_OUTPUT);
            mMessageList.add(bean);
            mDialogAdapter.notifyItemInserted(mMessageList.size());
            mRecyclerView.smoothScrollToPosition(mMessageList.size());
        }
    }

    @Override
    public boolean onMicClicked() {
        try {
            DDS.getInstance().getAgent().avatarClick();
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void notifyItemInserted() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mMessageList.size()>=10){
                    mMessageList.poll();
                }
                mDialogAdapter.notifyDataSetChanged();
                mRecyclerView.smoothScrollToPosition(mMessageList.size());
            }
        });
    }

    class DuiMessageObserver implements MessageObserver {

        private boolean isFirstVar = true;
        private boolean hasvar = false;
        @Override
        public void onMessage(String message, String data) {
            Log.d(TAG, "message : " + message + " data : " + data);
            MessageBean bean = null;
            switch (message) {
                case "context.output.text":
                    bean = new MessageBean();
                    String txt = "";
                    try {
                        JSONObject jo = new JSONObject(data);
                        txt = jo.optString("text", "");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    bean.setText(txt);
                    bean.setType(MessageBean.TYPE_OUTPUT);
                    mMessageList.add(bean);
                    notifyItemInserted();
                    break;
                case "context.input.text":
                    bean = new MessageBean();
                    try {
                        JSONObject jo = new JSONObject(data);
                        if (jo.has("var")) {
                            String var = jo.optString("var", "");
                            if (isFirstVar) {
                                isFirstVar = false;
                                hasvar = true;
                                bean.setText(var);
                                bean.setType(MessageBean.TYPE_INPUT);
                                mMessageList.add(bean);
                                notifyItemInserted();
                            } else {
                                mMessageList.pollLast();
                                bean.setText(var);
                                bean.setType(MessageBean.TYPE_INPUT);
                                mMessageList.add(bean);
                                notifyItemInserted();
                            }
                        }
                        if (jo.has("text")) {
                            if(hasvar) {
                                mMessageList.pollLast();
                                hasvar = false;
                                isFirstVar = true;
                            }
                            String text = jo.optString("text", "");
                            bean.setText(text);
                            bean.setType(MessageBean.TYPE_INPUT);
                            mMessageList.add(bean);
                            notifyItemInserted();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "context.widget.content":
                    bean = new MessageBean();
                    try {
                        JSONObject jo = new JSONObject(data);
                        String title = jo.optString("title", "");
                        String subTitle = jo.optString("subTitle", "");
                        String imgUrl = jo.optString("imageUrl", "");
                        bean.setTitle(title);
                        bean.setSubTitle(subTitle);
                        bean.setImgUrl(imgUrl);
                        bean.setType(MessageBean.TYPE_WIDGET_CONTENT);
                        mMessageList.add(bean);
                        notifyItemInserted();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "context.widget.list":
                    bean = new MessageBean();
                    try {
                        JSONObject jo = new JSONObject(data);
                        JSONArray array = jo.optJSONArray("content");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.optJSONObject(i);
                            String title = object.optString("title", "");
                            String subTitle = object.optString("subTitle", "");
                            MessageBean b = new MessageBean();
                            b.setTitle(title);
                            b.setSubTitle(subTitle);
                            bean.addMessageBean(b);
                        }
                        int currentPage = jo.optInt("currentPage");
                        bean.setCurrentPage(currentPage);
                        bean.setType(MessageBean.TYPE_WIDGET_LIST);
                        mMessageList.add(bean);
                        notifyItemInserted();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "context.widget.web":
                    bean = new MessageBean();
                    try {
                        JSONObject jo = new JSONObject(data);
                        String url = jo.optString("url");
                        bean.setUrl(url);
                        bean.setType(MessageBean.TYPE_WIDGET_WEB);
                        mMessageList.add(bean);
                        notifyItemInserted();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "avatar.silence":
                    inputField.toIdle();
                    break;
                case "avatar.listening":
                    inputField.toListen();
                    break;
                case "avatar.understanding":
                    inputField.toRecognize();
                    break;
                case "avatar.speaking":
                    inputField.toSpeak();
                    break;
                case "sys.dialog.state":
                    DialogAdapter.mState = data;
                    break;
                case "sys.resource.updated":
                    try {
                        DDS.getInstance().getUpdater().update(ddsUpdateListener);
                    } catch (DDSNotInitCompleteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String name = intent.getAction();
            if (name.equals("ddsdemo.intent.action.init_complete")) {
                inputField.getAvatarView().go();
                sendHiMessage();
                enableWakeup();
            }
        }
    }


    private DDSUpdateListener ddsUpdateListener = new DDSUpdateListener() {
        @Override
        public void onUpdateFound(String detail) {
            final String str = detail;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    showNewVersionDialog(str);
                }
            });

            try {
                DDS.getInstance().getAgent().getTTSEngine().speak("发现新版本,正在为您更新", 1);
            } catch (DDSNotInitCompleteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpdateFinish() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    showUpdateFinishedDialog();
                }
            });

            try {
                DDS.getInstance().getAgent().getTTSEngine().speak("更新成功", 1);
            } catch (DDSNotInitCompleteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDownloadProgress(float progress) {
            Log.d(TAG, "onDownloadProgress :" + progress);
        }

        @Override
        public void onError(int what, String error) {
            String productId = PrefUtil.getString(getApplicationContext(), DDSConfig.K_PRODUCT_ID);
            if (what == 70319) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showProductNeedUpdateDialog();
                    }
                });

            } else {
                Log.e(TAG, "UPDATE ERROR : " + error);
            }
        }

        @Override
        public void onUpgrade(String version) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    showApkUpdateDialog();
                }
            });

        }
    };

    protected void showNewVersionDialog(final String info) {
        if (!isActivityShowing) {
            return;
        }
        if (null != dialog) {
            dialog.dismiss();
            dialog = null;
        }
        dialog = new ProgressDialog(this);
        dialog.setCancelable(true);
        dialog.setTitle("发现新的更新资源, 正在为您更新...");
        ((ProgressDialog) dialog).setMessage(info);
        ((ProgressDialog) dialog).setProgress(0);
        dialog.show();
    }

    protected void showProductNeedUpdateDialog() {
        if (!isActivityShowing) {
            return;
        }
        if (null != dialog) {
            dialog.dismiss();
            dialog = null;
        }
        dialog = new AlertDialog.Builder(this).setCancelable(false).setTitle(R.string
                .update_product_title)
                .setMessage(R.string.update_product_message).setPositiveButton(R.string.update_product_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton(R.string.update_product_cancel, new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                        System.exit(0);
                    }
                }).create();
        dialog.show();
    }

    protected void showApkUpdateDialog() {
        if (!isActivityShowing) {
            return;
        }
        if (null != dialog) {
            dialog.dismiss();
            dialog = null;
        }
        dialog = new AlertDialog.Builder(this).setCancelable(false).setTitle(R.string
                .update_sdk_title)
                .setMessage(R.string.update_sdk_message).setPositiveButton(R.string.update_sdk_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton(R.string.update_sdk_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                        System.exit(0);
                    }
                }).create();
        dialog.show();
    }

    protected void showUpdateFinishedDialog() {
        if (!isActivityShowing) {
            return;
        }
        if (null != dialog) {
            dialog.dismiss();
            dialog = null;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("资源加载成功");

        dialog = builder.create();
        dialog.show();

        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                dialog.dismiss();
                t.cancel();
            }
        }, 2000);
    }

    private MessageObserver resourceUpdatedMessageObserver = new MessageObserver() {
        @Override
        public void onMessage(String message, String data) {
            try {
                DDS.getInstance().getUpdater().update(ddsUpdateListener);
            } catch (DDSNotInitCompleteException e) {
                e.printStackTrace();
            }
        }
    };

}
