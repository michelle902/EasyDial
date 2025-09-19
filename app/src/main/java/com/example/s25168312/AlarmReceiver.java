package com.example;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = intent.getStringExtra("message");
        if (msg == null) msg = "Reminder";
        // Simple behavior: a Toast when triggered. (Marker: you may change to Notification with sound)
        Toast.makeText(context, "Reminder: " + msg, Toast.LENGTH_LONG).show();
    }
}
