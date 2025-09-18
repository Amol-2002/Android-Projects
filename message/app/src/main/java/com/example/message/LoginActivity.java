package com.example.message;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

public class LoginActivity extends AppCompatActivity {

    EditText etUserid, etPassword;
    Button btnLogin, btnGoToSignup;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUserid = findViewById(R.id.etUserid);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToSignup = findViewById(R.id.btnGoToSignup);
        progressBar = findViewById(R.id.progressBar);

        btnGoToSignup.setOnClickListener(v -> {
            // Open signup activity if exists
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String userid = etUserid.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (userid.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter userid and password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String url = "https://trifrnd.co.in/school/api/data.php?apicall=login";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        if (jsonObject.has("id")) {
                            // Login successful
                            String studentId = jsonObject.getString("student_id");
                            String firstName = jsonObject.getString("first_name");
                            String middleName = jsonObject.getString("middle_name");
                            String lastName = jsonObject.getString("last_name");
                            String classId = jsonObject.getString("class_id");
                            String sectionId = jsonObject.getString("section_id");

                            Toast.makeText(this, "Welcome " + firstName + " " + lastName, Toast.LENGTH_SHORT).show();

                            // You can now open another activity
                             Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                             startActivity(intent);
                             finish();

                        } else {
                            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
