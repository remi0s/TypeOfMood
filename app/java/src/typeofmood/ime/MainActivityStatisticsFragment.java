package typeofmood.ime;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;

import typeofmood.ime.datahandler.DatabaseHelper;
import typeofmood.ime.datahandler.MoodDatabaseHelper;

public class MainActivityStatisticsFragment extends Fragment {
    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(
                R.layout.statistics_fragment, container, false);

        final ListView listView = rootView.findViewById(R.id.statisticsListView);
        GraphView graph = rootView.findViewById(R.id.graph);
        if(listView != null) {

            MoodDatabaseHelper myDB;
            myDB=new MoodDatabaseHelper(getActivity().getApplicationContext());
            ArrayList<String> theList = new ArrayList<>();
            int countHappy=myDB.getListMoodContents("Happy").getCount(),
                    countSad=myDB.getListMoodContents("Sad").getCount(),
                    countRelaxed=myDB.getListMoodContents("Relaxed").getCount(),
                    countStressed=myDB.getListMoodContents("Stressed").getCount();
            int totalCount=countHappy+countSad+countRelaxed+countStressed;

            if (totalCount==0) {
                Toast.makeText(getActivity(), "Database was empty", Toast.LENGTH_SHORT).show();
                theList.clear();
                ListAdapter listAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, theList);
                listView.setAdapter(listAdapter);
            } else {

                float percentHappy=(float)((countHappy*100)/totalCount);
                float percentSad=(float)((countSad*100)/totalCount);
                float percentRelaxed=(float)((countRelaxed*100)/totalCount);
                float percentStressed=(float)((countStressed*100)/totalCount);
                theList.add("Happy\n"+Float.toString(percentHappy)+"%");
                theList.add("Sad\n"+Float.toString(percentSad)+"%");
                theList.add("Relaxed\n"+Float.toString(percentRelaxed)+"%");
                theList.add("Stressed\n"+Float.toString(percentStressed)+"%");
                ListAdapter listAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, theList);
                listView.setAdapter(listAdapter);
                if(graph != null) {



                    BarGraphSeries<DataPoint> series = new BarGraphSeries<>(new DataPoint[]{
                            new DataPoint(1, percentHappy),
                            new DataPoint(2, percentSad),
                            new DataPoint(3, percentRelaxed),
                            new DataPoint(4, percentStressed),

                    });
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
                    staticLabelsFormatter.setHorizontalLabels(new String[] {" ","Happy", "Sad", "Relaxed", "Stressed"," "});
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
                                    return Color.rgb(0,0,255);  //relaxed
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
        return rootView;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }


}
