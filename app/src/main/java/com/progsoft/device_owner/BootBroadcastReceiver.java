package com.progsoft.device_owner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BootBroadcastReceiver";
    private static final Intent mIntent = null;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, MyService.class);
        context.startForegroundService(service);

        Log.e(TAG, "new intent:" + mIntent);

        String action = intent.getAction();
        Log.e(TAG, action + "***" + intent);

        if (action.equals("android.intent.action.PHONE_STATE")
                || action.equals("android.intent.action.NEW_OUTGOING_CALL")
                || action.equals("android.intent.action.SIM_STATE_CHANGED")) {
            Log.e(TAG, "Call in changed!");
            Bundle bundle = intent.getExtras();
            //String phone-number = bundle.getString("android.intent.extra.PHONE_NUMBER");
            String phoneNumber = bundle.getString("incoming_number");
            String state = bundle.getString("state");

            if ("IDLE".equals(state)) {
                if (phoneNumber != null && phoneNumber.equals("10086")) {
                    KioskModeApp.setIsSupperMode(!KioskModeApp.isIsSupperMode());
                }
            }
            Log.e(TAG, "state:" + state + " number:" + phoneNumber + " mode:" + KioskModeApp.isIsSupperMode() + " " + bundle);
            /*
             * for (String key:bundle.keySet()) {
                Log.e(TAG, "key=" + key + " content=" + bundle.getString(key));
            }
             */
        }
    }
}
