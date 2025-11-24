package com.example.studentdairy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HolidayFragment extends Fragment {

    private TableLayout tableLayout;
    private static final String API_URL = "https://testing.trifrnd.net.in/ishwar/school/api/holiday_api.php";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_holiday, container, false);

        // Find the ImageView
        ImageView attarrow = view.findViewById(R.id.attarrow);

        // Click → Intent to MainActivity
        attarrow.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

        tableLayout = view.findViewById(R.id.tableLayout);
        fetchHolidayData();
        return view;
    }

    private void fetchHolidayData() {
        if (getContext() == null) return;

        RequestQueue queue = Volley.newRequestQueue(getContext());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                API_URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.optBoolean("success", false);
                            if (success) {
                                JSONArray holidays = response.optJSONArray("data");
                                if (holidays != null) {
                                    populateTable(holidays);
                                } else {
                                    Toast.makeText(getContext(), "No holidays found", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), "No data found", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
                        }
                        Log.e("API_ERROR", error.toString());
                    }
                }
        );

        queue.add(jsonObjectRequest);
    }

    private void populateTable(JSONArray holidays) {
        try {
            // clear previous rows (if any)
            tableLayout.removeAllViews();

            // optional: add header row
            TableRow header = new TableRow(getContext());
            header.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
            TextView h1 = createTextView("Sr");
            TextView h2 = createTextView("Date");
            TextView h3 = createTextView("Holiday");
            h1.setTypeface(null, android.graphics.Typeface.BOLD);
            h2.setTypeface(null, android.graphics.Typeface.BOLD);
            h3.setTypeface(null, android.graphics.Typeface.BOLD);
            header.addView(h1);
            header.addView(h2);
            header.addView(h3);
            tableLayout.addView(header);

            for (int i = 0; i < holidays.length(); i++) {
                JSONObject holiday = holidays.getJSONObject(i);

                String srNo = String.valueOf(i + 1);
                String holidayDateRaw = holiday.optString("H_Date", "");
                String holidayDate = formatPrettyDate(holidayDateRaw);
                String description = holiday.optString("Details", "-");

                TableRow tableRow = new TableRow(getContext());
                tableRow.setLayoutParams(new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));

                TextView srNoText = createTextView(srNo);
                TextView dateText = createTextView(holidayDate);
                TextView descText = createTextView(description);

                tableRow.addView(srNoText);
                tableRow.addView(dateText);
                tableRow.addView(descText);

                tableLayout.addView(tableRow);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private TextView createTextView(String text) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        int pad = dpToPx(8);
        tv.setPadding(pad, pad, pad, pad);
        tv.setTextColor(getResources().getColor(android.R.color.black));
        return tv;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * Try to parse common date formats and return "d MMM yyyy" (e.g. "1 Jun 2023").
     * If parsing fails, returns trimmed raw string.
     */
    private String formatPrettyDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "-";
        raw = raw.trim();

        // Try several common patterns
        String[] patterns = new String[] {
                "yyyy-MM-dd",
                "yyyy/MM/dd",
                "dd-MM-yyyy",
                "dd/MM/yyyy",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss"
        };

        for (String p : patterns) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(p, Locale.getDefault());
                Date d = parser.parse(raw);
                if (d != null) {
                    SimpleDateFormat out = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
                    return out.format(d);
                }
            } catch (Exception ignored) {}
        }

        // fallback: try to extract date portion from ISO-like strings
        try {
            String cleaned = raw.split("T")[0].split(" ")[0];
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d = parser.parse(cleaned);
            if (d != null) {
                SimpleDateFormat out = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
                return out.format(d);
            }
        } catch (Exception ignored) {}

        return raw;
    }
}
