package com.progsoft.device_owner;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MyService extends Service {
    int mNotificationId = 1;
    int mGPSInfoId = 2;
    String channelId = "my_chn_01";

    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;

    private static final String TAG = "PROG_Service";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 10000;
    private static final float LOCATION_DISTANCE = 0;

    private static final int RUNNABLE_INTERVAL = 5000;
    private static final int DEFAULT_DELAY_TIME = 5;

    public void FileWrite(String context) {
        try {
            String newTitle = "";
            File file = new File(Environment.getExternalStorageDirectory(), "progsoft/log/Thread.txt");
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FileWrite("MyService onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        FileWrite("MyService onCreate");

        mNotificationManager =(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O) {
            CharSequence name = "GPS服务通知";
            String description = "不知道在哪里显示的内容？";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);

            mChannel.setDescription(description);
            mChannel.enableLights(true);

            mNotificationManager.createNotificationChannel(mChannel);
            mBuilder = new NotificationCompat.Builder(this, channelId);
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Log.e(TAG,"huang: "+notificationIntent);
        mBuilder.setSmallIcon(R.mipmap.words)
                .setContentTitle("GPS服务 " + mNotificationId).setContentText("服务已经开启...")
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);
        Notification notification = mBuilder.build();
        startForeground(mNotificationId, notification);
        mNotificationManager.notify(mNotificationId, notification);

        if (true) {
            FileWrite("Start MainActivity by Service");
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        if (mLocationManager == null) {
            FileWrite("Start GPS Listen");
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            if (mLocationManager != null) {
                FileWrite(mLocationListeners[0] + " " + mLocationListeners[1]);
                if (false)
                    mLocationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[0]);
                if (true)
                    mLocationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[1]);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FileWrite("MyService onDestroy");
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            //Log.e(TAG, "LocationListener " + provider);
            FileWrite("MyService LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(@NonNull Location location) {
            //getNewLocation(location, "Changed ");
            //Log.e(TAG, "onLocationChanged " + location);
            //FileWrite("MyService onLocationChanged " + location);
            FileWrite("MyService onLocationChanged Provider(" + location.getProvider() + "):" + location.getLatitude() + "," + location.getLongitude());
            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            //Log.e(TAG, "onProviderDisabled " + provider);
            FileWrite("MyService onProviderDisabled " + provider);
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            //Log.e(TAG, "onProviderEnabled " + provider);
            FileWrite("MyService onProviderEnabled " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //Log.e(TAG, "onStatusChanged " + provider);
            FileWrite("MyService onStatusChanged " + provider);
        }
    }

    private final LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

}
