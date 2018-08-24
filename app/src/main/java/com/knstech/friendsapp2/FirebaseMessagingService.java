package com.knstech.friendsapp2;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private DatabaseReference databaseReference;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notification_title= remoteMessage.getNotification().getTitle();
        String notification_msg=remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();
        String from_user_id=remoteMessage.getData().get("from_user_id");

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notification_title)
                .setContentText(notification_msg)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(notification_msg))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("uid",from_user_id);
        PendingIntent resultPendingintent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

        mBuilder.setContentIntent(resultPendingintent);

        int mNotificationId=(int) System.currentTimeMillis();
        NotificationManager mNnotifyMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mNnotifyMgr.notify(mNotificationId,mBuilder.build());

    }
}
