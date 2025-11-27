package com.example.classteacherportal;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final String STUDENT_API_URL = "https://testing.trifrnd.net.in/ishwar/school/api/student_api.php";
    private static final String TEACHER_PROFILE_URL = "https://testing.trifrnd.net.in/ishwar/school/api/teacher_profile_api.php";
    private static final String TEACHER_ATTEND_URL = "https://testing.trifrnd.net.in/ishwar/school/api/teacher_attend_api.php";

    private ProgressDialog progressDialog;
    private TextView etTeacherName;

    // Volley related
    private RequestQueue requestQueue;
    private final Object volleyRequestTag = new Object(); // tag used to cancel requests

    // staff id used across methods
    private String staffId = "";

    // teacher name from API, used in dialog
    private String teacherNameFromApi = "";

    public HomeFragment() { /* Required empty constructor */ }

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
        etTeacherName = view.findViewById(R.id.etTeacherName);

        // init progress
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        // init request queue
        requestQueue = Volley.newRequestQueue(requireContext());

        // Read staff id from SharedPreferences with fallback: "userid" then "mobile"
        SharedPreferences sp = requireActivity().getSharedPreferences("StudentProfile", Context.MODE_PRIVATE);
        staffId = sp.getString("userid", "");
        if (staffId == null || staffId.trim().isEmpty()) {
            staffId = sp.getString("mobile", "");
        }

        Log.d("HomeFragment", "Loaded staffId from prefs: [" + staffId + "]");

        if (staffId == null || staffId.trim().isEmpty()) {
            etTeacherName.setText("Teacher");
            Toast.makeText(requireContext(), "Please login", Toast.LENGTH_SHORT).show();
        } else {
            // Fetch teacher name; attendance popup will be triggered after API response
            fetchTeacherNameByStaffId(staffId);
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
            Fragment parentProfileFragment = new TeacherFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, parentProfileFragment)
                    .addToBackStack(null)
                    .commit();
        });

        itemClassSchedule.setOnClickListener(v -> {
            Fragment studentProfileFragment  = new StudentFragment();
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
            Fragment homeWorkFragment  = new HomeworkFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, homeWorkFragment)
                    .addToBackStack(null)
                    .commit();
        });

        itemCertificateIcon.setOnClickListener(v -> {
            Fragment certificateFragment = new AddhomeworkFragment();
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
            Fragment updatePasswordFragment = new ViewAttendanceFragmentGrid() ;
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, updatePasswordFragment)
                    .addToBackStack(null)
                    .commit();
        });

        itemRailwayPass.setOnClickListener(v -> {
            Fragment attendanceFragment = new StudentAttendanceFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, attendanceFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    // 🔹 1) Check local pref and show dialog if attendance not given for today
    private void checkAndShowAttendanceDialog() {
        if (staffId == null || staffId.trim().isEmpty()) return;

        // Today's date in yyyy-MM-dd (default today date always)
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Local prefs to remember attendance: key = staffId + "_" + date
        SharedPreferences attPrefs = requireActivity().getSharedPreferences("TeacherAttendance", Context.MODE_PRIVATE);
        String key = staffId + "_" + today;

        boolean alreadyMarked = attPrefs.getBoolean(key, false);

        if (!alreadyMarked) {
            showAttendanceDialog(staffId, today, key);
        }
    }

    // 🔹 2) Build and show the popup dialog (with teacher name from API)
    private void showAttendanceDialog(String staffId, String todayDate, String prefKey) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_teacher_attendance, null);

        TextView tvDate = dialogView.findViewById(R.id.tvAttendanceDate);
        TextView tvTeacherNameDialog = dialogView.findViewById(R.id.tvTeacherName);
        RadioButton rbPresent = dialogView.findViewById(R.id.rbPresent);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmitAttendance);
        ImageView ivClose = dialogView.findViewById(R.id.ivCloseDialog);

        tvDate.setText(todayDate); // just the date, header text is separate

        String nameToShow = (teacherNameFromApi != null && !teacherNameFromApi.trim().isEmpty())
                ? teacherNameFromApi.trim()
                : "Teacher";
        tvTeacherNameDialog.setText(nameToShow);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)   // user must choose submit or close
                .create();

        // Close (cut) button
        ivClose.setOnClickListener(v -> {
            // just close, do NOT save pref -> dialog can show again next time
            dialog.dismiss();
        });

        btnSubmit.setOnClickListener(v -> {
            // Only Present is available
            String status = "Present";   // change to "P" if your API expects that

            sendAttendanceToServer(staffId, todayDate, status, prefKey, dialog);
        });

        dialog.show();
    }

    // 🔹 3) POST attendance to your API: staff_id, a_date, status
    private void sendAttendanceToServer(String staffId,
                                        String aDate,
                                        String status,
                                        String prefKey,
                                        AlertDialog dialog) {

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(requireContext());
        }

        StringRequest req = new StringRequest(Request.Method.POST, TEACHER_ATTEND_URL,
                response -> {
                    // Save flag so popup won't show again for this staff+date
                    SharedPreferences attPrefs = requireActivity().getSharedPreferences("TeacherAttendance", Context.MODE_PRIVATE);
                    attPrefs.edit().putBoolean(prefKey, true).apply();

                    Toast.makeText(requireContext(), "Attendance submitted", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(requireContext(), "Failed to submit attendance", Toast.LENGTH_SHORT).show();
                    // Do NOT save pref, so popup will show again on next login
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("staff_id", staffId);
                params.put("a_date", aDate);
                params.put("status", status);
                return params;
            }
        };

        req.setTag(volleyRequestTag);
        requestQueue.add(req);
    }

    /**
     * Fetch teacher profile by staff_id and set teacher_name to etTeacherName.
     * Also stores teacherNameFromApi and then triggers attendance popup.
     * Expected response: JSON array like [{ "teacher_name":"Atul Sing Rajputn", ... }]
     */
    private void fetchTeacherNameByStaffId(String staffId) {
        if (!isAdded() || getContext() == null) {
            Log.d("HomeFragment", "Fragment not added or context null - abort fetch");
            return;
        }

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(requireContext());
        }

        try {
            if (progressDialog != null && !progressDialog.isShowing()) {
                progressDialog.show();
            }
        } catch (Exception e) {
            Log.w("HomeFragment", "progressDialog show failed: " + e.getMessage());
        }

        Log.d("HomeFragment", "Starting fetchTeacherNameByStaffId for: " + staffId);

        StringRequest req = new StringRequest(Request.Method.POST, TEACHER_PROFILE_URL,
                response -> {
                    Log.d("HomeFragment", "onResponse: " + (response == null ? "null" : response.substring(0, Math.min(200, response.length())) ));
                    // always try to dismiss progress dialog
                    try {
                        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
                    } catch (Exception ex) { Log.w("HomeFragment","dismiss failed: "+ex.getMessage()); }

                    try {
                        if (response == null || response.trim().isEmpty()) {
                            teacherNameFromApi = "Teacher";
                            etTeacherName.setText("Teacher");
                            Toast.makeText(getContext(), "Empty server response", Toast.LENGTH_SHORT).show();
                            // show attendance popup even if name not loaded
                            checkAndShowAttendanceDialog();
                            return;
                        }

                        // Parse as JSON array (adjust if your API returns a different shape)
                        JSONArray arr = new JSONArray(response);
                        if (arr.length() > 0) {
                            JSONObject obj = arr.getJSONObject(0);
                            String teacherName = obj.optString("teacher_name", "").trim();
                            if (teacherName.isEmpty()) {
                                teacherNameFromApi = "Teacher";
                                etTeacherName.setText("Teacher");
                            } else {
                                teacherNameFromApi = teacherName;
                                etTeacherName.setText(teacherName);
                            }
                        } else {
                            teacherNameFromApi = "Teacher";
                            etTeacherName.setText("Teacher");
                            Toast.makeText(getContext(), "Profile not found", Toast.LENGTH_SHORT).show();
                        }

                        // Now that teacherNameFromApi is set, show attendance dialog (if needed)
                        checkAndShowAttendanceDialog();

                    } catch (Exception e) {
                        Log.e("HomeFragment", "Parse error: " + e.getMessage(), e);
                        teacherNameFromApi = "Teacher";
                        etTeacherName.setText("Teacher");
                        Toast.makeText(getContext(), "Profile parse error", Toast.LENGTH_SHORT).show();
                        checkAndShowAttendanceDialog();
                    }
                },
                error -> {
                    Log.e("HomeFragment", "onError: " + (error == null ? "null" : error.toString()));
                    try {
                        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
                    } catch (Exception ex) { Log.w("HomeFragment","dismiss failed: "+ex.getMessage()); }

                    String msg = "Request failed";
                    if (error != null && error.networkResponse != null) {
                        msg = "Server returned code: " + error.networkResponse.statusCode;
                    } else if (error != null && error.getMessage() != null) {
                        msg = error.getMessage();
                    }
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();

                    teacherNameFromApi = "Teacher";
                    etTeacherName.setText("Teacher");
                    // still show attendance popup even on error
                    checkAndShowAttendanceDialog();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("staff_id", staffId);
                return params;
            }
        };

        // set tag so we can cancel later
        req.setTag(volleyRequestTag);

        // set a reasonable timeout & retry policy so it doesn't hang forever
        req.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                10000, // timeout in ms (10s)
                1,     // max retries
                com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

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

    // helper used previously
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
