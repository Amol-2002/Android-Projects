package com.example.studentdairy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String LOGIN_URL = "https://testing.trifrnd.net.in/ishwar/school/api/user_login.php";

    EditText etUserId, etPassword;
    Button btnLogin;

    SharedPreferences studentPrefs, parentPrefs, timePrefs, timetabelPrefs;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUserId = findViewById(R.id.etUserId);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // SharedPreferences
        studentPrefs    = getSharedPreferences("StudentProfile", MODE_PRIVATE);
        parentPrefs     = getSharedPreferences("ParentProfile", MODE_PRIVATE);
        timePrefs       = getSharedPreferences("Homework", MODE_PRIVATE);
        timetabelPrefs  = getSharedPreferences("Timetable", MODE_PRIVATE); // <-- note exact name

        requestQueue = Volley.newRequestQueue(this);

        // Skip login if already logged in
        if (studentPrefs.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        btnLogin.setOnClickListener(v -> {
            String userid = etUserId.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInput(userid, password)) {
                loginUser(userid, password);
            }
        });
    }

    private boolean validateInput(String userid, String password) {
        boolean isValid = true;

        if (userid.isEmpty()) {
            etUserId.setError("User ID required");
            isValid = false;
        }

        if (password.isEmpty() || password.length() < 4) {
            etPassword.setError("Password must be at least 4 characters");
            isValid = false;
        }

        return isValid;
    }

    private void loginUser(String userid, String password) {
        btnLogin.setEnabled(false);

        StringRequest request = new StringRequest(Request.Method.POST, LOGIN_URL,
                response -> {
                    btnLogin.setEnabled(true);

                    // Your original naive success check (keeps it)
                    String result = response.replaceAll("[^a-zA-Z]", "").toLowerCase();

                    if (result.equals("success")) {
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                        // Save Student info
                        SharedPreferences.Editor studentEditor = studentPrefs.edit();
                        studentEditor.putString("userid", userid);
                        studentEditor.putBoolean("isLoggedIn", true);
                        studentEditor.apply();

                        // Save Parent mobile (same as userid)
                        SharedPreferences.Editor parentEditor = parentPrefs.edit();
                        parentEditor.putString("mobile", userid);
                        parentEditor.apply();

                        // Save time table mobile (same as userid) -> IMPORTANT: save to "Timetable"
                        SharedPreferences.Editor ttEditor = timetabelPrefs.edit();
                        ttEditor.putString("mobile", userid);
                        ttEditor.apply();

                        // Also keep the Homework/mobile pref if you need (optional)
                        SharedPreferences.Editor timeEditor = timePrefs.edit();
                        timeEditor.putString("mobile", userid);
                        timeEditor.apply();

                        Log.d(TAG, "Saved mobile to prefs: " + userid + " (Timetable & Parent & Homework)");

                        // Go to MainActivity
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();

                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                    }

                },
                error -> {
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Network Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userid", userid);
                params.put("password", password);
                return params;
            }
        };

        // ensure request queue uses a tag if you want to cancel later
        request.setTag("LoginReq");
        requestQueue.add(request);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll("LoginReq");
        }
    }
}
