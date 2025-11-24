package com.example.studentdairy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class IdcardFragment extends Fragment {

    private ImageView profileArrow;
    private ImageView imgProfile;
    private TextView tvParentFullName;
    private TextView tvSchoolName, tvTagline, tvName, tvBirthDate, tvGender, tvRollNo, tvClassSection, tvMobileNumber, tvPermanentAddress, Schooladdress;
    private ProgressDialog progressDialog;

    // Replace with your real endpoint
    private static final String STUDENT_API_URL = "https://testing.trifrnd.net.in/ishwar/school/api/student_api.php";
    private static final String TAG = "IdcardFragment";

    public IdcardFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_idcard, container, false);
        initViews(view);

        // Read mobile from SharedPreferences saved at login
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("StudentProfile", Context.MODE_PRIVATE);

        // Use key that you saved on login. In earlier message you used "mobile" or "userid".
        String mobileFromPrefs = prefs.getString("mobile", "");
        if (mobileFromPrefs == null || mobileFromPrefs.isEmpty()) {
            // Try alternate key 'userid' (common in your snippets)
            mobileFromPrefs = prefs.getString("userid", "");
        }

        if (mobileFromPrefs == null || mobileFromPrefs.isEmpty()) {
            Toast.makeText(getContext(), "Mobile not found in StudentProfile prefs", Toast.LENGTH_LONG).show();
        } else {
            fetchStudentByMobile(mobileFromPrefs);
        }
// Back arrow -> MainActivity
        ImageView attarrow = view.findViewById(R.id.profilearrow);
        attarrow.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void initViews(View v) {
        profileArrow = v.findViewById(R.id.profilearrow);
        imgProfile = v.findViewById(R.id.imgProfile);
        tvSchoolName = v.findViewById(R.id.tvSchoolName);
        tvTagline = v.findViewById(R.id.tvTagline);
        tvName = v.findViewById(R.id.tvName);
        tvParentFullName = v.findViewById(R.id.tvParentFullName);
        tvGender = v.findViewById(R.id.tvGender);
        tvRollNo = v.findViewById(R.id.tvRollNo);
        tvClassSection = v.findViewById(R.id.tvClassSection);
        tvMobileNumber = v.findViewById(R.id.tvMobileNumber);
        tvPermanentAddress = v.findViewById(R.id.tvPermanentAddress);
        Schooladdress = v.findViewById(R.id.Schooladdress);

        // Initialize DOB TextView (this was missing previously)
        tvBirthDate = v.findViewById(R.id.tvBirthDate);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
    }

    private void fetchStudentByMobile(String mobile) {
        if (getContext() == null) return;

        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        // Build JSON body: {"mobile":"7887887788"}
        JSONObject body = new JSONObject();
        try {
            body.put("mobile", mobile);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                STUDENT_API_URL,
                body,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject dataObj = extractDataObject(response);
                            if (dataObj != null) {
                                populateViewsFromJson(dataObj);
                            } else {
                                Toast.makeText(getContext(), "No student data found", Toast.LENGTH_LONG).show();
                                Log.w(TAG, "Response did not contain usable 'data' object/array: " + response.toString());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        String msg = "Request failed";
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            msg = "Server returned status: " + statusCode;
                            try {
                                String data = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                                msg += " - " + data;
                            } catch (UnsupportedEncodingException ex) {
                                // ignore
                            }
                        } else if (error.getMessage() != null) {
                            msg = error.getMessage();
                        }
                        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Volley error: " + msg, error);
                    }
                }
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    /**
     * Extracts the primary student JSONObject from server response.
     * Handles: response.data (object), response.data (array) or response itself being the object.
     */
    private JSONObject extractDataObject(JSONObject response) {
        try {
            if (response.has("data") && !response.isNull("data")) {
                Object d = response.get("data");
                if (d instanceof JSONObject) {
                    return (JSONObject) d;
                } else if (d instanceof JSONArray) {
                    JSONArray arr = (JSONArray) d;
                    if (arr.length() > 0) return arr.getJSONObject(0);
                    return null;
                } else {
                    // unknown type, try to parse as JSONObject string
                    try {
                        return new JSONObject(d.toString());
                    } catch (Exception e) {
                        return null;
                    }
                }
            } else {
                // Some APIs directly return the object at root
                return response;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void populateViewsFromJson(JSONObject data) {
        try {
            // Log the JSON so you can inspect the exact keys returned by the server
            Log.d(TAG, "student json: " + data.toString());

            // Student name (try multiple possibilities)
            String firstName = data.optString("first_name", "");
            String middleName = data.optString("middle_name", "");
            String lastName = data.optString("last_name", "");
            String fullName = joinNonEmpty(" ", firstName, middleName, lastName);
            if (fullName.isEmpty()) fullName = data.optString("student_name", data.optString("name", "Student Name"));

            // Parent name: try a set of likely keys
            String parentName = data.optString("parent_fullname", "");
            if (parentName.isEmpty()) parentName = data.optString("parent_name", "");
            if (parentName.isEmpty()) parentName = data.optString("father_name", "");
            if (parentName.isEmpty()) parentName = data.optString("mother_name", "");
            if (parentName.isEmpty()) parentName = data.optString("guardian_name", "");
            if (parentName.isEmpty()) parentName = data.optString("parent", "-");
            if (parentName.isEmpty()) parentName = "-";

            // Date of birth: try multiple common keys and optionally reformat
            String dobRaw = data.optString("dob", "");
            if (dobRaw.isEmpty()) dobRaw = data.optString("birth_date", "");
            if (dobRaw.isEmpty()) dobRaw = data.optString("date_of_birth", "");
            if (dobRaw.isEmpty()) dobRaw = data.optString("dob_date", "");
            if (dobRaw.isEmpty()) dobRaw = data.optString("birthdate", "");

            String dob = (dobRaw == null || dobRaw.trim().isEmpty()) ? "-" : formatDateIfPossible(dobRaw.trim());

            String gender = data.optString("gender", "-");
            String rollNo = data.has("roll_no") ? String.valueOf(data.optInt("roll_no", 0)) : data.optString("roll_no", "-");
            // fallback to 'rollno' or 'roll_number'
            if ((rollNo == null || rollNo.trim().isEmpty() || "0".equals(rollNo))) {
                rollNo = data.optString("rollno", data.optString("roll_number", "-"));
            }

            // BUILD CLASS LABEL: "Class 7 - A" (if class id/name exists)
            String classIdOrNameRaw = data.optString("class_id", data.optString("class_name", ""));
            String section = data.optString("section_id", data.optString("section", ""));
            String classIdOrName = (classIdOrNameRaw == null || classIdOrNameRaw.isEmpty()) ? "" : (classIdOrNameRaw);
            String className = buildClassSection(classIdOrName, section);

            String mobile = data.optString("mobile", data.optString("phone", "-"));
            String permanentAddress = data.optString("permanent_address", data.optString("present_address", data.optString("address", "-")));
            String photoUrl = data.optString("photo", data.optString("profile_photo", ""));

            // Populate views
            tvName.setText(fullName);
            tvParentFullName.setText(parentName);
            tvBirthDate.setText(dob);
            tvGender.setText(capitalizeFirst(gender));
            tvRollNo.setText((rollNo == null || rollNo.isEmpty()) ? "-" : rollNo);
            tvClassSection.setText((className == null || className.isEmpty()) ? "-" : className);
            tvMobileNumber.setText((mobile == null || mobile.isEmpty()) ? "-" : mobile);
            tvPermanentAddress.setText((permanentAddress == null || permanentAddress.isEmpty()) ? "-" : permanentAddress);

            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(imgProfile);
            } else {
                imgProfile.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error populating UI: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String joinNonEmpty(String sep, String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.trim().isEmpty()) {
                if (sb.length() > 0) sb.append(sep);
                sb.append(p.trim());
            }
        }
        return sb.toString();
    }

    private String buildClassSection(String classIdOrName, String section) {
        if ((classIdOrName == null || classIdOrName.isEmpty()) && (section == null || section.isEmpty()))
            return "-";
        if (section == null || section.isEmpty()) return classIdOrName;
        return classIdOrName + " - " + section;
    }

    private String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) return "-";
        s = s.trim();
        return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
    }

    /**
     * Tries to parse common server date formats and converts to "dd MMM yyyy" (e.g. 05 Jan 2010).
     * If parsing fails, returns the raw input.
     */
    private String formatDateIfPossible(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "-";

        // Remove time if present (e.g., "2020-01-01 00:00:00" -> "2020-01-01")
        String cleaned = raw.split("T")[0].split(" ")[0];

        String[] patterns = new String[] {
                "yyyy-MM-dd",
                "dd-MM-yyyy",
                "dd/MM/yyyy",
                "MM/dd/yyyy",
                "yyyy/MM/dd",
                "yyyyMMdd",
                "dd MMM yyyy"
        };

        for (String pattern : patterns) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(pattern, Locale.getDefault());
                Date date = parser.parse(cleaned);
                if (date != null) {
                    SimpleDateFormat out = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    return out.format(date);
                }
            } catch (ParseException ignored) {
            }
        }

        // If none matched, return raw
        return raw;
    }
}
