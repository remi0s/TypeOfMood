package typeofmood.ime;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import typeofmood.ime.compat.TextViewCompatUtils;
import typeofmood.ime.latin.SystemBroadcastReceiver;


public class UserInfoConfiguration extends AppCompatActivity {
    private TextView mActionFinish;
    private RadioGroup mRadioGroupGender;
    private RadioButton rMale, rFemale, rDepressed, rNotDepressed;
    private RadioGroup mRadioGroupHealth;
    private EditText mAge;
    private EditText mUserID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.typeofmood_user_info_config);
        mActionFinish = findViewById(R.id.setup_finish);
        TextViewCompatUtils.setCompoundDrawablesRelativeWithIntrinsicBounds(mActionFinish,
                getResources().getDrawable(R.drawable.ic_setup_finish), null, null, null);

        mAge = findViewById(R.id.editTextAge);
        mUserID=findViewById(R.id.editTextUserID);

        mAge.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mUserID.setImeOptions(EditorInfo.IME_ACTION_DONE);

        mRadioGroupGender=findViewById(R.id.radioGroupGender);
        mRadioGroupHealth=findViewById(R.id.radioGroupHealth);
        rMale = findViewById(R.id.radioMale);
        rFemale =  findViewById(R.id.radioFemale);
        rDepressed =  findViewById(R.id.radioDepressed);
        rNotDepressed =  findViewById(R.id.radioNotDepressed);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String pref_age= pref.getString("Age", "");
        String pref_ID= pref.getString("ID", "");
        String pref_gender= pref.getString("Gender", "");
        String pref_health= pref.getString("Health", "");
        if(pref_ID.isEmpty()){
            pref_ID=android.provider.Settings.System.getString(this.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            editor.putString("ID", pref_ID);
            editor.apply();
        }

        mAge.setText(pref_age, TextView.BufferType.EDITABLE);
        mUserID.setText(pref_ID, TextView.BufferType.EDITABLE);
        if(pref_gender.equals("Male")){
            rMale.setChecked(true);
        }else if(pref_gender.equals("Female")){
            rFemale.setChecked(true);
        }

        if(pref_health.equals("Depressed")){
            rDepressed.setChecked(true);
        }else if(pref_health.equals("NotDepressed")){
            rNotDepressed.setChecked(true);
        }



        mActionFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String age = mAge.getText().toString();
                String ID = mUserID.getText().toString();
                String gender="";
                String health="";
                int selectedGender = mRadioGroupGender.getCheckedRadioButtonId();
                if(selectedGender == rMale.getId()) {
                    gender="Male";
                } else if(selectedGender == rFemale.getId()) {
                    gender="Female";
                }

                int selectedHealth = mRadioGroupHealth.getCheckedRadioButtonId();
                if(selectedHealth == rDepressed.getId()) {
                    health="Depressed";
                } else if(selectedHealth == rNotDepressed.getId()) {
                    health="NotDepressed";
                }

                if(!age.isEmpty() && !ID.isEmpty() && !gender.isEmpty() && !health.isEmpty()) {
                    SharedPreferences prefUser = getApplicationContext().getSharedPreferences("user_info", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefUser.edit();
                    editor.putString("Age", age);
                    editor.putString("ID", ID);
                    editor.putString("Gender", gender);
                    editor.putString("Health", health);
                    editor.apply();

                    SharedPreferences prefFinished = getApplicationContext().getSharedPreferences("user_info_finished_clicked", MODE_PRIVATE);
                    editor = prefFinished.edit();
                    editor.putBoolean("isClicked", true);
                    editor.apply();
                    SystemBroadcastReceiver.toggleAppIcon(getApplicationContext());


                    finish();
                }else{
                    Toast.makeText(getApplicationContext(),"You need to provide the above information!",Toast.LENGTH_SHORT).show();
                }

            }
        });



    }

}
