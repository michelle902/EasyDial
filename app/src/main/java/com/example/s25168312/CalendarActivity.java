package com.example.s25168312;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.List;

/**
 * Student: 25168312
 * Purpose: Simple calendar to add events and schedule reminders 24hrs and 1hr before
 */
public class CalendarActivity extends AppCompatActivity {
    Button btnAdd;
    ListView lvEvents;
    DatabaseHelper db;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        btnAdd = findViewById(R.id.btnAddEvent);
        lvEvents = findViewById(R.id.lvEvents);
        db = new DatabaseHelper(this);
        loadEvents();

        btnAdd.setOnClickListener(v -> pickDateTimeForEvent());
    }

    private void loadEvents() {
        List<Event> list = db.getAllEvents();
        String[] arr = new String[list.size()];
        for (int i=0;i<list.size();i++) {
            Event e = list.get(i);
            arr[i] = e.getTitle() + " - " + String.format("%02d-%02d-%04d %02d:%02d", e.getDay(), e.getMonth()+1, e.getYear(), e.getHour(), e.getMinute());
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arr);
        lvEvents.setAdapter(adapter);
    }

    private void pickDateTimeForEvent() {
        final Calendar now = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // after date picked, pick time
            TimePickerDialog tp = new TimePickerDialog(CalendarActivity.this, (timeView, hourOfDay, minute) -> {
                EditText inputTitle = new EditText(CalendarActivity.this);
                inputTitle.setHint("Event title");
                new android.app.AlertDialog.Builder(CalendarActivity.this)
                        .setTitle("Event title")
                        .setView(inputTitle)
                        .setPositiveButton("Save", (dialog, which) -> {
                            String title = inputTitle.getText().toString().trim();
                            if (title.isEmpty()) title = "Reminder";
                            Event e = new Event(title, year, month, dayOfMonth, hourOfDay, minute);
                            long id = db.addEvent(e);
                            scheduleReminders(e, (int)id);
                            loadEvents();
                            Toast.makeText(CalendarActivity.this, "Event saved", Toast.LENGTH_SHORT).show();
                        }).setNegativeButton("Cancel", null).show();
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
            tp.show();
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        dp.show();
    }

    private void scheduleReminders(Event e, int eventId) {
        Calendar eventTime = Calendar.getInstance();
        eventTime.set(e.getYear(), e.getMonth(), e.getDay(), e.getHour(), e.getMinute(), 0);

        // schedule 24 hours before
        long t1 = eventTime.getTimeInMillis() - 24L * 60 * 60 * 1000;
        scheduleAlarm(t1, eventId, e.getTitle() + " (24 hrs reminder)");

        // schedule 1 hour before
        long t2 = eventTime.getTimeInMillis() - 60L * 60 * 1000;
        scheduleAlarm(t2, eventId + 1000000, e.getTitle() + " (1 hr reminder)");
    }

    private void scheduleAlarm(long timeInMillis, int requestId, String message) {
        if (timeInMillis <= System.currentTimeMillis()) {
            // if time has already passed, do nothing
            return;
        }
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("message", message);
        PendingIntent pi = PendingIntent.getBroadcast(this, requestId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
    }
}
