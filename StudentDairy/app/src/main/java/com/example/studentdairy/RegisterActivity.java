package com.example.studentdairy;

import android.content.Intent;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText etMobile, etPassword;
    Button btnSignup;
    ProgressBar progressBar;
    TextView tvLogin;

    private static final String SIGNUP_URL = "https://trifrnd.co.in/school/api/data.php?apicall=signup";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        btnSignup = findViewById(R.id.btnSignup);
        progressBar = findViewById(R.id.progressBar);
        tvLogin = findViewById(R.id.tvLogin);

        // Signup button click
        btnSignup.setOnClickListener(v -> {
            String userid = etMobile.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInput(userid, password)) {
                registerUser(userid, password);
            }
        });

        // Go to Login
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private boolean validateInput(String userid, String password) {
        boolean isValid = true;

        if (userid.isEmpty() || !userid.matches("\\d{10}")) {
            etMobile.setError("Enter valid 10-digit mobile number");
            isValid = false;
        }

        if (password.isEmpty() || password.length() < 4) {
            etPassword.setError("Password must be at least 4 characters");
            isValid = false;
        }

        return isValid;
    }

    private void registerUser(String userid, String password) {
        progressBar.setVisibility(android.view.View.VISIBLE);
        btnSignup.setEnabled(false);

        StringRequest request = new StringRequest(Request.Method.POST, SIGNUP_URL,
                response -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    btnSignup.setEnabled(true);

                    // Safe JSON parsing
                    try {
                        String trimmed = response.trim();
                        if (trimmed.startsWith("{")) {
                            JSONObject obj = new JSONObject(trimmed);
                            String message = obj.optString("message", "Response received");
                            boolean error = obj.optBoolean("error", true);

                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();

                            if (!error) {
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                            }
                        } else {
                            // Response is not JSON
                            Toast.makeText(RegisterActivity.this, trimmed, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(RegisterActivity.this, "Invalid response format", Toast.LENGTH_SHORT).show();
                    }

                },
                error -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    btnSignup.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
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
