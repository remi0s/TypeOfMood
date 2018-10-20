package typeofmood.ime;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
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

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

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
    private static String storageContainer = "";
    private static String storageConnectionString = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myDB = new DatabaseHelper(getActivity());
        View rootView = inflater.inflate(
                R.layout.dbdebug_fragment, container, false);
        final TextView t = rootView.findViewById(R.id.textView4);
        final TextView userInfoText = rootView.findViewById(R.id.textView2);
        final ListView listView = rootView.findViewById(R.id.dataListView);
        final Button buttonNull = rootView.findViewById(R.id.buttonNull);
        final Button buttonSend = rootView.findViewById(R.id.buttonSend);
        storageContainer=getConfigValue(getActivity().getApplicationContext(), "storageContainer");
        storageConnectionString=getConfigValue(getActivity().getApplicationContext(), "storageConnectionString");
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
                    new HttpAsyncTask().execute("test");
                }
            }
        });

        SharedPreferences pref = getActivity().getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        pref_age= pref.getString("Age", "");
        pref_ID= pref.getString("ID", "");
        pref_gender= pref.getString("Gender", "");
        pref_health= pref.getString("Health", "");
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

            listView.post(new Runnable() {
                @Override
                public void run() {
                    listView.scrollTo(0, listView.getBottom());
                }
            });
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
            String result=upload(notSendData);//POST(urls[0],notSendData);

            return result;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("SQL","Tried to send data with result: "+result);
//            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
        }
    }

    protected String upload(ArrayList<KeyboardPayload> payload){
        String result = "";
        try
        {
            for(int i = 0; i < payload.size(); i++) { //payload.size()

                // 3. build jsonObject


                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("DOC_ID", payload.get(i).DocID);
                jsonObject.accumulate("USER_ID", pref_ID);
                jsonObject.accumulate("USER_AGE", pref_age);
                jsonObject.accumulate("USER_GENDER", pref_gender);
                jsonObject.accumulate("USER_PHQ9", pref_health);
//                jsonObject.accumulate("DATE_DATA", payload.get(i).DateData);
//                jsonObject.accumulate("SESSION_DATA", payload.get(i).SessionData);


                // 4. convert JSONObject to JSON to String


                // Retrieve storage account from connection-string.
                CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

                // Create the blob client.
                CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

                // Retrieve reference to a previously created container.
                CloudBlobContainer container = blobClient.getContainerReference(storageContainer);

                // Create or overwrite the blob (with the name "example.jpeg") with contents from a local file.

                String formattedDate=new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
                String formattedDateData="";

                try{
                    Date date =new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss", Locale.US).parse(payload.get(i).DateData);
                    jsonObject.accumulate("DATE_DATA", payload.get(i).DateData);
                    formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
                }catch (Exception e)
                {
                    try {
                        Date tempDate = new Date(payload.get(i).DateData);
                        formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(tempDate);
                        formattedDateData= new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss", Locale.US).format(tempDate);
                        jsonObject.accumulate("DATE_DATA", formattedDateData);
                    }catch (Exception a){
                        Log.d("dateError", "Even that failed");
                    }
                }
                jsonObject.accumulate("DATE_SEND", new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss", Locale.US).format(new Date()));
                jsonObject.accumulate("SESSION_DATA", payload.get(i).SessionData);

                String blobName=pref_ID+"/"+formattedDate+"/"+payload.get(i).DocID+".json";
                CloudBlockBlob blob = container.getBlockBlobReference(blobName);



                blob.uploadText(jsonObject.toString());
                result="success";

                myDB.setSend(payload.get(i).DocID);
            }

        }
        catch (Exception e)
        {
            // Output the stack trace.
            result="failed";
            e.printStackTrace();
            Log.d("Upload", e.getLocalizedMessage());
        }
        return result;
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

    public static String getConfigValue(Context context, String name) {
        Resources resources = context.getResources();

        try {
            InputStream rawResource = resources.openRawResource(R.raw.config);
            Properties properties = new Properties();
            properties.load(rawResource);
            return properties.getProperty(name);
        } catch (Resources.NotFoundException e) {
            Log.e("getConfig", "Unable to find the config file: " + e.getMessage());
        } catch (IOException e) {
            Log.e("getConfig", "Failed to open config file.");
        }

        return null;
    }




}
