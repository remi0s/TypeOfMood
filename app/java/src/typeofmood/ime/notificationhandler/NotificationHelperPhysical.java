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

import androidx.core.app.NotificationCompat;
import android.text.format.DateUtils;
import android.widget.RemoteViews;

import typeofmood.ime.R;

public class NotificationHelperPhysical extends ContextWrapper {
    public static final String ChannelID= "TypeOfMoodChannelID";
    public int notification_id=4231314;
    public static final String ChannelName= "TypeOfMoodChannel";
    private NotificationManager mManager;

    public NotificationHelperPhysical(Context base) {

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

    public NotificationCompat.Builder getTypeOfMoodNotification(String title, String message){



        Intent relaxationAction = new Intent(this,ActionReceiverPhysical.class);
        relaxationAction.putExtra("actionPhysical","Relaxation");

        Intent neutralAction = new Intent(this,ActionReceiverPhysical.class);
        neutralAction.putExtra("actionPhysical","Neutral");

        Intent tirednessAction = new Intent(this,ActionReceiverPhysical.class);
        tirednessAction.putExtra("actionPhysical","Tiredness");

        Intent sicknessAction = new Intent(this,ActionReceiverPhysical.class);
        sicknessAction.putExtra("actionPhysical","Sickness");

        Intent laterAction = new Intent(this,ActionReceiverPhysical.class);
        laterAction.putExtra("actionPhysical","Later");



        PendingIntent pIntentLater = PendingIntent.getBroadcast(this,10,laterAction,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_CANCEL_CURRENT );
        PendingIntent pIntentRelaxation = PendingIntent.getBroadcast(this,11,relaxationAction,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pIntentTiredness = PendingIntent.getBroadcast(this,12,tirednessAction,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pIntentSickness = PendingIntent.getBroadcast(this,13,sicknessAction,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pIntentNeutral = PendingIntent.getBroadcast(this,14,neutralAction,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_CANCEL_CURRENT);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.typeofmood_notification_physical_state);
//
        remoteViews.setTextViewText(R.id.timestamp, DateUtils.formatDateTime(this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
//
        remoteViews.setImageViewResource(R.id.smallIcon,R.mipmap.ic_typeofmood);
        remoteViews.setOnClickPendingIntent(R.id.buttonRelaxation,pIntentRelaxation);
        remoteViews.setOnClickPendingIntent(R.id.buttonTiredness,pIntentTiredness);
        remoteViews.setOnClickPendingIntent(R.id.buttonSickness,pIntentSickness);
        remoteViews.setOnClickPendingIntent(R.id.buttonLater,pIntentLater);
        remoteViews.setOnClickPendingIntent(R.id.buttonPhysicalNeutral,pIntentNeutral);



        RemoteViews smallremoteViews = new RemoteViews(getPackageName(), R.layout.typeofmood_notification_physical_state_small);
//        smallremoteViews.setTextViewText(R.id.notif_title,title+" - "+"How are you feeling?");
        smallremoteViews.setTextViewText(R.id.timestamp, DateUtils.formatDateTime(this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
        smallremoteViews.setImageViewResource(R.id.smallIcon,R.mipmap.ic_typeofmood);
        smallremoteViews.setOnClickPendingIntent(R.id.buttonRelaxation,pIntentRelaxation);
        smallremoteViews.setOnClickPendingIntent(R.id.buttonTiredness,pIntentTiredness);
        smallremoteViews.setOnClickPendingIntent(R.id.buttonSickness,pIntentSickness);
        smallremoteViews.setOnClickPendingIntent(R.id.buttonLater,pIntentLater);
        smallremoteViews.setOnClickPendingIntent(R.id.buttonPhysicalNeutral,pIntentNeutral);


        NotificationCompat.Builder builder=new NotificationCompat.Builder(getApplicationContext(),ChannelID);
        builder.setSmallIcon(R.mipmap.ic_typeofmood)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCustomContentView(smallremoteViews)
                .setCustomBigContentView(remoteViews)
                .setWhen(System.currentTimeMillis())
                .setVibrate(new long[] { 100, 100})
                .setColor(Color.GREEN)
                .setOngoing(true)
                .setAutoCancel(true)
                ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }


        return builder;



    }




}
