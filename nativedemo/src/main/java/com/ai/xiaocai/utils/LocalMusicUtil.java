package com.ai.xiaocai.utils;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.storage.StorageManager;

import com.ai.xiaocai.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * Created by Lucien on 2018/7/12.
 */

public class LocalMusicUtil {

    public static final String DATABASE_FILENAME = "fileinfo.db";                // 这个是DB文件名字
    public static final String PACKAGE_NAME = "MusicDB";    // 这个是自己项目包路径
    public static final String DATABASE_PATH = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
            + PACKAGE_NAME;    // 获取存储位置地址
    private static SQLiteDatabase database = null;
    private String name = "";                        // 股票搜索出来的代码
    static String databaseFilename = DATABASE_PATH + "/" + DATABASE_FILENAME;

    public static SQLiteDatabase openDatabase(Context context) {
        try {
//			String databaseFilename = DATABASE_PATH + "/" + DATABASE_FILENAME;
            File dir = new File(DATABASE_PATH);
            if (!dir.exists()) {
                dir.mkdir();//新建文件
            }
            if (!(new File(databaseFilename)).exists()) {
                InputStream is = context.getResources().openRawResource(
                        R.raw.fileinfo);
                FileOutputStream fos = new FileOutputStream(databaseFilename);
                byte[] buffer = new byte[8192];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                is.close();
            }
            database = SQLiteDatabase.openOrCreateDatabase(databaseFilename,
                    null);
            return database;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getPath(Context context, String value) throws Exception {
        String resultPath = null;
        LogUtils.e("getPath value = " + value);
        if (value.endsWith(".mp3")) value = value.replaceAll(".mp3", "");
        LogUtils.e("getPath value = " + value);
        if (database == null) {
            openDatabase(context);
        }
        Cursor cursor = database.query("user", null, "name=?", new String[]{value}, null, null, null);// 查询并获得游标
        // Cursor cursor = m_DB.query("user",null,null,null,null,null,null);//查询并获得游标
        //resultPath = "Count=" + cursor.getCount();
        if (cursor != null) {
            cursor.moveToFirst();
            resultPath = cursor.getString(cursor.getColumnIndex("pathname"));
        }
        LogUtils.e("getPath resultPath = " + resultPath);
        if (resultPath != null) {
            String sdPath = getSDPath(context);
            if (sdPath != null) {
                resultPath = sdPath + "/" + resultPath;
            } else {
                resultPath = null;
            }
        }
        return resultPath;
    }


    public static String[] getStoragePaths(Context context) {
        try {
            StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths",  null);
            String[] paths = (String[]) getVolumePathsMethod.invoke(sm, null);
            return paths;
        } catch (Exception e) {
            LogUtils.e("getPrimaryStoragePath() failed", e);
        }
        return null;
    }

    public static String getSDPath(Context context) {
        String[] paths = getStoragePaths(context);
        if (paths != null && paths.length >= 2) {
            return paths[1] /*+"/精简T卡"*/;
        }
        return null;
    }
}
