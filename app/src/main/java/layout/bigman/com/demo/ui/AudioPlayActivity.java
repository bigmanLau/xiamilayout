package layout.bigman.com.demo.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.linchaolong.android.floatingpermissioncompat.FloatingPermissionCompat;
import com.lzx.musiclibrary.aidl.listener.OnPlayerEventListener;
import com.lzx.musiclibrary.aidl.model.SongInfo;
import com.lzx.musiclibrary.constans.PlayMode;
import com.lzx.musiclibrary.manager.MusicManager;
import com.lzx.musiclibrary.manager.TimerTaskManager;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import com.transitionseverywhere.TransitionManager;
import com.zhy.autolayout.AutoLinearLayout;
import com.zhy.autolayout.AutoRelativeLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import layout.bigman.com.demo.other.ArmsUtils;
import layout.bigman.com.demo.other.AudioTimerService;
import layout.bigman.com.demo.other.CatalogItemBean;
import layout.bigman.com.demo.other.CatalogTitbleBean;
import layout.bigman.com.demo.other.FloatWindowManager;
import layout.bigman.com.demo.other.MultiCatalogAdapter;
import layout.bigman.com.demo.other.MyBottomSheet;
import layout.bigman.com.demo.R;
import layout.bigman.com.demo.other.StatusBarUtil;
import layout.bigman.com.demo.other.TimeUtils;
import layout.bigman.com.layout.XiaMiLayout;


public class AudioPlayActivity extends AppCompatActivity implements OnPlayerEventListener {

    //content View
    @BindView(R.id.tv_speed_text)
    TextView mSpeedText;
    @BindView(R.id.tv_time_text)
    TextView mTimeText;

    @BindView(R.id.tv_song_title)
    TextView mSongTitle;
    @BindView(R.id.tv_song_name)
    TextView mSongName;

    TextView mSongCurrentTime;

    TextView mSongTotalTime;
    @BindView(R.id.seekbar_progress)
    SeekBar mSongSeekBar;
    @BindView(R.id.iv_song_play)
    ImageView mSongPlay;
  
    @BindView(R.id.btn_middle_time)
    QMUIRoundButton mMiddleTime;

  
    @BindView(R.id.iv_song_head_play)
    ImageView mSongHeadPlay;
    @BindView(R.id.iv_song_head_last)
  

    AutoLinearLayout mLlSpeedLayout;

    ImageView mIvSongNext;

    AutoRelativeLayout mStickyContentView;

    ImageView mIvSongHeadBack;

    ImageView mIvSongHeadCover;

    ImageView mIvSongHeadLast;

    ImageView mIvSongHeadPlay;

    ImageView mIvSongHeadNext;

    AutoRelativeLayout mStickyHeadView;

    RecyclerView mStickyRecyclerView;

    XiaMiLayout mStickyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.study_activity_audio_play);
        mStickyRecyclerView=ButterKnife.findById(this,R.id.sticky_recycler_view);
        mSongSeekBar=ButterKnife.findById(this,R.id.seekbar_progress);
        mStickyLayout=ButterKnife.findById(this,R.id.sticky_layout);
        mStickyHeadView=ButterKnife.findById(this,R.id.sticky_head_view);
        mIvSongHeadNext=ButterKnife.findById(this,R.id.iv_song_head_next);
        mIvSongHeadPlay=ButterKnife.findById(this,R.id.iv_song_head_play);
        mIvSongHeadLast=ButterKnife.findById(this,R.id.iv_song_head_last);
        mIvSongHeadCover=ButterKnife.findById(this,R.id.iv_song_head_cover);
        mIvSongHeadBack=ButterKnife.findById(this,R.id.iv_song_head_back);
        mStickyContentView=ButterKnife.findById(this,R.id.sticky_content_view);
        mIvSongNext=ButterKnife.findById(this,R.id.iv_song_next);
        mLlSpeedLayout=ButterKnife.findById(this,R.id.ll_speed_layout);
        mSongCurrentTime=ButterKnife.findById(this,R.id.tv_song_current_time);
        mSongTotalTime=ButterKnife.findById(this,R.id.tv_song_total_time);
        initData(savedInstanceState);
    }

    private View mHeadView;
    private List<MultiItemEntity> mMultiItemEntityList;
    private MultiCatalogAdapter mMultiCatalogAdapter;
    private List<SongInfo> mSongInfoList;

    private MyBottomSheet mSpeedBottomSheet;
    private MyBottomSheet mTimeBottomSheet;
    protected QMUITipDialog mLoadingDialog;

    public final static String AUDIO_TIMER_ACTION = "com.wmzx.unicorn.audio_timer_actiion";
    private AudioTimerReceiver mAudioTimerReceiver;
    private TimerTaskManager mTimerTaskManager;
    //是否定时为播放完当前音频
    private boolean isPlayCurrentTime = false;

    private QMUIDialog mDialog;
    private SongInfo mSongInfo;




    public void initData(@Nullable Bundle savedInstanceState) {
        initReceiver();
        QMUIStatusBarHelper.setStatusBarDarkMode(this);
        StatusBarUtil.immersive(this, ArmsUtils.getColor(this, R.color.qmui_config_color_black), 0.5f);
        initTestData();
        initCatalogAdapter();
        initListener();
        initMusicData();
    }

    private void initMusicData() {
        mTimerTaskManager = new TimerTaskManager();
        mTimerTaskManager.setUpdateProgressTask(this::updateProgress);
        MusicManager.get().setPlayMode(PlayMode.PLAY_IN_ORDER);
        if (MusicManager.isIdea()) {
            updateUI(mSongInfoList.get(0));
            MusicManager.get().playMusic(mSongInfoList, 0);
        } else {
            updateUI(MusicManager.get().getCurrPlayingMusic());
            if (MusicManager.isPaused()) {
                MusicManager.get().resumeMusic();
            }
            mTimerTaskManager.scheduleSeekBarUpdate();
        }
    }

    /**
     * 刷新界面数据
     * @date 2018/6/27 10:38
     */
    private void updateUI(SongInfo info) {
        if (info != null) {
//            mImageLoader.loadImage(this, ImageConfigImpl.builder()
//                    .url(StringUtils.null2EmptyStr(info.getSongCover())).isCrossFade(true).imageView(mSongCover).build());
//            mImageLoader.loadImage(this, ImageConfigImpl.builder()
//                    .url(StringUtils.null2EmptyStr(info.getSongCover())).isCrossFade(true).imageView(mSongHeadCover).build());
            mSongTitle.setText(info.getSongName());
            mSongName.setText(info.getArtist());
            mSongSeekBar.setMax((int) info.getDuration());
            mSongTotalTime.setText(TimeUtils.formatMusicTime(info.getDuration()));
        }
        setPlayOrPauseImage();
    }

    /**
     * 更新进度
     */
    private void updateProgress() {
        long progress = MusicManager.get().getProgress();
        long bufferProgress = MusicManager.get().getBufferedPosition();
        mSongSeekBar.setProgress((int) progress);
//        mSongSeekBar.setSecondaryProgress((int) bufferProgress);
        mSongCurrentTime.setText(TimeUtils.formatMusicTime(progress));
    }

    private void initReceiver() {
        mAudioTimerReceiver = new AudioTimerReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mAudioTimerReceiver, new IntentFilter(AUDIO_TIMER_ACTION));
    }

    private void initListener() {
        MusicManager.get().addPlayerEventListener(this);
        mSongSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean isFromUser) {
                if (isFromUser) {
                    mSongCurrentTime.setText(TimeUtils.formatMusicTime(seekBar.getProgress()));
                    mMiddleTime.setText(getString(R.string.study_audio_middle_time, mSongCurrentTime.getText(), mSongTotalTime.getText()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                TransitionManager.beginDelayedTransition(mStickyLayout);
                mMiddleTime.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                TransitionManager.beginDelayedTransition(mStickyLayout);
                mMiddleTime.setVisibility(View.GONE);
                MusicManager.get().seekTo(seekBar.getProgress());
                if (isPlayCurrentTime) {
                    Intent intent = new Intent(AudioPlayActivity.this, AudioTimerService.class);
                    intent.putExtra(AudioTimerService.INTENT_AUDIO_TIME, MusicManager.get().getCurrPlayingMusic().getDuration() - MusicManager.get().getProgress());
                    intent.putExtra(AudioTimerService.INTENT_AUDIO_CURRENT, true);
                    startService(intent);
                }
            }
        });
        mStickyLayout.setOnTouchEventListener(new XiaMiLayout.OnTouchEventListener() {

            @Override
            public boolean isBottomViewTop() {
                return isRecyclerViewTop(mStickyRecyclerView);
            }

            @Override
            public void onLayoutExpanded() {
                StatusBarUtil.showFullScreen(AudioPlayActivity.this, true);
            }

            @Override
            public void onLayoutCollapsed() {
                StatusBarUtil.showFullScreen(AudioPlayActivity.this, false);
            }

        });
    }

    private boolean isRecyclerViewTop(RecyclerView recyclerView) {
        if (recyclerView != null) {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                int firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                View childAt = recyclerView.getChildAt(0);
                if (childAt == null || (firstVisibleItemPosition == 0 && childAt.getTop() >= 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void initCatalogAdapter() {
        mHeadView = LayoutInflater.from(this).inflate(R.layout.study_head_audio_list, null);
        TextView headTitleView = mHeadView.findViewById(R.id.tv_play_list_title);
//        headTitleView.setTypeface(ArmsUtils.getArialBlack(this));
        mStickyRecyclerView.setLayoutManager(new LinearLayoutManager(AudioPlayActivity.this));
        mMultiCatalogAdapter = new MultiCatalogAdapter(mMultiItemEntityList);
        mStickyRecyclerView.setAdapter(mMultiCatalogAdapter);
        mMultiCatalogAdapter.addHeaderView(mHeadView);
        mMultiCatalogAdapter.expandAll();
    }

    private void initTestData() {
        mMultiItemEntityList = new ArrayList<>();
        mSongInfoList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            CatalogTitbleBean lv0 = new CatalogTitbleBean("管理的真谛到底是什么 " + i);
            for (int j = 0; j < 2; j++) {
                CatalogItemBean lv1 = new CatalogItemBean("", 1);
                lv0.addSubItem(lv1);
            }
            mMultiItemEntityList.add(lv0);
        }

        SongInfo songInfo = new SongInfo();
        songInfo.setArtist("王菲");
        songInfo.setSongCover("http://qukufile2.qianqian.com/data2/pic/4865939a77b87edc79789df87b6f22d8/569080825/569080825.png@s_1,w_500,h_500");
        songInfo.setSongName("无问西东");
        songInfo.setSongId("111");
        songInfo.setDuration(289000L);
        songInfo.setSongUrl("http://m10.music.126.net/20180702162434/a0c99b830f2d04857f286d7d453745ee/ymusic/d1b3/30f0/3e31/1c439bb23a33ac74f00c793ffeed0279.mp3");
        mSongInfoList.add(songInfo);

        SongInfo songInfo2 = new SongInfo();
        songInfo2.setArtist("王菲2");
        songInfo2.setSongCover("http://qukufile2.qianqian.com/data2/pic/2f9f55f0bd9096fe0d8419e009553c1a/556028372/556028372.jpg@s_1,w_500,h_500");
        songInfo2.setSongName("我爱你");
        songInfo2.setSongId("222");
        songInfo2.setDuration(310000L);
        songInfo2.setSongUrl("http://m10.music.126.net/20180702162531/ec7e49087205ff432f86d36ca1b0a728/ymusic/303f/32bb/88ea/44841d887ae848ce1f35c04ca39d8aaf.mp3");
        mSongInfoList.add(songInfo2);
    }

    /**
     * 显示定时选择弹出框
     *
     * @author 
     * @date 2018/6/25 15:36
     */
    private void showTimeBottomSheet() {
        if (mTimeBottomSheet == null) {
            mTimeBottomSheet = new MyBottomSheet.BottomListSheetBuilder(this, true)
                    .addItem(getString(R.string.study_time_current), 60 * 1 * 1000L)
                    .addItem(getString(R.string.study_time_10), 60 * 1 * 1000L)
                    .addItem(getString(R.string.study_time_20), 60 * 2 * 1000L)
                    .addItem(getString(R.string.study_time_30), 60 * 3 * 1000L)
                    .addItem(getString(R.string.study_time_60), 60 * 6 * 1000L)
                    .addItem(getString(R.string.study_time_90), 60 * 9 * 1000L)
                    .addItem(getString(R.string.study_time_close))
                    .setCheckedIndex(6)
                    .setOnSheetItemClickListener((dialog, itemView, position, tag, data) -> {
                        Intent intent = new Intent(AudioPlayActivity.this, AudioTimerService.class);
                        if (position == 0) {
                            intent.putExtra(AudioTimerService.INTENT_AUDIO_TIME, MusicManager.get().getCurrPlayingMusic().getDuration() - MusicManager.get().getProgress());
                            intent.putExtra(AudioTimerService.INTENT_AUDIO_CURRENT, true);
                            startService(intent);
                        } else if (position == 6) {
                            stopService(intent);
                            mTimeText.setText(tag);
                        } else {
                            intent.putExtra(AudioTimerService.INTENT_AUDIO_TIME, (Long) data);
                            intent.putExtra(AudioTimerService.INTENT_AUDIO_CURRENT, false);
                            startService(intent);
                        }
                        dialog.dismiss();
                    })
                    .build();
        }
        mTimeBottomSheet.show();
    }

    /**
     * 显示速率选择弹出框
     *
     * @author 
     * @date 2018/6/25 15:36
     */
    private void showSpeedBottomSheet() {
        if (mSpeedBottomSheet == null) {
            mSpeedBottomSheet = new MyBottomSheet.BottomListSheetBuilder(this, true)
                    .addItem(getString(R.string.study_speed_0_5), 0.5f)
                    .addItem(getString(R.string.study_speed_0_75), 0.75f)
                    .addItem(getString(R.string.study_speed_normal), 1f)
                    .addItem(getString(R.string.study_speed_1_25), 1.25f)
                    .addItem(getString(R.string.study_speed_1_5), 1.5f)
                    .addItem(getString(R.string.study_speed_1_75), 1.75f)
                    .addItem(getString(R.string.study_speed_2), 2f)
                    .setCheckedIndex(2)
                    .setOnSheetItemClickListener(new MyBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                        @Override
                        public void onClick(MyBottomSheet dialog, View itemView, int position, String tag, Object data) {
                            dialog.dismiss();
                            mSpeedText.setText(tag);
                            try {
                                MusicManager.get().setPlaybackParameters((Float) data, 1f);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .build();
        }
        mSpeedBottomSheet.show();
    }


    public void showLoading() {
        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }


    public void hideLoading() {
        mLoadingDialog.dismiss();
    }


    public void showMessage(@NonNull String message) {
        Toasty.normal(this, message).show();
    }

    @OnClick({R.id.iv_audio_back, R.id.iv_video, R.id.ll_time_layout, R.id.ll_speed_layout,
            R.id.iv_song_last, R.id.iv_song_play, R.id.iv_song_next, R.id.iv_song_head_last,
            R.id.iv_song_head_play, R.id.iv_song_head_next, R.id.iv_song_head_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_audio_back:
            case R.id.iv_song_head_back:

                break;
            case R.id.iv_video:

                break;
            case R.id.ll_time_layout:
                showTimeBottomSheet();
                break;
            case R.id.ll_speed_layout:
                showSpeedBottomSheet();
                break;
            case R.id.iv_song_last:
            case R.id.iv_song_head_last:
                if (MusicManager.get().hasPre()) {
                    MusicManager.get().playPre();
                } else {
                    showMessage(getString(R.string.study_no_last_song));
                }
                break;
            case R.id.iv_song_play:
            case R.id.iv_song_head_play:
                if (MusicManager.isPlaying()) {
                    MusicManager.get().pauseMusic();
                } else {
                    MusicManager.get().resumeMusic();
                }
                break;
            case R.id.iv_song_next:
            case R.id.iv_song_head_next:
                if (MusicManager.get().hasNext()) {
                    MusicManager.get().playNext();
                } else {
                    showMessage(getString(R.string.study_no_next_song));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onMusicSwitch(SongInfo music) {
        setPlayOrPauseImage();
        updateUI(music);
        if (isPlayCurrentTime) {
            Intent intent = new Intent(AudioPlayActivity.this, AudioTimerService.class);
            intent.putExtra(AudioTimerService.INTENT_AUDIO_TIME, music.getDuration() - MusicManager.get().getProgress());
            intent.putExtra(AudioTimerService.INTENT_AUDIO_CURRENT, true);
            startService(intent);
        }
    }

    @Override
    public void onPlayerStart() {
        setPlayOrPauseImage();
        mTimerTaskManager.scheduleSeekBarUpdate();
    }

    @Override
    public void onPlayerPause() {
        setPlayOrPauseImage();
        mTimerTaskManager.stopSeekBarUpdate();
    }

    @Override
    public void onPlayCompletion() {
        setPlayOrPauseImage();
        mTimerTaskManager.stopSeekBarUpdate();
        mSongSeekBar.setProgress(0);
        mSongCurrentTime.setText(getString(R.string.study_play_start_time));
    }

    @Override
    public void onPlayerStop() {
//        showMessage("停止播放");
    }

    @Override
    public void onError(String errorMsg) {
        showMessage(errorMsg);
    }

    /**
     * 设置不同状态时播放按钮的图片
     */
    private void setPlayOrPauseImage() {
        if (MusicManager.isPaused()) {
            TransitionManager.beginDelayedTransition(mStickyLayout);
            mSongPlay.setImageResource(R.mipmap.study_ic_audio_pause);
            mSongHeadPlay.setImageResource(R.mipmap.study_ic_audio_pause);
        } else if (MusicManager.isPlaying()) {
            TransitionManager.beginDelayedTransition(mStickyLayout);
            mSongPlay.setImageResource(R.mipmap.study_ic_audio_play);
            mSongHeadPlay.setImageResource(R.mipmap.study_ic_audio_play);
        }
    }

    @Override
    public void onAsyncLoading(boolean isFinishLoading) {
//        showMessage("音频加载完成");
    }




    class AudioTimerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //倒计进度回调
            if (mTimeText != null && intent != null) {
                mTimeText.setText(intent.getStringExtra(AudioTimerService.INTENT_AUDIO_DATA));
                if (intent.hasExtra(AudioTimerService.INTENT_AUDIO_STOP)) {
                    //计时完成
                    Toasty.success(AudioPlayActivity.this, getString(R.string.study_time_play_finish)).show();

                }
                if (intent.getBooleanExtra(AudioTimerService.INTENT_AUDIO_CURRENT, false)) {
                    isPlayCurrentTime = true;
                } else {
                    isPlayCurrentTime = false;
                }
            }
        }
    }


    /**
     * 检查悬浮窗口权限
     * @date 2018/6/28 14:49
     */
    private void checkFloatWindowPermission() {
        if (FloatingPermissionCompat.get().check(this)) {
            FloatWindowManager.get().show(getApplicationContext());//要用applicationContext,不然会内存泄漏
            overridePendingTransition(R.anim.enter_anim_bottom, R.anim.exit_anim_bottom);
        } else {
            if (mDialog == null) {
                mDialog = new QMUIDialog.MessageDialogBuilder(this)
                        .setTitle(R.string.mine_exit_login_title)
                        .setMessage(R.string.study_float_window_des)
                        .addAction(R.string.mine_cancel, (dialog, index) -> {
                            dialog.dismiss();

                            overridePendingTransition(R.anim.enter_anim_bottom, R.anim.exit_anim_bottom);
                        })
                        .addAction(R.string.study_start, (dialog, index) -> {
                            FloatingPermissionCompat.get().apply(AudioPlayActivity.this);
                            dialog.dismiss();
                        }).create(R.style.DialogTheme2);

            }
            mDialog.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FloatWindowManager.get().hide();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimerTaskManager.onRemoveUpdateProgressTask();
        MusicManager.get().removePlayerEventListener(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mAudioTimerReceiver);
    }
}
