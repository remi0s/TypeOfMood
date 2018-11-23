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
            performRelaxation(context);
            mydb.addPhysicalStateData("Relaxation");
        }else if(action.equals("Tiredness")){
            performTiredness(context);
            mydb.addPhysicalStateData("Tiredness");
        }else if(action.equals("Sickness")){
            performSickness(context);
            mydb.addPhysicalStateData("Sickness");
        }else if(action.equals("Neutral")){
            performNeutral(context);
            mydb.addPhysicalStateData("Neutral");
        }else{

        }

        LatinIME.latestNotificationTimeTemp=new Date(System.currentTimeMillis());
        LatinIME.latestNotificationTime= System.currentTimeMillis();

        NotificationHelperPhysical mNotificationHelperPhysical;
        mNotificationHelperPhysical= new NotificationHelperPhysical(context);
        mNotificationHelperPhysical.getManager().cancel(mNotificationHelperPhysical.notification_id);
        mydb.close();

    }

    public void performLater(Context context){
        Toast.makeText(context,"Not Now", Toast.LENGTH_SHORT).show();
        LatinIME.laterPressed=true;
        LatinIME.currentPhysicalState="Postponing";

    }

    public void performRelaxation(Context context){
        Toast.makeText(context,"Relaxation", Toast.LENGTH_SHORT).show();
        LatinIME.currentPhysicalState="Relaxation";
        LatinIME.laterPressed=false;

    }

    public void performNeutral(Context context){
        Toast.makeText(context,"Neutral", Toast.LENGTH_SHORT).show();
        LatinIME.currentPhysicalState="Neutral";
        LatinIME.laterPressed=false;

    }

    public void performTiredness(Context context){
        Toast.makeText(context,"Tiredness", Toast.LENGTH_SHORT).show();
        LatinIME.currentPhysicalState="Tiredness";
        LatinIME.laterPressed=false;

    }

    public void performSickness(Context context){
        Toast.makeText(context,"Sickness", Toast.LENGTH_SHORT).show();
        LatinIME.currentPhysicalState="Sickness";
        LatinIME.laterPressed=false;

    }



}
