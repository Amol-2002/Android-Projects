package com.example.studentdairy;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
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

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class StudentProfileFragment extends Fragment {

    private ImageView imgProfile;
    private TextView tvName;
    private TextView  etClassSection, etSectionid;
    private TextView  tvParentFullName;
    private TextView tvRollNo, tvAdmissionDate, tvBirthDate, tvPermanentAddress,tvPresentAddress, tvGender, tvBloodGroup, tvMobileNumber, tvSessionId;
    private Button btnUpdate2;
    private SharedPreferences prefs;
    private static final String PROFILE_URL = "https://testing.trifrnd.net.in/ishwar/school/api/student_api.php";

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

//        // --- Initialize views ---
        imgProfile = view.findViewById(R.id.imgProfile);
        tvName = view.findViewById(R.id.tvName);
//        etStudentId = view.findViewById(R.id.tvStudentId);
        etClassSection = view.findViewById(R.id.tvClassSection);
        etSectionid = view.findViewById(R.id.tvSectionid);
//        btnUpdate2 = view.findViewById(R.id.btnUpdate2);

//        tvFirstName = view.findViewById(R.id.tvFirstName);
//        tvMiddleName = view.findViewById(R.id.tvMiddleName);
//        tvLastName = view.findViewById(R.id.tvLastName);
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

        // --- Load saved data on startup ---
        loadLocalData();

        // --- Fetch profile automatically using saved mobile ---
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

                            tvName.setText(data.optString("first_name") + " " + data.optString("middle_name") + " " + data.optString("last_name"));
                            etClassSection.setText(data.optString("class_id"));
                            etSectionid.setText(data.optString("section_id"));
//                            tvFirstName.setText(data.optString("first_name"));
//                            tvMiddleName.setText(data.optString("middle_name"));
//                            tvLastName.setText(data.optString("last_name"));
                            tvParentFullName.setText(data.optString("parent_fullname"));
                            tvRollNo.setText(data.optString("roll_no"));
                            tvAdmissionDate.setText(data.optString("admission_date"));
                            tvBirthDate.setText(data.optString("birth_date"));
                            tvPresentAddress.setText(data.optString("present_address"));
                            tvPermanentAddress.setText(data.optString("permanent_address"));
                            tvGender.setText(data.optString("gender"));
                            tvBloodGroup.setText(data.optString("blood_group"));
                            tvSessionId.setText(data.optString("session_id"));
                            tvMobileNumber.setText(data.optString("mobile"));

                            String photo = "https://trifrnd.co.in/school/" + data.optString("photo");
                            Glide.with(getContext()).load(photo).into(imgProfile);

                            saveFetchedData(data);

                            Toast.makeText(getContext(), "Welcome to Student Profile", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Data not found, fill manually", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "No Internet / Fetch failed", Toast.LENGTH_SHORT).show()) {
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


    private void saveFetchedData(JSONObject data) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("userid", data.optString("mobile", ""));
        editor.putString("first_name", data.optString("first_name", ""));
        editor.putString("middle_name", data.optString("middle_name", ""));
        editor.putString("last_name", data.optString("last_name", ""));
        editor.putString("student_id", data.optString("student_id", ""));
        editor.putString("class_id", data.optString("class_id", ""));
        editor.putString("section_id", data.optString("section_id", ""));
        editor.putString("roll_no", data.optString("roll_no", ""));
        editor.putString("admission_date", data.optString("admission_date", ""));
        editor.putString("birth_date", data.optString("birth_date", ""));
        editor.putString("parent_full_name", data.optString("parent_fullname", ""));
        editor.putString("present_address", data.optString("present_address", ""));
        editor.putString("permanent_address", data.optString("permanent_address", ""));
        editor.putString("gender", data.optString("gender", ""));
        editor.putString("blood_group", data.optString("blood_group", ""));
        editor.putString("session_id", data.optString("session_id", ""));
        editor.putString("photo", "https://trifrnd.co.in/school/" + data.optString("photo", ""));
        editor.apply();
    }

    private void loadLocalData() {
        tvName.setText(prefs.getString("name", ""));
//        etStudentId.setText(prefs.getString("student_id", ""));
        etClassSection.setText(prefs.getString("class_section", ""));
        etSectionid.setText(prefs.getString("section_id", ""));
//        tvFirstName.setText(prefs.getString("first_name", ""));
//        tvMiddleName.setText(prefs.getString("middle_name", ""));
//        tvLastName.setText(prefs.getString("last_name", ""));
        tvParentFullName.setText(prefs.getString("parent_full_name", ""));
        tvRollNo.setText(prefs.getString("roll_no", ""));
        tvAdmissionDate.setText(prefs.getString("admission_date", ""));
        tvBirthDate.setText(prefs.getString("birth_date", ""));
        tvPresentAddress.setText(prefs.getString("present_address", ""));
        tvPermanentAddress.setText(prefs.getString("permanent_address", ""));
        tvGender.setText(prefs.getString("gender", ""));
        tvBloodGroup.setText(prefs.getString("blood_group", ""));
        tvSessionId.setText(prefs.getString("session_id", ""));
        tvMobileNumber.setText(prefs.getString("mobile", ""));

        String photo = prefs.getString("photo", "");
        if (!photo.isEmpty()) Glide.with(getContext()).load(photo).into(imgProfile);
    }
}
