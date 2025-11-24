package com.example.studentdairy;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SupportFragment extends Fragment {

    private EditText etName, etEmail, etSubject, etDescription;
    private Button btnSend;
    private ProgressBar progress;
    private ImageView ivBack;

    private static final String SUPPORT_URL =
            "https://testing.trifrnd.net.in/ishwar/school/api/std_email_api.php";

    public SupportFragment() { }

    public static SupportFragment newInstance() {
        return new SupportFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_support, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etSubject = view.findViewById(R.id.etSubject);
        etDescription = view.findViewById(R.id.etDescription);
        btnSend = view.findViewById(R.id.btnSend);
        progress = view.findViewById(R.id.progress);
        ivBack = view.findViewById(R.id.attarrow);

        btnSend.setOnClickListener(v -> attemptSend());

        ivBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });
    }

    private void attemptSend() {

        final String name = etName.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        final String subject = etSubject.getText().toString().trim();
        final String description = etDescription.getText().toString().trim();

        // NAME VALIDATION – alphabets + single spaces only
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        // Improved regex: Only alphabets, optional single spaces between words
        if (!name.matches("^[A-Za-z]+( [A-Za-z]+)*$")) {
            etName.setError("Name must contain alphabets only");
            etName.requestFocus();
            return;
        }

        if (name.length() < 2) {
            etName.setError("Name is too short");
            etName.requestFocus();
            return;
        }

        // EMAIL VALIDATION
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        // SUBJECT
        if (TextUtils.isEmpty(subject)) {
            etSubject.setError("Subject is required");
            etSubject.requestFocus();
            return;
        }

        // DESCRIPTION
        if (TextUtils.isEmpty(description)) {
            etDescription.setError("Message is required");
            etDescription.requestFocus();
            return;
        }

        sendSupportRequest(name, email, subject, description);
    }

    private void sendSupportRequest(final String name, final String email,
                                    final String subject, final String description) {

        progress.setVisibility(View.VISIBLE);
        btnSend.setEnabled(false);

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest postRequest = new StringRequest(Request.Method.POST, SUPPORT_URL,
                response -> {
                    progress.setVisibility(View.GONE);
                    btnSend.setEnabled(true);

                    try {
                        JSONObject json = new JSONObject(response);
                        boolean status = json.optBoolean("status", false);
                        String message = json.optString("message", "No response");

                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();

                        if (status) {
                            etName.setText("");
                            etEmail.setText("");
                            etSubject.setText("");
                            etDescription.setText("");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(),
                                "Invalid server response",
                                Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    progress.setVisibility(View.GONE);
                    btnSend.setEnabled(true);

                    String msg = error.getMessage();
                    Toast.makeText(requireContext(),
                            "Network error: " + (msg != null ? msg : "Try again"),
                            Toast.LENGTH_LONG).show();
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("subject", subject);
                params.put("description", description);

                return params;
            }
        };

        queue.add(postRequest);
    }
}
