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


public class Phq9questionnaire extends AppCompatActivity{
    private TextView mActionFinish;
    private RadioGroup[] mRadioQuestions=new RadioGroup[9];
//
//    private RadioGroup mRadioQuestion1,mRadioQuestion2,mRadioQuestion3,mRadioQuestion4,mRadioQuestion5,mRadioQuestion6,
//            mRadioQuestion7,mRadioQuestion8,mRadioQuestion9;
    private RadioButton[][] mAnswers=new RadioButton[9][4];
//    private RadioButton mQuestion1answer1,mQuestion1answer2,mQuestion1answer3,mQuestion1answer4;
//    private RadioButton mQuestion2answer1,mQuestion2answer2,mQuestion2answer3,mQuestion2answer4;
//    private RadioButton mQuestion3answer1,mQuestion3answer2,mQuestion3answer3,mQuestion3answer4;
//    private RadioButton mQuestion4answer1,mQuestion4answer2,mQuestion4answer3,mQuestion4answer4;
//    private RadioButton mQuestion5answer1,mQuestion5answer2,mQuestion5answer3,mQuestion5answer4;
//    private RadioButton mQuestion6answer1,mQuestion6answer2,mQuestion6answer3,mQuestion6answer4;
//    private RadioButton mQuestion7answer1,mQuestion7answer2,mQuestion7answer3,mQuestion7answer4;
//    private RadioButton mQuestion8answer1,mQuestion8answer2,mQuestion8answer3,mQuestion8answer4;
//    private RadioButton mQuestion9answer1,mQuestion9answer2,mQuestion9answer3,mQuestion9answer4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.typeofmood_patient_health_questionnaire);
        mActionFinish = findViewById(R.id.setup_finish);
        TextViewCompatUtils.setCompoundDrawablesRelativeWithIntrinsicBounds(mActionFinish,
                getResources().getDrawable(R.drawable.ic_setup_finish), null, null, null);

        SharedPreferences prefUser = getApplicationContext().getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUser.edit();
        editor.putBoolean("CompleteClicked", false);
        editor.apply();

        mRadioQuestions[0]=findViewById(R.id.radioQuestion1);
        mRadioQuestions[1]=findViewById(R.id.radioQuestion2);
        mRadioQuestions[2]=findViewById(R.id.radioQuestion3);
        mRadioQuestions[3]=findViewById(R.id.radioQuestion4);
        mRadioQuestions[4]=findViewById(R.id.radioQuestion5);
        mRadioQuestions[5]=findViewById(R.id.radioQuestion6);
        mRadioQuestions[6]=findViewById(R.id.radioQuestion7);
        mRadioQuestions[7]=findViewById(R.id.radioQuestion8);
        mRadioQuestions[8]=findViewById(R.id.radioQuestion9);
        mAnswers[0][0] = findViewById(R.id.question1answer1);
        mAnswers[0][1] = findViewById(R.id.question1answer2);
        mAnswers[0][2] = findViewById(R.id.question1answer3);
        mAnswers[0][3] = findViewById(R.id.question1answer4);
        mAnswers[1][0] = findViewById(R.id.question2answer1);
        mAnswers[1][1] = findViewById(R.id.question2answer2);
        mAnswers[1][2] = findViewById(R.id.question2answer3);
        mAnswers[1][3] = findViewById(R.id.question2answer4);
        mAnswers[2][0] = findViewById(R.id.question3answer1);
        mAnswers[2][1] = findViewById(R.id.question3answer2);
        mAnswers[2][2] = findViewById(R.id.question3answer3);
        mAnswers[2][3] = findViewById(R.id.question3answer4);
        mAnswers[3][0] = findViewById(R.id.question4answer1);
        mAnswers[3][1] = findViewById(R.id.question4answer2);
        mAnswers[3][2] = findViewById(R.id.question4answer3);
        mAnswers[3][3] = findViewById(R.id.question4answer4);
        mAnswers[4][0] = findViewById(R.id.question5answer1);
        mAnswers[4][1] = findViewById(R.id.question5answer2);
        mAnswers[4][2] = findViewById(R.id.question5answer3);
        mAnswers[4][3] = findViewById(R.id.question5answer4);
        mAnswers[5][0] = findViewById(R.id.question6answer1);
        mAnswers[5][1] = findViewById(R.id.question6answer2);
        mAnswers[5][2] = findViewById(R.id.question6answer3);
        mAnswers[5][3] = findViewById(R.id.question6answer4);
        mAnswers[6][0] = findViewById(R.id.question7answer1);
        mAnswers[6][1] = findViewById(R.id.question7answer2);
        mAnswers[6][2] = findViewById(R.id.question7answer3);
        mAnswers[6][3] = findViewById(R.id.question7answer4);
        mAnswers[7][0] = findViewById(R.id.question8answer1);
        mAnswers[7][1] = findViewById(R.id.question8answer2);
        mAnswers[7][2] = findViewById(R.id.question8answer3);
        mAnswers[7][3] = findViewById(R.id.question8answer4);
        mAnswers[8][0] = findViewById(R.id.question9answer1);
        mAnswers[8][1] = findViewById(R.id.question9answer2);
        mAnswers[8][2] = findViewById(R.id.question9answer3);
        mAnswers[8][3] = findViewById(R.id.question9answer4);



        mActionFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int score=0;
                int numberOfAnsweredQuestions=0;
                for(int i=0;i<=8;i++){
                    int answer=mRadioQuestions[i].getCheckedRadioButtonId();
                    if (answer!=-1){
                        numberOfAnsweredQuestions=numberOfAnsweredQuestions+1;
                    }
                    for(int j=0;j<=3;j++)
                    if(answer==mAnswers[i][j].getId()){
                        score=score+j;
                    }

                }


                if(numberOfAnsweredQuestions==9) {
                    int finalScore=(int) Math.ceil((double)score*9 / numberOfAnsweredQuestions);
                    //this form allows us to change it later for the user to be able to answer at least 7 out of 9 questions
                    //lower number than 7 answered questions should not been accepted

                    SharedPreferences prefUser = getApplicationContext().getSharedPreferences("user_info", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefUser.edit();
                    editor.putString("Health", Integer.toString(finalScore));
                    editor.apply();

                    finish();
                }else{
                    SharedPreferences prefUser = getApplicationContext().getSharedPreferences("user_info", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefUser.edit();
                    String prev_score=prefUser.getString("Health", "");
                    boolean CompleteClicked=prefUser.getBoolean("CompleteClicked", false);
                    editor.apply();
                    if(!prev_score.isEmpty()){

                        if(CompleteClicked){
                            finish();
                        }else{
                            editor = prefUser.edit();
                            editor.putBoolean("CompleteClicked", true);
                            editor.apply();
                            Toast.makeText(getApplicationContext(),"You should answer all the questions above!\nPress again Complete to exit and your last score will be held",Toast.LENGTH_LONG).show();
                        }


                    }else{
                        Toast.makeText(getApplicationContext(),"You should answer all the questions above!",Toast.LENGTH_LONG).show();
                    }


                }

            }
        });



    }


}
