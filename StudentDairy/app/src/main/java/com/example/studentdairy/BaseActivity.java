package com.example.studentdairy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    protected SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!sessionManager.isLoggedIn()) {
            forceLogout("Please login");
            return;
        }

        if (sessionManager.isSessionExpired()) {
            forceLogout("Session expired\nPlease login again.");
            return;
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (sessionManager.isLoggedIn()) {
            sessionManager.updateLastActiveTime();
        }
    }

    private void forceLogout(String msg) {
        sessionManager.clearSession();

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}
