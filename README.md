# KioskMode-Android
Screen Pinning Android Lollipop without Rooting

### Set as Admin open CMD

    adb shell dpm set-device-owner device_owner/.AdminReceiver

### If you want allow some apps

    mDpm.setLockTaskPackages(
                        deviceAdmin, //deviceAdmin = new ComponentName(this, AdminReceiver.class);
                        whitelistedPackages); //String[]
