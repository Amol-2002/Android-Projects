package com.example.classteacherportal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TeacherFragment extends Fragment {

    private static final String PROFILE_URL = "https://testing.trifrnd.net.in/ishwar/school/api/teacher_profile_api.php";
    private RequestQueue requestQueue;

    private TextView tvInitials, etTeacherName, tvDesignation, tvDepartment, tvPhone, tvGender, tvJoiningDate, tvPresentAddress, tvPermanentAddress ,tvUserId, tvClassId, tvSectionId, tvState;
    private String staffId = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher, container, false);

        // Find the ImageView
        ImageView attarrow = view.findViewById(R.id.attarrow);

        // Click → Intent to MainActivity
        attarrow.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

        tvInitials = view.findViewById(R.id.tvInitials);
        etTeacherName = view.findViewById(R.id.etTeacherName);
        tvDesignation = view.findViewById(R.id.tvDesignation);
        tvDepartment = view.findViewById(R.id.tvDepartment);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvGender = view.findViewById(R.id.tvGender);
        tvJoiningDate = view.findViewById(R.id.tvJoiningDate);
        tvPresentAddress = view.findViewById(R.id.tvPresentAddress);
        tvPermanentAddress = view.findViewById(R.id.tvPermanentAddress);
        tvClassId = view.findViewById(R.id.tvClassId);
        tvSectionId = view.findViewById(R.id.tvSectionId);
        tvUserId = view.findViewById(R.id.tvUserId);
        tvState = view.findViewById(R.id.tvState);


        requestQueue = Volley.newRequestQueue(requireContext());

        SharedPreferences sp = requireActivity().getSharedPreferences("StudentProfile", requireActivity().MODE_PRIVATE);
        staffId = sp.getString("userid", "");

        if (TextUtils.isEmpty(staffId)) {
            Toast.makeText(requireContext(), "Please login", Toast.LENGTH_SHORT).show();
        } else {
            loadProfile(staffId);
        }

        return view;
    }

    private void loadProfile(String staff_id) {
        StringRequest req = new StringRequest(Request.Method.POST, PROFILE_URL,
                response -> {
                    try {
                        // expected single-array like [{...}]
                        JSONArray arr = new JSONArray(response);
                        if (arr.length() > 0) {
                            JSONObject obj = arr.getJSONObject(0);

                            String name = obj.optString("teacher_name", "");
                            String department = obj.optString("departments", "");
                            String designation = obj.optString("designation", "");
                            String phone = obj.optString("phone", "");
                            String gender = obj.optString("gender", "");
                            String joining = obj.optString("joining_date", "");
                            String present = obj.optString("present_address", "");
                            String permanent = obj.optString("permanent_address", "");

                            String userId = obj.optString("user_id", "");
                            String state = obj.optString("state", "");
                            String classId = obj.optString("class_id", "");
                            String sectionId = obj.optString("section_id", "");


                            etTeacherName.setText(name);
                            tvState.setText(state);
                            tvUserId.setText(userId);
                            tvClassId.setText(classId);
                            tvSectionId.setText(sectionId);
                            tvDesignation.setText(designation);
                            tvDepartment.setText(department);
                            tvPhone.setText(phone);
                            tvGender.setText(gender);
                            tvJoiningDate.setText(joining);
                            tvPresentAddress.setText(present);
                            tvPermanentAddress.setText(permanent);

                            // initials
                            String initials = getInitials(name);
                            tvInitials.setText(initials);
                        } else {
                            Toast.makeText(requireContext(), "Profile not found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> p = new HashMap<>();
                p.put("staff_id", staff_id);
                return p;
            }
        };

        req.setTag("TeacherProfileReq");
        requestQueue.add(req);
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, parts.length); i++) {
            if (!parts[i].isEmpty()) sb.append(parts[i].charAt(0));
        }
        return sb.toString().toUpperCase();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (requestQueue != null) {
            requestQueue.cancelAll("TeacherProfileReq");
            requestQueue = null;
        }
    }
}
