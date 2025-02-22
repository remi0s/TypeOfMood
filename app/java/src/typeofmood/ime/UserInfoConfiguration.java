package typeofmood.ime;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tsongkha.spinnerdatepicker.DatePicker;
import com.tsongkha.spinnerdatepicker.DatePickerDialog;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Properties;

import typeofmood.ime.compat.TextViewCompatUtils;


public class UserInfoConfiguration extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private TextView mActionFinish,mTerms;
    private CheckBox mAgreeTerms;
    private RadioGroup mRadioGroupGender;
    private RadioButton rMale, rFemale;

    private RadioGroup mRadioGroupEducation;
    private RadioButton rLowestEducation, rLowEducation, rMediumEducation, rHighEducation, rHighestEducation;

    private RadioGroup mRadioGroupUsage;
    private RadioButton rLowUsage, rMediumUsage, rHighUsage;

    private RadioGroup mRadioGroupIncome;
    private RadioButton rZeroIncome, rLowIncome, rMediumIncome, rHighIncome;

    private RadioGroup mRadioGroupMedication;
    private RadioButton rMedicationFalse, rMedicationTrue;


    private EditText mAge,mEditTextPHQ9,mEditTextScore;
    private EditText mUserID;
//    private EditText mPassword;
    private SimpleDateFormat simpleDateFormat;
    private LinearLayout mLayout3,mLayout4,mLayout5,mLayout6,mLayout7,mLayout2,mLayout1;
    private String mMEI;
    private boolean reloadNeed = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.typeofmood_user_info_config);
        mActionFinish = findViewById(R.id.setup_finish);
        TextViewCompatUtils.setCompoundDrawablesRelativeWithIntrinsicBounds(mActionFinish,
                getResources().getDrawable(R.drawable.ic_setup_finish), null, null, null);

        mLayout1=findViewById(R.id.layout1);
        mLayout2=findViewById(R.id.layout2);
        mLayout3=findViewById(R.id.layout3);
        mLayout4=findViewById(R.id.layout4);
        mLayout5=findViewById(R.id.layout5);
        mLayout6=findViewById(R.id.layout6);
        mLayout7=findViewById(R.id.layout7);

        mAge = findViewById(R.id.editTextAge);

        mAgreeTerms = findViewById(R.id.checkBoxAgreeTerms);


        mTerms = (TextView) findViewById(R.id.textViewTerms);
        mTerms.setMovementMethod(new ScrollingMovementMethod());

        mEditTextPHQ9 = findViewById(R.id.editTextPHQ9);
        mEditTextScore = findViewById(R.id.editTextScore);
        mMEI = android.provider.Settings.System.getString(this.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        mUserID = findViewById(R.id.editTextUserID);
//        mPassword = findViewById(R.id.editTextPassword);

        mAge.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mUserID.setImeOptions(EditorInfo.IME_ACTION_DONE);
//        mPassword.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mEditTextScore.setImeOptions(EditorInfo.IME_ACTION_DONE);

        mRadioGroupGender = findViewById(R.id.radioGroupGender);
        rMale = findViewById(R.id.radioMale);
        rFemale = findViewById(R.id.radioFemale);


        mRadioGroupEducation = findViewById(R.id.radioGroupEducation);
        rLowestEducation = findViewById(R.id.radioLowestEducation);
        rLowEducation = findViewById(R.id.radioLowEducation);
        rMediumEducation = findViewById(R.id.radioMediumEducation);
        rHighEducation = findViewById(R.id.radioHighEducation);
        rHighestEducation = findViewById(R.id.radioHighestEducation);



        mRadioGroupUsage = findViewById(R.id.radioGroupUsage);
        rLowUsage = findViewById(R.id.radioLowUsage);
        rMediumUsage = findViewById(R.id.radioMediumUsage);
        rHighUsage = findViewById(R.id.radioHighUsage);



        mRadioGroupIncome = findViewById(R.id.radioGroupIncome);
        rZeroIncome = findViewById(R.id.radioZeroIncome);
        rLowIncome = findViewById(R.id.radioLowIncome);
        rMediumIncome = findViewById(R.id.radioMediumIncome);
        rHighIncome = findViewById(R.id.radioHighIncome);

        mRadioGroupMedication = findViewById(R.id.radioGroupMedication);
        rMedicationFalse = findViewById(R.id.radioMedicationFalse);
        rMedicationTrue = findViewById(R.id.radioMedicationTrue);



        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String pref_birth_date = pref.getString("BirthDate", "");
        String pref_ID = pref.getString("ID", "");
        String pref_gender = pref.getString("Gender", "");
        String pref_health = pref.getString("Health", "");

        String pref_education = pref.getString("Education", "");
        String pref_usage = pref.getString("Usage", "");
        String pref_income = pref.getString("Income", "");
        String pref_medication= pref.getString("Medication", "");
//        String pref_password = pref.getString("Password", "");
        Boolean pref_terms_agree = pref.getBoolean("TermsAgreement", false);



        if (pref_ID.isEmpty()) {
            editor.putString("ID", mMEI);
            editor.apply();
            mUserID.setHint(mMEI);

        }else if(!pref_ID.equals(mMEI)) {
            mUserID.setText(pref_ID);
        }
        mUserID.setHint(mMEI);

//        if(!pref_password.isEmpty()) {
//            mPassword.setText(pref_password);
//        }
//        checkPassword(pref_password);


        if(pref_terms_agree){
            mAgreeTerms.setChecked(true);
        }else{
            mAgreeTerms.setChecked(false);
        }


        mAge.setText(pref_birth_date, TextView.BufferType.EDITABLE);

        if(pref_gender.equals("Male")){
            rMale.setChecked(true);
        }else if(pref_gender.equals("Female")){
            rFemale.setChecked(true);
        }


        if(pref_education.equals("0")){
            rLowestEducation.setChecked(true);
        }else if(pref_education.equals("1")){
            rLowEducation.setChecked(true);
        }else if(pref_education.equals("2")){
            rMediumEducation.setChecked(true);
        }else if(pref_education.equals("3")){
            rHighEducation.setChecked(true);
        }else if(pref_education.equals("4")){
            rHighestEducation.setChecked(true);
        }

        if(pref_usage.equals("0")){
            rLowUsage.setChecked(true);
        }else if(pref_usage.equals("1")){
            rMediumUsage.setChecked(true);
        }else if(pref_usage.equals("2")){
            rHighUsage.setChecked(true);
        }


        if(pref_income.equals("0")){
            rZeroIncome.setChecked(true);
        }else if(pref_income.equals("1")){
            rLowIncome.setChecked(true);
        }else if(pref_income.equals("2")){
            rMediumIncome.setChecked(true);
        }else if(pref_income.equals("3")){
            rHighIncome.setChecked(true);
        }

        if(pref_medication.equals("0")){
            rMedicationFalse.setChecked(true);
        }else if(pref_medication.equals("1")){
            rMedicationTrue.setChecked(true);
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

        mUserID.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if(mUserID.getText().toString().isEmpty()){
                    mUserID.setHint(mMEI);
                }

            }
        });

        mAgreeTerms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(mAgreeTerms.isChecked()){
                        SharedPreferences prefUser = getApplicationContext().getSharedPreferences("user_info", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefUser.edit();
                        editor.putBoolean("TermsAgreement", true);
                        editor.apply();
                    }else{
                        SharedPreferences prefUser = getApplicationContext().getSharedPreferences("user_info", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefUser.edit();
                        editor.putBoolean("TermsAgreement", false);
                        editor.apply();
                    }
                }
            }
        );

//        mPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if(hasFocus){
//                }else {
//                    checkPassword(mPassword.getText().toString());
//                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(mPassword.getWindowToken(), 0);
//                }
//            }
//        });

//        mPassword.setOnEditorActionListener(new EditText.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if(actionId==EditorInfo.IME_ACTION_DONE){
//                    //Clear focus here from edittext
//                    mPassword.clearFocus();
//                }
//                return false;
//            }
//        });


// setOnFocusChangeListener(new View.OnFocusChangeListener() {
//
//            public void onFocusChange(View v, boolean hasFocus) {
//                checkPassword(mPassword.getText().toString());
//                Log.d("qqqq",mPassword.getText().toString());
//                findViewById(R.id.parentScrollView).invalidate();
//            }
//        });



        mActionFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onFinishChecker();

            }
        });






    }

    private void onFinishChecker() {
        String BirthDate = mAge.getText().toString();
        String ID = mUserID.getText().toString();
//        String password = mPassword.getText().toString();
        String gender="";
        String health=mEditTextScore.getText().toString();
        int selectedGender = mRadioGroupGender.getCheckedRadioButtonId();
        if(selectedGender == rMale.getId()) {
            gender="Male";
        } else if(selectedGender == rFemale.getId()) {
            gender="Female";
        }


        String education="";
        int selectedEducation = mRadioGroupEducation.getCheckedRadioButtonId();
        if(selectedEducation == rLowestEducation.getId()) {
            education="0";
        } else if(selectedEducation == rLowEducation.getId()) {
            education="1";
        }else if(selectedEducation == rMediumEducation.getId()) {
            education="2";
        }else if(selectedEducation == rHighEducation.getId()) {
            education="3";
        }else if(selectedEducation == rHighestEducation.getId()) {
            education="4";
        }

        String smartphone_usage="";
        int selectedUsage = mRadioGroupUsage.getCheckedRadioButtonId();
        if(selectedUsage == rLowUsage.getId()) {
            smartphone_usage="0";
        } else if(selectedUsage == rMediumUsage.getId()) {
            smartphone_usage="1";
        }else if(selectedUsage == rHighUsage.getId()) {
            smartphone_usage="2";
        }


        String income="";
        int selectedIncome = mRadioGroupIncome.getCheckedRadioButtonId();
        if(selectedIncome == rZeroIncome.getId()) {
            income="0";
        } else if(selectedIncome == rLowIncome.getId()) {
            income="1";
        }else if(selectedIncome == rMediumIncome.getId()) {
            income="2";
        }else if(selectedIncome == rHighIncome.getId()) {
            income="3";
        }

        String medication="";
        int selectedMedication = mRadioGroupMedication.getCheckedRadioButtonId();
        if(selectedMedication == rMedicationFalse.getId()) {
            medication="0";
        } else if(selectedMedication == rMedicationTrue.getId()) {
            medication="1";
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

        if(!BirthDate.isEmpty() && (!ID.isEmpty() || !pref_ID.isEmpty())  && !gender.isEmpty() && !health.isEmpty()
                && !education.isEmpty() && !income.isEmpty() && !smartphone_usage.isEmpty() && !medication.isEmpty()
        ) {
            editor = prefUser.edit();
            editor.putString("BirthDate", BirthDate);
            if(!ID.isEmpty() ){
                editor.putString("ID", ID);
            }else{
                editor.putString("ID", mMEI);
            }
            editor.putString("Gender", gender);
            editor.putString("Health", health);

            editor.putString("Education", education);
            editor.putString("Usage", smartphone_usage);
            editor.putString("Income", income);
            editor.putString("Medication", medication);
//            editor.putString("Password", password);
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

    @Override
    public void onBackPressed(){
        onFinishChecker();
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

//    private void checkPassword(String pref_password){
//        if(!pref_password.isEmpty()){
//            if(!pref_password.equals(getConfigValue(getApplicationContext(), "password"))){
//                mLayout2.setVisibility(View.GONE);
//                mLayout3.setVisibility(View.GONE);
//                mLayout4.setVisibility(View.GONE);
//                mLayout5.setVisibility(View.GONE);
//                mLayout6.setVisibility(View.GONE);
//
//            }else{
//                mLayout2.setVisibility(View.VISIBLE);
//                mLayout3.setVisibility(View.VISIBLE);
//                mLayout4.setVisibility(View.VISIBLE);
//                mLayout5.setVisibility(View.VISIBLE);
//                mLayout6.setVisibility(View.VISIBLE);
////                mLayout1.setVisibility(View.GONE);
//            }
//
//        }else{
//            mLayout3.setVisibility(View.GONE);
//            mLayout4.setVisibility(View.GONE);
//            mLayout5.setVisibility(View.GONE);
//            mLayout6.setVisibility(View.GONE);
//
//        }
//    }


//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
//            View view = getCurrentFocus();
//            if (view != null && view instanceof EditText) {
//                Rect r = new Rect();
//                view.getGlobalVisibleRect(r);
//                int rawX = (int)ev.getRawX();
//                int rawY = (int)ev.getRawY();
//                if (!r.contains(rawX, rawY)) {
//                    view.clearFocus();
//                }
//            }
//        }
//        return super.dispatchTouchEvent(ev);
//    }

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
