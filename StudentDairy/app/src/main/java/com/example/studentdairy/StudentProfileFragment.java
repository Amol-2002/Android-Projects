package com.example.studentdairy;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class StudentProfileFragment extends Fragment {

    private ImageView imgProfile;
    private TextView tvName;
    private TextView etClassSection, etSectionid;
    private TextView tvParentFullName;
    private TextView tvRollNo, tvAdmissionDate, tvBirthDate, tvPermanentAddress, tvPresentAddress, tvGender, tvBloodGroup, tvMobileNumber, tvSessionId;
    private Button btnUpdate2;
    private SharedPreferences prefs;
    private static final String PROFILE_URL = "https://testing.trifrnd.net.in/ishwar/school/api/student_api.php";

    /**
     * Format raw date (expected "yyyy-MM-dd") into "d MMM yyyy" (e.g. "1 Jun 2023").
     */
    private String formatPrettyDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) return "-";

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(rawDate);

            SimpleDateFormat outputFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
            return outputFormat.format(date); // Example: 1 Jun 2023

        } catch (Exception e) {
            e.printStackTrace();
            return rawDate; // If parsing fails, return original
        }
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_student_profile, container, false);

        // Find the ImageView
        ImageView attarrow = view.findViewById(R.id.attarrow);

        // Click → Intent to MainActivity
        attarrow.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

        // Initialize views
        imgProfile = view.findViewById(R.id.imgProfile);
        tvName = view.findViewById(R.id.tvName);
        etClassSection = view.findViewById(R.id.tvClassSection);
        etSectionid = view.findViewById(R.id.tvSectionid);
        tvParentFullName = view.findViewById(R.id.tvParentFullName);
        tvRollNo = view.findViewById(R.id.tvRollNo);
        tvAdmissionDate = view.findViewById(R.id.tvAdmissionDate);
        tvBirthDate = view.findViewById(R.id.tvBirthDate);
        tvPresentAddress = view.findViewById(R.id.tvPresentAddress);
        tvPermanentAddress = view.findViewById(R.id.tvPermanentAddress);
        tvGender = view.findViewById(R.id.tvGender);
        tvBloodGroup = view.findViewById(R.id.tvBloodGroup);
        tvMobileNumber = view.findViewById(R.id.tvMobileNumber);
        tvSessionId = view.findViewById(R.id.tvsessionid);

        prefs = requireActivity().getSharedPreferences("StudentProfile", android.content.Context.MODE_PRIVATE);

        // Load saved data on startup (formatted)
        loadLocalData();

        // Fetch profile automatically using saved mobile
        String mobile = prefs.getString("userid", "");
        if (!mobile.isEmpty()) {
            fetchProfile(mobile);
        }

        return view;
    }

    // -------------------- Fetch Profile --------------------
    private void fetchProfile(String mobile) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, PROFILE_URL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.has("data")) {
                            JSONObject data = json.getJSONObject("data");

                            tvName.setText(joinNames(data.optString("first_name"), data.optString("middle_name"), data.optString("last_name")));
                            etClassSection.setText(data.optString("class_id"));
                            etSectionid.setText(data.optString("section_id"));
                            tvParentFullName.setText(data.optString("parent_fullname"));
                            tvRollNo.setText(data.optString("roll_no"));

                            // Format dates before setting
                            tvAdmissionDate.setText(formatPrettyDate(data.optString("admission_date")));
                            tvBirthDate.setText(formatPrettyDate(data.optString("birth_date")));

                            tvPresentAddress.setText(data.optString("present_address"));
                            tvPermanentAddress.setText(data.optString("permanent_address"));
                            tvGender.setText(capitalizeFirst(data.optString("gender")));
                            tvBloodGroup.setText(data.optString("blood_group"));
                            tvSessionId.setText(data.optString("session_id"));
                            tvMobileNumber.setText(data.optString("mobile"));

                            String photo = data.optString("photo");
                            if (photo != null && !photo.isEmpty()) {
                                Glide.with(getContext()).load(photo).into(imgProfile);
                            }

                            // Save fetched data synchronously for critical keys
                            saveFetchedData(data);

                        } else {
                            Toast.makeText(getContext(), "Data not found, fill manually", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(getContext(), "No Internet / Fetch failed", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("mobile", mobile);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(stringRequest);
    }

    /**
     * Save fetched profile to SharedPreferences.
     * Critical keys (student_id, class_id, section_id, userid/mobile) are written using commit()
     * so other fragments using them immediately will see them.
     */
    private void saveFetchedData(JSONObject data) {
        try {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("userid", data.optString("mobile", ""));
            editor.putString("first_name", data.optString("first_name", ""));
            editor.putString("middle_name", data.optString("middle_name", ""));
            editor.putString("last_name", data.optString("last_name", ""));
            editor.putString("student_id", data.optString("student_id", ""));
            editor.putString("class_id", data.optString("class_id", ""));
            editor.putString("section_id", data.optString("section_id", ""));
            editor.putString("roll_no", data.optString("roll_no", ""));
            editor.putString("admission_date", data.optString("admission_date", "")); // store raw yyyy-MM-dd
            editor.putString("birth_date", data.optString("birth_date", ""));         // store raw yyyy-MM-dd
            editor.putString("parent_full_name", data.optString("parent_fullname", ""));
            editor.putString("present_address", data.optString("present_address", ""));
            editor.putString("permanent_address", data.optString("permanent_address", ""));
            editor.putString("gender", data.optString("gender", ""));
            editor.putString("blood_group", data.optString("blood_group", ""));
            editor.putString("session_id", data.optString("session_id", ""));
            editor.putString("photo", data.optString("photo", ""));

            // commit synchronously so AttendanceFragment / other readers see updated values immediately
            boolean ok = editor.commit();

            // helpful debug log
            Log.d("StudentProfile", "Saved profile commit=" + ok +
                    " student_id=" + data.optString("student_id") +
                    " class_id=" + data.optString("class_id") +
                    " section_id=" + data.optString("section_id"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadLocalData() {
        tvName.setText(prefs.getString("name", ""));
        etClassSection.setText(prefs.getString("class_section", ""));
        etSectionid.setText(prefs.getString("section_id", ""));
        tvParentFullName.setText(prefs.getString("parent_full_name", ""));
        tvRollNo.setText(prefs.getString("roll_no", ""));

        // Format saved dates (stored as yyyy-MM-dd)
        String savedAdmission = prefs.getString("admission_date", "");
        String savedBirth = prefs.getString("birth_date", "");
        tvAdmissionDate.setText(formatPrettyDate(savedAdmission));
        tvBirthDate.setText(formatPrettyDate(savedBirth));

        tvPresentAddress.setText(prefs.getString("present_address", ""));
        tvPermanentAddress.setText(prefs.getString("permanent_address", ""));
        tvGender.setText(prefs.getString("gender", ""));
        tvBloodGroup.setText(prefs.getString("blood_group", ""));
        tvSessionId.setText(prefs.getString("session_id", ""));
        tvMobileNumber.setText(prefs.getString("mobile", ""));

        String photo = prefs.getString("photo", "");
        if (photo != null && !photo.isEmpty()) Glide.with(getContext()).load(photo).into(imgProfile);
    }

    private String joinNames(String f, String m, String l) {
        StringBuilder sb = new StringBuilder();
        if (f != null && !f.trim().isEmpty()) sb.append(f.trim());
        if (m != null && !m.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(m.trim());
        }
        if (l != null && !l.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(l.trim());
        }
        return sb.toString();
    }

    private String capitalizeFirst(String s) {
        if (s == null || s.trim().isEmpty()) return s;
        s = s.trim();
        return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
