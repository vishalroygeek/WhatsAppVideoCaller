package com.vishalroy.whatsappvideocaller.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;

import com.vishalroy.whatsappvideocaller.Helpers.Utils;
import com.vishalroy.whatsappvideocaller.MainActivity;
import com.vishalroy.whatsappvideocaller.R;

import static com.vishalroy.whatsappvideocaller.Helpers.Constants.NOTIFICATION_CHANNEL_ID;
import static com.vishalroy.whatsappvideocaller.Helpers.Constants.NOTIFICATION_CHANNEL_NAME;

public class DisableTouch extends Service implements View.OnClickListener {
    private WindowManager windowManager;
    private View floating_view, disabled_area;
    private Utils utils;

    public DisableTouch() {}

    @Override
    public void onCreate() {
        super.onCreate();

        //Initializing some objects
        utils = new Utils(this);

        //Inflating the disabled touch layout
        floating_view = LayoutInflater.from(this).inflate(R.layout.disable_touch, null);

        //Creating a layout params for the floating view
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);

        //Aligning floating view to the top
        params.gravity = Gravity.TOP;


        //Lets get the window manager
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        //Adding the floating view to the windows manager
        windowManager.addView(floating_view, params);

        //Finding view by ID
        disabled_area = floating_view.findViewById(R.id.disabled_area);

        //Initiating listeners
        disabled_area.setOnClickListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Creating a notification channel if the device has Android Oreo or above
        createNotificationChannel();

        //Creating a notification intent and passing it to a pending intent
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        //Binding the pending intent to the notification
        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.touch_disabled))
                .setContentText(getString(R.string.touch_service_running))
                .setSmallIcon(R.drawable.ic_logo_notification)
                .setContentIntent(pendingIntent)
                .build();

        //Starting the service
        startForeground(1, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //Removing the view when the service is destroyed
        if (floating_view != null){
            windowManager.removeView(floating_view);
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.disabled_area:
                //Providing feedback on touch
                utils.toast(getString(R.string.restricted_area));
                utils.vibrate(100, this);
                break;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

}
