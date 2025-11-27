package com.example.classteacherportal;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Button;
import android.widget.ImageView;
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

public class HomeworkFragment extends Fragment {

    private TextView tvSelectedDate;
    private Button btnPick, btnClear;
    private RecyclerView rvHomework;
    private TextView tvEmpty;

    private HomeworkAdapter adapter;
    private List<HomeworkAdapter.HomeworkItem> allItems = new ArrayList<>();
    private List<HomeworkAdapter.HomeworkItem> filtered = new ArrayList<>();

    private RequestQueue queue;
    private SimpleDateFormat apiSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displaySdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    private static final String HW_URL = "https://testing.trifrnd.net.in/ishwar/school/api/homework_list_api.php";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_homework, container, false);

        // Back arrow -> MainActivity
        ImageView attarrow = view.findViewById(R.id.attarrow);
        attarrow.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        btnPick = view.findViewById(R.id.btnPickDate);
        btnClear = view.findViewById(R.id.btnClear);
        rvHomework = view.findViewById(R.id.rvHomework);
        tvEmpty = view.findViewById(R.id.tvHwEmpty);

        tvSelectedDate.setClickable(true);
        tvSelectedDate.setFocusable(true);

        tvSelectedDate.setOnClickListener(v -> showDatePicker());
        btnPick.setOnClickListener(v -> showDatePicker());

        Date today = new Date();
        tvSelectedDate.setText(displaySdf.format(today));
        // If you want to auto-filter for today: filterByDate(apiSdf.format(today));

        rvHomework.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HomeworkAdapter(requireContext(), filtered);
        rvHomework.setAdapter(adapter);

        queue = Volley.newRequestQueue(requireContext());

        btnClear.setOnClickListener(v -> {
            tvSelectedDate.setText("Select date");
            filtered.clear();
            adapter.notifyDataSetChanged();
            tvEmpty.setVisibility(View.GONE);
            rvHomework.setVisibility(View.VISIBLE);
        });

        fetchHomework();

        return view;
    }

    private void fetchHomework() {
        // ✅ staff_id = userid from StudentProfile (same as login)
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("StudentProfile", Context.MODE_PRIVATE);
        final String staffId = prefs.getString("userid", "");  // <- important

        if (staffId.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Staff ID missing. Please login again.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest req = new StringRequest(Request.Method.POST, HW_URL,
                response -> {
                    try {
                        allItems.clear();

                        // ✅ Response format:
                        // [
                        //   [
                        //     { "subject": "...", "hw_date": "2024-09-17", "file_name": "..." },
                        //     ...
                        //   ]
                        // ]
                        JSONArray outer = new JSONArray(response);
                        if (outer.length() > 0) {
                            JSONArray inner = outer.getJSONArray(0);
                            for (int i = 0; i < inner.length(); i++) {
                                JSONObject obj = inner.getJSONObject(i);
                                String subject = obj.optString("subject", "-");
                                String hwDate = obj.optString("hw_date", "");
                                String file = obj.optString("file_name", "");
                                allItems.add(new HomeworkAdapter.HomeworkItem(subject, hwDate, file));
                            }
                        }

                        // Initially show nothing until user picks a date
                        filtered.clear();
                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(View.GONE);
                        rvHomework.setVisibility(View.VISIBLE);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(),
                                "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(requireContext(),
                            "Failed to load homework", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // ✅ API needs "staff_id"
                params.put("staff_id", staffId);
                return params;
            }
        };

        // Optional: tag if you want to cancel later with same tag
        req.setTag("HW_REQ");
        queue.add(req);
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar sel = Calendar.getInstance();
                    sel.set(year, month, dayOfMonth);
                    String apiDate = apiSdf.format(sel.getTime()); // yyyy-MM-dd
                    String display = displaySdf.format(sel.getTime());
                    tvSelectedDate.setText(display);
                    filterByDate(apiDate);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void filterByDate(String yyyyMMdd) {
        filtered.clear();
        for (HomeworkAdapter.HomeworkItem it : allItems) {
            if (it.hwDate != null && it.hwDate.equals(yyyyMMdd)) {
                filtered.add(it);
            }
        }
        adapter.notifyDataSetChanged();

        if (filtered.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("No homework for " + formatDisplayDate(yyyyMMdd));
            rvHomework.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvHomework.setVisibility(View.VISIBLE);
        }
    }

    private String formatDisplayDate(String yyyyMMdd) {
        try {
            Date d = apiSdf.parse(yyyyMMdd);
            return displaySdf.format(d);
        } catch (Exception e) {
            return yyyyMMdd;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (queue != null) {
            queue.cancelAll("HW_REQ");   // match the tag used in fetchHomework()
        }
    }
}
