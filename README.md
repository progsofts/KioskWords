# KioskWrite
Screen Pinning without Rooting

### Set as Admin open CMD
    adb shell dpm set-device-owner com.progsoft.device_owner/.AdminReceiver
    adb shell dumpsys account

### If you want allow some apps
    mDpm.setLockTaskPackages(
                        deviceAdmin, //deviceAdmin = new ComponentName(this, AdminReceiver.class);
                        whitelistedPackages); //String[]

### todo
    1 还能被业务直接杀死
    2 当前如果锁定后，如果没有锁屏是可以切换回桌面，但是不能使用应用