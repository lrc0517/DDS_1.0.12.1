package com.ai.xiaocai;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }

    public void start(View view) {
        startService(new Intent(this,XiaocaiService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this,XiaocaiService.class));
    }

    public void send(View view) {
        sendBroadcast(new Intent("action_speech_send_txt"));
    }


}
