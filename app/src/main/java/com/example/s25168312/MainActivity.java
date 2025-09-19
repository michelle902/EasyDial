package com.example.s25168312;



import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_CALL_PERMISSION = 100;
    private static final int REQ_SMS_LOCATION_PERM = 101;
    private static final int PICK_CONTACT = 102;

    EditText edtNumber;
    Button btnCall, btnDialContact, btnAnswer, btnDecline, btnCalendar, btnUpdate, btnEmergency;
    DatabaseHelper db;
    String emPhone = "";
    String emMessage = "";
    String emName = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enable edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtNumber = findViewById(R.id.edtNumber);
        btnCall = findViewById(R.id.btnCall);
        btnDialContact = findViewById(R.id.btnContactCall);
        btnAnswer = findViewById(R.id.btnAnswer);
        btnDecline = findViewById(R.id.btnDecline);
        btnCalendar = findViewById(R.id.btnCalendar);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnEmergency = findViewById(R.id.btnEmergency);

        db = new DatabaseHelper(this);
        loadEmergencyInfo();

        btnCall.setOnClickListener(v -> dialNumberDirect());

        btnDialContact.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PICK_CONTACT);
            } else {
                pickContact();
            }
        });

        btnAnswer.setOnClickListener(v -> Toast.makeText(this, "ANSWER pressed (simulated)", Toast.LENGTH_SHORT).show());
        btnDecline.setOnClickListener(v -> Toast.makeText(this, "DECLINE pressed (simulated)", Toast.LENGTH_SHORT).show());
        btnUpdate.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, UpdateActivity.class)));
        btnCalendar.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CalendarActivity.class)));
        btnEmergency.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION},
                        REQ_SMS_LOCATION_PERM);
            } else {
                sendEmergencySmsWithLocation();
            }
        });
    }

    private void loadEmergencyInfo() {
        String[] data = db.loadEmergency();
        emName = data[0];
        emPhone = data[1];
        emMessage = data[2];
        if (TextUtils.isEmpty(emPhone)) {
            emPhone = "";
            emMessage = "Emergency! I need help.";
            emName = "";
        }
    }

    private void dialNumberDirect() {
        String number = edtNumber.getText().toString().trim();
        if (TextUtils.isEmpty(number)) {
            Toast.makeText(this, "Enter number to call", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQ_CALL_PERMISSION);
            return;
        }
        startActivity(intent);
    }

    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT && resultCode == Activity.RESULT_OK) {
            Uri contactUri = data.getData();
            String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};
            try (android.database.Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    String number = cursor.getString(index);
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + number));
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQ_CALL_PERMISSION);
                        return;
                    }
                    startActivity(intent);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendEmergencySmsWithLocation() {
        loadEmergencyInfo();
        if (TextUtils.isEmpty(emPhone)) {
            Toast.makeText(this, "No emergency phone set. Use Update screen to set one.", Toast.LENGTH_LONG).show();
            return;
        }

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        Location l = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (l == null) l = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        String locationPart = "";
        if (l != null) {
            locationPart = " https://maps.google.com/?q=" + l.getLatitude() + "," + l.getLongitude();
        } else {
            locationPart = " (Location not available)";
        }

        String smsText = (emName.isEmpty() ? "" : emName + ": ") + emMessage + locationPart;

        try {
            SmsManager.getDefault().sendTextMessage(emPhone, null, smsText, null, null);
            Toast.makeText(this, "Emergency SMS sent", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] perms, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, perms, results);
        if (requestCode == REQ_CALL_PERMISSION) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                dialNumberDirect();
            } else {
                Toast.makeText(this, "Call permission required to make calls", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQ_SMS_LOCATION_PERM) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                sendEmergencySmsWithLocation();
            } else {
                Toast.makeText(this, "SMS and Location permission required for Emergency", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == PICK_CONTACT) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                pickContact();
            }
        }
    }
}
