package com.ai.xiaocai.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * ClassName:LogUtils <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Date: 2016年6月7日 下午9:13:31 <br/>
 *
 * @author Mason
 */
public final class LogUtils {
    /**
     * 日志的过滤标记
     */
    private static String sTagDefault = "XC";

    /**
     * 如果是false，所有日志都打印，如果是true，只打印info，warn和error的日志.
     */
    private static boolean sToggleRelease = false;

    /**
     * 是否打印抛出的异常.
     */
    private static boolean sToggleThrowable = true;

    /**
     * 是否打印线程的名称.
     */
    private static boolean sToggleThread = false;

    /**
     * 是否打印类名和方法名.
     */
    private static boolean sToggleClassMethod = false;

    /**
     * 是否打印出日志所在的行数.
     */
    private static boolean sToggleFileLineNumber = false;

    public static void e(String tag, String msg, Throwable e) {
        printLog(Log.ERROR, tag, msg, e);
    }

    public static void e(String msg, Throwable e) {
        printLog(Log.ERROR, null, msg, e);
    }

    public static void e(String msg) {
        printLog(Log.ERROR, null, msg, null);
    }

    public static void w(String tag, String msg, Throwable e) {
        printLog(Log.WARN, tag, msg, e);
    }

    public static void w(String msg, Throwable e) {
        printLog(Log.WARN, null, msg, e);
    }

    public static void w(String msg) {
        printLog(Log.WARN, null, msg, null);
    }

    public static void i(String tag, String msg, Throwable e) {
        printLog(Log.INFO, tag, msg, e);
    }

    public static void i(String msg) {
        printLog(Log.INFO, null, msg, null);
    }

    public static void d(String tag, String msg, Throwable e) {
        printLog(Log.DEBUG, tag, msg, e);
    }

    public static void d(String msg, Throwable e) {
        printLog(Log.DEBUG, null, msg, e);
    }

    public static void d(String tag, String msg) {
        printLog(Log.DEBUG, tag, msg, null);
    }

    public static void d(String msg) {
        printLog(Log.DEBUG, null, msg, null);
    }

    public static void v(String tag, String msg, Throwable e) {
        printLog(Log.VERBOSE, tag, msg, e);
    }

    public static void v(String tag, String msg) {
        printLog(Log.VERBOSE, tag, msg, null);
    }

    public static void v(String msg) {
        printLog(Log.VERBOSE, null, msg, null);
    }

    private static void printLog(int logType, String tag, String msg, Throwable e) {
        String tagStr = (tag == null) ? sTagDefault : tag;
        if (sToggleRelease) {
            if (logType < Log.INFO) {
                return;
            }
            String msgStr =
                    (e == null) ? msg : (msg + "\n" + Log.getStackTraceString(e));

            switch (logType) {
                case Log.ERROR:
                    Log.e(tagStr, msgStr);

                    break;
                case Log.WARN:
                    Log.w(tagStr, msgStr);

                    break;
                case Log.INFO:
                    Log.i(tagStr, msgStr);

                    break;
                default:
                    break;
            }

        } else {
            StringBuilder msgStr = new StringBuilder();

            if (sToggleThread || sToggleClassMethod || sToggleFileLineNumber) {
                Thread currentThread = Thread.currentThread();

                if (sToggleThread) {
                    msgStr.append("<");
                    msgStr.append(currentThread.getName());
                    msgStr.append("> ");
                }

                if (sToggleClassMethod) {
                    StackTraceElement ste = currentThread.getStackTrace()[4];
                    String className = ste.getClassName();
                    msgStr.append("[");
                    msgStr.append(className == null ? null
                            : className.substring(className.lastIndexOf('.') + 1));
                    msgStr.append("--");
                    msgStr.append(ste.getMethodName());
                    msgStr.append("] ");
                }

                if (sToggleFileLineNumber) {
                    StackTraceElement ste = currentThread.getStackTrace()[4];
                    msgStr.append("[");
                    msgStr.append(ste.getFileName());
                    msgStr.append("--");
                    msgStr.append(ste.getLineNumber());
                    msgStr.append("] ");
                }
            }

            msgStr.append(msg);
            if (e != null && sToggleThrowable) {
                msgStr.append('\n');
                msgStr.append(Log.getStackTraceString(e));
            }

            switch (logType) {
                case Log.ERROR:
                    Log.e(tagStr, msgStr.toString());

                    break;
                case Log.WARN:
                    Log.w(tagStr, msgStr.toString());

                    break;
                case Log.INFO:
                    Log.i(tagStr, msgStr.toString());

                    break;
                case Log.DEBUG:
                    Log.d(tagStr, msgStr.toString());

                    break;
                case Log.VERBOSE:
                    Log.v(tagStr, msgStr.toString());

                    break;
                default:
                    break;
            }
        }
    }

    public static void printLongLog(String local) {
        if (local.length() > 200) {
            String needPrint = local.substring(0, 200);
            LogUtils.i(needPrint);
            printLongLog(local.substring(200, local.length() - 1));
        } else {
            LogUtils.i(local);
        }
    }

    public static void saveLongString(String tag,String str) {
        String filePath = null;
        boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (hasSDCard) {
            filePath = Environment.getExternalStorageDirectory().toString() + File.separator +"/"+sTagDefault+"/"+ tag+".txt";
        } else
            filePath = Environment.getDownloadCacheDirectory().toString() + File.separator +"/"+sTagDefault+"/"+ tag+ ".txt";
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(str.getBytes());
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
