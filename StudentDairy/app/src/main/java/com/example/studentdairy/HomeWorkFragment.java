package com.example.studentdairy;

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

public class HomeWorkFragment extends Fragment {

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

    private static final String HW_URL = "https://testing.trifrnd.net.in/ishwar/school/api/homework_api.php";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_work, container, false);

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

        // make sure TextView acts like a button
        tvSelectedDate.setClickable(true);
        tvSelectedDate.setFocusable(true);

        // Open date picker when user clicks the TextView (same as btnPick)
        tvSelectedDate.setOnClickListener(v -> showDatePicker());
        btnPick.setOnClickListener(v -> showDatePicker());

        // Optionally initialize tvSelectedDate to today's date
        Date today = new Date();
        tvSelectedDate.setText(displaySdf.format(today));

        // If you want to auto-filter for today on launch, uncomment the next line:
        // filterByDate(apiSdf.format(today));

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
        // get mobile from SharedPreferences (fallback to userid)
        SharedPreferences prefs = requireActivity().getSharedPreferences("StudentProfile", Context.MODE_PRIVATE);
        final String mobile = prefs.getString("mobile", prefs.getString("userid", ""));
        // ensure non-null
        // (not strictly necessary since prefs.getString won't return null when default provided,
        // but keeps behavior explicit)
        // if (mobile == null) mobile = "";

        StringRequest req = new StringRequest(Request.Method.POST, HW_URL,
                response -> {
                    try {
                        allItems.clear();
                        JSONObject root = new JSONObject(response);
                        if (root.has("data")) {
                            JSONArray arr = root.getJSONArray("data");
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = arr.getJSONObject(i);
                                String subject = obj.optString("subject", "-");
                                String hwDate = obj.optString("hw_date", "");
                                String file = obj.optString("file_name", "");
                                allItems.add(new HomeworkAdapter.HomeworkItem(subject, hwDate, file));
                            }
                        }
                        // initially show nothing until user picks a date (you can change to show all)
                        filtered.clear();
                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(View.GONE);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(requireContext(), "Failed to load homework", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // use the same mobile retrieved earlier or a preferred prefs key
                params.put("mobile", mobile);
                return params;
            }
        };

        queue.add(req);
    }

    private void showDatePicker() {
        // default to today
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
        } catch (Exception e) { return yyyyMMdd; }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (queue != null) queue.cancelAll(this);
    }
}
