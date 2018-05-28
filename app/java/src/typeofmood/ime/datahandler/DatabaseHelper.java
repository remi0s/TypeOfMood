package typeofmood.ime.datahandler;

import android.database.sqlite.SQLiteOpenHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Settings;
import android.provider.Settings.System;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import typeofmood.ime.latin.LatinIME;


public class DatabaseHelper extends SQLiteOpenHelper {
//    KeyboardPayload payload;

    private static final String DATABASE_NAME = "typeofmood.db";
    private static final String TABLE_NAME = "typeofmoodData";
    private static final String SESSION_DATA = "SESSION_DATA";
    private static final String DATETIME_DATA = "DATETIME_DATA";
    private static final String DOC_ID = "DOC_ID";
    private static final String SEND_TIME = "SEND_TIME";





    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (DOC_ID TEXT PRIMARY KEY, " +
                " DATETIME_DATA TEXT, "+" SESSION_DATA TEXT, "+ " SEND_TIME TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String sessionData) {
        SQLiteDatabase db = this.getWritableDatabase();
        String date=String.valueOf(new Date(java.lang.System.currentTimeMillis()));
        String textToHash=sessionData+date;
        String encoded;
        try {
            encoded = hash256(textToHash);
        }catch (Exception e){
            e.printStackTrace();
            encoded="Failed";
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(DOC_ID, encoded);
        contentValues.put(DATETIME_DATA, date);
        contentValues.put(SESSION_DATA, sessionData);
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

    public Cursor getNotSendContents(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME +" WHERE SEND_TIME IS NULL", null);
        return data;
    }

    public void setSend(String docId){
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("UPDATE "+TABLE_NAME+" SET SEND_TIME = datetime('now') WHERE DOC_ID = ?",new String[] { docId});

        }catch(Exception e)
        {
            e.printStackTrace();
        }


    }


    private static String hash256(String data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(data.getBytes());
        return bytesToHex(md.digest());
    }
    private static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte byt : bytes) result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }
}

