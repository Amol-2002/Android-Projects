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
                            boolean success = response.getBoolean("success");
                            if (success) {
                                JSONArray holidays = response.getJSONArray("data");
                                populateTable(holidays);
                            } else {
                                Toast.makeText(getContext(), "No data found", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
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
            for (int i = 0; i < holidays.length(); i++) {
                JSONObject holiday = holidays.getJSONObject(i);

                String srNo = String.valueOf(i + 1);
                String holidayDate = holiday.getString("H_Date");
                String description = holiday.getString("Details");

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
        tv.setPadding(8, 8, 8, 8);
        tv.setTextColor(getResources().getColor(android.R.color.black));
        return tv;
    }
}
