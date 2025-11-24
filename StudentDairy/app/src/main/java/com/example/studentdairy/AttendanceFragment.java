package com.example.studentdairy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.studentdairy.adapter.DayAdapter;
import com.example.studentdairy.model.Day;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class AttendanceFragment extends Fragment {

    private RecyclerView rvCalendar;
    private TextView tvMonthYear, tvLoading;
    private ImageButton btnPrev, btnNext;
    private ImageView attArrow;
    private TextView headerTitle;

    private Calendar displayCal = Calendar.getInstance();
    private List<Day> dayList = new ArrayList<>();
    private DayAdapter adapter;

    // data sets
    private Set<String> presentDates = new HashSet<>();
    private Set<String> absentDates = new HashSet<>();
    private Set<String> holidayDates = new HashSet<>();

    // IDs (load from prefs; defaults empty)
    private String classId = "";
    private String sectionId = "";
    private String studentId = "";

    private final String ATTEND_URL = "https://testing.trifrnd.net.in/ishwar/school/api/std_attend_api.php";
    private final String HOLIDAY_URL = "https://testing.trifrnd.net.in/ishwar/school/api/holiday_api.php";
    private final String STUDENT_API_URL = "https://testing.trifrnd.net.in/ishwar/school/api/student_api.php";

    private RequestQueue requestQueue;

    private SimpleDateFormat serverSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat monthYearSdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

    private static final String TAG = "AttendanceFragment";

    // prefs + listener
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;

    public AttendanceFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_attendance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rvCalendar = view.findViewById(R.id.rvCalendar);
        tvMonthYear = view.findViewById(R.id.tvMonthYear);
        btnPrev = view.findViewById(R.id.btnPrev);
        btnNext = view.findViewById(R.id.btnNext);
        tvLoading = view.findViewById(R.id.tvLoadingOrError);
        attArrow = view.findViewById(R.id.attarrow);
        headerTitle = view.findViewById(R.id.headerTitle);

        prefs = requireActivity().getSharedPreferences("StudentProfile", Context.MODE_PRIVATE);

        // register prefs listener to auto-refresh when profile updates
        prefsListener = (sharedPreferences, key) -> {
            if ("class_id".equals(key) || "section_id".equals(key) || "student_id".equals(key) || "userid".equals(key) || "mobile".equals(key)) {
                Log.d(TAG, "Prefs changed (" + key + "), reloading IDs and refreshing attendance");
                loadIdsFromPrefs();
                // Start flow again: fetch student profile (if mobile available) then holidays & attendance
                fetchStudentProfileThenAttendance();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(prefsListener);

        // initial load IDs (may be empty)
        loadIdsFromPrefs();

        // args override (if provided)
        if (getArguments() != null) {
            if (getArguments().containsKey("class_id")) classId = getArguments().getString("class_id", classId);
            if (getArguments().containsKey("section_id")) sectionId = getArguments().getString("section_id", sectionId);
            if (getArguments().containsKey("student_id")) studentId = getArguments().getString("student_id", studentId);
        }

        Log.d(TAG, "Initial IDs -> classId: " + classId + " sectionId: " + sectionId + " studentId: " + studentId);

        if (headerTitle != null) headerTitle.setText("Attendance");
        if (attArrow != null) attArrow.setOnClickListener(v -> requireActivity().onBackPressed());

        requestQueue = Volley.newRequestQueue(requireContext());

        rvCalendar.setHasFixedSize(true);
        rvCalendar.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        adapter = new DayAdapter(dayList, presentDates, absentDates, holidayDates);
        rvCalendar.setAdapter(adapter);

        btnPrev.setOnClickListener(v -> {
            displayCal.add(Calendar.MONTH, -1);
            renderCalendar();
        });

        btnNext.setOnClickListener(v -> {
            displayCal.add(Calendar.MONTH, 1);
            renderCalendar();
        });

        // Flow: fetch student profile first (if possible), then holidays -> attendance
        fetchStudentProfileThenAttendance();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (prefs != null && prefsListener != null) {
            prefs.unregisterOnSharedPreferenceChangeListener(prefsListener);
        }
    }

    /**
     * Try multiple keys to load IDs from StudentProfile SharedPreferences.
     * This handles inconsistent key-names used across fragments.
     */
    private void loadIdsFromPrefs() {
        try {
            SharedPreferences prefsLocal = requireActivity().getSharedPreferences("StudentProfile", Context.MODE_PRIVATE);

            // try the common keys first
            String c1 = prefsLocal.getString("class_id", "");
            String s1 = prefsLocal.getString("section_id", "");
            String st1 = prefsLocal.getString("student_id", "");

            // also consider alternate keys that other code used
            String c2 = prefsLocal.getString("class", prefsLocal.getString("class_id", ""));
            String s2 = prefsLocal.getString("section", prefsLocal.getString("section_id", ""));
            String st2 = prefsLocal.getString("userid", prefsLocal.getString("student_id", ""));

            // Some places stored 'class_section' or 'class_name'
            String c3 = prefsLocal.getString("class_section", prefsLocal.getString("class_name", ""));

            // pick first non-empty for each
            classId = firstNonEmpty(c1, c2, c3, "");
            sectionId = firstNonEmpty(s1, s2, "");
            studentId = firstNonEmpty(st1, st2, "");

            // As a last resort, try StudentProfile 'mobile' (sometimes student id used as mobile)
            if (studentId.isEmpty()) {
                studentId = prefsLocal.getString("mobile", prefsLocal.getString("userid", ""));
            }

            Log.d(TAG, "Loaded IDs from prefs -> classId:" + classId + " sectionId:" + sectionId + " studentId:" + studentId);
        } catch (Exception e) {
            Log.e(TAG, "Error reading prefs", e);
        }
    }

    private String firstNonEmpty(String... vals) {
        for (String v : vals) {
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        return "";
    }

    /**
     * Step 1: Attempt to call student_api.php with mobile (if available).
     * If successful, extract student_id, class_id, section_id, save to prefs (commit),
     * then proceed to fetch holidays and attendance.
     *
     * If mobile is not available or student API fails, fallback to reading existing prefs and continue.
     */
    /**
     * Step 1: Attempt to call student_api.php with mobile (if available).
     * If successful, extract student_id, class_id, section_id, save to prefs (commit),
     * then proceed to fetch holidays and attendance.
     *
     * If mobile is not available or student API fails, fallback to reading existing prefs and continue.
     */
    private void fetchStudentProfileThenAttendance() {
        // reload IDs from prefs (to get the mobile or userid if already present)
        loadIdsFromPrefs();

        // get mobile or userid from prefs
        String mobile = prefs.getString("mobile", prefs.getString("userid", ""));
        if (mobile == null) mobile = "";

        // make a final copy for lambda capture
        final String mobileFinal = mobile;

        if (mobileFinal.trim().isEmpty()) {
            Log.w(TAG, "No mobile/userid in prefs; will proceed with whatever IDs we have (may be empty)");
            // proceed with holidays & attendance using whatever is in prefs
            fetchHolidayThenAttendance();
            return;
        }

        tvLoading.setVisibility(View.VISIBLE);
        tvLoading.setText("Fetching profile...");

        StringRequest profileReq = new StringRequest(Request.Method.POST, STUDENT_API_URL,
                response -> {
                    try {
                        JSONObject root = new JSONObject(response);
                        JSONObject data = root.optJSONObject("data");
                        if (data != null) {
                            // Extract fields (convert to strings)
                            String fetchedStudentId = data.optString("student_id", data.optString("userid", ""));
                            String fetchedClassId = String.valueOf(data.optString("class_id", data.optString("class", "")));
                            String fetchedSectionId = data.optString("section_id", data.optString("section", ""));

                            // Update local fields
                            if (fetchedStudentId != null && !fetchedStudentId.trim().isEmpty()) studentId = fetchedStudentId.trim();
                            if (fetchedClassId != null && !fetchedClassId.trim().isEmpty()) classId = fetchedClassId.trim();
                            if (fetchedSectionId != null && !fetchedSectionId.trim().isEmpty()) sectionId = fetchedSectionId.trim();

                            // Save critical keys to StudentProfile prefs synchronously so other fragments see them immediately
                            try {
                                SharedPreferences.Editor ed = prefs.edit();
                                ed.putString("student_id", studentId);
                                ed.putString("class_id", classId);
                                ed.putString("section_id", sectionId);
                                ed.putString("userid", mobileFinal);
                                ed.putString("mobile", mobileFinal);
                                ed.commit(); // commit synchronously
                                Log.d(TAG, "Saved fetched profile to prefs: student_id=" + studentId + " class_id=" + classId + " section_id=" + sectionId);
                            } catch (Exception e) {
                                Log.e(TAG, "Error committing profile prefs", e);
                            }
                        } else {
                            Log.w(TAG, "student_api returned no data for mobile=" + mobileFinal);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing student_api response", e);
                    } finally {
                        // proceed to holidays & attendance whether profile fetch succeeded or not
                        fetchHolidayThenAttendance();
                    }
                },
                error -> {
                    Log.e(TAG, "student_api request failed: " + error.getMessage(), error);
                    // fallback: proceed with whatever we have in prefs
                    fetchHolidayThenAttendance();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("mobile", mobileFinal);
                return map;
            }
        };

        requestQueue.add(profileReq);
    }


    // fetch holidays then attendance
    private void fetchHolidayThenAttendance() {
        if (getContext() == null) return;

        tvLoading.setVisibility(View.VISIBLE);
        tvLoading.setText("Loading holidays...");

        StringRequest holidayRequest = new StringRequest(Request.Method.GET, HOLIDAY_URL,
                response -> {
                    try {
                        holidayDates.clear();
                        JSONObject root = new JSONObject(response);
                        if (root.has("data")) {
                            JSONArray arr = root.getJSONArray("data");
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = arr.getJSONObject(i);
                                String hDate = obj.optString("H_Date", "").trim();
                                if (!hDate.isEmpty() && hDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                                    holidayDates.add(hDate);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing holiday API", e);
                    } finally {
                        fetchAttendanceAndRender();
                    }
                },
                error -> {
                    Log.e(TAG, "Holiday API failed, continuing", error);
                    fetchAttendanceAndRender();
                }
        );

        requestQueue.add(holidayRequest);
    }

    // fetch attendance (POST) — reload IDs from prefs to ensure latest values are used
    private void fetchAttendanceAndRender() {
        // reload IDs from prefs to ensure latest saved values (in case student_api just wrote them)
        loadIdsFromPrefs();

        tvLoading.setVisibility(View.VISIBLE);
        tvLoading.setText("Loading attendance...");

        StringRequest postRequest = new StringRequest(Request.Method.POST, ATTEND_URL,
                response -> {
                    tvLoading.setVisibility(View.GONE);
                    try {
                        JSONArray arr;
                        if (response.trim().startsWith("[")) {
                            arr = new JSONArray(response);
                        } else {
                            JSONObject obj = new JSONObject(response);
                            if (obj.has("data")) arr = obj.getJSONArray("data");
                            else if (obj.has("attendance")) arr = obj.getJSONArray("attendance");
                            else arr = new JSONArray();
                        }

                        presentDates.clear();
                        absentDates.clear();

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject at = arr.getJSONObject(i);
                            String status = at.optString("status", "").trim();
                            String a_date = at.optString("a_date", "").trim();

                            if (a_date.isEmpty()) continue;

                            if ("P".equalsIgnoreCase(status) || "present".equalsIgnoreCase(status)) presentDates.add(a_date);
                            else if ("A".equalsIgnoreCase(status) || "absent".equalsIgnoreCase(status)) absentDates.add(a_date);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing attendance", e);
                    } finally {
                        renderCalendar();
                    }
                },
                error -> {
                    tvLoading.setVisibility(View.VISIBLE);
                    tvLoading.setText("Attendance load failed");
                    Log.e(TAG, "Attendance API error", error);
                    renderCalendar();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("class_id", classId);
                map.put("section_id", sectionId);
                map.put("student_id", studentId);
                Log.d(TAG, "Attendance POST params: " + map.toString());
                return map;
            }
        };

        requestQueue.add(postRequest);
    }

    // build calendar grid and set adapter
    private void renderCalendar() {
        dayList.clear();
        tvMonthYear.setText(monthYearSdf.format(displayCal.getTime()));

        Calendar cal = (Calendar) displayCal.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDow = cal.get(Calendar.DAY_OF_WEEK);
        int blanks = firstDow - Calendar.SUNDAY;
        if (blanks < 0) blanks += 7;

        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < blanks; i++) dayList.add(new Day(0, "", false));
        for (int d = 1; d <= maxDays; d++) {
            cal.set(Calendar.DAY_OF_MONTH, d);
            String dateStr = serverSdf.format(cal.getTime());
            dayList.add(new Day(d, dateStr, true));
        }
        while (dayList.size() % 7 != 0) dayList.add(new Day(0, "", false));

        adapter = new DayAdapter(dayList, presentDates, absentDates, holidayDates);
        rvCalendar.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        // hide loading if shown
        tvLoading.setVisibility(View.GONE);
    }
}
