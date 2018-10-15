package layout.bigman.com.demo.other;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.lzx.musiclibrary.manager.MusicManager;
import com.lzx.musiclibrary.manager.TimerTaskManager;

import layout.bigman.com.demo.ui.AudioPlayActivity;

/**
 *  音频定时服务
 * @author 曾凡达
 * @date 2018/6/26
 */
public class AudioTimerService extends Service implements TimerTaskManager.OnCountDownFinishListener {

    private TimerTaskManager mTimerTaskManager;

    private String mTimeText ="";
    public static final String INTENT_AUDIO_TIME = "INTENT_AUDIO_TIME";
    public static final String INTENT_AUDIO_DATA = "INTENT_AUDIO_DATA";
    public static final String INTENT_AUDIO_STOP= "INTENT_AUDIO_STOP";
    public static final String INTENT_AUDIO_CURRENT= "INTENT_AUDIO_CURRENT";
    private Intent mIntent;
    private LocalBroadcastManager mLocalBroadcastManager;

    private boolean isPlayCurrentTime = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mIntent = new Intent(AudioPlayActivity.AUDIO_TIMER_ACTION);
        mTimerTaskManager = new TimerTaskManager();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mTimerTaskManager.cancelCountDownTask();
        mTimerTaskManager.starCountDownTask(intent.getLongExtra(INTENT_AUDIO_TIME,0L),this);
        isPlayCurrentTime = intent.getBooleanExtra(INTENT_AUDIO_CURRENT, false);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimerTaskManager.cancelCountDownTask();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onFinish() {
        //暂停音频播放
        MusicManager.get().stopMusic();
        MusicManager.get().stopNotification();
        mIntent.putExtra(INTENT_AUDIO_STOP, mTimeText);
        mIntent.putExtra(INTENT_AUDIO_CURRENT, isPlayCurrentTime);
        mLocalBroadcastManager.sendBroadcast(mIntent);
        stopSelf();
    }

    @Override
    public void onTick(long millisUntilFinished) {
        mTimeText = TimeUtils.formatMusicTime(millisUntilFinished);
        mIntent.putExtra(INTENT_AUDIO_DATA, mTimeText);
        mIntent.putExtra(INTENT_AUDIO_CURRENT, isPlayCurrentTime);
        mLocalBroadcastManager.sendBroadcast(mIntent);
    }

}
