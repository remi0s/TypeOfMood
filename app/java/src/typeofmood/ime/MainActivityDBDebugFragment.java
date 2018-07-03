package typeofmood.ime;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import typeofmood.ime.datahandler.DatabaseHelper;
import typeofmood.ime.datahandler.KeyboardPayload;
import typeofmood.ime.latin.LatinIME;

import static android.content.Context.MODE_PRIVATE;

public class MainActivityDBDebugFragment extends Fragment {
    public static DatabaseHelper myDB;
    public static String pref_age="";
    public static String pref_ID="";
    public static String pref_gender="";
    public static String pref_health="";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myDB = new DatabaseHelper(getActivity());
        View rootView = inflater.inflate(
                R.layout.dbdebug_fragment, container, false);
        final TextView t = rootView.findViewById(R.id.textView4);
        final TextView userInfoText = rootView.findViewById(R.id.textView2);
        final ListView listView = rootView.findViewById(R.id.dataListView);
        final Button buttonNull = rootView.findViewById(R.id.buttonSend);
        final Button buttonSend = rootView.findViewById(R.id.buttonSend);
        buttonNull.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean result=myDB.updateToNullForResend();
                if (result){
                    Toast.makeText(getActivity(), "Made all send dates NULL", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity(), "Failed to make dates NULL", Toast.LENGTH_SHORT).show();
                }

            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(isConnected()){
                    new HttpAsyncTask().execute("http://155.207.18.93:5000/determine_escalation/");
                }
            }
        });

        SharedPreferences pref = getActivity().getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String pref_age= pref.getString("Age", "");
        String pref_ID= pref.getString("ID", "");
        String pref_gender= pref.getString("Gender", "");
        String pref_health= pref.getString("Health", "");
        editor.apply();
        String UserInfo="\nUSER_ID = "+pref_ID+"\nUSER_AGE = "+pref_age+"\nUSER_GENDER = "+pref_gender+"\nUSER_PHQ9 = "+pref_health;
        userInfoText.setText(UserInfo);

        if(listView != null) {
            DatabaseHelper myDB;
            myDB=new DatabaseHelper(getActivity().getApplicationContext());
            ArrayList<String> theList = new ArrayList<>();
            Cursor data = myDB.getNotSendContents();
            String text="Number of sessions: "+data.getCount();
            t.setText(text);
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

    public static String POST(String url,ArrayList<KeyboardPayload> payload){
        InputStream inputStream = null;
        String result = "";


        try {
            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";

            for(int i = 0; i < payload.size(); i++) { //payload.size()

                // 3. build jsonObject

                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("DOC_ID", payload.get(i).DocID);
                jsonObject.accumulate("USER_ID",pref_ID);
                jsonObject.accumulate("USER_AGE", pref_age);
                jsonObject.accumulate("USER_GENDER", pref_gender);
                jsonObject.accumulate("USER_PHQ9", pref_health);
                jsonObject.accumulate("DATE_DATA", payload.get(i).DateData);
                jsonObject.accumulate("SESSION_DATA", payload.get(i).SessionData);


                // 4. convert JSONObject to JSON to String
                json = jsonObject.toString();

                Log.d("server","Json to be sent:\n"+json);


                // ** Alternative way to convert Person object to JSON string usin Jackson Lib
                // ObjectMapper mapper = new ObjectMapper();
                // json = mapper.writeValueAsString(person);
//                Gson gson = new Gson();
//                json = gson.toJson(payload.get(i), KeyboardPayload.class);

                // 5. set json to StringEntity
                StringEntity se = new StringEntity(json);

                // 6. set httpPost Entity
                httpPost.setEntity(se);

                // 7. Set some headers to inform server about the type of the content
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                // 8. Execute POST request to the given URL
                HttpResponse httpResponse = httpclient.execute(httpPost);

                // 9. receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();




                // 10. convert inputstream to string
                if(inputStream != null) {
                    result = convertInputStreamToString(inputStream);
                    Log.d("SQL","I'm at inputstream");
                    myDB.setSend(payload.get(i).DocID);
                    Log.d("SQL","Data Sent!");
                }
                else {
                    result = "Did not work!";
                }

            }


        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Activity.CONNECTIVITY_SERVICE);
        try {
            NetworkInfo networkInfo = null;
            if (connMgr != null) {
                networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            }
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected())
                return true;
            else
                return false;

        }catch (Exception e){
            return false;
        }

    }

    public class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            ArrayList<KeyboardPayload> notSendData = new ArrayList<>();
            Cursor data = myDB.getNotSendContents();
            if (data.getCount() != 0) {
                while (data.moveToNext()) {
                    KeyboardPayload payload=new KeyboardPayload();
                    payload.DocID=data.getString(0);//DocID
                    payload.DateData=data.getString(1); //DateTime
//                    payload.UserID=data.getString(2); //UserID
                    payload.SessionData= data.getString(2); //SessionData
                    notSendData.add(payload);
                }
            }else{
                Log.d("SQL","I'm in do in background 0 data");
            }
            String result=POST(urls[0],notSendData);

            return result;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("SQL","Tried to send data with result: "+result);
//            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }




}
