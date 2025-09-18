package com.example.studentdairy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

    EditText etUsername, etEmail, etMobile, etPassword;
    Spinner spGender;
    Button btnSignup;
    ProgressBar progressBar;

    private static final String SIGNUP_URL = "https://trifrnd.co.in/school/api/data.php?apicall=signup";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        spGender = findViewById(R.id.spGender);
        btnSignup = findViewById(R.id.btnSignup);
        progressBar = findViewById(R.id.progressBar);

        // Gender dropdown
        String[] genders = {"Select Gender", "Male", "Female", "Other"};
        spGender.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genders));

        btnSignup.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String mobile = etMobile.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String gender = spGender.getSelectedItem().toString();

            // --- VALIDATION ---
            if (username.isEmpty()) {
                etUsername.setError("Username required");
                etUsername.requestFocus();
                return;
            }

            if (username.length() < 3) {
                etUsername.setError("Username must be at least 3 characters");
                etUsername.requestFocus();
                return;
            }

            if (email.isEmpty()) {
                etEmail.setError("Email required");
                etEmail.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter a valid email");
                etEmail.requestFocus();
                return;
            }

            if (mobile.isEmpty()) {
                etMobile.setError("Mobile number required");
                etMobile.requestFocus();
                return;
            }

            if (!mobile.matches("\\d{10}")) {
                etMobile.setError("Enter a valid 10-digit mobile number");
                etMobile.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Password required");
                etPassword.requestFocus();
                return;
            }

            if (password.length() < 6) {
                etPassword.setError("Password must be at least 6 characters");
                etPassword.requestFocus();
                return;
            }

            // Optional: Strong password (uppercase, number, special char)
            /*
            if (!password.matches("^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=]).{6,}$")) {
                etPassword.setError("Password must contain uppercase, number & special char");
                etPassword.requestFocus();
                return;
            }
            */

            if (gender.equals("Select Gender")) {
                Toast.makeText(RegisterActivity.this, "Please select a gender", Toast.LENGTH_SHORT).show();
                return;
            }

            // All validations passed → register user
            registerUser(username, email, gender, mobile, password);
        });

        TextView tvLogin = findViewById(R.id.tvLogin); // Add this line
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // optional: close RegisterActivity
        });

    }

    private void registerUser(String username, String email, String gender, String mobile, String password) {
        progressBar.setVisibility(View.VISIBLE);
        btnSignup.setEnabled(false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, SIGNUP_URL,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    btnSignup.setEnabled(true);
                    try {
                        JSONObject obj = new JSONObject(response);
                        boolean error = obj.getBoolean("error");
                        String message = obj.getString("message");

                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();

                        if (!error) {
                            // Registration successful → Go to Login
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(RegisterActivity.this, "JSON Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    btnSignup.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Volley Error: " + (error.getMessage() != null ? error.getMessage() : "Network Error"), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("email", email);
                params.put("gender", gender);
                params.put("mobile", mobile);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
