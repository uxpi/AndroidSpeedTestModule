package com.android.luas.networkspeedtest;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;
import java.util.List;

public class SQLiteDatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "SpeedTest";
    private static final String TABLE_NAME = "SpeedResults";
    private static final String KEY_ID = "id";
    private static final String KEY_TIME = "time";
    private static final String KEY_DOWNLOAD_SPEED = "downloadSpeed";
    private static final String KEY_UPLOAD_SPEED = "uploadSpeed";
    private static final String KEY_TOTAL_DOWNLOAD_MB = "totalDownloadMB";
    private static final String KEY_TOTAL_UPLOAD_MB = "totalUploadMB";
    private static final String KEY_PING_TIME = "pingTime";
    private static final String KEY_SSL_BOOL = "sslBool";

    SQLiteDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATION_TABLE = "CREATE TABLE " +TABLE_NAME + " ( "
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + KEY_DOWNLOAD_SPEED + " REAL, " + KEY_UPLOAD_SPEED + " REAL, " + KEY_TOTAL_DOWNLOAD_MB + " INTEGER, "
                + KEY_TOTAL_UPLOAD_MB + " INTEGER, " + KEY_PING_TIME + " REAL, " + KEY_SSL_BOOL + " INTEGER )";

        db.execSQL(CREATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(db);
    }

    public List<SpeedResults> allSpeedResults() {
        List<SpeedResults> results = new LinkedList<>();
        String query = "SELECT  * FROM " + TABLE_NAME + " ORDER BY datetime(" + KEY_TIME + ") DESC";
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);
        SpeedResults speedResults;

        if (cursor.moveToFirst()) {
            do {
                speedResults = new SpeedResults();
                speedResults.setId(Integer.parseInt(cursor.getString(0)));
                speedResults.setTime(cursor.getString(1));
                speedResults.setDownloadSpeed(Double.parseDouble(cursor.getString(2)));
                speedResults.setUploadSpeed(Double.parseDouble(cursor.getString(3)));
                speedResults.setTotalDownloadMB(Integer.parseInt(cursor.getString(4)));
                speedResults.setTotalUploadMB(Integer.parseInt(cursor.getString(5)));
                speedResults.setPingTime(Double.parseDouble(cursor.getString(6)));
                speedResults.setSslOn(Integer.parseInt(cursor.getString(7)) == 1);
                results.add(speedResults);
            } while (cursor.moveToNext());
        }

        return results;
    }

    public void addSpeedResult(SpeedResults speedResults) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DOWNLOAD_SPEED, speedResults.getDownloadSpeed());
        values.put(KEY_UPLOAD_SPEED, speedResults.getUploadSpeed());
        values.put(KEY_TOTAL_DOWNLOAD_MB, speedResults.getTotalDownloadMB());
        values.put(KEY_TOTAL_UPLOAD_MB, speedResults.getTotalUploadMB());
        values.put(KEY_PING_TIME, speedResults.getPingTime());
        values.put(KEY_SSL_BOOL, speedResults.isSslOn());
        db.insert(TABLE_NAME,null, values);
        db.close();
    }

}