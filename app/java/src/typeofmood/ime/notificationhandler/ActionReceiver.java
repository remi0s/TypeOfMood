package typeofmood.ime.notificationhandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
        LatinIME.latestNotificationTime=new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss", Locale.US).format(new Date());

        NotificationHelper mNotificationHelper;
        mNotificationHelper= new NotificationHelper(context);
        mNotificationHelper.getManager().cancel(mNotificationHelper.notification_id);
        mydb.close();

//        mNotificationHelper.getManager().cancel(action,intent.getExtras().getInt("action"));
        //This is used to close the notification tray
//        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//        context.sendBroadcast(it);
    }

    public void performLater(Context context){
        Toast.makeText(context,"Not Now", Toast.LENGTH_SHORT).show();
        LatinIME.laterPressed=true;
        LatinIME.currentMood="Postponing";

    }

    public void performHappy(Context context){
        Toast.makeText(context,"Happy", Toast.LENGTH_SHORT).show();
        LatinIME.currentMood="Happy";

    }

    public void performSad(Context context){
        Toast.makeText(context,"Sad", Toast.LENGTH_SHORT).show();
        LatinIME.currentMood="Sad";

    }

    public void performRelaxed(Context context){
        Toast.makeText(context,"Neutral", Toast.LENGTH_SHORT).show();
        LatinIME.currentMood="Neutral";

    }

    public void performStressed(Context context){
        Toast.makeText(context,"Stressed", Toast.LENGTH_SHORT).show();
        LatinIME.currentMood="Stressed";

    }



}
