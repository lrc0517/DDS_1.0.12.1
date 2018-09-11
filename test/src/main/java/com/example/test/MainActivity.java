package com.example.test;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SX";

    private EditText number2;
    private EditText number1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
         number1 = (EditText) findViewById(R.id.number1);
         number2 = (EditText) findViewById(R.id.number2);


    }

    void swap(int a,int b){
        Log.e(TAG,"a = "+a);
        Log.e(TAG,"b = "+b);
        a = a^b;
        Log.e(TAG,"a = "+a);
        b = a^b;
        Log.e(TAG,"b = "+b);
        a = a^b;
        Log.e(TAG,"a = "+a);
        Log.e(TAG,"b = "+b);
        number1.setText(""+a,null);
        number2.setText(""+b,null);
    }

    public void swap(View view) {
        String s1 = number1.getText().toString();
        String s2 = number2.getText().toString();

        int i1 = Integer.parseInt(s1);
        int i2 = Integer.parseInt(s2);
        swap(i1,i2);

    }
}
