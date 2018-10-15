package layout.bigman.com.demo.other;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.extra.Scale;

/**
 * 悬浮窗口管理类
 *
 * @author 曾凡达
 * @date 2018/6/28
 */
public class FloatWindowManager {

    private static volatile FloatWindowManager mInstance;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private FloatView mFloatView;

    public static FloatWindowManager get() {
        if (mInstance == null) {
            synchronized ((FloatWindowManager.class)) {
                if (mInstance == null) {
                    mInstance = new FloatWindowManager();
                }
            }
        }
        return mInstance;
    }


    /**
     * 显示窗口是否正在显示中
     *
     * @author 曾凡达
     * @date 2018/6/28 15:13
     */
    public boolean isShowing() {
        return mFloatView != null && mFloatView.isShowing();
    }

    /**
     * 显示窗口
     *
     * @author 曾凡达
     * @date 2018/6/28 15:15
     */
    public void show(Context context) {
        if (!isShowing()) {
            if (mWindowManager == null) {
                mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            }

            mFloatView = new FloatView(context);
            mLayoutParams = new WindowManager.LayoutParams();
            mLayoutParams.packageName = context.getPackageName();
            mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }

            mLayoutParams.format = PixelFormat.RGBA_8888;
            mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;//坐标点参照，左上角
            mLayoutParams.x = QMUIDisplayHelper.getScreenWidth(context) - QMUIDisplayHelper.dpToPx(75);
            mLayoutParams.y = QMUIDisplayHelper.getScreenHeight(context) * 2 / 3;

            mFloatView.setLayoutParams(mLayoutParams);
            mFloatView.setShowing(true);
            mWindowManager.addView(mFloatView, mLayoutParams);
        }
    }

    /**
     * 关闭窗口
     *
     * @author 曾凡达
     * @date 2018/6/28 15:30
     */
    public void dismiss() {
        if (isShowing()) {
            if (mFloatView != null) {
                mFloatView.dismiss();
                if (mWindowManager != null) {
                    mWindowManager.removeViewImmediate(mFloatView);
                }
            }
        }
    }

    /**
     * 隐藏窗口，只是不可见而已
     */
    public void hide() {
        if (mFloatView != null) {
            if (isShowing()) {
                TransitionManager.beginDelayedTransition(mFloatView, new Scale());
                if (mFloatView.getVisibility() == View.VISIBLE) {
                    mFloatView.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * 显示窗口
     */
    public void visible() {
        if (mFloatView != null) {
            if (isShowing()) {
                TransitionManager.beginDelayedTransition(mFloatView, new Scale());
                if (mFloatView.getVisibility() == View.GONE) {
                    mFloatView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

}
