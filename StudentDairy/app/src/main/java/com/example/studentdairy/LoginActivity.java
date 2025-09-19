package com.example.studentdairy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText etUserId, etPassword;
    Button btnLogin;
    ProgressBar progressBar;
    CheckBox cbRemember;
    TextView tvSignup;

    SharedPreferences sharedPreferences;

    private static final String LOGIN_URL = "https://trifrnd.co.in/school/api/data.php?apicall=login";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        etUserId = findViewById(R.id.etUserId);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        cbRemember = findViewById(R.id.cbRemember);
        tvSignup = findViewById(R.id.tvLogin);

        sharedPreferences = getSharedPreferences("MyPref", MODE_PRIVATE);

        // Login button click
        btnLogin.setOnClickListener(v -> {
            String userid = etUserId.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!isValidUserId(userid)) {
                etUserId.setError("Enter valid Email or Mobile number");
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError("Password required");
                return;
            }

            loginUser(userid, password);
        });

        // Signup TextView click
        tvSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private boolean isValidUserId(String userid) {
        // Check if email or 10-digit mobile
        return Patterns.EMAIL_ADDRESS.matcher(userid).matches() ||
                userid.matches("\\d{10}");
    }

    private void loginUser(String userid, String password) {
        progressBar.setVisibility(android.view.View.VISIBLE);
        btnLogin.setEnabled(false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
                response -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    btnLogin.setEnabled(true);

                    Log.d("LoginAPI", "Response: " + response); // 👈 Debugging

                    try {
                        // Check if response is JSON object
                        if (response.trim().startsWith("{")) {
                            JSONObject obj = new JSONObject(response);
                            boolean error = obj.getBoolean("error");
                            String message = obj.getString("message");

                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();

                            if (!error) {
                                // Save login info
                                sharedPreferences.edit().putString("userid", userid).apply();

                                // TODO: Save "Remember Me" if required
                                // sharedPreferences.edit().putBoolean("remember", cbRemember.isChecked()).apply();

                                // Go to MainActivity
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            // Response is plain text (like "Invalid username or password")
                            Toast.makeText(LoginActivity.this, response, Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "JSON Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                },
                error -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Volley Error: " + (error.getMessage() != null ? error.getMessage() : "Network Error"), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userid", userid);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
