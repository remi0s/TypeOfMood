package typeofmood.ime.notificationhandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import typeofmood.ime.datahandler.MoodDatabaseHelper;
import typeofmood.ime.latin.LatinIME;
import java.util.Date;


public class ActionReceiver extends BroadcastReceiver {
    MoodDatabaseHelper mydb;
    @Override
    public void onReceive(Context context, Intent intent) {
        mydb=new MoodDatabaseHelper(context);
        String action=intent.getStringExtra("action");
        if(action.equals("Later")){
            performLater(context);
        }
        else if(action.equals("Happy")){
            performHappy(context);
            mydb.addData("Happy");
        }else if(action.equals("Sad")){
            performSad(context);
            mydb.addData("Sad");
        }else if(action.equals("Relaxed")){
            performRelaxed(context);
            mydb.addData("Relaxed");
        }else if(action.equals("Stressed")){
            performStressed(context);
            mydb.addData("Stressed");
        }else{

        }
        LatinIME.latestNotificationTime=new Date(System.currentTimeMillis());

        NotificationHelper mNotificationHelper;
        mNotificationHelper= new NotificationHelper(context);
        mNotificationHelper.getManager().cancel(mNotificationHelper.notification_id);

//        mNotificationHelper.getManager().cancel(action,intent.getExtras().getInt("action"));
        //This is used to close the notification tray
//        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//        context.sendBroadcast(it);
    }

    public void performLater(Context context){
        Toast.makeText(context,"Later", Toast.LENGTH_SHORT).show();
        LatinIME.laterPressed=true;

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
        Toast.makeText(context,"Relaxed", Toast.LENGTH_SHORT).show();
        LatinIME.currentMood="Relaxed";

    }

    public void performStressed(Context context){
        Toast.makeText(context,"Stressed", Toast.LENGTH_SHORT).show();
        LatinIME.currentMood="Stressed";

    }

}
