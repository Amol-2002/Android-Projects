package com.example.classteacherportal;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class TimetableFragment extends Fragment {

    private final String[] DAYS = new String[]{"Mon","Tue","Wed","Thu","Fri","Sat"};

    private LinearLayout daysContainer;
    private View selectedPill;
    private FrameLayout selectorFrame;
    private TableLayout tableTimetable;
    private LinearLayout emptyBox;
    private ImageView emptyImage;
    private TextView emptyText;
    private ImageView attarrow;

    private RequestQueue requestQueue;
    private Map<String, List<Period>> mapByDay = new HashMap<>();

    private static final String TIMETABLE_URL = "https://testing.trifrnd.net.in/ishwar/school/api/class_timetable_api.php";
    private static final String REQ_TAG = "timetable_req";
    private static final String CACHE_PREF = "TimetableCache";
    private static final String CACHE_KEY = "json";

    private ProgressBar progressBar;
    private boolean isFetching = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_timetable, container, false);

        daysContainer = view.findViewById(R.id.days_container);
        selectedPill = view.findViewById(R.id.selected_pill);
        selectorFrame = view.findViewById(R.id.selector_frame);
        tableTimetable = view.findViewById(R.id.tableTimetable);
        emptyBox = view.findViewById(R.id.emptyBox);
        emptyImage = view.findViewById(R.id.emptyImage);
        emptyText = view.findViewById(R.id.emptyText);
        attarrow = view.findViewById(R.id.attarrow);
        if (attarrow != null) attarrow.setOnClickListener(v -> requireActivity().onBackPressed());

        requestQueue = Volley.newRequestQueue(requireContext());

        // create a small programmatic progress indicator (centered inside selectorFrame)
        progressBar = new ProgressBar(requireContext());
        FrameLayout.LayoutParams pbLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pbLp.gravity = Gravity.CENTER;
        progressBar.setVisibility(View.GONE);
        if (selectorFrame != null) selectorFrame.addView(progressBar, pbLp);

        // create day labels
        for (int i = 0; i < DAYS.length; i++) {
            final int idx = i;
            TextView tv = new TextView(requireContext());
            tv.setText(DAYS[i]);
            tv.setTextSize(14);
            tv.setTextColor(0xFFFFFFFF);
            tv.setPadding(dp(18), dp(8), dp(18), dp(8));
            tv.setGravity(Gravity.CENTER);
            tv.setTag(i);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            tv.setLayoutParams(lp);

            tv.setOnClickListener(v -> onDaySelected(idx));
            daysContainer.addView(tv);
        }

        // show cached first if present
        String cached = requireActivity().getSharedPreferences(CACHE_PREF, Context.MODE_PRIVATE).getString(CACHE_KEY, null);
        if (cached != null && !cached.isEmpty()) {
            parseAndPopulateFromJson(cached, /*saveToCache=*/ false);
        }

        // After layout ready, fetch data and select today's
        daysContainer.post(this::fetchTimetableFromApi);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // cancel outstanding requests for this fragment to avoid leaks/crashes
        if (requestQueue != null) requestQueue.cancelAll(REQ_TAG);
    }

    private void fetchTimetableFromApi() {
        if (isFetching) return;
        isFetching = true;
        progressBar.setVisibility(View.VISIBLE);

        // read mobile from prefs - consistent source
        SharedPreferences prefs = requireActivity().getSharedPreferences("StudentProfile", Context.MODE_PRIVATE);
        String staff_id = prefs.getString("staff_id", prefs.getString("userid", ""));
        if (staff_id == null) staff_id = "";

        StringRequest req = new StringRequest(Request.Method.POST, TIMETABLE_URL,
                response -> {
                    isFetching = false;
                    progressBar.setVisibility(View.GONE);
                    try {
                        // Save to cache
                        requireActivity().getSharedPreferences(CACHE_PREF, Context.MODE_PRIVATE)
                                .edit().putString(CACHE_KEY, response).apply();

                        parseAndPopulateFromJson(response, /*saveToCache=*/ false);
                    } catch (Exception e) {
                        e.printStackTrace();
                        // keep previously shown cache if parse fails
                        showEmptyForToday();
                    }
                },
                error -> {
                    isFetching = false;
                    progressBar.setVisibility(View.GONE);
                    error.printStackTrace();
                    Toast.makeText(requireContext(), "Failed to load timetable", Toast.LENGTH_SHORT).show();
                    showEmptyForToday();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // consistent source for mobile param
                SharedPreferences prefsLocal = requireActivity().getSharedPreferences("StudentProfile", Context.MODE_PRIVATE);
                String m = prefsLocal.getString("staff_id", prefsLocal.getString("userid", ""));
                if (m == null) m = "";
                params.put("staff_id", m);
                return params;
            }
        };

        // small retry policy (optional) to be resilient on flaky mobile networks
        req.setRetryPolicy(new DefaultRetryPolicy(8000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        req.setTag(REQ_TAG);
        requestQueue.add(req);
    }

    private void parseAndPopulateFromJson(String response, boolean saveToCache) {
        try {
            JSONArray outer = new JSONArray(response);
            mapByDay.clear();
            // API shape: outer array with JSON array at index 0 (as original code)
            if (outer.length() > 0) {
                JSONArray arr = outer.getJSONArray(0);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    String day = obj.optString("day", "").trim();
                    String subj = obj.optString("subject", "-");
                    String teacher = obj.optString("teacher_name", "-");
                    String start = obj.optString("start_time", "");
                    String end = obj.optString("end_time", "");
                    int perId = obj.optInt("peroid_id", 0);

                    String shortDay = toShortDay(day);
                    Period p = new Period(perId, shortDay, subj, teacher, start, end);

                    if (!mapByDay.containsKey(shortDay)) mapByDay.put(shortDay, new ArrayList<>());
                    mapByDay.get(shortDay).add(p);
                }

                // sort each day by period id
                for (List<Period> list : mapByDay.values()) {
                    Collections.sort(list, Comparator.comparingInt(o -> o.peroidId));
                }
            }

            // select today's day by default (safe bounds)
            int todayIndex = indexForToday();
            if (todayIndex < 0 || todayIndex >= DAYS.length) todayIndex = 0;
            animatePillTo(todayIndex);
            populateTableForDay(DAYS[todayIndex]);

            // optionally save to cache (already saved earlier)
            if (saveToCache) {
                requireActivity().getSharedPreferences(CACHE_PREF, Context.MODE_PRIVATE)
                        .edit().putString(CACHE_KEY, response).apply();
            }

        } catch (Exception e) {
            e.printStackTrace();
            // parsing failed -> keep prior cache view if any
            showEmptyForToday();
        }
    }

    private void showEmptyForToday() {
        int todayIndex = indexForToday();
        if (todayIndex < 0 || todayIndex >= DAYS.length) todayIndex = 0;
        animatePillTo(todayIndex);
        populateTableForDay(DAYS[todayIndex]);
    }

    private void onDaySelected(int index) {
        // animate pill and populate
        animatePillTo(index);
        populateTableForDay(DAYS[index]);
    }

    /** animate pill translationX and width for nice UX */
    private void animatePillTo(final int index) {
        // set label colors immediately
        for (int i = 0; i < daysContainer.getChildCount(); i++) {
            TextView t = (TextView) daysContainer.getChildAt(i);
            if (i == index) t.setTextColor(0xFF1E4F7A); else t.setTextColor(0xFFFFFFFF);
        }

        final View child = daysContainer.getChildAt(index);
        if (child == null || selectedPill == null) return;

        // ensure measured values available
        child.post(() -> {
            int childLeft = child.getLeft();
            int childWidth = child.getWidth();
            if (childWidth <= 0) childWidth = child.getMeasuredWidth();
            final int pillTargetWidth = Math.max(dp(48), childWidth);
            final int targetX = Math.max(0, childLeft + (childWidth - pillTargetWidth) / 2);

            // animate translationX
            float currentX = selectedPill.getTranslationX();
            ObjectAnimator trans = ObjectAnimator.ofFloat(selectedPill, "translationX", currentX, targetX);
            trans.setInterpolator(new DecelerateInterpolator());
            trans.setDuration(260);

            // animate width via ValueAnimator (update LayoutParams)
            final ViewGroup.LayoutParams lp = selectedPill.getLayoutParams();
            final int startW = (lp == null || lp.width <= 0) ? selectedPill.getMeasuredWidth() : lp.width;
            ValueAnimator widthAnim = ValueAnimator.ofInt(startW, pillTargetWidth);
            widthAnim.setDuration(260);
            widthAnim.setInterpolator(new DecelerateInterpolator());
            widthAnim.addUpdateListener(animation -> {
                int val = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams newLp = selectedPill.getLayoutParams();
                if (newLp == null) newLp = new FrameLayout.LayoutParams(val, ViewGroup.LayoutParams.WRAP_CONTENT);
                newLp.width = val;
                selectedPill.setLayoutParams(newLp);
            });

            trans.start();
            widthAnim.start();

            // scroll to center the chosen item
            View parent = (View) daysContainer.getParent();
            if (parent instanceof HorizontalScrollView) {
                int scrollTo = childLeft - (((HorizontalScrollView) parent).getWidth() / 2) + (childWidth / 2);
                if (scrollTo < 0) scrollTo = 0;
                ((HorizontalScrollView) parent).smoothScrollTo(scrollTo, 0);
            }
        });
    }

    private void populateTableForDay(String dayKey) {
        if (tableTimetable == null || emptyBox == null) return;
        tableTimetable.removeAllViews();

        // add header row
        TableRow header = new TableRow(requireContext());
        header.setPadding(dp(8), dp(8), dp(8), dp(8));

        TextView th1 = headerCell("Sr", true);
        TextView th2 = headerCell("Time", true);
        TextView th3 = headerCell("Subject", true);
        TextView th4 = headerCell("Teacher", true);

        header.addView(th1);
        header.addView(th2);
        header.addView(th3);
        header.addView(th4);

        tableTimetable.addView(header);

        List<Period> list = mapByDay.get(dayKey);
        if (list == null || list.isEmpty()) {
            // show empty box
            tableTimetable.setVisibility(View.GONE);
            emptyBox.setVisibility(View.VISIBLE);
            emptyText.setText("No schedule for " + dayKey);
            return;
        } else {
            tableTimetable.setVisibility(View.VISIBLE);
            emptyBox.setVisibility(View.GONE);
        }

        // add rows
        for (int i = 0; i < list.size(); i++) {
            Period p = list.get(i);
            TableRow row = new TableRow(requireContext());
            row.setPadding(dp(8), dp(10), dp(8), dp(10));
            if (i % 2 == 1) row.setBackgroundColor(0x11FFFFFF); // subtle striping

            TextView c1 = cell(String.valueOf(i+1), false);
            TextView c2 = cell(p.start + " - " + p.end, false);
            TextView c3 = cell(p.subject, false);
            TextView c4 = cell(p.teacher, false);
            // Make teacher italic
            c4.setTypeface(null, Typeface.ITALIC);

            row.addView(c1);
            row.addView(c2);
            row.addView(c3);
            row.addView(c4);

            tableTimetable.addView(row);
        }
    }

    private TextView headerCell(String txt, boolean bold) {
        TextView tv = new TextView(requireContext());
        tv.setText(txt);
        tv.setPadding(dp(8), dp(4), dp(8), dp(4));
        tv.setTypeface(null, bold ? Typeface.BOLD : Typeface.NORMAL);
        tv.setTextColor(0xFF000000);
        tv.setGravity(Gravity.CENTER_VERTICAL);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tv.setLayoutParams(lp);
        return tv;
    }

    private TextView cell(String txt, boolean bold) {
        TextView tv = new TextView(requireContext());
        tv.setText(txt);
        tv.setPadding(dp(8), dp(6), dp(8), dp(6));
        tv.setTextColor(0xFF111111);
        tv.setGravity(Gravity.CENTER_VERTICAL);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tv.setLayoutParams(lp);
        if (bold) tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }

    private int indexForToday() {
        Calendar c = Calendar.getInstance();
        int w = c.get(Calendar.DAY_OF_WEEK);
        switch (w) {
            case Calendar.MONDAY: return 0;
            case Calendar.TUESDAY: return 1;
            case Calendar.WEDNESDAY: return 2;
            case Calendar.THURSDAY: return 3;
            case Calendar.FRIDAY: return 4;
            case Calendar.SATURDAY: return 5;
            default: return 0; // Sunday -> default to Monday (index 0)
        }
    }

    private String toShortDay(String fullDay) {
        if (fullDay == null) return "";
        fullDay = fullDay.trim().toLowerCase(Locale.ROOT);
        if (fullDay.startsWith("mon")) return "Mon";
        if (fullDay.startsWith("tue")) return "Tue";
        if (fullDay.startsWith("wed")) return "Wed";
        if (fullDay.startsWith("thu")) return "Thu";
        if (fullDay.startsWith("fri")) return "Fri";
        if (fullDay.startsWith("sat")) return "Sat";
        return fullDay;
    }

    private int dp(int v) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(v * density);
    }

    // small POJO
    static class Period {
        int peroidId;
        String day, subject, teacher, start, end;
        Period(int id, String d, String s, String t, String st, String en) {
            this.peroidId = id; this.day = d; this.subject = s; this.teacher = t; this.start = st; this.end = en;
        }
    }
}
