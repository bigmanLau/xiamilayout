package layout.bigman.com.layout;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import com.zhy.autolayout.AutoFrameLayout;

/**
 * 仿虾米手势滑动
 * @date 2018/6/13
 */
public class XiaMiLayout extends AutoFrameLayout {

    /**
     * 分别记录上次滑动的坐标
     */
    private float mLastY = 0;

    /**
     * 分别记录上次滑动的坐标(onInterceptTouchEvent)
     */
    private float mLastYIntercept = 0;

    private int mTouchSlop;
    private static final int ANIMATION_TIME = 200;

    private View mContentView, mHeadView, mBottomView;

    private LayoutParams mBottomViewParams, mHeadViewParams, mContentViewParams;

    private int mOriginalBottomMargin, mOriginalHeadMargin, mOriginalMiddleMargin, mOriginalContentMargin;

    private ValueAnimator mBottomAnimator, mHeadAnimator, mContentAnimator;

    private OnTouchEventListener mOnTouchEventListener;

    /**
     *  两种状态 折叠和展开
     *  头部播放条默认展开的，对于用户是不可见的
     *  折叠的意思是完全显示出来
     */
    private STATUS mStatus = STATUS.EXPANDED_STATUS;

    //滑动两个方向 上下方向
    private DIRECTION mDirection = DIRECTION.UP;


    //折叠和展开
    private enum STATUS {
        COLLAPSED_STATUS, EXPANDED_STATUS
    }

    enum DIRECTION {
        UP, DOWN
    }

    public XiaMiLayout(@NonNull Context context) {
        this(context, null);
    }

    public XiaMiLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XiaMiLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获取安卓默认的触摸阈值
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setClickable(true);

    }

    //布局完毕之后获取默认参数
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContentView = findViewById(R.id.sticky_content_view);
        mHeadView = findViewById(R.id.sticky_head_view);
        mBottomView = findViewById(R.id.sticky_recycler_view);
        mBottomViewParams = (LayoutParams) mBottomView.getLayoutParams();
        mHeadViewParams = (LayoutParams) mHeadView.getLayoutParams();
        mContentViewParams = (LayoutParams) mContentView.getLayoutParams();
        mOriginalBottomMargin = mBottomViewParams.topMargin;
        mOriginalHeadMargin = mHeadViewParams.topMargin;
        mOriginalContentMargin = mContentViewParams.topMargin;
        mOriginalMiddleMargin = mOriginalBottomMargin + mOriginalHeadMargin;

    }

    public void setOnTouchEventListener(OnTouchEventListener topTouchEventListener) {
        mOnTouchEventListener = topTouchEventListener;
    }


    //拦截触摸事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        float y = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //记录开始触摸点y坐标
                mLastYIntercept = y;
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                //y轴方向移动差值
                float deltaY = y - mLastYIntercept;
                if (mStatus == STATUS.COLLAPSED_STATUS) {//头部View是折叠的状态
                    if (mOnTouchEventListener != null) {
                        //这里需要说明一下,如果我们的列表已经滑动到了顶部
                        //用户下滑，触摸事件就交给父布局处理就是StickLayout处理
                        if (mOnTouchEventListener.isBottomViewTop()) {//内容View滑到了顶部
                            if (deltaY > 0 && Math.abs(deltaY) > mTouchSlop) {//下滑，父布局拦截
                                return true;
                            }
                        }
                    }
                } else if (Math.abs(deltaY) > mTouchSlop) {//头部View是展开的状态
                    //只要头部View是展开状态都由父布局处理就是StickLayout处理
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                mLastYIntercept = 0;
                break;
            default:
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }


    //父布局处理触摸事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaY = y - mLastY;
                if (mDirection == DIRECTION.UP) {
                    //收拢，手指可上下滑动
                    //底部View处理
                    if (mBottomViewParams.topMargin > Math.abs(mOriginalHeadMargin)) {
                        //底部View处理
                        setUpBottomMargin(deltaY);

                        //头部View处理
                        float percent = setUpHeadMargin();

                        //内容View处理，移动的高度等于头部View的高度
                        setContentMargin((int) (percent * mOriginalHeadMargin));
                    }
                } else if (mStatus == STATUS.COLLAPSED_STATUS) {
                    //展开，手指可上下滑动
                    if (mBottomViewParams.topMargin < mOriginalBottomMargin) {
                        //底部View处理
                        setDownBottomMargin(deltaY);

                        //头部View处理
                        float percent = setDownHeadMargin();

                        //内容View处理，移动的高度等于头部View的高度
                        setContentMargin(mOriginalHeadMargin - (int) (percent * mOriginalHeadMargin));

                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mDirection == DIRECTION.UP) {
                    dealUpSlidAnimator();
                } else {
                    dealDownSlidAnimator();
                }
                break;
            default:
                break;
        }
        mLastY = y;
        return super.onTouchEvent(event);
    }

    //设置下滑时头部margin
    private float setDownHeadMargin() {
        float moveheight = mBottomViewParams.topMargin - Math.abs(mOriginalHeadMargin);//底部View移动的高度
        float percent = moveheight / mOriginalMiddleMargin;//底部View移动的百分比
        mHeadViewParams.topMargin = (int) (percent * mOriginalHeadMargin);//头部View移动的高度
        mHeadView.setLayoutParams(mHeadViewParams);
        return percent;
    }

    //设置上滑时头部margin
    private float setUpHeadMargin() {
        float moveheight = mOriginalBottomMargin - mBottomViewParams.topMargin;//底部View移动的高度
        float percent = moveheight / mOriginalMiddleMargin;//底部View移动的百分比
        mHeadViewParams.topMargin = mOriginalHeadMargin - (int) (percent * mOriginalHeadMargin);//头部View移动的高度
        mHeadView.setLayoutParams(mHeadViewParams);
        return percent;
    }

    //设置上滑时底部margin
    private void setUpBottomMargin(float deltaY) {
        mBottomViewParams.topMargin += deltaY;
        //防止上滑越界，还没回调MOVE
        preventUpBottomBeyond();
        mBottomView.setLayoutParams(mBottomViewParams);
    }


    //设置下滑时底部margin
    private void setDownBottomMargin(float deltaY) {
        mBottomViewParams.topMargin += deltaY;
        //防止下滑越界，还没回调MOVE
        preventDownBottomBeyond();
        mBottomView.setLayoutParams(mBottomViewParams);
    }

    //防止下滑越界
    private void preventDownBottomBeyond() {
        if (mBottomViewParams.topMargin > mOriginalBottomMargin) {
            mBottomViewParams.topMargin = mOriginalBottomMargin;
        } else if (mBottomViewParams.topMargin < Math.abs(mOriginalHeadMargin)) {
            mBottomViewParams.topMargin = Math.abs(mOriginalHeadMargin);
        }
    }

    //防止上滑越界
    private void preventUpBottomBeyond() {
        if (mBottomViewParams.topMargin < Math.abs(mOriginalHeadMargin)) {
            mBottomViewParams.topMargin = Math.abs(mOriginalHeadMargin);
        } else if (mBottomViewParams.topMargin >= mOriginalBottomMargin) {
            mBottomViewParams.topMargin =mOriginalBottomMargin;
        }
    }

    //设置内容margin
    private void setContentMargin(int margin) {
        mContentViewParams.topMargin = margin;
        mContentView.setLayoutParams(mContentViewParams);
    }

    /**
     * 处理上滑逻辑
     * 利用属性动画做视图滑动
     */
    private void dealUpSlidAnimator() {

        if (isUpSlideScrollToSlide() ) {
            //滑动距离超过阈值折叠
            mStatus = STATUS.COLLAPSED_STATUS;
            //改变状态就要重置这个方向值
            mDirection = DIRECTION.DOWN;
            if (mOnTouchEventListener != null) {
                mOnTouchEventListener.onLayoutExpanded();
            }
            mHeadAnimator = ValueAnimator.ofInt(mHeadViewParams.topMargin, 0);
            mContentAnimator = ValueAnimator.ofInt(mContentViewParams.topMargin, mOriginalHeadMargin);
            mBottomAnimator = ValueAnimator.ofInt(mBottomViewParams.topMargin, Math.abs(mOriginalHeadMargin));
        } else {
            //没有超过阈值重新展开
            mHeadAnimator = ValueAnimator.ofInt(mHeadViewParams.topMargin, mOriginalHeadMargin);
            mContentAnimator = ValueAnimator.ofInt(mContentViewParams.topMargin, mOriginalContentMargin);
            mBottomAnimator = ValueAnimator.ofInt(mBottomViewParams.topMargin, mOriginalBottomMargin);
        }

        doAnimation();

    }

    /**
     * 处理下滑逻辑
     * 利用属性动画做视图滑动
     */
    private void dealDownSlidAnimator() {
        if (isDownSlideScrollToSlide() ) {
            //滑动距离超过阈值展开
            mStatus = STATUS.EXPANDED_STATUS;
            //改变状态就要重置这个方向值
            mDirection = DIRECTION.UP;
            if (mOnTouchEventListener != null) {
                mOnTouchEventListener.onLayoutCollapsed();
            }
            mHeadAnimator = ValueAnimator.ofInt(mHeadViewParams.topMargin, mOriginalHeadMargin);
            mContentAnimator = ValueAnimator.ofInt(mContentViewParams.topMargin, mOriginalContentMargin);
            mBottomAnimator = ValueAnimator.ofInt(mBottomViewParams.topMargin, mOriginalBottomMargin);
        } else {
            //没有超过阈值重新折叠
            mHeadAnimator = ValueAnimator.ofInt(mHeadViewParams.topMargin, 0);
            mContentAnimator = ValueAnimator.ofInt(mContentViewParams.topMargin, mOriginalHeadMargin);
            mBottomAnimator = ValueAnimator.ofInt(mBottomViewParams.topMargin, Math.abs(mOriginalHeadMargin));
        }

        //开始属性动画
        doAnimation();

    }


    /**
     * 设置topmargin属性动画
     */
    private void doAnimation() {
        mHeadAnimator.removeAllUpdateListeners();
        mContentAnimator.removeAllUpdateListeners();
        mBottomAnimator.removeAllUpdateListeners();

        mHeadAnimator.addUpdateListener(valueAnimator -> {
            mHeadViewParams.topMargin = (int) valueAnimator.getAnimatedValue();
            mHeadView.setLayoutParams(mHeadViewParams);
        });

        mContentAnimator.addUpdateListener(valueAnimator -> {
            mContentViewParams.topMargin = (int) valueAnimator.getAnimatedValue();
            mContentView.setLayoutParams(mContentViewParams);
        });

        mBottomAnimator.addUpdateListener(valueAnimator -> {
            mBottomViewParams.topMargin = (int) valueAnimator.getAnimatedValue();
            mBottomView.setLayoutParams(mBottomViewParams);
        });

        mHeadAnimator.setDuration(ANIMATION_TIME).start();
        mContentAnimator.setDuration(ANIMATION_TIME).start();
        mBottomAnimator.setDuration(ANIMATION_TIME).start();
    }

    /**
     * 上滑到两边的临界点
     * 临界点是1/6
     */
    private boolean isUpSlideScrollToSlide() {
        //滑动距离超过底部View的topMargin的阈值
        return mBottomViewParams.topMargin < mOriginalBottomMargin *5/6 ;
    }

    /**
     * 下滑动到两边的临界点
     * 临界点是1/6
     */
    private boolean isDownSlideScrollToSlide() {
        //滑动距离超过底部View的topMargin的阈值
        return mBottomViewParams.topMargin > mOriginalBottomMargin /6;
    }

    public interface OnTouchEventListener {
        boolean isBottomViewTop();

        void onLayoutExpanded();

        void onLayoutCollapsed();

    }

}
