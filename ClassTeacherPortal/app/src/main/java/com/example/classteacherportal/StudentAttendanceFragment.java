package com.example.classteacherportal;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

public class StudentAttendanceFragment extends Fragment {

    private static final String TAG = "StudentAttendanceFrag";

    private static final String ADD_ATTEND_URL =
            "https://testing.trifrnd.net.in/ishwar/school/api/add_attend_std_api.php";

    private static final String STUDENT_LIST_URL =
            "https://testing.trifrnd.net.in/ishwar/school/api/class_std_api.php";

    private TextView tvClassName, tvSection;
    private EditText etDate;
    private RecyclerView rvStudents;
    private Button btnAdd, btnClear;

    private RequestQueue queue;

    private SimpleDateFormat apiSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displaySdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    private String staffId = "";
    private String classId = "";
    private String sectionId = "";

    private List<StudentAttendanceAdapter.StudentItem> studentItems = new ArrayList<>();
    private StudentAttendanceAdapter adapter;

    private SharedPreferences attPrefs;
    private static final String ATT_PREF_NAME = "AttendanceSubmitPrefs";

    public StudentAttendanceFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_student_attendance, container, false);

        tvClassName = v.findViewById(R.id.tvClassNameAtt);
        tvSection = v.findViewById(R.id.tvSectionAtt);
        etDate = v.findViewById(R.id.etAttDate);
        rvStudents = v.findViewById(R.id.rvStudents);
        btnAdd = v.findViewById(R.id.btnAddAttendance);
        btnClear = v.findViewById(R.id.btnClearAttendance);

        queue = Volley.newRequestQueue(requireContext());
        attPrefs = requireActivity().getSharedPreferences(ATT_PREF_NAME, Context.MODE_PRIVATE);

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("StudentProfile", Context.MODE_PRIVATE);
        staffId = prefs.getString("userid", "");

        Log.d(TAG, "staffId from prefs = " + staffId);

        Date today = new Date();
        etDate.setText(displaySdf.format(today));
        etDate.setOnClickListener(v12 -> showDatePicker());

        rvStudents.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new StudentAttendanceAdapter(requireContext(), studentItems);
        rvStudents.setAdapter(adapter);

        btnAdd.setOnClickListener(v13 -> submitAttendance());
        btnClear.setOnClickListener(v14 -> resetAttendance());

        if (!TextUtils.isEmpty(staffId)) {
            fetchStudentList();
        }

        return v;
    }

    private void fetchStudentList() {
        StringRequest req = new StringRequest(Request.Method.POST, STUDENT_LIST_URL,
                response -> {
                    try {
                        studentItems.clear();

                        String trimmed = response.trim();
                        Log.d(TAG, "Student list response = " + trimmed);

                        JSONArray outer = new JSONArray(trimmed);
                        JSONArray inner = outer.getJSONArray(0);

                        for (int i = 0; i < inner.length(); i++) {
                            JSONObject obj = inner.getJSONObject(i);

                            String sid = obj.optString("student_id", "");
                            String sname = obj.optString("student_name", "");
                            String cId = obj.optString("class_id", "");
                            String sId = obj.optString("section_id", "");

                            if (i == 0) {
                                classId = cId;
                                sectionId = sId;
                                tvClassName.setText(classId);
                                tvSection.setText(sectionId);
                            }

                            studentItems.add(new StudentAttendanceAdapter.StudentItem(sid, sname));
                        }

                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(),
                                "Student list parse error",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(requireContext(),
                            "Failed to load student list",
                            Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("staff_id", staffId);
                return p;
            }
        };

        queue.add(req);
    }

    private void submitAttendance() {
        if (TextUtils.isEmpty(classId) || TextUtils.isEmpty(sectionId)) {
            Toast.makeText(requireContext(),
                    "Class/Section not loaded yet",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String displayDate = etDate.getText().toString().trim();
        if (TextUtils.isEmpty(displayDate)) {
            Toast.makeText(requireContext(), "Please select date", Toast.LENGTH_SHORT).show();
            return;
        }

        String apiDate;
        try {
            Date d = displaySdf.parse(displayDate);
            apiDate = apiSdf.format(d);
        } catch (Exception e) {
            apiDate = displayDate;
        }

        if (studentItems.isEmpty()) {
            Toast.makeText(requireContext(),
                    "No student to submit",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String submitKey = staffId + "_" + classId + "_" + sectionId + "_" + apiDate;
        if (attPrefs.getBoolean(submitKey, false)) {
            Toast.makeText(requireContext(),
                    "Attendance already submitted for this date",
                    Toast.LENGTH_LONG).show();
            return;
        }

        try {
            JSONArray arr = new JSONArray();
            for (StudentAttendanceAdapter.StudentItem it : studentItems) {
                JSONObject obj = new JSONObject();
                obj.put("class_id", Integer.parseInt(classId));
                obj.put("section_id", sectionId);
                obj.put("student_id", it.studentId);
                obj.put("a_date", apiDate);
                obj.put("status", it.status);
                arr.put(obj);
            }

            final String requestBody = arr.toString();
            Log.d(TAG, "Attendance JSON = " + requestBody);

            // 🔥 FIX – make variables final for lambda
            final String finalApiDate = apiDate;
            final String finalSubmitKey = submitKey;

            StringRequest req = new StringRequest(Request.Method.POST, ADD_ATTEND_URL,
                    apiResponse -> {
                        Log.d(TAG, "Attendance save response = " + apiResponse);
                        handleAttendanceResponse(apiResponse, finalApiDate, finalSubmitKey);
                    },
                    error -> {
                        error.printStackTrace();
                        Toast.makeText(requireContext(),
                                "Failed to save attendance: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }) {

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() {
                    try {
                        return requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException e) {
                        return null;
                    }
                }
            };

            queue.add(req);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(),
                    "Error building JSON",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void handleAttendanceResponse(String response, String apiDate, String submitKey) {
        try {
            JSONObject obj = new JSONObject(response);

            int success = obj.optInt("success_count", 0);
            int failed = obj.optInt("failed_count", 0);

            // ❌ Attendance already submitted on server
            if (success == 0 && failed > 0) {
                JSONArray errors = obj.optJSONArray("errors");
                StringBuilder sb = new StringBuilder();
                if (errors != null) {
                    for (int i = 0; i < errors.length(); i++) {
                        sb.append(errors.getString(i)).append("\n");
                    }
                }

                new AlertDialog.Builder(requireContext())
                        .setTitle("Attendance Already Exists")
                        .setMessage(sb.toString())
                        .setPositiveButton("OK", null)
                        .show();

                return;
            }

            // ✔ Success
            Toast.makeText(requireContext(),
                    "Attendance saved successfully",
                    Toast.LENGTH_SHORT).show();

            attPrefs.edit().putBoolean(submitKey, true).apply();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(),
                    "Unexpected server response",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(requireContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    Calendar sel = Calendar.getInstance();
                    sel.set(year, month, dayOfMonth);
                    etDate.setText(displaySdf.format(sel.getTime()));
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void resetAttendance() {
        for (StudentAttendanceAdapter.StudentItem it : studentItems) {
            it.status = "P";
        }
        adapter.notifyDataSetChanged();
        Toast.makeText(requireContext(),
                "Reset to all Present",
                Toast.LENGTH_SHORT).show();
    }
}
