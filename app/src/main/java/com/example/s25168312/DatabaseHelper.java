package com.example;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * Purpose: store emergency contact/message and calendar events for Telephone App for the Elderly
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "teleapp.db";
    private static final int DB_VERSION = 1;

    // Emergency table
    private static final String TABLE_EMERGENCY = "emergency";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_PHONE = "phone";
    private static final String COL_MESSAGE = "message";

    // Events table
    private static final String TABLE_EVENTS = "events";
    private static final String EV_ID = "id";
    private static final String EV_TITLE = "title";
    private static final String EV_YEAR = "year";
    private static final String EV_MONTH = "month";
    private static final String EV_DAY = "day";
    private static final String EV_HOUR = "hour";
    private static final String EV_MINUTE = "minute";

    public DatabaseHelper(Context c) {
        super(c, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createEmergency = "CREATE TABLE " + TABLE_EMERGENCY + " (" +
                COL_ID + " INTEGER PRIMARY KEY, " +
                COL_NAME + " TEXT, " +
                COL_PHONE + " TEXT, " +
                COL_MESSAGE + " TEXT)";
        db.execSQL(createEmergency);

        String createEvents = "CREATE TABLE " + TABLE_EVENTS + " (" +
                EV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EV_TITLE + " TEXT, " +
                EV_YEAR + " INTEGER, " +
                EV_MONTH + " INTEGER, " +
                EV_DAY + " INTEGER, " +
                EV_HOUR + " INTEGER, " +
                EV_MINUTE + " INTEGER)";
        db.execSQL(createEvents);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMERGENCY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }

    // Emergency CRUD
    public void saveEmergency(String name, String phone, String message) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_EMERGENCY, null, null); // keep only one row
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, name);
        cv.put(COL_PHONE, phone);
        cv.put(COL_MESSAGE, message);
        db.insert(TABLE_EMERGENCY, null, cv);
        db.close();
    }

    public String[] loadEmergency() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_EMERGENCY, null, null, null, null, null, null);
        if (c.moveToFirst()) {
            String name = c.getString(c.getColumnIndexOrThrow(COL_NAME));
            String phone = c.getString(c.getColumnIndexOrThrow(COL_PHONE));
            String message = c.getString(c.getColumnIndexOrThrow(COL_MESSAGE));
            c.close();
            db.close();
            return new String[]{name, phone, message};
        }
        c.close();
        db.close();
        return new String[]{"", "", ""};
    }

    // Events CRUD
    public long addEvent(Event e) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(EV_TITLE, e.getTitle());
        cv.put(EV_YEAR, e.getYear());
        cv.put(EV_MONTH, e.getMonth());
        cv.put(EV_DAY, e.getDay());
        cv.put(EV_HOUR, e.getHour());
        cv.put(EV_MINUTE, e.getMinute());
        long id = db.insert(TABLE_EVENTS, null, cv);
        db.close();
        return id;
    }

    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_EVENTS, null, null, null, null, null, EV_YEAR + "," + EV_MONTH + "," + EV_DAY + "," + EV_HOUR + "," + EV_MINUTE);
        while (c.moveToNext()) {
            Event e = new Event(
                    c.getInt(c.getColumnIndexOrThrow(EV_ID)),
                    c.getString(c.getColumnIndexOrThrow(EV_TITLE)),
                    c.getInt(c.getColumnIndexOrThrow(EV_YEAR)),
                    c.getInt(c.getColumnIndexOrThrow(EV_MONTH)),
                    c.getInt(c.getColumnIndexOrThrow(EV_DAY)),
                    c.getInt(c.getColumnIndexOrThrow(EV_HOUR)),
                    c.getInt(c.getColumnIndexOrThrow(EV_MINUTE))
            );
            events.add(e);
        }
        c.close();
        db.close();
        return events;
    }
}
