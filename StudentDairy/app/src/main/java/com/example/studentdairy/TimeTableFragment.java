package com.example.studentdairy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.studentdairy.adapter.DayAdapter;
import com.example.studentdairy.adapter.DayAdapterr;
import com.example.studentdairy.model.DayItem;
import com.example.studentdairy.model.PeriodModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class TimetableFragment extends Fragment {

    private static final String API_URL = "https://testing.trifrnd.net.in/ishwar/school/api/time_table_api.php";
    private static final String TAG = "TimetableFragment_Debug";
    private static final String REQ_TAG = "TimetableReq";

    private RequestQueue requestQueue;
    private String mobile = "";
    private ProgressBar progress;

    // adapter + data
    private DayAdapterr dayAdapter;
    private final List<DayItem> dayItems = new ArrayList<>();

    // desired day order
    private final List<String> dayOrder = Arrays.asList(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // init queue
        requestQueue = Volley.newRequestQueue(requireContext());

        // read mobile from prefs
        SharedPreferences timetabelPrefs =
                requireActivity().getSharedPreferences("Timetable", MODE_PRIVATE);
        mobile = timetabelPrefs.getString("mobile", "");
        Log.d(TAG, "MOBILE FROM PREFS = " + mobile);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_timetable, container, false);

        RecyclerView rv = root.findViewById(R.id.rvTimetable);
        progress = root.findViewById(R.id.progress);

        // set up DayAdapter (one card per day)
        dayAdapter = new DayAdapterr(dayItems);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(dayAdapter);
        rv.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        fetchTimetable();

        return root;
    }

    private void fetchTimetable() {
        if (!isAdded()) { Log.w(TAG, "Fragment not added. Skip fetch."); return; }

        if (mobile == null || mobile.trim().isEmpty()) {
            Log.w(TAG, "Mobile missing - not calling API");
            Toast.makeText(requireContext(), "Mobile not found. Please login.", Toast.LENGTH_LONG).show();
            return;
        }

        progress.setVisibility(View.VISIBLE);

        StringRequest req = new StringRequest(Request.Method.POST, API_URL, response -> {
            if (!isAdded()) { Log.w(TAG, "Detached - ignoring response"); return; }
            progress.setVisibility(View.GONE);

            Log.d(TAG, "Raw server response (preview): " + (response == null ? "null" : response.substring(0, Math.min(response.length(), 1200))));

            try {
                List<PeriodModel> allPeriods = parseResponse(response);

                // build ordered map of days -> periods
                LinkedHashMap<String, List<PeriodModel>> byDay = new LinkedHashMap<>();
                // initialize with dayOrder to keep order and show empty days (remove if you don't want empty days)
                for (String d : dayOrder) byDay.put(d, new ArrayList<>());

                for (PeriodModel p : allPeriods) {
                    String day = p.getDay();
                    if (day == null || day.trim().isEmpty()) day = "Unknown";
                    if (!byDay.containsKey(day)) byDay.put(day, new ArrayList<>());
                    byDay.get(day).add(p);
                }

                // sort each day's periods by period id (or start time)
                for (List<PeriodModel> list : byDay.values()) {
                    list.sort((a, b) -> Integer.compare(a.getPeriodId(), b.getPeriodId()));
                }

                // build dayItems list (include all days; if you prefer to show only days with data, filter where list not empty)
                dayItems.clear();
                for (Map.Entry<String, List<PeriodModel>> e : byDay.entrySet()) {
                    String dayName = e.getKey();
                    List<PeriodModel> periods = e.getValue();
                    dayItems.add(new DayItem(dayName, periods));
                }

                dayAdapter.notifyDataSetChanged();

            } catch (Exception e) {
                Log.e(TAG, "Parsing error", e);
                if (isAdded()) Toast.makeText(requireContext(), "Invalid server response. Check Logcat.", Toast.LENGTH_LONG).show();
            }

        }, error -> {
            if (!isAdded()) { Log.w(TAG, "Detached - ignoring error"); return; }
            progress.setVisibility(View.GONE);
            String serverBody = null;
            if (error.networkResponse != null && error.networkResponse.data != null) {
                try {
                    serverBody = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers, "utf-8"));
                } catch (Exception ex) { serverBody = "could not decode body"; }
            }
            Log.e(TAG, "Network error: " + error.getMessage() + " body=" + serverBody);
            if (isAdded()) Toast.makeText(requireContext(), "Network error. Check Logcat.", Toast.LENGTH_LONG).show();
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> p = new LinkedHashMap<>();
                p.put("mobile", mobile);
                Log.d(TAG, "POST params: " + p);
                return p;
            }
        };

        req.setTag(REQ_TAG);
        requestQueue.add(req);
    }

    /**
     * Robust parser: supports array shapes and object wrappers like:
     * - [ [ {...}, ... ] ]
     * - [ {...}, ... ]
     * - { "success":..., "message": [...] }
     * - { "data": [...] }
     */
    private List<PeriodModel> parseResponse(String response) throws Exception {
        if (response == null) throw new Exception("Response null");
        response = response.trim();
        if (response.startsWith("<") || response.toLowerCase().contains("<html")) {
            throw new Exception("HTML returned");
        }

        List<PeriodModel> list = new ArrayList<>();
        try {
            JSONArray root = new JSONArray(response);
            if (root.length() > 0 && root.get(0) instanceof JSONArray) {
                JSONArray inner = root.getJSONArray(0);
                for (int i = 0; i < inner.length(); i++) list.add(jsonToPeriod(inner.getJSONObject(i)));
            } else {
                for (int i = 0; i < root.length(); i++) list.add(jsonToPeriod(root.getJSONObject(i)));
            }
            return list;
        } catch (Exception exArray) {
            JSONObject jobj = new JSONObject(response);

            // handle { success:..., message:... }
            if (jobj.has("message")) {
                Object msg = jobj.get("message");
                if (msg instanceof JSONArray) {
                    JSONArray arr = jobj.getJSONArray("message");
                    for (int i = 0; i < arr.length(); i++) list.add(jsonToPeriod(arr.getJSONObject(i)));
                    return list;
                } else if (msg instanceof JSONObject) {
                    list.add(jsonToPeriod((JSONObject) msg));
                    return list;
                } else if (msg instanceof String) {
                    Log.w(TAG, "Server message: " + msg);
                    return new ArrayList<>(); // no data
                }
            }

            // handle { data: [...] }
            if (jobj.has("data")) {
                Object d = jobj.get("data");
                if (d instanceof JSONArray) {
                    JSONArray arr = jobj.getJSONArray("data");
                    for (int i = 0; i < arr.length(); i++) list.add(jsonToPeriod(arr.getJSONObject(i)));
                    return list;
                } else if (d instanceof JSONObject) {
                    list.add(jsonToPeriod((JSONObject) d));
                    return list;
                }
            }

            // search for any JSONArray in top-level keys
            Iterator<String> keys = jobj.keys();
            while (keys.hasNext()) {
                String k = keys.next();
                Object v = jobj.opt(k);
                if (v instanceof JSONArray) {
                    JSONArray arr = jobj.getJSONArray(k);
                    for (int i = 0; i < arr.length(); i++) list.add(jsonToPeriod(arr.getJSONObject(i)));
                    return list;
                }
            }

            throw new Exception("Unrecognized JSON keys");
        }
    }

    private PeriodModel jsonToPeriod(JSONObject o) {
        int pid = o.optInt("peroid_id", o.optInt("period_id", 0));
        String day = o.optString("day", "");
        String subject = o.optString("subject", "");
        String teacher = o.optString("teacher_name", o.optString("teacher", ""));
        String start = o.optString("start_time", o.optString("start", ""));
        String end = o.optString("end_time", o.optString("end", ""));
        return new PeriodModel(pid, day, subject, teacher, start, end);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (requestQueue != null) requestQueue.cancelAll(REQ_TAG);
    }
}
