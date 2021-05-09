package typeofmood.ime.notificationhandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.core.app.NotificationCompat;
import android.widget.Toast;

import typeofmood.ime.Phq9questionnaire;
import typeofmood.ime.datahandler.MoodDatabaseHelper;


public class ActionReceiverPHQ9 extends BroadcastReceiver {
    MoodDatabaseHelper mydb;
    @Override
    public void onReceive(Context context, Intent intent) {
        mydb=new MoodDatabaseHelper(context);
        String action=intent.getStringExtra("action");
        if(action.equals("Later")){
            performLater(context);
        }else if(action.equals("Retake")){
            performRetake(context);
        }else{

        }


        NotificationHelperPHQ9 mNotificationHelper;
        mNotificationHelper= new NotificationHelperPHQ9(context);
        mNotificationHelper.getManager().cancel(mNotificationHelper.notification_id);
        mydb.close();


    }

    public void performLater(Context context){
        Toast.makeText(context,"Not Now", Toast.LENGTH_SHORT).show();
        SharedPreferences pref = context.getSharedPreferences("user_info", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        long one_week=(long)(1000*60*60*24*7); //1000*60*60*24*7
        long latest_phq9_date= pref.getLong("latest_phq9_date", 0);
        latest_phq9_date+=one_week;
        editor.putLong("latest_phq9_date", latest_phq9_date);
        editor.apply();

    }

    public void performRetake(Context context){
        Toast.makeText(context,"Answer PHQ9", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(context, Phq9questionnaire.class);
        context.startActivity(intent);


    }



}
