package com.example.studentdairy;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class UpdatePasswordFragment extends Fragment {

    TextInputEditText psUserId, psPassword;
    Button btnUpdate;
    SharedPreferences sharedPreferences;

    private static final String UPDATE_URL = "https://testing.trifrnd.net.in/ishwar/school/api/pwd_update_api.php";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_password, container, false);

        // Find the ImageView
        ImageView attarrow = view.findViewById(R.id.attarrow);

        // Click → Intent to MainActivity
        attarrow.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

        psUserId = view.findViewById(R.id.psUserId);
        psPassword = view.findViewById(R.id.psPassword);
        btnUpdate = view.findViewById(R.id.btnLogin);

        // SharedPreferences same as Login & StudentFragment
        sharedPreferences = requireActivity().getSharedPreferences("StudentProfile", android.content.Context.MODE_PRIVATE);

        // Pre-fill userid from SharedPreferences
        String savedUserId = sharedPreferences.getString("userid", "");
        psUserId.setText(savedUserId);

        btnUpdate.setOnClickListener(v -> {
            String userid = psUserId.getText().toString().trim();
            String password = psPassword.getText().toString().trim();

            if (validateInputs(userid, password)) {
                updatePassword(userid, password);
            }
        });

        return view;
    }

    private boolean validateInputs(String userid, String password) {
        if (TextUtils.isEmpty(userid)) {
            psUserId.setError("User ID required");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            psPassword.setError("Password required");
            return false;
        }
        return true;
    }

    private void updatePassword(String userid, String password) {
        btnUpdate.setEnabled(false);

        StringRequest request = new StringRequest(Request.Method.POST, UPDATE_URL,
                response -> {
                    btnUpdate.setEnabled(true);
                    // ✅ Show full server response to debug why update failed
                    Toast.makeText(getContext(), "Password Updated Successfully", Toast.LENGTH_SHORT).show();
//                    Toast.makeText(getContext(), "Password Updated Successfully" + response, Toast.LENGTH_LONG).show();
                },
                error -> {
                    btnUpdate.setEnabled(true);
                    Toast.makeText(getContext(), "Network Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userid", userid);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(request);
    }
}
