package com.progsoft.device_owner;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.List;


public class FloatingWindow {

    private WindowManager mWindowManager;
    private View mShowView;

    private TextView tv;

    private WindowManager.LayoutParams mFloatParams;

    @SuppressLint("StaticFieldLeak")
    private static volatile FloatingWindow sInstance;
    private static final Object SERVICE_LOCK = new Object();
    public static FloatingWindow getInstance( /* Context context, Activity activity*/) {
        synchronized (SERVICE_LOCK) {
            if (sInstance == null) {
                sInstance = new FloatingWindow();
            }
            return sInstance;
        }
    }
    public void updateText(String text) {
        if (mShowView != null) {
            tv.setText(text);
            /*
            c++;
            tv.setText("text == " + c);
            Log.e("Huang", mShowView + " " + c);
            Log.e("Huang", tv + " " + text);
             */
        }
    }
    public void showFloatingWindowView(Context context, View view) {

        if (mShowView != null)
            return;
        // 悬浮窗显示视图
        mShowView = view;

        tv = mShowView.findViewById(R.id.textView);

        // 获取系统窗口管理服务
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        // 悬浮窗口参数设置及返回
        mFloatParams = getParams();
        // 设置窗口触摸移动事件
        mShowView.setOnTouchListener(new FloatViewMoveListener());
        // 悬浮窗生成
        mWindowManager.addView(mShowView, mFloatParams);
    }

    private WindowManager.LayoutParams getParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        //设置悬浮窗口类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        //设置悬浮窗口属性
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        //设置悬浮窗口透明
        //layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.alpha = (float) 0.85;

        layoutParams.format = PixelFormat.TRANSPARENT;
        //layoutParams.format = PixelFormat.OPAQUE;

        //layoutParams.format = PixelFormat.RGB_888;
        //设置悬浮窗口长宽数据
        /*
        layoutParams.width = 1212;
        layoutParams.height = 2517 + 200;
         */

//        layoutParams.width = 1060;
//        layoutParams.height = 720;

        layoutParams.width = 480;
        layoutParams.height = 120;

        //设置悬浮窗显示位置
        layoutParams.gravity = Gravity.TOP | Gravity.CENTER;
        //layoutParams.x = 100;
        layoutParams.y = 80;
        return layoutParams;
    }

    public void dismiss() {
        if (mWindowManager != null && mShowView != null && mShowView.isAttachedToWindow()) {
            mWindowManager.removeView(mShowView);
            mShowView = null;
        }
    }

    /**
     * 浮窗移动/点击监听
     */
    private class FloatViewMoveListener implements View.OnTouchListener {

        //开始触控的坐标，移动时的坐标（相对于屏幕左上角的坐标）
        private int mTouchStartX;
        private int mTouchStartY;
        //开始时的坐标和结束时的坐标（相对于自身控件的坐标）
        private int mStartX, mStartY;
        //判断悬浮窗口是否移动，这里做个标记，防止移动后松手触发了点击事件
        private boolean isMove;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int action = motionEvent.getAction();
            int x = (int) motionEvent.getX();
            int y = (int) motionEvent.getY();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    isMove = false;
                    mTouchStartX = (int) motionEvent.getRawX();
                    mTouchStartY = (int) motionEvent.getRawY();
                    mStartX = x;
                    mStartY = y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int mTouchCurrentX = (int) motionEvent.getRawX();
                    int mTouchCurrentY = (int) motionEvent.getRawY();
                    mFloatParams.x += mTouchCurrentX - mTouchStartX;
                    mFloatParams.y += mTouchCurrentY - mTouchStartY;
                    mWindowManager.updateViewLayout(mShowView, mFloatParams);
                    mTouchStartX = mTouchCurrentX;
                    mTouchStartY = mTouchCurrentY;
                    float deltaX = x - mStartX;
                    float deltaY = y - mStartY;
                    if (Math.abs(deltaX) >= 5 || Math.abs(deltaY) >= 5) {
                        isMove = true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                default:
                    break;
            }
            //如果是移动事件不触发OnClick事件，防止移动的时候一放手形成点击事件
            return isMove;
        }
    }

    /**
     * 将本应用置顶到最前端
     * 当本应用位于后台时，则将它切换到最前端
     *
     * @param context 上下文
     */
    public void setTopApp(Context context) {
        //获取ActivityManager
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //获得当前运行的task(任务)
        List<ActivityManager.RunningTaskInfo> taskInfoList = null;
        if (activityManager != null) {
            taskInfoList = activityManager.getRunningTasks(100);
        }
        if (taskInfoList != null) {
            for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
                //找到本应用的 task，并将它切换到前台
                if (taskInfo.topActivity != null && taskInfo.topActivity.getPackageName().equals(context.getPackageName())) {
                    activityManager.moveTaskToFront(taskInfo.id, 0);
                    break;
                }
            }
        }
    }
}