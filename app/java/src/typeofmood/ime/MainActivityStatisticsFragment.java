package typeofmood.ime;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import typeofmood.ime.datahandler.MoodDatabaseHelper;

public class MainActivityStatisticsFragment extends Fragment {
    private static EditText mStartDate;
    private static EditText mEndDate;
    private static String dbStartDate="";
    private static String dbEndDate="";
    private static ListView listView;
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy",Locale.US);
    private static DatePickerDialogFragment mDatePickerDialogFragment;
    private static Button btnBeginning;
    private static Button btnMood;
    private static Button btnPhysical;
    private static GraphView graph;
    private static MoodDatabaseHelper myDB;



    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(
                R.layout.statistics_fragment, container, false);
        mStartDate = rootView.findViewById(R.id.editTextStart);
        mEndDate =  rootView.findViewById(R.id.editTextEnd);
        btnBeginning = rootView.findViewById(R.id.buttonFromBeginning);
        btnMood = rootView.findViewById(R.id.buttonMood);
        btnPhysical = rootView.findViewById(R.id.buttonPhysical);
        mDatePickerDialogFragment = new DatePickerDialogFragment();
        graph = rootView.findViewById(R.id.graph);
        myDB=new MoodDatabaseHelper(getActivity().getApplicationContext());

        btnBeginning.setBackgroundColor(Color.GRAY);
        btnMood.setBackgroundColor(Color.GRAY);
        btnPhysical.setBackgroundColor(Color.LTGRAY);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Context context=getActivity();
        listView = view.findViewById(R.id.statisticsListView);
        if(listView != null) {
            updateStatisticMood(view.getRootView(), listView, dbStartDate, dbEndDate,true,context);
        }

        mStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatePickerDialogFragment.setFlag(DatePickerDialogFragment.FLAG_START_DATE);
                mDatePickerDialogFragment.show(getFragmentManager(), "datePicker");
            }

        });

        mEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatePickerDialogFragment.setFlag(DatePickerDialogFragment.FLAG_END_DATE);
                mDatePickerDialogFragment.show(getFragmentManager(), "datePicker");
            }

        });
        btnBeginning.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(listView != null) {
                    if(((ColorDrawable)btnBeginning.getBackground()).getColor()==Color.GRAY){
                        if (((ColorDrawable)btnMood.getBackground()).getColor()==Color.GRAY) {
                            updateStatisticMood(v.getRootView(), listView, dbStartDate, dbEndDate, false, context);
                        } else {
                            updateStatisticPhysical(v.getRootView(), listView, dbStartDate, dbEndDate, false, context);
                        }

                        if (mStartDate.getText().toString().isEmpty() && mEndDate.getText().toString().isEmpty()) {
                            Toast.makeText(context, "You haven't chose any dates", Toast.LENGTH_SHORT).show();
                        }
                        btnBeginning.setBackgroundColor(Color.LTGRAY);
                        mStartDate.setText("");
                        mEndDate.setText("");

                    }else{

                            if (((ColorDrawable) btnMood.getBackground()).getColor() == Color.GRAY) {
                                updateStatisticMood(v.getRootView(), listView, dbStartDate, dbEndDate, true, context);
                            } else {
                                updateStatisticPhysical(v.getRootView(), listView, dbStartDate, dbEndDate, true, context);
                            }

                        btnBeginning.setBackgroundColor(Color.GRAY);

                    }

                }


            }

        });

        btnMood.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(listView != null) {
                    if(((ColorDrawable)btnBeginning.getBackground()).getColor()==Color.GRAY){
                        if (((ColorDrawable)btnMood.getBackground()).getColor()==Color.GRAY) {
                            updateStatisticPhysical(v.getRootView(), listView, dbStartDate, dbEndDate, true, context);
                            btnMood.setBackgroundColor(Color.LTGRAY);
                            btnPhysical.setBackgroundColor(Color.GRAY);
                        } else {
                            updateStatisticMood(v.getRootView(), listView, dbStartDate, dbEndDate, true, context);
                            btnMood.setBackgroundColor(Color.GRAY);
                            btnPhysical.setBackgroundColor(Color.LTGRAY);
                        }
                    }else {
                        if (((ColorDrawable) btnMood.getBackground()).getColor() == Color.LTGRAY) {
                            if (mStartDate.getText().toString().isEmpty() && mEndDate.getText().toString().isEmpty()) {
                                Toast.makeText(context, "You haven't chose any dates", Toast.LENGTH_SHORT).show();
                            }
                            updateStatisticMood(v.getRootView(), listView, dbStartDate, dbEndDate, false, context);
                            btnMood.setBackgroundColor(Color.GRAY);
                            btnPhysical.setBackgroundColor(Color.LTGRAY);
                        } else {
                            if (mStartDate.getText().toString().isEmpty() && mEndDate.getText().toString().isEmpty()) {
                                Toast.makeText(context, "You haven't chose any dates", Toast.LENGTH_SHORT).show();
                            }
                            updateStatisticPhysical(v.getRootView(), listView, dbStartDate, dbEndDate, false, context);
                            btnMood.setBackgroundColor(Color.LTGRAY);
                            btnPhysical.setBackgroundColor(Color.GRAY);
                        }

                    }
                }
            }

        });

        btnPhysical.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (listView != null) {
                    if(((ColorDrawable)btnBeginning.getBackground()).getColor()==Color.GRAY){
                        if (((ColorDrawable)btnPhysical.getBackground()).getColor()==Color.GRAY) {
                            updateStatisticMood(v.getRootView(), listView, dbStartDate, dbEndDate, true, context);
                            btnPhysical.setBackgroundColor(Color.LTGRAY);
                            btnMood.setBackgroundColor(Color.GRAY);
                        } else {
                            updateStatisticPhysical(v.getRootView(), listView, dbStartDate, dbEndDate, true, context);
                            btnPhysical.setBackgroundColor(Color.GRAY);
                            btnMood.setBackgroundColor(Color.LTGRAY);
                        }

                    }else{
                        if (((ColorDrawable) btnPhysical.getBackground()).getColor() == Color.LTGRAY) {
                            if (mStartDate.getText().toString().isEmpty() && mEndDate.getText().toString().isEmpty()) {
                                Toast.makeText(context, "You haven't chose any dates", Toast.LENGTH_SHORT).show();
                            }
                            updateStatisticPhysical(v.getRootView(), listView, dbStartDate, dbEndDate, false, context);
                            btnPhysical.setBackgroundColor(Color.GRAY);
                            btnMood.setBackgroundColor(Color.LTGRAY);
                        } else {
                            if (mStartDate.getText().toString().isEmpty() && mEndDate.getText().toString().isEmpty()) {
                                Toast.makeText(context, "You haven't chose any dates", Toast.LENGTH_SHORT).show();
                            }
                            updateStatisticMood(v.getRootView(), listView, dbStartDate, dbEndDate, false, context);
                            btnPhysical.setBackgroundColor(Color.LTGRAY);
                            btnMood.setBackgroundColor(Color.GRAY);
                        }


                    }
                }
            }


        });

    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    public static void updateStatisticMood(View rootView,ListView listView,String StartDate,String EndDate,boolean FromTheBeginning, Context context){

        ArrayList<String> theList = new ArrayList<>();
        int countHappy , countSad, countNeutral, countStressed;
        String dbStartDate1,dbEndDate1;
        if(FromTheBeginning) {
            Cursor data =myDB.getListContents();
            try {
                if(data.getCount()!=0) {
                    data.moveToFirst();
                    dbStartDate1 = data.getString(3);
                    data.moveToLast();
                    dbEndDate1 = data.getString(3);
                    mStartDate.setText(parseDate(dbStartDate1));
                    mEndDate.setText(parseDate(dbEndDate1));
                }
            } finally {
                data.close();
            }


            countHappy = myDB.getListMoodContents("Happy").getCount();
            countSad = myDB.getListMoodContents("Sad").getCount();
            countNeutral = myDB.getListMoodContents("Neutral").getCount();
            countStressed = myDB.getListMoodContents("Stressed").getCount();
        }else{
            countHappy=myDB.getPeriodContentsMood(StartDate,EndDate,"Happy");
            countSad=myDB.getPeriodContentsMood(StartDate,EndDate,"Sad");
            countNeutral=myDB.getPeriodContentsMood(StartDate,EndDate,"Neutral");
            countStressed=myDB.getPeriodContentsMood(StartDate,EndDate,"Stressed");
        }
//
        int totalCount=countHappy+countSad+countNeutral+countStressed;

        if (totalCount==0) {
            Toast.makeText(context, "Didn't find any registered data", Toast.LENGTH_SHORT).show();
            graph.removeAllSeries();
            theList.clear();
            ListAdapter listAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, theList);
            listView.setAdapter(listAdapter);
        } else {
            DecimalFormat df = new DecimalFormat("#0.00");
            float percentHappy=((float)countHappy*100)/totalCount;
            float percentSad=((float)countSad*100)/totalCount;
            float percentNeutral=((float)countNeutral*100)/totalCount;
            float percentStressed=((float)countStressed*100)/totalCount;
            theList.add("Happy\n"+df.format(percentHappy)+"%");
            theList.add("Sad\n"+df.format(percentSad)+"%");
            theList.add("Neutral\n"+df.format(percentNeutral)+"%");
            theList.add("Stressed\n"+df.format(percentStressed)+"%");
            ListAdapter listAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, theList);
            listView.setAdapter(listAdapter);
            if(graph != null) {




                BarGraphSeries<DataPoint> series = new BarGraphSeries<>(new DataPoint[]{
                        new DataPoint(1, percentHappy),
                        new DataPoint(2, percentSad),
                        new DataPoint(3, percentNeutral),
                        new DataPoint(4, percentStressed),

                });
                graph.removeAllSeries();
                graph.addSeries(series);

                graph.getViewport().setMinX(0);
                graph.getViewport().setMaxX(5);
                graph.getViewport().setMinY(0.0);
                graph.getViewport().setMaxY(100.0);
                graph.getViewport().setYAxisBoundsManual(true);
                graph.getViewport().setXAxisBoundsManual(true);

                series.setSpacing(10);

                GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
                StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
                staticLabelsFormatter.setHorizontalLabels(new String[] {" ","Happy", "Sad", "Neutral", "Stressed"," "});
                graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
                graph.getGridLabelRenderer().setNumHorizontalLabels(6);

                series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
                    @Override
                    public int get(DataPoint data) {
                        switch((int)data.getX()){
                            case 1:
                                return Color.rgb(102,153,0); //happy
                            case 2:
                                return Color.rgb(0,0,0);  //sad
                            case 3:
                                return Color.rgb(0,0,255);  //Neutral
                            case 4:
                                return Color.rgb(255,69,0);  //stressed
                            default :
                                return Color.rgb((int) data.getX()*255/4, (int) Math.abs(data.getY()*255/6), 100);


                        }
                    }
                });


// draw values on top
                series.setDrawValuesOnTop(true);
                series.setValuesOnTopColor(Color.WHITE);


            }


        }
    }

    public static void updateStatisticPhysical(View rootView,ListView listView,String StartDate,String EndDate,boolean FromTheBeginning, Context context){

        ArrayList<String> theList = new ArrayList<>();
        int countRelaxation , countTiredness, countSickness, countNeutral;
        String dbStartDate1,dbEndDate1;
        if(FromTheBeginning) {
            Cursor data =myDB.getListContents();
            try {
                if(data.getCount()!=0){
                    data.moveToFirst();
                    dbStartDate1=data.getString(3);
                    data.moveToLast();
                    dbEndDate1=data.getString(3);
                    mStartDate.setText(parseDate(dbStartDate1));
                    mEndDate.setText(parseDate(dbEndDate1));
                }
            } finally {
                data.close();
            }


            countRelaxation = myDB.getListPhysicalContents("Relaxation").getCount();
            countTiredness = myDB.getListPhysicalContents("Tiredness").getCount();
            countSickness = myDB.getListPhysicalContents("Sickness").getCount();
            countNeutral = myDB.getListPhysicalContents("Neutral").getCount();

        }else{
            countRelaxation=myDB.getPeriodContentsPhysical(StartDate,EndDate,"Relaxation");
            countTiredness=myDB.getPeriodContentsPhysical(StartDate,EndDate,"Tiredness");
            countSickness=myDB.getPeriodContentsPhysical(StartDate,EndDate,"Sickness");
            countNeutral=myDB.getPeriodContentsPhysical(StartDate,EndDate,"Neutral");

        }
//
        int totalCount=countRelaxation+countTiredness+countSickness+countNeutral;

        if (totalCount==0) {
            Toast.makeText(context, "Didn't find any registered data", Toast.LENGTH_SHORT).show();
            graph.removeAllSeries();
            theList.clear();
            ListAdapter listAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, theList);
            listView.setAdapter(listAdapter);
        } else {
            DecimalFormat df = new DecimalFormat("#0.00");
            float percentRelaxation=((float)countRelaxation*100)/totalCount;
            float percentTiredness=((float)countTiredness*100)/totalCount;
            float percentSickness=((float)countSickness*100)/totalCount;
            float percentNeutral=((float)countNeutral*100)/totalCount;
            theList.add("Relaxation\n"+df.format(percentRelaxation)+"%");
            theList.add("Tiredness\n"+df.format(percentTiredness)+"%");
            theList.add("Neutral\n"+df.format(percentNeutral)+"%");
            theList.add("Sickness\n"+df.format(percentSickness)+"%");


            ListAdapter listAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, theList);
            listView.setAdapter(listAdapter);
            if(graph != null) {




                BarGraphSeries<DataPoint> series = new BarGraphSeries<>(new DataPoint[]{
                        new DataPoint(1, percentRelaxation),
                        new DataPoint(2, percentTiredness),
                        new DataPoint(3, percentNeutral),
                        new DataPoint(4, percentSickness),

                });
                graph.removeAllSeries();
                graph.addSeries(series);

                graph.getViewport().setMinX(0);
                graph.getViewport().setMaxX(5);
                graph.getViewport().setMinY(0.0);
                graph.getViewport().setMaxY(100.0);
                graph.getViewport().setYAxisBoundsManual(true);
                graph.getViewport().setXAxisBoundsManual(true);

                series.setSpacing(10);

                GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
                StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
                staticLabelsFormatter.setHorizontalLabels(new String[] {" ","Relaxed", "Tired", "Neutral","Sick"," "});
                graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
                graph.getGridLabelRenderer().setNumHorizontalLabels(6);

                series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
                    @Override
                    public int get(DataPoint data) {
                        switch((int)data.getX()){
                            case 1:
                                return Color.rgb(102,153,0); //Relaxation
                            case 2:
                                return Color.rgb(0,0,0);  //Tiredness
                            case 3:
                                return Color.rgb(0,0,255);  //Neutral
                            case 4:
                                return Color.rgb(255,69,0);  //Sickness
                            default :
                                return Color.rgb((int) data.getX()*255/4, (int) Math.abs(data.getY()*255/6), 100);



                        }
                    }
                });


// draw values on top
                series.setDrawValuesOnTop(true);
                series.setValuesOnTopColor(Color.WHITE);


            }


        }
    }

    public static class DatePickerDialogFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {
        public static final int FLAG_START_DATE = 0;
        public static final int FLAG_END_DATE = 1;

        private int flag = 0;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void setFlag(int i) {
            flag = i;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar calendar = Calendar.getInstance();
            Calendar dbCalendar = Calendar.getInstance();
            calendar.set(year, monthOfYear, dayOfMonth);
            dbCalendar.set(year, monthOfYear, dayOfMonth);
            SimpleDateFormat textFormat = new SimpleDateFormat("dd-MM-yyyy",Locale.US);
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.US);


            if (flag == FLAG_START_DATE) {
                mStartDate.setText(textFormat.format(calendar.getTime()));
                dbStartDate=dbFormat.format(calendar.getTime());
            } else if (flag == FLAG_END_DATE) {
                mEndDate.setText(textFormat.format(calendar.getTime()));
                dbEndDate=dbFormat.format(calendar.getTime());
            }

            if(!mStartDate.getText().toString().isEmpty() && !mEndDate.getText().toString().isEmpty()){
                if(listView != null) {
                    Context context=getActivity();
                    if(((ColorDrawable)btnMood.getBackground()).getColor()==Color.GRAY){
                        updateStatisticMood(view.getRootView(), listView, dbStartDate, dbEndDate,false,context);
                    }else{
                        updateStatisticPhysical(view.getRootView(), listView, dbStartDate, dbEndDate,false,context);
                    }
//                    updateStatisticMood(view.getRootView(), listView, dbStartDate, dbEndDate, false,context);
                }
                btnBeginning.setBackgroundColor(Color.LTGRAY);
            }
        }
    }

    public static String parseDate(String date){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date newDate = null;
        try {
            newDate = format.parse(date);
        } catch (ParseException e) {
            return "";
        }

        format = new SimpleDateFormat("dd-MM-yyyy");
        date = format.format(newDate);
        return date;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(myDB!=null){
            myDB.close();
        }
    }



}
