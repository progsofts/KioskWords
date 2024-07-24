package com.progsoft.device_owner;

import android.app.Application;

import com.progsoft.device_owner.iflytek.TtsDemo;

public class KioskModeApp extends Application {

    public static boolean isInLockMode;
    public static boolean isSupperMode = false;
    public static int timeToLock = 10;
    public static String lastTimeLocked = "None";
    public static int LOCK_TIME_10MIN = 100; // 100 = 5分钟，  200 = 10分钟
    public static int TestNum = 12; // 默认做12题
    public static int RightNum = 0;
    public static int min = 0, max = 90;
    public static int level = 10;

    public static void setRange(int x, int y) {
        KioskModeApp.min = x;
        KioskModeApp.max = y;
    }

    public static void setLevel(int l) {
        KioskModeApp.level = l;
    }

    public static int getMin() {
        return min;
    }

    public static int getMax() {
        return max;
    }

    public static int getLevel() {
        return level;
    }


    public static void setTestNum(int l) {
        TestNum = l;
        RightNum = 0;
    }

    public static int getTestNum() {
        return TestNum;
    }

    public static int getRightNum() {
        return RightNum;
    }

    public static boolean isInLockMode() {
        return isInLockMode;
    }

    public static void setIsInLockMode(boolean isInLockMode) {
        KioskModeApp.isInLockMode = isInLockMode;
    }

    public static boolean isIsSupperMode() {
        return isSupperMode;
    }

    public static void setIsSupperMode(boolean mode) {
        KioskModeApp.isSupperMode = mode;
    }

    public static int getTimeToLock() {
        return KioskModeApp.timeToLock;
    }

    public static void setTimeToLock(int l) {
        KioskModeApp.timeToLock = l;
    }

    public static void addTimeToLock(int l) {
        KioskModeApp.timeToLock += l;

    }

    public static boolean isTimeToLock() {
        if (KioskModeApp.timeToLock > 0) {
            KioskModeApp.timeToLock--;
        } else {
            KioskModeApp.timeToLock = 10;
            return true;
        }
        return false;
    }

    public static void setLastTimeToLocked (String s) {
        KioskModeApp.lastTimeLocked = s;
    }

    public static String getLastTimeLocked() {
        return KioskModeApp.lastTimeLocked;
    }
}
