package com.example.classteacherportal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
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

import java.text.SimpleDateFormat;
import java.util.*;

public class ViewAttendanceFragmentGrid extends Fragment {

    private static final String TAG = "ViewAttGridFragment";
    private static final String ATT_LIST_URL =
            "https://testing.trifrnd.net.in/ishwar/school/api/std_attend_list_api.php";

    private Spinner spMonth, spYear;
    private RecyclerView rvGrid;
    private TextView tvEmpty;
    private TextView[] headerDays = new TextView[31];

    private RequestQueue queue;

    private String staffId = "";

    private SimpleDateFormat apiSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // keep ALL records from API
    private static class RawRecord {
        String studentId, studentName, dateYmd, status;
        RawRecord(String sid, String sname, String dateYmd, String status) {
            this.studentId = sid; this.studentName = sname;
            this.dateYmd = dateYmd; this.status = status;
        }
    }
    private List<RawRecord> allRecords = new ArrayList<>();

    private List<AttendanceGridAdapter.StudentMonthItem> monthItems = new ArrayList<>();
    private AttendanceGridAdapter adapter;

    public ViewAttendanceFragmentGrid() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_view_attendance_grid, container, false);


        ImageView back = v.findViewById(R.id.attarrow);
        back.setOnClickListener(view -> {
            Fragment home = new HomeFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, home)
                    .addToBackStack(null)
                    .commit();
        });


        spMonth = v.findViewById(R.id.spMonth);
        spYear = v.findViewById(R.id.spYear);
        rvGrid = v.findViewById(R.id.rvAttendanceGrid);
        tvEmpty = v.findViewById(R.id.tvEmptyAttendance);

        // header day TextViews
        headerDays[0] = v.findViewById(R.id.tvH1);
        headerDays[1] = v.findViewById(R.id.tvH2);
        headerDays[2] = v.findViewById(R.id.tvH3);
        headerDays[3] = v.findViewById(R.id.tvH4);
        headerDays[4] = v.findViewById(R.id.tvH5);
        headerDays[5] = v.findViewById(R.id.tvH6);
        headerDays[6] = v.findViewById(R.id.tvH7);
        headerDays[7] = v.findViewById(R.id.tvH8);
        headerDays[8] = v.findViewById(R.id.tvH9);
        headerDays[9] = v.findViewById(R.id.tvH10);
        headerDays[10] = v.findViewById(R.id.tvH11);
        headerDays[11] = v.findViewById(R.id.tvH12);
        headerDays[12] = v.findViewById(R.id.tvH13);
        headerDays[13] = v.findViewById(R.id.tvH14);
        headerDays[14] = v.findViewById(R.id.tvH15);
        headerDays[15] = v.findViewById(R.id.tvH16);
        headerDays[16] = v.findViewById(R.id.tvH17);
        headerDays[17] = v.findViewById(R.id.tvH18);
        headerDays[18] = v.findViewById(R.id.tvH19);
        headerDays[19] = v.findViewById(R.id.tvH20);
        headerDays[20] = v.findViewById(R.id.tvH21);
        headerDays[21] = v.findViewById(R.id.tvH22);
        headerDays[22] = v.findViewById(R.id.tvH23);
        headerDays[23] = v.findViewById(R.id.tvH24);
        headerDays[24] = v.findViewById(R.id.tvH25);
        headerDays[25] = v.findViewById(R.id.tvH26);
        headerDays[26] = v.findViewById(R.id.tvH27);
        headerDays[27] = v.findViewById(R.id.tvH28);
        headerDays[28] = v.findViewById(R.id.tvH29);
        headerDays[29] = v.findViewById(R.id.tvH30);
        headerDays[30] = v.findViewById(R.id.tvH31);

        queue = Volley.newRequestQueue(requireContext());

        SharedPreferences sp = requireActivity()
                .getSharedPreferences("StudentProfile", Context.MODE_PRIVATE);
        staffId = sp.getString("userid", "");
        if (TextUtils.isEmpty(staffId)) {
            staffId = sp.getString("mobile", "");
        }

        rvGrid.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AttendanceGridAdapter(requireContext(), monthItems, 31, 0);
        rvGrid.setAdapter(adapter);

        setupMonthYearSpinners();

        if (!TextUtils.isEmpty(staffId)) {
            fetchAttendance();
        } else {
            Toast.makeText(requireContext(),
                    "Staff ID missing. Please login again.",
                    Toast.LENGTH_LONG).show();
        }

        return v;
    }

    private void setupMonthYearSpinners() {
        String[] monthNames = {"January","February","March","April","May","June",
                "July","August","September","October","November","December"};

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, monthNames);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMonth.setAdapter(monthAdapter);

        Calendar c = Calendar.getInstance();
        int currentYear = c.get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        years.add(String.valueOf(currentYear - 1));
        years.add(String.valueOf(currentYear));
        years.add(String.valueOf(currentYear + 1));

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spYear.setAdapter(yearAdapter);

        spMonth.setSelection(c.get(Calendar.MONTH));
        int idx = years.indexOf(String.valueOf(currentYear));
        if (idx >= 0) spYear.setSelection(idx);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                buildGridForSelectedMonth();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        };

        spMonth.setOnItemSelectedListener(listener);
        spYear.setOnItemSelectedListener(listener);
    }

    private void fetchAttendance() {
        StringRequest req = new StringRequest(Request.Method.POST, ATT_LIST_URL,
                response -> {
                    try {
                        allRecords.clear();
                        String trimmed = response.trim();
                        Log.d(TAG, "API resp: " + trimmed.substring(0, Math.min(200, trimmed.length())));

                        JSONArray outer = new JSONArray(trimmed);
                        if (outer.length() > 0) {
                            JSONArray inner = outer.getJSONArray(0);
                            for (int i = 0; i < inner.length(); i++) {
                                JSONObject o = inner.getJSONObject(i);
                                String sid = o.optString("student_id", "");
                                String sname = o.optString("student_name", "");
                                String date = o.optString("a_date", "");
                                String status = o.optString("status", "");
                                if (!sid.isEmpty() && !date.isEmpty()) {
                                    allRecords.add(new RawRecord(sid, sname, date, status));
                                }
                            }
                        }

                        buildGridForSelectedMonth();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(),
                                "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(requireContext(),
                            "Failed to load attendance", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("staff_id", staffId);
                return p;
            }
        };

        req.setTag(TAG);
        queue.add(req);
    }

    /** Build month grid (per student, per day) from allRecords */
    private void buildGridForSelectedMonth() {
        monthItems.clear();

        if (allRecords.isEmpty()) {
            adapter.notifyDataSetChanged();
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        int monthIndex = spMonth.getSelectedItemPosition(); // 0..11
        int year = Integer.parseInt((String) spYear.getSelectedItem());

        Calendar cal = Calendar.getInstance();

        // Days in selected month
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthIndex);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // set header labels "01".."31", hide >daysInMonth
        for (int d = 1; d <= 31; d++) {
            TextView tv = headerDays[d - 1];
            if (d <= daysInMonth) {
                tv.setVisibility(View.VISIBLE);
                tv.setText(String.format(Locale.getDefault(), "%02d", d));
            } else {
                tv.setVisibility(View.GONE);
            }
        }

        // Map studentId -> StudentMonthItem (preserve order of appearance)
        LinkedHashMap<String, AttendanceGridAdapter.StudentMonthItem> map = new LinkedHashMap<>();

        for (RawRecord r : allRecords) {
            try {
                Date d = apiSdf.parse(r.dateYmd);
                cal.setTime(d);
                int m = cal.get(Calendar.MONTH);
                int y = cal.get(Calendar.YEAR);
                if (m != monthIndex || y != year) continue;

                int day = cal.get(Calendar.DAY_OF_MONTH); // 1..31

                AttendanceGridAdapter.StudentMonthItem item = map.get(r.studentId);
                if (item == null) {
                    item = new AttendanceGridAdapter.StudentMonthItem(
                            r.studentId,
                            r.studentName,
                            daysInMonth
                    );
                    map.put(r.studentId, item);
                }

                // store status at index day-1 ("P"/"A")
                if (day >= 1 && day <= 31) {
                    item.dayStatus[day - 1] = r.status;
                }

            } catch (Exception ignored) {}
        }

        monthItems.addAll(map.values());

        // highlight today if same month/year, else 0
        Calendar today = Calendar.getInstance();
        int highlight = 0;
        if (today.get(Calendar.YEAR) == year &&
                today.get(Calendar.MONTH) == monthIndex) {
            highlight = today.get(Calendar.DAY_OF_MONTH);
        }

        adapter = new AttendanceGridAdapter(requireContext(), monthItems, daysInMonth, highlight);
        rvGrid.setAdapter(adapter);

        tvEmpty.setVisibility(monthItems.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (queue != null) queue.cancelAll(TAG);
    }
}
