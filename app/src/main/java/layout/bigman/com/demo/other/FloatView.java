package layout.bigman.com.demo.other;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.lzx.musiclibrary.aidl.listener.OnPlayerEventListener;
import com.lzx.musiclibrary.aidl.model.SongInfo;
import com.lzx.musiclibrary.manager.MusicManager;
import com.orhanobut.logger.Logger;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.transitionseverywhere.TransitionManager;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import layout.bigman.com.demo.R;


/**
 * 悬浮View
 *
 * @author 曾凡达
 * @date 2018/6/28
 */
public class FloatView extends FrameLayout implements OnPlayerEventListener {

    /**
     * 记录手指按下时在小悬浮窗的View上的横坐标的值
     */
    private float xInView;

    /**
     * 记录手指按下时在小悬浮窗的View上的纵坐标的值
     */
    private float yInView;
    /**
     * 记录当前手指位置在屏幕上的横坐标值
     */
    private float xInScreen;

    /**
     * 记录当前手指位置在屏幕上的纵坐标值
     */
    private float yInScreen;

    /**
     * 记录手指按下时在屏幕上的横坐标的值
     */
    private float xDownInScreen;

    /**
     * 记录手指按下时在屏幕上的纵坐标的值
     */
    private float yDownInScreen;

    private ValueAnimator mAnchorToSideAnimator;
    private ValueAnimator mCircleAnimator;
    private Disposable mDisposable;
    private long mCurrentPlayTime =0L;
    private boolean isShowing = false;
    private WindowManager mWindowManager = null;
    private WindowManager.LayoutParams mParams = null;
    private Context mContext;
    private String mConverUrl = "";

    @BindView(R.id.iv_windown_cover)
    ImageView mWindowCover;

    @BindView(R.id.iv_window_bg)
    ImageView mWindowBg;

    @BindView(R.id.iv_close_window)
    ImageView mWindowClose;



    public FloatView(Context context ) {
        super(context);
        this.mContext = context;
        if (MusicManager.get().getCurrPlayingMusic() != null) {
            this.mConverUrl = MusicManager.get().getCurrPlayingMusic().getSongCover();
        }

        MusicManager.get().addPlayerEventListener(this);
        initView();
    }

    private void initView() {
        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        View mView = LayoutInflater.from(mContext).inflate(R.layout.view_float_window, null);
        addView(mView);
        ButterKnife.bind(this, mView);
        setCoverImage(mConverUrl);
        initMusicCoverAnim();
        startCoverAnim();
    }

    /**
     *  设置封面
     *
     * @author 曾凡达
     * @date 2018/6/29 16:25
     */
    private void setCoverImage(String converUrl) {
//        mImageLoader.loadImage(mContext, ImageConfigImpl.builder()
//                .url(StringUtils.null2EmptyStr(converUrl)).isCrossFade(true)
//                .placeholder(R.mipmap.mine_ic_defult_circle_avator).isCircle(true).imageView(mWindowCover).build());
    }

    @OnClick(R.id.iv_close_window)
    public void closeWindow() {
        MusicManager.get().pauseMusic();
        MusicManager.get().stopNotification();
        FloatWindowManager.get().dismiss();
        mCircleAnimator.removeAllUpdateListeners();
    }

    public void setLayoutParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    public void setShowing(boolean isShowing) {
        this.isShowing = isShowing;
    }

    public boolean isShowing() {
        return isShowing;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                xInScreen = event.getRawX();
                yInScreen = event.getRawY();
                // 手指移动的时候更新小悬浮窗的位置
                if (Math.abs(xDownInScreen - xInScreen) > ViewConfiguration.get(getContext()).getScaledTouchSlop()
                        || Math.abs(yDownInScreen - yInScreen) > ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                    updateViewPosition();
                }
                break;
            case MotionEvent.ACTION_UP:
                //吸附效果
                anchorToSide();
                setCloseImageTimer();
                if (Math.abs(xDownInScreen - xInScreen) <= ViewConfiguration.get(getContext()).getScaledTouchSlop()
                        && Math.abs(yDownInScreen - yInScreen) <= ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                    // 点击效果
                    FloatWindowManager.get().dismiss();
//                    RouterHelper.launchWithBottomAnim(ArmsUtils.obtainAppComponentFromContext(mContext).appManager().getCurrentActivity(), RouterConstants.PATH_STUDY_AUDIO_PLAY);
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 关闭按钮定时
     */
    private void setCloseImageTimer() {
        TransitionManager.beginDelayedTransition(this);
        mWindowClose.setVisibility(VISIBLE);
        mDisposable = Observable.timer(3000, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    TransitionManager.beginDelayedTransition(this);
                    mWindowClose.setVisibility(INVISIBLE);
                });
    }

    private void anchorToSide() {
        int screenWidth = QMUIDisplayHelper.getScreenWidth(mContext);
        int xDistance =0;
        int dp_5 = QMUIDisplayHelper.dp2px(mContext, 5);

        if (mParams.x + getWidth()/2 < screenWidth / 2) {
            xDistance = -mParams.x + dp_5;
        } else if (mParams.x + getWidth()/2 > screenWidth / 2) {
            xDistance = screenWidth - mParams.x - getWidth() - dp_5;
        }

        mAnchorToSideAnimator = ValueAnimator.ofInt(mParams.x, mParams.x+xDistance);
        mAnchorToSideAnimator.removeAllUpdateListeners();

        mAnchorToSideAnimator.addUpdateListener(valueAnimator -> {
            mParams.x = (int) valueAnimator.getAnimatedValue();
            mWindowManager.updateViewLayout(FloatView.this, mParams);
        });
        mAnchorToSideAnimator.setDuration(200).start();
    }

    private void updateViewPosition() {
        //增加移动误差
        Logger.d("x  " + mParams.x + "   y  " + mParams.y);
        mParams.x = (int) (xInScreen - xInView);
        mParams.y = (int) (yInScreen - yInView - QMUIDisplayHelper.getStatusBarHeight(mContext));
        Logger.d("x  " + mParams.x + "   y  " + mParams.y + "xInView  " + xInView + "   yInView  " + yInView);
        mWindowManager.updateViewLayout(this, mParams);
    }

    /**
     * 取消动画
     */
    public void cancelAnimation() {
        if (mAnchorToSideAnimator != null) {
            mAnchorToSideAnimator.removeAllUpdateListeners();
            mAnchorToSideAnimator.cancel();
        }
    }

    /**
     * 清除数据，动画，监听
     */
    public void dismiss() {
        setShowing(false);
        resetCoverAnim();
        cancelAnimation();
        unSubscribe(mDisposable);
        MusicManager.get().removePlayerEventListener(this);
    }


    @Override
    public void onMusicSwitch(SongInfo music) {
        setCoverImage(music.getSongCover());
    }

    @Override
    public void onPlayerStart() {
        startCoverAnim();
    }

    @Override
    public void onPlayerPause() {
        pauseCoverAnim();
    }

    @Override
    public void onPlayCompletion() {
        resetCoverAnim();
    }

    @Override
    public void onPlayerStop() {
        closeWindow();
    }

    @Override
    public void onError(String errorMsg) {
        resetCoverAnim();
    }

    @Override
    public void onAsyncLoading(boolean isFinishLoading) {

    }

    /**
     * 转圈动画
     */
    private void initMusicCoverAnim() {
        mCircleAnimator = ValueAnimator.ofFloat(0.0f, 359.0f);
        mCircleAnimator.setDuration(20000);
        mCircleAnimator.setInterpolator(new LinearInterpolator());
        mCircleAnimator.setRepeatCount(-1);

        mCircleAnimator.addUpdateListener(valueAnimator -> {
            mWindowBg.setRotation((Float) valueAnimator.getAnimatedValue());
            mWindowCover.setRotation((Float) valueAnimator.getAnimatedValue());
        });
    }

    /**
     * 开始转圈
     */
    private void startCoverAnim() {
        if (MusicManager.isPlaying()) {
            resetCoverAnim();
            mCircleAnimator.start();
            mCircleAnimator.setCurrentPlayTime(mCurrentPlayTime);
        }
    }

    private void pauseCoverAnim() {
        mCurrentPlayTime = mCircleAnimator.getCurrentPlayTime();
        mCircleAnimator.cancel();
    }

    private void resetCoverAnim() {
        pauseCoverAnim();
        mWindowCover.setRotation(0);
        mWindowBg.setRotation(0);
    }

    public void unSubscribe(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

}
