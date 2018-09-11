package com.ai.xiaocai.number.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.storage.StorageManager;
import android.util.Base64;

import com.ai.xiaocai.utils.LogUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Lucien on 2018/5/31.
 */

public class SmartUtil {

    private static final String TAG = "XC/Util";


    public static boolean isSDCardChanged(Context context){

        StorageManager storageManager = (StorageManager) context
                .getSystemService(Context.STORAGE_SERVICE);

        return false;
    }

    /**
     *
     * @param str
     * @return Is contain chinese char
     */
    public static boolean isContainsChinese(String str) {
        Pattern pat = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher matcher = pat.matcher(str);
        boolean flg = false;
        if (matcher.find()) {
            flg = true;
        }
        return flg;
    }

    /**
     *
     * @param string
     * @return unicode string
     */
    public static String string2Unicode(String string) {

        StringBuffer unicode = new StringBuffer();

        for (int i = 0; i < string.length(); i++) {

            // 取出每一个字符
            char c = string.charAt(i);

            // 转换为unicode
            unicode.append("\\u" + Integer.toHexString(c));
        }

        return unicode.toString();
    }

    public static boolean isNetworkAvalible(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        } else {
            NetworkInfo[] net_info = connectivityManager.getAllNetworkInfo();
            if (net_info != null) {
                for (int i = 0; i < net_info.length; i++) {
                    if (net_info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void postHelpMessage(String deviceID) {
        if (deviceID == null) return;

        String url = ReSmartContants.URL + ReSmartContants.URL_SOS + "?equip=" + deviceID;
        LogUtils.d(TAG, "url = " + url);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.e(TAG, "send Help Message Failed  ", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                LogUtils.d(TAG, "response.code()==" + response.code());
                if (response.isSuccessful()) {
                    String strReponse = response.body().string();
                    LogUtils.d(TAG, "strReponse ==" + strReponse);
                    try {
                        JSONObject reponJson = new JSONObject(strReponse);
                        String reponCode = reponJson.getString("code");
                        reponCode = reponCode.trim();
                        if ("0".equals(reponCode)) {
                            LogUtils.d(TAG, "send help message succeed ");
                        } else {
                            LogUtils.d(TAG, "send help message failed ");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtils.e(TAG, "get Help Message Response Error  ", e);
                    }
                }
            }
        });
    }


    public static String getMac() {
        String str = "";
        String macSerial = "";
        try {
            Process pp = Runtime.getRuntime().exec(
                    "cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (macSerial == null || "".equals(macSerial)) {
            try {
                return loadFileAsString("/sys/class/net/eth0/address")
                        .toUpperCase().substring(0, 17);
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
        return macSerial;
    }

    public static String loadFileAsString(String fileName) throws Exception {
        FileReader reader = new FileReader(fileName);
        String text = loadReaderAsString(reader);
        reader.close();
        return text;
    }

    public static String loadReaderAsString(Reader reader) throws Exception {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[4096];
        int readLength = reader.read(buffer);
        while (readLength >= 0) {
            builder.append(buffer, 0, readLength);
            readLength = reader.read(buffer);
        }
        return builder.toString();
    }

    public static String getFileByteString(File file) throws Exception{
        //Base64 b64 = new Base64();
        FileInputStream fis = new FileInputStream(file);
        System.out.print(file.length());
        byte[] buffer = new byte[(int)file.length()];

        fis.read(buffer);
        fis.close();

        return Base64.encodeToString(buffer,Base64.DEFAULT);
    }




}
