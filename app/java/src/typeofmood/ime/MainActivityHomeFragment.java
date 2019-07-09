package typeofmood.ime;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Locale;

import typeofmood.ime.datahandler.MoodDatabaseHelper;

public class MainActivityHomeFragment extends Fragment {

    private static MoodDatabaseHelper myDB;
    private static TextView mHappyStats,mRelaxedStats;
    private static TextView mNeutralStats,mPNeutralStats;
    private static TextView mSadStats,mTiredStats;
    private static TextView mStressedStats,mSickStats;
    private static TextView mToday;
    private static TextView mWeek;
    private static TextView mMonth;
    private static TextView latest_sync;


    private static Calendar calendar = Calendar.getInstance();
    private static SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.US);

    private static String StartDate, EndDate;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        //Inflate the layout for this fragment


        View rootView = inflater.inflate(
                R.layout.home_fragment, container, false);

        myDB=new MoodDatabaseHelper(getActivity().getApplicationContext());
        mHappyStats=rootView.findViewById(R.id.textView_stats_happy);
        mNeutralStats=rootView.findViewById(R.id.textView_stats_neutral);
        mSadStats=rootView.findViewById(R.id.textView_stats_sad);
        mStressedStats=rootView.findViewById(R.id.textView_stats_Stressed);

        mRelaxedStats=rootView.findViewById(R.id.textView_stats_relaxed);
        mPNeutralStats=rootView.findViewById(R.id.textView_stats_pneutral);
        mTiredStats=rootView.findViewById(R.id.textView_stats_tired);
        mSickStats=rootView.findViewById(R.id.textView_stats_sick);

        mToday=rootView.findViewById(R.id.textView_today);
        mWeek=rootView.findViewById(R.id.textView_week);
        mMonth=rootView.findViewById(R.id.textView_month);
        latest_sync=rootView.findViewById(R.id.latest_sync);

        return rootView;

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(myDB!=null){
            myDB.close();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Context context = getActivity();


        mWeek.setTextColor(Color.BLUE);
        mWeek.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        mToday.setTextColor(Color.GRAY);
        mToday.setPaintFlags(0);

        mMonth.setTextColor(Color.GRAY);
        mMonth.setPaintFlags(0);

        calendar = Calendar.getInstance();
        StartDate=dbFormat.format(calendar.getTime());
        calendar.add(Calendar.DATE,-7);
        EndDate=dbFormat.format(calendar.getTime());

        SharedPreferences pref =getActivity().getApplicationContext().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String temp="Latest sync: "+pref.getString("latest_send_date", "");
        latest_sync.setText(temp);
        editor.apply();

        updateStatisticMood(view.getRootView(), StartDate, EndDate, context);
        updateStatisticPhysical(view.getRootView(), StartDate, EndDate, context);



        mToday.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mToday.setTextColor(Color.BLUE);
                mToday.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

                mWeek.setTextColor(Color.GRAY);
                mWeek.setPaintFlags(0);

                mMonth.setTextColor(Color.GRAY);
                mMonth.setPaintFlags(0);

                calendar = Calendar.getInstance();
                StartDate=dbFormat.format(calendar.getTime());
                calendar.add(Calendar.DATE,-0);
                EndDate=dbFormat.format(calendar.getTime());

                updateStatisticMood(v.getRootView(), StartDate, EndDate, context);
                updateStatisticPhysical(v.getRootView(), StartDate, EndDate, context);
                }
        });

        mWeek.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mWeek.setTextColor(Color.BLUE);
                mWeek.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

                mToday.setTextColor(Color.GRAY);
                mToday.setPaintFlags(0);

                mMonth.setTextColor(Color.GRAY);
                mMonth.setPaintFlags(0);

                calendar = Calendar.getInstance();
                StartDate=dbFormat.format(calendar.getTime());
                calendar.add(Calendar.DATE,-7);
                EndDate=dbFormat.format(calendar.getTime());

                updateStatisticMood(v.getRootView(), StartDate, EndDate, context);
                updateStatisticPhysical(v.getRootView(), StartDate, EndDate, context);

            }
        });

        mMonth.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mMonth.setTextColor(Color.BLUE);
                mMonth.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

                mToday.setTextColor(Color.GRAY);
                mToday.setPaintFlags(0);

                mWeek.setTextColor(Color.GRAY);
                mWeek.setPaintFlags(0);

                calendar = Calendar.getInstance();
                StartDate=dbFormat.format(calendar.getTime());
                calendar.add(Calendar.DATE,-30);
                EndDate=dbFormat.format(calendar.getTime());

                updateStatisticMood(v.getRootView(), StartDate, EndDate, context);
                updateStatisticPhysical(v.getRootView(), StartDate, EndDate, context);

            }
        });

    }

    public static void updateStatisticMood(View rootView,String StartDate,String EndDate, Context context){

        int countHappy , countSad, countNeutral, countStressed;
        countHappy=myDB.getPeriodContentsMood(StartDate,EndDate,"Happy");
        countSad=myDB.getPeriodContentsMood(StartDate,EndDate,"Sad");
        countNeutral=myDB.getPeriodContentsMood(StartDate,EndDate,"Neutral");
        countStressed=myDB.getPeriodContentsMood(StartDate,EndDate,"Stressed");
        int totalCount=countHappy+countSad+countNeutral+countStressed;
        float percentHappy=0;
        float percentSad=0;
        float percentNeutral=0;
        float percentStressed=0;
        DecimalFormat df = new DecimalFormat("#0.00");

        if (totalCount>0){
            percentHappy=((float)countHappy*100)/totalCount;
            percentSad=((float)countSad*100)/totalCount;
            percentNeutral=((float)countNeutral*100)/totalCount;
            percentStressed=((float)countStressed*100)/totalCount;
        }
        mHappyStats.setText(df.format(percentHappy)+"%");
        mNeutralStats.setText(df.format(percentNeutral)+"%");
        mSadStats.setText(df.format(percentSad)+"%");
        mStressedStats.setText(df.format(percentStressed)+"%");


        }


    public static void updateStatisticPhysical(View rootView,String StartDate,String EndDate, Context context){

        int countRelaxation , countTiredness, countSickness, countNeutral;

        countRelaxation=myDB.getPeriodContentsPhysical(StartDate,EndDate,"Relaxation");
        countTiredness=myDB.getPeriodContentsPhysical(StartDate,EndDate,"Tiredness");
        countSickness=myDB.getPeriodContentsPhysical(StartDate,EndDate,"Sickness");
        countNeutral=myDB.getPeriodContentsPhysical(StartDate,EndDate,"Neutral");

        int totalCount=countRelaxation+countTiredness+countSickness+countNeutral;
        float percentRelaxation=0;
        float percentTiredness=0;
        float percentSickness=0;
        float percentNeutral=0;
        DecimalFormat df = new DecimalFormat("#0.00");

        if (totalCount>0) {
            percentRelaxation=((float)countRelaxation*100)/totalCount;
            percentTiredness=((float)countTiredness*100)/totalCount;
            percentSickness=((float)countSickness*100)/totalCount;
            percentNeutral=((float)countNeutral*100)/totalCount;

        }
        mRelaxedStats.setText(df.format(percentRelaxation)+"%");
        mPNeutralStats.setText(df.format(percentNeutral)+"%");
        mTiredStats.setText(df.format(percentTiredness)+"%");
        mSickStats.setText(df.format(percentSickness)+"%");
    }







    }
