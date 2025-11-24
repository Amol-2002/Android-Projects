package com.example.studentdairy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import org.json.JSONException;

public class HomeFragment extends Fragment {

    private static final String STUDENT_API_URL = "https://testing.trifrnd.net.in/ishwar/school/api/student_api.php";

    private ProgressDialog progressDialog;
    private TextView etStudentName;

    // Volley related
    private RequestQueue requestQueue;
    private final Object volleyRequestTag = new Object(); // tag used to cancel requests

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Views
        etStudentName = view.findViewById(R.id.etStudentName);

        // init progress
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        // Read mobile from StudentProfile prefs (saved on login)
        SharedPreferences prefsStudent = requireContext()
                .getSharedPreferences("StudentProfile", Context.MODE_PRIVATE);

        String mobile = prefsStudent.getString("mobile", "");
        if (mobile == null || mobile.isEmpty()) {
            // fallback to "userid" if you stored under that key earlier
            mobile = prefsStudent.getString("userid", "");
        }

        if (mobile != null && !mobile.isEmpty()) {
            fetchStudentNameByMobile(mobile);
        } else {
            // Optional: show default name or leave the XML default
            etStudentName.setText("Student");
        }

        // --- existing click listeners (unchanged) ---
        ImageView itemAttendance = view.findViewById(R.id.item_attendance);
        ImageView itemClassSchedule = view.findViewById(R.id.item_class_schedule);
        ImageView itemOnlineClass = view.findViewById(R.id.onlineClasses_icon);
        ImageView itemFeesPaid = view.findViewById(R.id.feespaid_icon);
        ImageView itemRegisterSubject = view.findViewById(R.id.registersubjects_icon);
        ImageView itemCertificateIcon = view.findViewById(R.id.Certificate_icon);
        ImageView itemCalenderIcon = view.findViewById(R.id.Calender_icon);
        ImageView itemItleIcon = view.findViewById(R.id.itle_icon);
        ImageView itemRailwayPass = view.findViewById(R.id.railway_icon);

        itemAttendance.setOnClickListener(v -> {
            Fragment parentProfileFragment = new ParentProfileFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, parentProfileFragment)
                    .addToBackStack(null)
                    .commit();
        });

        itemClassSchedule.setOnClickListener(v -> {
            Fragment studentProfileFragment  = new StudentProfileFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, studentProfileFragment)
                    .addToBackStack(null)
                    .commit();
        });

        itemOnlineClass.setOnClickListener(v -> {
            Fragment holidayFragment = new HolidayFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, holidayFragment)
                    .addToBackStack(null)
                    .commit();
        });

        itemFeesPaid.setOnClickListener(v -> {
            Fragment timeTableFragment  = new TimetableFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, timeTableFragment)
                    .addToBackStack(null)
                    .commit();
        });

        itemRegisterSubject.setOnClickListener(v -> {
            Fragment homeWorkFragment  = new HomeWorkFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, homeWorkFragment)
                    .addToBackStack(null)
                    .commit();
        });

        itemCertificateIcon.setOnClickListener(v -> {
            Fragment certificateFragment = new CertificateFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, certificateFragment)
                    .addToBackStack(null)
                    .commit();
        });

        itemCalenderIcon.setOnClickListener(v -> {
            Fragment calenderFragment = new CalenderFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, calenderFragment)
                    .addToBackStack(null)
                    .commit();
        });

        itemItleIcon.setOnClickListener(v -> {
            Fragment updatePasswordFragment = new UpdatePasswordFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, updatePasswordFragment)
                    .addToBackStack(null)
                    .commit();
        });

        itemRailwayPass.setOnClickListener(v -> {
            Fragment attendanceFragment = new AttendanceFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, attendanceFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void fetchStudentNameByMobile(String mobile) {
        // If fragment not attached, bail out
        if (!isAdded() || getContext() == null) return;

        // Lazily create requestQueue using current context
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getContext());
        }

        // Show progress only if fragment attached and progressDialog exists
        if (isAdded() && progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }

        JSONObject body = new JSONObject();
        try {
            body.put("mobile", mobile);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                STUDENT_API_URL,
                body,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // If fragment is no longer attached, ignore the response
                        if (!isAdded()) {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                try { progressDialog.dismiss(); } catch (Exception ignored) {}
                            }
                            return;
                        }

                        // safely dismiss progress
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        try {
                            JSONObject dataObj = null;
                            if (response.has("data") && !response.isNull("data")) {
                                dataObj = response.optJSONObject("data");
                            } else {
                                dataObj = response;
                            }

                            if (dataObj != null) {
                                String first = dataObj.optString("first_name", "");
                                String middle = dataObj.optString("middle_name", "");
                                String last = dataObj.optString("last_name", "");
                                String fullName = joinNonEmpty(" ", first, middle, last);
                                if (fullName.isEmpty()) fullName = dataObj.optString("parent_fullname", "Student");
                                // Update UI safely (fragment is attached here)
                                etStudentName.setText(fullName);
                            } else {
                                etStudentName.setText("Student");
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Student data not found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Parse error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // If fragment is no longer attached, ignore the response
                        if (!isAdded()) {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                try { progressDialog.dismiss(); } catch (Exception ignored) {}
                            }
                            return;
                        }

                        // safely dismiss progress
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        etStudentName.setText("Student");
                        String msg = "Request failed";
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            msg = "Server returned: " + statusCode;
                            try {
                                String data = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                                msg += " - " + data;
                            } catch (Exception ignored) {}
                        } else if (error.getMessage() != null) {
                            msg = error.getMessage();
                        }
                        if (getContext() != null) {
                            Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

        // tag the request so we can cancel it later if fragment view is destroyed
        req.setTag(volleyRequestTag);
        requestQueue.add(req);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Cancel any pending requests tagged with volleyRequestTag
        if (requestQueue != null) {
            requestQueue.cancelAll(volleyRequestTag);
        }

        // Dismiss and nullify progressDialog to avoid window leaks
        if (progressDialog != null) {
            try {
                if (progressDialog.isShowing()) progressDialog.dismiss();
            } catch (Exception ignored) {}
            progressDialog = null;
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
}
