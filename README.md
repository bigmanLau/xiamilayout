# Xiamilayout 
#####虾米播放界面的滑动布局做的很好，故这里模仿了一个
先看效果图吧
![Gif_20181015_160653.gif](https://upload-images.jianshu.io/upload_images/12262980-ace094c07f477a0a.gif?imageMogr2/auto-orient/strip)

#####具体的实现逻辑是这样的
自定义一个ViewGroup,然后通过事件拦截和改变视图的topMargin的属性动画来实现的，具体我们分析一下核心代码
>1.获取布局中初始margin边距
````
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
````
>2.事件拦截
````
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
````
这里的主要逻辑就是如果是展开状态，就是头部视图不可见时，事件都由父布局即我们自定义的这个容器处理，如果是折叠状态，如果用户下滑就由父布局处理，否则由子视图处理
>3.父布局触摸事件处理
````
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
````
这里主要是折叠和展开动画，这里有个阈值就是如果上滑超过原来的topMargin的六分之一，就折叠布局，如果下滑，就展开布局，滑动主要用属性动画完成，具体可以去看看代码


>4.最后看看布局代码
````
<?xml version="1.0" encoding="utf-8"?>
<layout.bigman.com.layout.XiaMiLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/sticky_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <include layout="@layout/study_include_audio_play_content" />

    <include layout="@layout/study_include_audio_play_head" />

    <android.support.v7.widget.RecyclerView
        android:id="@+id/sticky_recycler_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_marginTop="538dp"
        android:background="@color/base_white" />

</layout.bigman.com.layout.XiaMiLayout>
````
这里布局的核心就是每个布局的topMargin的设置，比如这里的head布局topMargin为负66dp，content布局高度为538dp和topMargin为0dp，RecyclerView的topMargin为538dp，这些值为初始状态也是展开状态
>5.最后说明一下

折叠状态时的值状态时head的topMargin为0dp，content布局高度为538dp和topMargin为66dp，RecyclerView的topMargin为66dp



依赖方式
Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:

````
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
````
Step 2. Add the dependency
````
dependencies {
	        implementation 'com.github.bigmanLau:xiamilayout:1.0.0'
	}
````


##### 有些同学不喜欢我贴代码，那么直接上我的github地址去看

具体原理看代码 
[github地址] (https://github.com/bigmanLau/xiamilayout)

>注意：三个布局的id是有要求的 ，必须和我demo中的布局一直，或者去库的ids.xml文件去看看




       please  buy me a cup of  Cappuccinoz![weixin.png](https://upload-images.jianshu.io/upload_images/12262980-bf63eba14451d236.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![zhifubao.jpg](https://upload-images.jianshu.io/upload_images/12262980-41391bb7afb9c1b5.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
