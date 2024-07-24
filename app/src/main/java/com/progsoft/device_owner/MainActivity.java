package com.progsoft.device_owner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import com.progsoft.device_owner.iflytek.TtsDemo;

import java.util.Date;

@SuppressLint("SetTextI18n")
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private Button mBtnState;
    private Button mBtnCamera;
    private TextView mLT;
    private EditText mET, mET2;
    private static Thread mThread = null;
    private FloatingWindow mFloatingWindow;


    class MyRunnable implements Runnable {
        @Override
        public void run() {
            int DEFAULT_DELAY = 3;
            do {
                checkDpm();
                int k = KioskModeApp.getTimeToLock();
                if (k % 5 == 1 || k < 6) {
                    FileWrite("Count:" + k);
                }
                updateButtonState();
                // 由于健康使用过手机时间超时所以没有锁定屏幕成功，但是已经进入锁定模式true
                // 所以k一直没设置为10，无法进入下面isTimeToLock去--操作，也就没有count打印，也无法再次设置
                // 重启界面的interrupt exit1.2 exit3是老线程关闭打印， ****终止了也没法改变锁定状态****
                if (!KioskModeApp.isInLockMode()) {
                    if (KioskModeApp.isTimeToLock()) {
                        /*
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        try {
                            Thread.sleep(2000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                         */
                        //KioskModeApp.setTimeToLock(5);
                        KioskModeApp.setTestNum(12);
                        Intent intent2 = new Intent(getApplicationContext(), SecondActivity.class);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent2);
                        try {
                            Thread.sleep(1500L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            FileWrite("Exit Thread, exit1.1");
                            break;
                        }
                        enableKioskMode(true);
                        KioskModeApp.setLastTimeToLocked(new Date().toString());
                        FileWrite("enableKioskMode: true");
                    } else {
                        enableKioskMode(false);
                        if (k % 5 == 1 || k < 6) {
                            FileWrite("enableKioskMode: false");
                        }
                    }
                }
                try {
                    Thread.sleep(DEFAULT_DELAY * 1000L);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    FileWrite("Exit Thread, exit1.2");
                    break;
                }

                if (Thread.interrupted()) {
                    FileWrite("Exit Thread, exit2");
                    break;
                }
            } while(true);
            FileWrite("Exit Thread, exit3");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FileWrite("MainActivity onResume:" + this);
        setUpAdmin();
        updateButtonState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileWrite("MainActivity onDestroy:" + this);
        //mThread.interrupt(); //必须杀死该线程否则MainActivity又会被拉起来
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //requestPermission();

        mBtnState = findViewById(R.id.btnState);
        Button mBtnMove = findViewById(R.id.btnMove);
        mBtnCamera = findViewById(R.id.btmCamera);
        TextView mTV = findViewById(R.id.textView);
        mLT = findViewById(R.id.LastTime);
        mET = findViewById(R.id.editTextTextPersonName);

        mTV.setText("Version:" + BuildConfig.VERSION_NAME + " " + BuildConfig.BUILD_TYPE + " (" + BuildConfig.APPLICATION_ID  + ")");

        mET2 = findViewById(R.id.min);
        mET2.setText("" + KioskModeApp.getMin());
        mET2 = findViewById(R.id.max);
        mET2.setText("" + KioskModeApp.getMax());
        /* 杀死后不能恢复 */

        FileWrite("MainActivity onCreate:" + this);
        mBtnState.setOnClickListener(this);
        mBtnMove.setOnClickListener(this);
        mBtnCamera.setOnClickListener(this);

        setUpAdmin();
        updateButtonState();


        checkDpm();
        if (mThread == null) {
            FileWrite("MainActivity MyRunnable, mThread null. " + this);
        } else {
            FileWrite("MainActivity MyRunnable:" + mThread.isAlive());
            mThread.interrupt();
            mThread = null;
        }

        if (mThread == null || !mThread.isAlive()) {
            MyRunnable run = new MyRunnable();
            mThread = new Thread(run, "Timer Thread");
            mThread.start();
            FileWrite("MainActivity MyRunnable New Thread. " + this);
        }
        if (KioskModeApp.isIsSupperMode())
            finish();
    }

    @SuppressLint("DefaultLocale")
    private void updateButtonState() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int left = KioskModeApp.getTimeToLock() * 3;
                int min, sec, hr;
                sec = left % 60;
                min = (left / 60) % 60;
                hr = (left / 3600) % 60;

                FloatingWindow.getInstance().updateText(String.format("%02d:%02d:%02d", hr, min, sec));
                if (KioskModeApp.isInLockMode()) {
                    mBtnState.setText("Disable Kiosk Mode");
                } else {
                    mBtnState.setText("Enable Kiosk Mode");
                    switch (left) {
                        case 60 * 60:
                            showFloatingWindow();
                            TtsDemo.getInstance(getApplicationContext()).playText("锁屏测验，还剩60分钟");
                            break;
                        case 60 * 30:
                            showFloatingWindow();
                            TtsDemo.getInstance(getApplicationContext()).playText("锁屏测验，还剩30分钟");
                            break;
                        case 60 * 10:
                            showFloatingWindow();
                            TtsDemo.getInstance(getApplicationContext()).playText("注意，还剩10分钟");
                            break;
                        case 60 * 5:
                            showFloatingWindow();
                            TtsDemo.getInstance(getApplicationContext()).playText("注意注意，还剩5分钟");
                            break;
                        case 60:
                            showFloatingWindow();
                            TtsDemo.getInstance(getApplicationContext()).playText("注意非常紧急，还剩1分钟");
                            break;
                        case 30:
                            showFloatingWindow();
                            TtsDemo.getInstance(getApplicationContext()).playText("注意最后躲避机会，还剩30秒");
                            break;
                    }
                }
                mBtnCamera.setText("Time to Lock:" + left + "秒");
                mET.setText(KioskModeApp.getLastTimeLocked());
                mLT.setText(KioskModeApp.getLastTimeLocked());
            }
        });
    }

    private void showFloatingWindow() {
        mFloatingWindow = FloatingWindow.getInstance();
        View view = initFloatView();
        mFloatingWindow.showFloatingWindowView(this, view);
        //Log.e(TAG, " mFloatingWindow：" + mFloatingWindow + " view：" + view);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        //Log.e(TAG, dm.widthPixels + " " + dm.heightPixels);
    }

    private View initFloatView() {
        View view = View.inflate(this, R.layout.view_floating_window, null);
        // 设置视频封面
        //final ImageView mThumb = (ImageView) view.findViewById(R.id.thumb_floating_view);
        //mThumb.setImageResource(R.drawable.white);
        //mThumb.setImageResource(R.mipmap.icon_power);
        //Glide.with(this).load(R.drawable.thumb).into(mThumb);

        //String font = "fonts/GenJyuuGothic-Monospace-Bold-2.ttf";
        //Typeface typeface = Typeface.createFromAsset(getAssets(), font);
        TextView tv;
        tv = (TextView) view.findViewById(R.id.textView);
        tv.setTextSize(30);
        tv.setTextColor(Color.RED);
        //tv2.setTypeface(typeface);
        //tv2.setText("");
        //tv2.append("@@:##:**\n");

        // 悬浮窗关闭
        view.findViewById(R.id.close_floating_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFloatingWindow.dismiss();
            }
        });
        // 返回前台页面
        view.findViewById(R.id.back_floating_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFloatingWindow.setTopApp(MainActivity.this);
                //mFloatingWindow.dismiss();
            }
        });
        return view;
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View view) {
        FileWrite("MainActivity onClick: " + view);
        if (view.getId() == R.id.btnState) {
            enableKioskMode(!KioskModeApp.isInLockMode());
            updateButtonState();
            showFloatingWindow();
        } else if (view.getId() == R.id.btnMove) {
            int min, max;
            mET2 = findViewById(R.id.level);
            int level;
            level = Integer.parseInt(mET2.getText().toString());
            if (level == 0) {
                KioskModeApp.setLevel(10);
                mET2.setText("10");

                mET2 = findViewById(R.id.min);
                min = Integer.parseInt(mET2.getText().toString());
                mET2 = findViewById(R.id.max);
                max = Integer.parseInt(mET2.getText().toString());
                KioskModeApp.setRange(min, max);

                mET2 = findViewById(R.id.number);
                KioskModeApp.setTestNum(Integer.parseInt(mET2.getText().toString()));
            } else {
                KioskModeApp.setLevel(level);

                mET2 = findViewById(R.id.min);
                mET2.setText("" + KioskModeApp.getMin());
                mET2 = findViewById(R.id.max);
                mET2.setText("" + KioskModeApp.getMax());

                mET2 = findViewById(R.id.number);
                mET2.setText("" + KioskModeApp.getTestNum());
            }
            SecondActivity.startThisActivity(mContext);
        } else if (view.getId() == R.id.btmCamera) {
            KioskModeApp.setTimeToLock(3);
            //reportAnswer(mET.getText().toString());
            //Intent service = new Intent(MainActivity.this, MyService.class);
            //startForegroundService(service);

        }
    }
    private static final int REQUEST_CODE = 1024;
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //先判断有没有权限
            if (Environment.isExternalStorageManager()) {
                writeFile();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
        }
    }
    /**
     * 模拟文件写人
     */
    private void writeFile() {
        Log.e("aaaa", "写入文件成功");
    }
}
