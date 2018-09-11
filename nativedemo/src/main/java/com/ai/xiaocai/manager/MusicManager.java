package com.ai.xiaocai.manager;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.ArrayMap;

import com.ai.xiaocai.bean.BeanMediaMusic;
import com.ai.xiaocai.utils.LogUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Created by Lucien on 2018/5/9.
 */

public class MusicManager {
    private static final String TAG = "XC/MusicManager";
    private final Context mContext;
    private final AudioManager mAm;
    private final Handler mHandler;

    private MediaPlayer myMediaPlayer;
    private int mCurrentPosition = -1;
    private String mUrl;


    public MusicManager(Context context, Handler handler, MusicControlListener musicControlListener) {
        this.mContext = context;
        this.mHandler = handler;
        this.mMusicControlListener = musicControlListener;
        mAm = (AudioManager) context.getSystemService(AUDIO_SERVICE);
    }

    public boolean getPlayingState() {
        return myMediaPlayer != null && myMediaPlayer.isPlaying();
    }

    public void stopPlay() {
        LogUtils.w("stopPlay  ");
        try {
            myMediaPlayer.stop();
            myMediaPlayer.release();
            myMediaPlayer = null;
            mCurrentPosition = -1;
            listIndex = -1;
            listSize = -1;
            if (mediaList!=null)mediaList.clear();
        } catch (Exception e) {
            mCurrentPosition = -1;
            listIndex = -1;
            listSize = -1;
            LogUtils.w(TAG, "stopPlay ", e);
        }
    }

    public void pausePlay() {
        LogUtils.w("pausePlay ");
        try {
            if (myMediaPlayer == null)
                return;
            myMediaPlayer.pause();
            mCurrentPosition = myMediaPlayer.getCurrentPosition();
        } catch (Exception e) {
            mCurrentPosition = -1;
            LogUtils.w(TAG, "pausePlay ", e);
            e.printStackTrace();
        }
    }

    public void continuePlay() {
        LogUtils.w("continuePlay  ");
        try {
            if (requestFocus()) {
                if (myMediaPlayer == null)
                    return;
                myMediaPlayer.start();
                if (mCurrentPosition != -1) myMediaPlayer.seekTo(mCurrentPosition);
            }
        } catch (Exception e) {
            mCurrentPosition = -1;
            LogUtils.w(TAG, "continuePlay ", e);
            e.printStackTrace();
        }
    }

    public void previous() {
        if (listSize != -1) {
            listIndex -= 1;
            if (listIndex < 0 || listIndex >= listSize) {
                if (isNeedLoop) {
                    listIndex = listSize - 1;
                } else {
                    stop();
                    return;
                }
            }
            playMediaData(mediaList.get(listIndex));
        } else {
            if (isNeedLoop)
            if (mMusicControlListener != null) mMusicControlListener.onControlError(1);
        }
    }

    public void next() {
        if (listSize != -1) {
            listIndex += 1;
            if (listIndex < 0 || listIndex >= listSize) {
                if (isNeedLoop) {
                    listIndex = 0;
                } else {
                    stop();
                    return;
                }
            }
            playMediaData(mediaList.get(listIndex));
        } else {
            if (isNeedLoop)
            if (mMusicControlListener != null) mMusicControlListener.onControlError(1);
        }
    }

    private boolean isNeedLoop = true;

    public void setNewPlayList(List<BeanMediaMusic.ContentBean> content) throws Exception{
        isNeedLoop = false;
        ArrayMap<String,BeanMediaMusic.ContentBean> newsData = new ArrayMap<>();
        int i = 0;
        for (BeanMediaMusic.ContentBean contentBean:content) {
            String title = contentBean.getTitle();
            String number = getNumber(title);
            if (number!=null){
                // Log.e(TAG, "setPlayList: number = "+number);
                // Log.e(TAG, "setPlayList: title = "+title);
                newsData.put(number,contentBean);
                int i1 = Integer.parseInt(number);
                i = i<i1?i1:i;
            }
        }
        if (i!=0){
            BeanMediaMusic.ContentBean contentBean = newsData.get("" + i);
            playMediaData(contentBean);
        }
        newsData.clear();
    }

    private int listSize = -1;
    private int listIndex = -1;
    private List<BeanMediaMusic.ContentBean> mediaList;

    public void setPlayList(List<BeanMediaMusic.ContentBean> content, boolean needRandom) throws Exception {
        mediaList = content;
        listSize = content.size();
        listIndex = 0;//needRandom?(int) (Math.random() * content.size()):0;
        BeanMediaMusic.ContentBean contentBean = content.get(listIndex);
        playMediaData(contentBean);
        isNeedLoop = true;
    }

    private String getNumber(String str){
        String regEx="[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    private void playMediaData(BeanMediaMusic.ContentBean contentBean) {
        String linkUrl = contentBean.getLinkUrl();
        String label = contentBean.getLabel();
        String title = contentBean.getTitle();
        String subTitle = contentBean.getSubTitle();
        setUrlAndPlay(linkUrl);
        Message msg = Message.obtain();
        msg.what = DoMain.MSG_HANDLER_TIPS_NEED_SHOW;
        if (label == null || "null".equals(label)) label = "";
        if (title == null || "null".equals(title)) title = "";
        if (subTitle == null || "null".equals(subTitle)) subTitle = "";


        String tips = label + "\n" + title + "\n" + subTitle;
        LogUtils.d(TAG, "tips = " + tips);
        LogUtils.d(TAG, "linkUrl = " + linkUrl);
        msg.obj = tips;
        mHandler.sendMessage(msg);
    }


    public void setUrlAndPlay(String playUrl) {
        if (requestFocus()) {
            try {
                try{
                    if (myMediaPlayer != null) {
                        myMediaPlayer.stop();
                        myMediaPlayer.release();
                    }
                }catch(Exception e){
                    LogUtils.w(TAG, "setUrlAndPlay Error", e);
                }
                myMediaPlayer = new MediaPlayer();
                myMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                myMediaPlayer.setOnPreparedListener(mOnPreparedListener);
                myMediaPlayer.setOnCompletionListener(mOnCompletionListener);
                myMediaPlayer.setOnErrorListener(mOnErrorListener);
                myMediaPlayer.setDataSource(playUrl);
                myMediaPlayer.prepare();
                mUrl = playUrl;
            } catch (Exception e) {
                LogUtils.w(TAG, "setUrlAndPlay Error", e);
                //  doLocalTTS();
                e.printStackTrace();
                stop();
                if (mMusicControlListener != null) mMusicControlListener.onPlayError();
            }
        }
    }

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
                pausePlay();
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                //Resume playback
                continuePlay();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // mAm.unregisterMediaButtonEventReceiver(RemoteControlReceiver);
                mAm.abandonAudioFocus(afChangeListener);
                //Stop playback
                //stopPlay();
                pausePlay();
            }
        }
    };


    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            LogUtils.d(TAG, "MediaPlayer onPrepared");
            myMediaPlayer.start();
        }
    };


    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (!mp.isLooping()) {
                mAm.abandonAudioFocus(afChangeListener);
            }
            mUrl = null;
            mCurrentPosition = -1;
            if (mMusicControlListener != null) mMusicControlListener.onPlayComplete();
            next();

        }
    };

    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            LogUtils.w("mOnErrorListener what = "+what+",extra = "+extra);
            stop();
            if (mMusicControlListener != null) mMusicControlListener.onPlayError();
            return false;
        }
    };


    public String getUrlPlaying() {
        return mUrl;
    }

    //+++++++++++++++++++++++++++++++++++++++++++++//

    private MusicControlListener mMusicControlListener;

    public void stop() {
        if (mAm != null) mAm.abandonAudioFocus(afChangeListener);
        stopPlay();
    }


    public interface MusicControlListener {
        void onPlayError();

        void onPlayComplete();

        void onControlError(int id);
    }
}
