package typeofmood.ime;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import typeofmood.ime.datahandler.DatabaseHelper;

public class MainActivityDBDebugFragment extends Fragment {
    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(
                R.layout.dbdebug_fragment, container, false);

        final ListView listView = rootView.findViewById(R.id.dataListView);
        if(listView != null) {
            DatabaseHelper myDB;
            myDB=new DatabaseHelper(getActivity().getApplicationContext());
            ArrayList<String> theList = new ArrayList<>();
            Cursor data = myDB.getNotSendContents();
            if (data.getCount() == 0) {
                Toast.makeText(getActivity(), "Database was empty", Toast.LENGTH_SHORT).show();
                theList.clear();
                ListAdapter listAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, theList);
                listView.setAdapter(listAdapter);
            } else {
                while (data.moveToNext()) {
                    theList.add("DOC_ID="+data.getString(0)+
                            "\n\nDATETIME_DATA="+data.getString(1)+
                            "\n\nSEND_TIME="+data.getString(3)+
                            "\n\nSESSION_DATA="+data.getString(2)
                            );
                    ListAdapter listAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, theList);
                    listView.setAdapter(listAdapter);
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
