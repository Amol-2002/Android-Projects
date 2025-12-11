package com.example.astrotalk; // 🔁 change to your package name

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String LOGIN_URL = "https://testing.trifrnd.net.in/ishwar/chat/login_api.php";

    private EditText etUserId, etPassword;
    private MaterialButton btnLogin;
    private TextView tvSkip, btnEmail;

    private ProgressDialog progressDialog;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 🔁 Use your actual layout file name here
        setContentView(R.layout.activity_login);

        // Init views
        etUserId = findViewById(R.id.etUserId);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSkip = findViewById(R.id.tvSkip);
        btnEmail = findViewById(R.id.btnEmail);

        // Progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        // Volley queue
        requestQueue = Volley.newRequestQueue(this);

        // Login button click
        btnLogin.setOnClickListener(v -> {
            String username = etUserId.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                etUserId.setError("Enter username");
                etUserId.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Enter password");
                etPassword.requestFocus();
                return;
            }

            doLogin(username, password);
        });

        // Optional: Skip click (go directly to main)
        tvSkip.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class); // 🔁 change if needed
            startActivity(intent);
            finish();
        });

        // Optional: Login with Email click
        btnEmail.setOnClickListener(v -> {
            // If you have another screen, open here
            // Intent i = new Intent(LoginActivity.this, EmailLoginActivity.class);
            // startActivity(i);
        });
    }

    private void doLogin(String username, String password) {
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        parseLoginResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this,
                                "Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // Sending POST params: username & password
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void parseLoginResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);

            // Your API returns:
            // { "Login successfully": true, "users": { "userid": 5, "username": "ishwar", "is_online": 1 } }
            boolean loginSuccess = jsonObject.optBoolean("Login successfully", false);

            if (loginSuccess) {
                JSONObject userObj = jsonObject.optJSONObject("users");
                if (userObj != null) {
                    int userId = userObj.optInt("userid", -1);
                    String username = userObj.optString("username", "");
                    int isOnline = userObj.optInt("is_online", 0);

                    // Save in SharedPreferences
                    saveUserData(this, userId, username, isOnline);

                    Toast.makeText(this, "Login successfully", Toast.LENGTH_SHORT).show();

                    // Go to main/home activity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class); // 🔁 change target activity
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "User data not found in response", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserData(Context context, int userId, String username, int isOnline) {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("userid", userId);
        editor.putString("username", username);
        editor.putInt("is_online", isOnline);
        editor.putBoolean("is_logged_in", true);
        editor.apply();
    }
}
