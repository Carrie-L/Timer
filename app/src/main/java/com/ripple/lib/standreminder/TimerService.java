package com.ripple.lib.standreminder;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.ripple.lib.standreminder.utils.LogUtil;

import static com.ripple.lib.standreminder.MainActivity.STATUS_RELAXING;
import static com.ripple.lib.standreminder.MainActivity.STATUS_WORKING;


/**
 * Created by Carrie on 2017/9/29.
 */

public class TimerService extends Service {
    private static final String TAG = "TimerService";
    private NotificationCompat.Builder mBuilder;
    private Bitmap bmWork;
    private Bitmap bmRelax;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.d(TAG, "onBind");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.i(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.i(TAG, "onStartCommand");
        if (mBuilder == null) {
            LogUtil.e(TAG, "mBuilder==null, initNotification");
            initNotification();
        }
        int status = intent.getIntExtra("STATUS", 0);
        int times = intent.getIntExtra("times", 0);
        String msg = intent.getStringExtra("msg");
        if (status == STATUS_RELAXING) {
            mBuilder.setSmallIcon(R.drawable.ic_relax)
                    .setLargeIcon(bmRelax);
        } else if (status == STATUS_WORKING) {
            mBuilder.setSmallIcon(R.drawable.ic_busy)
                    .setLargeIcon(bmWork);
        }
        mBuilder.setContentText(msg)
                .setTicker(msg)
                .setContentTitle(String.format(getString(R.string.times), times));

        //will do such methods in MainActivity: onNewIntent() 、 onResume()
        Intent intent1 = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        startForeground(1, mBuilder.build());// 前台服务
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private void initNotification() {
        bmWork = BitmapFactory.decodeResource(getResources(), R.drawable.ic_busy);
        bmRelax = BitmapFactory.decodeResource(getResources(), R.drawable.ic_relax);
        mBuilder = new NotificationCompat.Builder(this.getApplicationContext())
                .setSmallIcon(R.drawable.ic_busy)
                .setLargeIcon(bmWork)
                .setContentTitle(getString(R.string.app_name));
        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        String mSoundPath = "android.resource://com.ripple.lib.standreminder/";
        mBuilder.setSound(Uri.parse(mSoundPath + R.raw.mps));

    }

}
