package typeofmood.ime.notificationhandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import typeofmood.ime.datahandler.MoodDatabaseHelper;
import typeofmood.ime.latin.LatinIME;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ActionReceiver extends BroadcastReceiver {
    MoodDatabaseHelper mydb;
    @Override
    public void onReceive(Context context, Intent intent) {
        mydb=new MoodDatabaseHelper(context);
        String action=intent.getStringExtra("action");
        if(action.equals("Later")){
            performLater(context);
        }else if(action.equals("Happy")){
            performHappy(context);
            mydb.addData("Happy");
        }else if(action.equals("Sad")){
            performSad(context);
            mydb.addData("Sad");
        }else if(action.equals("Neutral")){
            performRelaxed(context);
            mydb.addData("Neutral");
        }else if(action.equals("Stressed")){
            performStressed(context);
            mydb.addData("Stressed");
        }else{

        }
        LatinIME.latestNotificationTimeTemp=new Date(System.currentTimeMillis());
        LatinIME.latestNotificationTime= System.currentTimeMillis();

        NotificationHelper mNotificationHelper;
        mNotificationHelper= new NotificationHelper(context);
        mNotificationHelper.getManager().cancel(mNotificationHelper.notification_id);
        mydb.close();

        if(!LatinIME.laterPressed){
            NotificationHelperPhysical mNotificationHelperPhysical = new NotificationHelperPhysical(context);
            NotificationCompat.Builder nbPhysical = mNotificationHelperPhysical.getTypeOfMoodNotification("TypeOfMood", "Please Expand to describe your mood!");
            mNotificationHelperPhysical.getManager().notify(mNotificationHelperPhysical.notification_id, nbPhysical.build());
        }else{
            LatinIME.currentPhysicalState="Postponing";
        }


    }

    public void performLater(Context context){
        Toast.makeText(context,"Not Now", Toast.LENGTH_SHORT).show();
        LatinIME.laterPressed=true;
        LatinIME.currentMood="Postponing";

    }

    public void performHappy(Context context){
        Toast.makeText(context,"Happy", Toast.LENGTH_SHORT).show();
        LatinIME.currentMood="Happy";
        LatinIME.laterPressed=false;

    }

    public void performSad(Context context){
        Toast.makeText(context,"Sad", Toast.LENGTH_SHORT).show();
        LatinIME.currentMood="Sad";
        LatinIME.laterPressed=false;

    }

    public void performRelaxed(Context context){
        Toast.makeText(context,"Neutral", Toast.LENGTH_SHORT).show();
        LatinIME.currentMood="Neutral";
        LatinIME.laterPressed=false;
    }

    public void performStressed(Context context){
        Toast.makeText(context,"Stressed", Toast.LENGTH_SHORT).show();
        LatinIME.currentMood="Stressed";
        LatinIME.laterPressed=false;

    }



}
