package com.example.studentdairy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * MainActivity with auto-logout check on startup.
 */
public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // Check auto logout right away
        checkAutoLogout();

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set default fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_message) {
                selectedFragment = new MessageFragment();
            } else if (id == R.id.nav_idCard) {
                selectedFragment = new IdcardFragment();
            } else if (id == R.id.nav_notification) {
                selectedFragment = new NotificationFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update last active time whenever user returns to app
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            sessionManager.updateLastActiveTime();
        }
    }

    private void checkAutoLogout() {
        if (sessionManager == null) sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) return;

        long lastActive = sessionManager.getLastActiveTime();
        long now = System.currentTimeMillis();
        long fifteenMinutesMs = 15L * 60L * 1000L; // 15 minutes

        if (lastActive == 0) {
            // first time — initialize and continue
            sessionManager.updateLastActiveTime();
            return;
        }

        if (now - lastActive > fifteenMinutesMs) {
            // Clear session and other stored pref groups
            sessionManager.clearSession();

            // Clear other SharedPreferences used in your app
            getSharedPreferences("StudentProfile", MODE_PRIVATE).edit().clear().apply();
            getSharedPreferences("ParentProfile", MODE_PRIVATE).edit().clear().apply();
            getSharedPreferences("Homework", MODE_PRIVATE).edit().clear().apply();
            getSharedPreferences("Timetable", MODE_PRIVATE).edit().clear().apply();

            // Go to LoginActivity and clear back stack
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            Toast.makeText(this, "Logged out due to inactivity", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
