package typeofmood.ime.datahandler;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;




public class MoodDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "typeofmood_mood.db";
    private static final String TABLE_NAME = "typeofmood_mood_data";
    private static final String ID = "ID";
    private static final String MOOOD_DATA = "MOOOD_DATA";
    private static final String DATE_DATA = "DATE_DATA";
    private static final String DATETIME_DATA = "DATETIME_DATA";






    public MoodDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " MOOOD_DATA TEXT, "+" DATE_DATA TEXT, "+ "DATETIME_DATA TEXT)";
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
        contentValues.put(MOOOD_DATA, moodData);
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

    public Cursor getListDateContents(String Date){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE DATE_DATA = ?",new String[] { Date});
        return data;
    }

    public Cursor getListMoodContents(String mood){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME +" WHERE MOOOD_DATA = ?", new String[] { mood});
        return data;
    }


    private String getCurrentDate() {
        return new SimpleDateFormat("dd-MM-yyyy").format(new Date());
    } //"dd-MM-yyyy" might need fix to "d-M-yyyy"

    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

}

