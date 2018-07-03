package typeofmood.ime;

import android.content.Intent;
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

import com.tsongkha.spinnerdatepicker.DatePicker;
import com.tsongkha.spinnerdatepicker.DatePickerDialog;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import typeofmood.ime.compat.TextViewCompatUtils;
import typeofmood.ime.latin.SystemBroadcastReceiver;


public class UserInfoConfiguration extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private TextView mActionFinish;
    private RadioGroup mRadioGroupGender;
    private RadioButton rMale, rFemale;
    private EditText mAge,mEditTextPHQ9,mEditTextScore;
    private EditText mUserID;
    private SimpleDateFormat simpleDateFormat;
    private boolean reloadNeed = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.typeofmood_user_info_config);
        mActionFinish = findViewById(R.id.setup_finish);
        TextViewCompatUtils.setCompoundDrawablesRelativeWithIntrinsicBounds(mActionFinish,
                getResources().getDrawable(R.drawable.ic_setup_finish), null, null, null);

        mAge = findViewById(R.id.editTextAge);
        mEditTextPHQ9=findViewById(R.id.editTextPHQ9);
        mEditTextScore=findViewById(R.id.editTextScore);

        mUserID=findViewById(R.id.editTextUserID);

        mAge.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mUserID.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mEditTextScore.setImeOptions(EditorInfo.IME_ACTION_DONE);

        mRadioGroupGender=findViewById(R.id.radioGroupGender);

        rMale = findViewById(R.id.radioMale);
        rFemale =  findViewById(R.id.radioFemale);


        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String pref_birth_date= pref.getString("BirthDate", "");
        String pref_ID= pref.getString("ID", "");
        String pref_gender= pref.getString("Gender", "");
        String pref_health= pref.getString("Health", "");
        if(pref_ID.isEmpty()){
            pref_ID=android.provider.Settings.System.getString(this.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            editor.putString("ID", pref_ID);

        }
        editor.apply();

        mAge.setText(pref_birth_date, TextView.BufferType.EDITABLE);
        mUserID.setHint(pref_ID);
        if(pref_gender.equals("Male")){
            rMale.setChecked(true);
        }else if(pref_gender.equals("Female")){
            rFemale.setChecked(true);
        }

        if(!pref_health.isEmpty()){
            mEditTextScore.setHint("Your latest PHQ-9 score was : "+pref_health);
        }

        mEditTextPHQ9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadNeed=true;
                Intent intent = new Intent(getApplication(), Phq9questionnaire.class);
                startActivityForResult(intent,1);
            }

        });

        mEditTextScore.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                SharedPreferences pref = getApplicationContext().getSharedPreferences("user_info", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                String pref_health= pref.getString("Health", "");
                editor.apply();
                mEditTextScore.setHint("Your latest PHQ-9 score was : "+pref_health);
            }
        });



        mActionFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String BirthDate = mAge.getText().toString();
                String ID = mUserID.getText().toString();
                String gender="";
                String health=mEditTextScore.getText().toString();
                int selectedGender = mRadioGroupGender.getCheckedRadioButtonId();
                if(selectedGender == rMale.getId()) {
                    gender="Male";
                } else if(selectedGender == rFemale.getId()) {
                    gender="Female";
                }


                SharedPreferences prefUser = getApplicationContext().getSharedPreferences("user_info", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefUser.edit();
                String pref_ID= prefUser.getString("ID", "");
                if(health.isEmpty()){
                    health= prefUser.getString("Health", "");
                }else{
                    editor.putString("Health", health);
                }

                editor.apply();

                if(!BirthDate.isEmpty() && (!ID.isEmpty() || !pref_ID.isEmpty())  && !gender.isEmpty() && !health.isEmpty()) {
                    editor = prefUser.edit();
                    editor.putString("BirthDate", BirthDate);
                    if(!ID.isEmpty() ){
                        editor.putString("ID", ID);
                    }else{
                        editor.putString("ID", pref_ID);
                    }
                    editor.putString("Gender", gender);
                    editor.putString("Health", health);
                    editor.apply();

                    SharedPreferences prefFinished = getApplicationContext().getSharedPreferences("user_info_finished_clicked", MODE_PRIVATE);
                    editor = prefFinished.edit();
                    editor.putBoolean("isClicked", true);
                    editor.apply();
//                    SystemBroadcastReceiver.toggleAppIcon(getApplicationContext());


                    finish();
                }else{
                    Toast.makeText(getApplicationContext(),"You need to provide the above information!",Toast.LENGTH_SHORT).show();
                }

            }
        });



    }
    public void showDatePickerDialog(View v) {
        int year=1990;
        int month =0;
        int day=1;
        SharedPreferences pref = getApplicationContext().getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String pref_birth_date= pref.getString("BirthDate", "");
        editor.apply();
        if(!pref_birth_date.isEmpty()){
            String[] calend = pref_birth_date.split("-");
            year=Integer.parseInt(calend[2]);
            month = Integer.parseInt(calend[1])-1; //-1 because it starts from 0
            day=Integer.parseInt(calend[0]);
        }

        showDate(year, month, day, R.style.NumberPickerStyle);
    }


    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        mAge.setText(simpleDateFormat.format(calendar.getTime()));
        String Age=getAge(year,monthOfYear,dayOfMonth);
        SharedPreferences prefUser = getApplicationContext().getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUser.edit();
        editor.putString("Age", Age);
        editor.apply();

    }

    void showDate(int year, int monthOfYear, int dayOfMonth, int spinnerTheme) {
        new SpinnerDatePickerDialogBuilder()
                .context(UserInfoConfiguration.this)
                .callback(UserInfoConfiguration.this)
                .spinnerTheme(spinnerTheme)
                .defaultDate(year, monthOfYear, dayOfMonth)
                .build()
                .show();
    }

    private String getAge(int year, int month, int day){
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        Integer ageInt = new Integer(age);
        String ageS = ageInt.toString();

        return ageS;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences pref = getApplicationContext().getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String pref_health= pref.getString("Health", "");
        editor.apply();

        if (reloadNeed)
            if(!pref_health.isEmpty()){
                mEditTextScore.setHint("Your latest PHQ-9 score was : "+pref_health);
            }


        reloadNeed = false; // do not reload anymore, unless I tell you so...
    }

}
