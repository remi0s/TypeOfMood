package typeofmood.ime;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.tsongkha.spinnerdatepicker.DatePicker;
import com.tsongkha.spinnerdatepicker.DatePickerDialog;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Properties;

import android.text.Editable;
import android.view.inputmethod.InputMethodManager;
import typeofmood.ime.compat.TextViewCompatUtils;
import typeofmood.ime.latin.SystemBroadcastReceiver;


public class UserInfoConfiguration extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private TextView mActionFinish,mTerms;
    private CheckBox mAgreeTerms;
    private RadioGroup mRadioGroupGender;
    private RadioButton rMale, rFemale;
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


        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String pref_birth_date = pref.getString("BirthDate", "");
        String pref_ID = pref.getString("ID", "");
        String pref_gender = pref.getString("Gender", "");
        String pref_health = pref.getString("Health", "");
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
                editor.putString("ID", mMEI);
            }
            editor.putString("Gender", gender);
            editor.putString("Health", health);
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
