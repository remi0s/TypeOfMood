package typeofmood.ime.datahandler;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MoodDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "typeofmood_mood.db";
    private static final String TABLE_NAME = "typeofmood_mood_data";
    private static final String ID = "ID";
    private static final String MOOD_DATA = "MOOD_DATA";
    private static final String PHYSICAL_STATE_DATA = "PHYSICAL_STATE_DATA";
    private static final String DATE_DATA = "DATE_DATA";
    private static final String DATETIME_DATA = "DATETIME_DATA";
    private static final String SEND_TIME = "SEND_TIME";






    public MoodDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " MOOD_DATA TEXT, "+" PHYSICAL_STATE_DATA TEXT, "+" DATE_DATA TEXT, "+ "DATETIME_DATA TEXT, "+"SEND_TIME TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String moodData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(MOOD_DATA, moodData);
        contentValues.put(DATE_DATA, getCurrentDate());
        contentValues.put(DATETIME_DATA, getCurrentTime());

        long result = db.insert(TABLE_NAME, null, contentValues);
        //if date as inserted incorrectly it will return -1
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean addPhysicalStateData(String physicalStateData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(PHYSICAL_STATE_DATA, physicalStateData);
        contentValues.put(DATE_DATA, getCurrentDate());
        contentValues.put(DATETIME_DATA, getCurrentTime());

        long result = db.insert(TABLE_NAME, null, contentValues);
        //if date as inserted incorrectly it will return -1
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }
    public Cursor getListContents(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return data;
    }

    public Cursor getListDateContentsMood(String Date){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE DATE_DATA = ? AND MOOD_DATA IS NOT NULL",new String[] { Date});
        return data;
    }
    public Cursor getListDateContentsPhysical(String Date){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE DATE_DATA = ? AND PHYSICAL_STATE_DATA IS NOT NULL",new String[] { Date});
        return data;
    }


    public int getPeriodContentsMood(String Start,String End, String mood){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT ID FROM "+TABLE_NAME+" WHERE (DATE_DATA >= ? AND  DATE_DATA <= ?) AND MOOD_DATA = ?",new String[] { Start, End , mood});
        if (data.getCount()==0){
            data = db.rawQuery("SELECT ID FROM "+TABLE_NAME+" WHERE (DATE_DATA >= ? AND  DATE_DATA <= ?) AND MOOD_DATA = ?",new String[] { End, Start , mood});
            return data.getCount();
        }else{
            return data.getCount();
        }

    }

    public int getPeriodContentsPhysical(String Start,String End, String state){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT ID FROM "+TABLE_NAME+" WHERE (DATE_DATA >= ? AND  DATE_DATA <= ?) AND PHYSICAL_STATE_DATA = ?",new String[] { Start, End , state});
        if (data.getCount()==0){
            data = db.rawQuery("SELECT ID FROM "+TABLE_NAME+" WHERE (DATE_DATA >= ? AND  DATE_DATA <= ?) AND PHYSICAL_STATE_DATA = ?",new String[] { End, Start , state});
            return data.getCount();
        }else{
            return data.getCount();
        }

    }

    public Cursor getListMoodContents(String mood){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME +" WHERE MOOD_DATA = ?", new String[] { mood});
        return data;
    }

    public Cursor getNotSendContents(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME +" WHERE SEND_TIME IS NULL", null);
        return data;
    }

    public void setSend(String id){
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("UPDATE "+TABLE_NAME+" SET SEND_TIME = datetime('now') WHERE ID = ?",new String[] { id});

        }catch(Exception e)
        {
            e.printStackTrace();
        }


    }


    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    } //"dd-MM-yyyy" might need fix to "d-M-yyyy"

    private String getCurrentTime() {
        return new SimpleDateFormat("hh:mm:ss", Locale.US).format(new Date());
    }

}

