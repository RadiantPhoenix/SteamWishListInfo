package com.example.steamwishlistinfo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends Activity {

    public static final String PREF_USER_ID = "UserId";
    public static final String PREFS_NAME = "com.example.steamwishlistinfo.PREFERENCES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final EditText userIdInput = findViewById(R.id.user_id_input);
        Button saveButton = findViewById(R.id.btn_save);

        // Load saved User ID if available
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedUserId = preferences.getString(PREF_USER_ID, "");
        userIdInput.setText(savedUserId);

        // Save button logic
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = userIdInput.getText().toString();

                // Save the User ID to SharedPreferences
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(PREF_USER_ID, userId);
                editor.apply();

                // Close the activity after saving
                finish();
            }
        });
    }
}

