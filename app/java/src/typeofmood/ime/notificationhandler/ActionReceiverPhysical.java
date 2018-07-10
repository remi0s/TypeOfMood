package typeofmood.ime.notificationhandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import typeofmood.ime.datahandler.MoodDatabaseHelper;
import typeofmood.ime.latin.LatinIME;
import java.util.Date;


public class ActionReceiverPhysical extends BroadcastReceiver {
    MoodDatabaseHelper mydb;
    @Override
    public void onReceive(Context context, Intent intent) {
        mydb=new MoodDatabaseHelper(context);
        String action=intent.getStringExtra("actionPhysical");
        if(action.equals("Later")){
            performLater(context);
        }
        else if(action.equals("Relaxation")){
            performHappy(context);
            mydb.addPhysicalStateData("Relaxation");
        }else if(action.equals("Tiredness")){
            performSad(context);
            mydb.addPhysicalStateData("Tiredness");
        }else if(action.equals("Sickness")){
            performRelaxed(context);
            mydb.addPhysicalStateData("Sickness");
        }else{

        }

        LatinIME.latestNotificationTime=new Date(System.currentTimeMillis());

        NotificationHelperPhysical mNotificationHelperPhysical;
        mNotificationHelperPhysical= new NotificationHelperPhysical(context);
        mNotificationHelperPhysical.getManager().cancel(mNotificationHelperPhysical.notification_id);

//        mNotificationHelper.getManager().cancel(action,intent.getExtras().getInt("action"));
        //This is used to close the notification tray
//        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//        context.sendBroadcast(it);
    }

    public void performLater(Context context){
        Toast.makeText(context,"Not Now", Toast.LENGTH_SHORT).show();
        LatinIME.laterPressed=true;
        LatinIME.currentPhysicalState="Postponing";

    }

    public void performHappy(Context context){
        Toast.makeText(context,"Relaxation", Toast.LENGTH_SHORT).show();
        LatinIME.currentPhysicalState="Relaxation";

    }

    public void performSad(Context context){
        Toast.makeText(context,"Tiredness", Toast.LENGTH_SHORT).show();
        LatinIME.currentPhysicalState="Tiredness";

    }

    public void performRelaxed(Context context){
        Toast.makeText(context,"Sickness", Toast.LENGTH_SHORT).show();
        LatinIME.currentPhysicalState="Sickness";

    }



}
