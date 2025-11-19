package com.example.studentdairy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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

    // IDs (from prefs / args)
    private String classId = "1";
    private String sectionId = "A";
    private String studentId = "123";

    private final String ATTEND_URL = "https://trifrnd.co.in/school/api/data.php?apicall=attend";
    private final String HOLIDAY_URL = "https://testing.trifrnd.net.in/ishwar/school/api/holiday_api.php"; // your holiday API

    private RequestQueue requestQueue;

    private SimpleDateFormat serverSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat monthYearSdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

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

        // load IDs from SharedPreferences (if saved)
        SharedPreferences prefs = requireActivity().getSharedPreferences("StudentProfile", Context.MODE_PRIVATE);
        classId = prefs.getString("class_id", classId);
        sectionId = prefs.getString("section_id", sectionId);
        studentId = prefs.getString("student_id", studentId);

        // args override
        if (getArguments() != null) {
            if (getArguments().containsKey("class_id")) classId = getArguments().getString("class_id");
            if (getArguments().containsKey("section_id")) sectionId = getArguments().getString("section_id");
            if (getArguments().containsKey("student_id")) studentId = getArguments().getString("student_id");
        }

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

        // IMPORTANT: fetch holidays first, then attendance
        fetchHolidayThenAttendance();
    }

    // fetch holidays then attendance
    private void fetchHolidayThenAttendance() {
        tvLoading.setVisibility(View.VISIBLE);
        tvLoading.setText("Loading holidays...");

        StringRequest holidayRequest = new StringRequest(Request.Method.GET, HOLIDAY_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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
                            Log.e("HolidayParse", "Error parsing holiday API", e);
                        } finally {
                            fetchAttendanceAndRender();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("HolidayAPI", "Holiday API failed, continuing", error);
                        fetchAttendanceAndRender();
                    }
                }
        );

        requestQueue.add(holidayRequest);
    }

    // fetch attendance (POST)
    private void fetchAttendanceAndRender() {
        tvLoading.setVisibility(View.VISIBLE);
        tvLoading.setText("Loading attendance...");

        StringRequest postRequest = new StringRequest(Request.Method.POST, ATTEND_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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

                                if ("P".equalsIgnoreCase(status)) presentDates.add(a_date);
                                else if ("A".equalsIgnoreCase(status)) absentDates.add(a_date);
                            }
                        } catch (Exception e) {
                            Log.e("AttendanceParse", "Error parsing attendance", e);
                        } finally {
                            renderCalendar();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        tvLoading.setVisibility(View.VISIBLE);
                        tvLoading.setText("Attendance load failed");
                        renderCalendar();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("class_id", classId);
                map.put("section_id", sectionId);
                map.put("student_id", studentId);
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
