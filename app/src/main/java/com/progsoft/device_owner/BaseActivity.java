package com.progsoft.device_owner;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "PROG_BaseActivity";
    protected Context mContext = this;
    protected View mDecorView;
    protected static DevicePolicyManager mDpm;

    protected void setUpAdmin() {
        mDpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (!KioskModeApp.isInLockMode()) {
            ComponentName deviceAdmin = new ComponentName(this, AdminReceiver.class);
            mDpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (!mDpm.isAdminActive(deviceAdmin)) {
                FileWrite("enableKioskMode Error " + getString(R.string.not_device_admin));
                //Log.e("Kiosk Mode Error", getString(R.string.not_device_admin));
            }

            if (mDpm.isDeviceOwnerApp(getPackageName())) {
                mDpm.setLockTaskPackages(deviceAdmin, new String[]{getPackageName()});
            } else {
                FileWrite("enableKioskMode Error " + getString(R.string.not_device_owner));
                //Log.e("Kiosk Mode Error", getString(R.string.not_device_owner));
            }

            //enableKioskMode(true);
            //TODO : for clear device Owner
//        } else {
//            mDpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
//            mDpm.clearDeviceOwnerApp(getPackageName());
        }

        mDecorView = getWindow().getDecorView();
        hideSystemUI();
    }

    public void FileWrite(String context) {
        try {
            String newTitle = "";
            File file;
            if (context.contains("SecondActivity")) {
                file = new File(Environment.getExternalStorageDirectory(), "progsoft/Kiosk/exam.txt");
            } else {
                file = new File(Environment.getExternalStorageDirectory(), "progsoft/Kiosk/log.txt");
            }
            String directory = file.toString().substring(0, file.toString().lastIndexOf("/"));
            new File(directory).mkdir();

            if (!file.exists()) {
                newTitle = "开启新的一天，加油吧\n";
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            Date now = new Date();
            bw.write(newTitle + now + " , " + context);
            bw.newLine();
            bw.flush();
            bw.close();
            Log.e(TAG, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void checkDpm() {
        if (mDpm == null) {
            Log.e("checkDpm****", "mDpm null");
        } else {
            Log.e("checkDpm****", "mDpm " + mDpm);
        }
    }

    protected void enableKioskMode(boolean enabled) {
        try {
            if (enabled) {
                if (mDpm == null ) {
                    FileWrite("enableKioskMode Error mDpm null");
                }
                if (mDpm.isLockTaskPermitted(this.getPackageName())) {
                    KioskModeApp.setIsInLockMode(true);
                    startLockTask();
                    FileWrite("enableKioskMode Start");
                } else {
                    KioskModeApp.setIsInLockMode(false);
                    FileWrite("enableKioskMode Error " + getString(R.string.kiosk_not_permitted));
                }
            } else {
                KioskModeApp.setIsInLockMode(false);
                stopLockTask();
                //FileWrite("enableKioskMode Stop");
            }
        } catch (Exception e) {
            KioskModeApp.setIsInLockMode(false);
            // TODO: Log and handle appropriately
            FileWrite("enableKioskMode Error Exception" + e.getMessage());
        }
    }

    protected void hideSystemUI() {
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

}