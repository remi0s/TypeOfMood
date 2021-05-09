package typeofmood.ime.notificationhandler;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import androidx.core.app.NotificationCompat;
import android.text.format.DateUtils;
import android.widget.RemoteViews;

import typeofmood.ime.R;

public class NotificationHelperPHQ9 extends ContextWrapper {
    public static final String ChannelID= "TypeOfMoodChannelID";
    public int notification_id=911199;
    public static final String ChannelName= "TypeOfMoodChannel";
    private NotificationManager mManager;

    public NotificationHelperPHQ9(Context base) {

        super(base);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel();
        }


    }


    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel typeofmoodChannel= new NotificationChannel(ChannelID,ChannelName, NotificationManager.IMPORTANCE_HIGH);
        typeofmoodChannel.enableLights(true);
        typeofmoodChannel.enableVibration(true);
        typeofmoodChannel.setLightColor(Color.GREEN);
        typeofmoodChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        getManager().createNotificationChannel(typeofmoodChannel);
    }

    public NotificationManager getManager(){
        if(mManager==null){
            mManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public NotificationCompat.Builder getTypeOfMoodNotification(String title, String message,NotificationCompat.Builder builder){

        Intent retakeAction = new Intent(this,ActionReceiverPHQ9.class);
        retakeAction.putExtra("action","Retake");

        Intent laterAction = new Intent(this,ActionReceiverPHQ9.class);
        laterAction.putExtra("action","Later");



        PendingIntent pIntentLater = PendingIntent.getBroadcast(this,1,laterAction,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_CANCEL_CURRENT );
        PendingIntent pIntentRetake = PendingIntent.getBroadcast(this,2,retakeAction,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_CANCEL_CURRENT);


        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.typeofmood_notification_phq9);

        remoteViews.setTextViewText(R.id.timestamp, DateUtils.formatDateTime(this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));


        remoteViews.setImageViewResource(R.id.smallIcon,R.mipmap.ic_typeofmood);
        remoteViews.setOnClickPendingIntent(R.id.buttonRetake,pIntentRetake);
        remoteViews.setOnClickPendingIntent(R.id.buttonLater,pIntentLater);



        RemoteViews smallremoteViews = new RemoteViews(getPackageName(), R.layout.typeofmood_notification_phq9_small);

        smallremoteViews.setTextViewText(R.id.timestamp, DateUtils.formatDateTime(this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
        smallremoteViews.setImageViewResource(R.id.smallIcon,R.mipmap.ic_typeofmood);
        smallremoteViews.setOnClickPendingIntent(R.id.buttonRetake,pIntentRetake);
        smallremoteViews.setOnClickPendingIntent(R.id.buttonLater,pIntentLater);


//        NotificationManager.getActiveNotifications();

        builder.setSmallIcon(R.mipmap.ic_typeofmood)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCustomContentView(smallremoteViews)
                .setCustomBigContentView(remoteViews)
                .setWhen(System.currentTimeMillis())
                .setColor(Color.GREEN)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(new long[] { 100, 100})
                .setOngoing(true)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
        ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (mNotificationManager!=null){
                StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
                for (StatusBarNotification notification : notifications) {
                    if (notification.getId() == notification_id) {
//                        Log.d("notif", "here");
                        builder.setVibrate(null);
                    }
                }
            }

        }


        return builder;



    }






}
