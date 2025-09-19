package com.example;


import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Purpose: Update admin screen for emergency contact and message
 */
public class UpdateActivity extends AppCompatActivity {
    EditText edtName, edtPhone, edtMessage;
    Button btnSave;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtMessage = findViewById(R.id.edtMsg);
        btnSave = findViewById(R.id.btnSave);
        db = new DatabaseHelper(this);
        loadExisting();

        btnSave.setOnClickListener(v -> {
            String n = edtName.getText().toString().trim();
            String p = edtPhone.getText().toString().trim();
            String m = edtMessage.getText().toString().trim();
            if (TextUtils.isEmpty(p)) {
                Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
                return;
            }
            db.saveEmergency(n, p, m);
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            finish(); // return to main screen
        });
    }

    private void loadExisting() {
        String[] data = db.loadEmergency();
        edtName.setText(data[0]);
        edtPhone.setText(data[1]);
        edtMessage.setText(data[2]);
    }
}
