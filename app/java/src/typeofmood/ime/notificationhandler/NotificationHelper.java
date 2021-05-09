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

public class NotificationHelper extends ContextWrapper {
    public static final String ChannelID= "TypeOfMoodChannelID";
    public int notification_id=1234564321;
    public static final String ChannelName= "TypeOfMoodChannel";
    private NotificationManager mManager;

    public NotificationHelper(Context base) {
        
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

        Intent happyAction = new Intent(this,ActionReceiver.class);
        happyAction.putExtra("action","Happy");

        Intent sadAction = new Intent(this,ActionReceiver.class);
        sadAction.putExtra("action","Sad");

        Intent relaxedAction = new Intent(this,ActionReceiver.class);
        relaxedAction.putExtra("action","Neutral");

        Intent stressedAction = new Intent(this,ActionReceiver.class);
        stressedAction.putExtra("action","Stressed");

        Intent laterAction = new Intent(this,ActionReceiver.class);
        laterAction.putExtra("action","Later");

//        happyAction.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        sadAction.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        relaxedAction.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        stressedAction.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        laterAction.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);


        PendingIntent pIntentLater = PendingIntent.getBroadcast(this,1,laterAction,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_CANCEL_CURRENT );
        PendingIntent pIntentHappy = PendingIntent.getBroadcast(this,2,happyAction,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pIntentSad = PendingIntent.getBroadcast(this,3,sadAction,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pIntentRelaxed = PendingIntent.getBroadcast(this,4,relaxedAction,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pIntentStressed = PendingIntent.getBroadcast(this,5,stressedAction,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_CANCEL_CURRENT);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.typeofmood_notification);
//        remoteViews.setTextViewText(R.id.notif_title,"@string/my_english_ime_name");
//        remoteViews.setTextViewText(R.id.notif_message,"@string/my_english_ime_name");
        remoteViews.setTextViewText(R.id.timestamp, DateUtils.formatDateTime(this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
//        Log.d("timer", DateUtils.formatDateTime(this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
        remoteViews.setImageViewResource(R.id.smallIcon,R.mipmap.ic_typeofmood);
        remoteViews.setOnClickPendingIntent(R.id.buttonHappy,pIntentHappy);
        remoteViews.setOnClickPendingIntent(R.id.buttonSad,pIntentSad);
        remoteViews.setOnClickPendingIntent(R.id.buttonNeutral,pIntentRelaxed);
        remoteViews.setOnClickPendingIntent(R.id.buttonStressed,pIntentStressed);
        remoteViews.setOnClickPendingIntent(R.id.buttonLater,pIntentLater);



        RemoteViews smallremoteViews = new RemoteViews(getPackageName(), R.layout.typeofmood_notification_small);
//        smallremoteViews.setTextViewText(R.id.notif_title,title+" - "+"How are you feeling?");
        smallremoteViews.setTextViewText(R.id.timestamp, DateUtils.formatDateTime(this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
        smallremoteViews.setImageViewResource(R.id.smallIcon,R.mipmap.ic_typeofmood);
        smallremoteViews.setOnClickPendingIntent(R.id.buttonHappy,pIntentHappy);
        smallremoteViews.setOnClickPendingIntent(R.id.buttonSad,pIntentSad);
        smallremoteViews.setOnClickPendingIntent(R.id.buttonNeutral,pIntentRelaxed);
        smallremoteViews.setOnClickPendingIntent(R.id.buttonStressed,pIntentStressed);
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
