package com.example.studentdairy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText etUserId, etPassword;
    Button btnLogin;
    ProgressBar progressBar;
    TextView tvSignup;

    SharedPreferences sharedPreferences;

    private static final String LOGIN_URL = "https://trifrnd.co.in/school/api/data.php?apicall=login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences("MyPref", MODE_PRIVATE);
        if (isUserLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        etUserId = findViewById(R.id.etUserId);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvSignup = findViewById(R.id.tvSignup);

        btnLogin.setOnClickListener(v -> {
            String userid = etUserId.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInput(userid, password)) {
                loginUser(userid, password);
            }
        });

        tvSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }

    private boolean isUserLoggedIn() {
        String userId = sharedPreferences.getString("userid", null);
        return userId != null && !userId.isEmpty();
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
        progressBar.setVisibility(android.view.View.VISIBLE);
        btnLogin.setEnabled(false);

        StringRequest request = new StringRequest(Request.Method.POST, LOGIN_URL,
                response -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    btnLogin.setEnabled(true);

                    // Remove all non-letter characters and convert to lowercase
                    String result = response.replaceAll("[^a-zA-Z]", "").toLowerCase();

                    if (result.equals("success")) {
                        // Correct login
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("userid", userid);
                        editor.apply();

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();

                    } else {
                        // Any other response → login failed
                        Toast.makeText(LoginActivity.this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                    }

                },
                error -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userid", userid);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
