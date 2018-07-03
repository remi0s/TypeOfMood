package typeofmood.ime;

import android.os.Bundle;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.Date;

import typeofmood.ime.datahandler.MoodDatabaseHelper;
import typeofmood.ime.latin.LatinIME;


public class ChooseMood extends AppCompatActivity {
    private RadioGroup radioGroup;
    private RadioButton Happy, Sad, Neutral, Stressed, Later;
    private Button button;
    MoodDatabaseHelper mydb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.typeofmood_alert_with_radiobuttons);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        radioGroup =findViewById(R.id.myRadioGroup);

//        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                // find which radio button is selected
//                if(checkedId == R.id.radioHappy) {
//                    Toast.makeText(getApplicationContext(), "choice: Happy",
//                            Toast.LENGTH_SHORT).show();
//                } else if(checkedId == R.id.radioSad) {
//                    Toast.makeText(getApplicationContext(), "choice: Sad",
//                            Toast.LENGTH_SHORT).show();
//                }else if(checkedId == R.id.radioRelaxed) {
//                    Toast.makeText(getApplicationContext(), "choice: Neutral",
//                            Toast.LENGTH_SHORT).show();
//                }else if(checkedId == R.id.radioStressed) {
//                    Toast.makeText(getApplicationContext(), "choice: Stressed",
//                            Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getApplicationContext(), "choice: Later",
//                            Toast.LENGTH_SHORT).show();
//                }
//            }
//
//        });

        Happy = findViewById(R.id.radioHappy);
        Sad =  findViewById(R.id.radioSad);
        Neutral =  findViewById(R.id.radioRelaxed);
        Stressed =  findViewById(R.id.radioStressed);
        Later = findViewById(R.id.radioLater);

        mydb=new MoodDatabaseHelper(getApplicationContext());
        button = findViewById(R.id.Btn);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int selectedId = radioGroup.getCheckedRadioButtonId();

                // find which radioButton is checked by id
                if(selectedId == Happy.getId()) {
                    LatinIME.currentMood="Happy";
                    mydb.addData("Happy");
                    Toast.makeText(getApplicationContext(),"Happy", Toast.LENGTH_SHORT).show();
                } else if(selectedId == Sad.getId()) {
                    LatinIME.currentMood="Sad";
                    mydb.addData("Sad");
                    Toast.makeText(getApplicationContext(),"Sad", Toast.LENGTH_SHORT).show();
                }else if(selectedId == Neutral.getId()) {
                    LatinIME.currentMood="Neutral";
                    mydb.addData("Neutral");
                    Toast.makeText(getApplicationContext(),"Neutral", Toast.LENGTH_SHORT).show();
                }else if(selectedId == Stressed.getId()) {
                    LatinIME.currentMood="Stressed";
                    mydb.addData("Stressed");
                    Toast.makeText(getApplicationContext(),"Stressed", Toast.LENGTH_SHORT).show();
                } else {
                    LatinIME.laterPressed=true;
                    Toast.makeText(getApplicationContext(),"Later", Toast.LENGTH_SHORT).show();
                }
                LatinIME.latestNotificationTime=new Date(System.currentTimeMillis());
                finish();
            }
        });


    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onBackPressed() {
        LatinIME.laterPressed=true;
        Toast.makeText(getApplicationContext(),"Later", Toast.LENGTH_SHORT).show();
        super.onBackPressed();
        finish();
    }

}
