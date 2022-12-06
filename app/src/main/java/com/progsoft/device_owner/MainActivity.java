package com.progsoft.device_owner;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;

/**
 * Created by derohimat on 19/08/2016.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private Button mBtnState, mBtnMove, mBtnCamera;
    private TextView mTV, mLT;
    private EditText mET, mET2;
    private static Thread mThread = null;


    class MyRunnalbe implements Runnable {
        @Override
        public void run() {
            int DEFAULT_DELAY = 3;
            do {
                Date date = new Date();
                checkDpm();
                int k = KioskModeApp.getTimeToLock();
                if (k % 5 == 1 || k < 6) {
                    FileWrite("Count:" + k);
                }
                updateButtonState();
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
                        KioskModeApp.setTestNum(8);
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

        mBtnState = findViewById(R.id.btnState);
        mBtnMove = findViewById(R.id.btnMove);
        mBtnCamera = findViewById(R.id.btmCamera);
        mTV = findViewById(R.id.textView);
        mLT = findViewById(R.id.LastTime);
        mET = findViewById(R.id.editTextTextPersonName);

        mTV.setText("PC-VER:" + BuildConfig.VERSION_NAME + " " + BuildConfig.BUILD_TYPE);

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
            FileWrite("MainActivity MyRunable, mThread null. " + this);
        } else {
            FileWrite("MainActivity MyRunable:" + mThread.isAlive());
            mThread.interrupt();
            mThread = null;
        }

        if (mThread == null || !mThread.isAlive()) {
            MyRunnalbe run = new MyRunnalbe();
            mThread = new Thread(run, "Timer Thread");
            mThread.start();
            FileWrite("MainActivity MyRunable New Thread. " + this);
        }
        if (KioskModeApp.isIsSupperMode())
            finish();
    }

    private void updateButtonState() {
        if (KioskModeApp.isInLockMode()) {
            mBtnState.setText("Disable Kiosk Mode");
        } else {
            mBtnState.setText("Enable Kiosk Mode");
        }
        mBtnCamera.setText("Time to Lock:" + KioskModeApp.getTimeToLock() * 3 + "s");
        mET.setText("" + KioskModeApp.getLastTimeLocked());
        mLT.setText("" + KioskModeApp.getLastTimeLocked());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View view) {
        FileWrite("MainActivity onClick: " + view);
        if (view.getId() == R.id.btnState) {
            enableKioskMode(!KioskModeApp.isInLockMode());
            updateButtonState();
        } else if (view.getId() == R.id.btnMove) {
            int min, max;

            mET2 = findViewById(R.id.min);
            min = Integer.parseInt(mET2.getText().toString());
            mET2 = findViewById(R.id.max);
            max = Integer.parseInt(mET2.getText().toString());
            KioskModeApp.setRange(min, max);

            mET2 = findViewById(R.id.level);
            KioskModeApp.setLevel(Integer.parseInt(mET2.getText().toString()));

            mET2 = findViewById(R.id.number);
            KioskModeApp.setTestNum(Integer.parseInt(mET2.getText().toString()));
            SecondActivity.startThisActivity(mContext);
        } else if (view.getId() == R.id.btmCamera) {
            KioskModeApp.setTimeToLock(3);
            //reportAnswer(mET.getText().toString());
            //Intent service = new Intent(MainActivity.this, MyService.class);
            //startForegroundService(service);

        }
    }
}
