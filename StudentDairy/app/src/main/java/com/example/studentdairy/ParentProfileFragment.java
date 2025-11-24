package com.example.studentdairy;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ParentProfileFragment extends Fragment {

    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_PERMISSION = 2;

    private ImageView imgProfile;
    private ImageButton btnChangeProfile;
    private TextView etFullName, etMiddleName, etLastName, etOccupation, etCompanyName,
            etMobile, etAlternateMobile, etPermanentAddress, etPresentAddress, etCity, etState;

    private TextView tvParentName;
    private SharedPreferences prefs;

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



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parent_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the ImageView
        ImageView attarrow = view.findViewById(R.id.attarrow);

        // Click → Intent to MainActivity
        attarrow.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });


        etFullName = view.findViewById(R.id.etFullName);
        etMiddleName = view.findViewById(R.id.etMiddleName);
        etLastName = view.findViewById(R.id.etLastName);
        etOccupation = view.findViewById(R.id.etOccupation);
        etCompanyName = view.findViewById(R.id.etCompanyName);
        etMobile = view.findViewById(R.id.etMobile);
        etAlternateMobile = view.findViewById(R.id.etAlternateMobile);
        etPermanentAddress = view.findViewById(R.id.etPermanentAddress);
        etPresentAddress = view.findViewById(R.id.etPresentAddress);
        etCity = view.findViewById(R.id.etCity);
        etState = view.findViewById(R.id.etState);

        tvParentName = view.findViewById(R.id.etParentName);

        prefs = requireContext().getSharedPreferences("ParentProfile", Activity.MODE_PRIVATE);

        // Load locally saved data
        loadLocalData();

        // Set full name on profile screen
        setParentFullName(tvParentName);

        // Fetch API
        String mobile = prefs.getString("mobile", "");
        if (!mobile.isEmpty()) {
            fetchParentProfile(mobile, tvParentName);
        } else {
            Toast.makeText(getContext(), "Parent mobile not found. Please login again.", Toast.LENGTH_SHORT).show();
        }

    }

    private void loadLocalData() {
        etFullName.setText(prefs.getString("first_name", ""));
        etMiddleName.setText(prefs.getString("middle_name", ""));
        etLastName.setText(prefs.getString("last_name", ""));
        etOccupation.setText(prefs.getString("occupation", ""));
        etCompanyName.setText(prefs.getString("company_name", ""));
        etMobile.setText(prefs.getString("mobile", ""));
        etAlternateMobile.setText(prefs.getString("alternate_mobile", ""));
        etPermanentAddress.setText(prefs.getString("permanent_address", ""));
        etPresentAddress.setText(prefs.getString("present_address", ""));
        etCity.setText(prefs.getString("city", ""));
        etState.setText(prefs.getString("state", ""));
    }

    private void setParentFullName(TextView tv) {
        String first = prefs.getString("first_name", "");
        String middle = prefs.getString("middle_name", "");
        String last = prefs.getString("last_name", "");

        String fullName = first;
        if (!middle.isEmpty()) fullName += " " + middle;
        if (!last.isEmpty()) fullName += " " + last;

        tv.setText(fullName);
    }

    private void fetchParentProfile(String mobile, TextView tvParentName) {
        String url = "https://testing.trifrnd.net.in/ishwar/school/api/parent_api.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.has("data")) {
                            JSONObject data = obj.getJSONObject("data");

                            // Update views
                            etFullName.setText(data.optString("first_name"));
                            etMiddleName.setText(data.optString("middle_name"));
                            etLastName.setText(data.optString("last_name"));
                            etOccupation.setText(data.optString("occupation"));
                            etCompanyName.setText(data.optString("company_name"));
                            etMobile.setText(data.optString("mobile"));
                            etAlternateMobile.setText(data.optString("alternate_mobile"));
                            etPermanentAddress.setText(data.optString("permanent_address"));
                            etPresentAddress.setText(data.optString("present_address"));
                            etCity.setText(data.optString("city"));
                            etState.setText(data.optString("state"));

                            // Save each field locally
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("first_name", data.optString("first_name"));
                            editor.putString("middle_name", data.optString("middle_name"));
                            editor.putString("last_name", data.optString("last_name"));
                            editor.putString("occupation", data.optString("occupation"));
                            editor.putString("company_name", data.optString("company_name"));
                            editor.putString("mobile", data.optString("mobile"));
                            editor.putString("alternate_mobile", data.optString("alternate_mobile"));
                            editor.putString("permanent_address", data.optString("permanent_address"));
                            editor.putString("present_address", data.optString("present_address"));
                            editor.putString("city", data.optString("city"));
                            editor.putString("state", data.optString("state"));
                            editor.apply();

                            // Create full name & save for HomeFragment
                            String fullName = data.optString("first_name") + " "
                                    + data.optString("middle_name") + " "
                                    + data.optString("last_name");

                            SharedPreferences.Editor editor2 = prefs.edit();
                            editor2.putString("parent_fullname", fullName.trim());
                            editor2.apply();

                            // Update full name on profile screen
                            setParentFullName(tvParentName);



                        } else {
                            Toast.makeText(getContext(), "Profile not found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error parsing profile", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Failed to fetch profile", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<>();
                params.put("mobile", mobile);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(request);
    }


    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) imgProfile.setImageURI(uri);
        }
    }
}
