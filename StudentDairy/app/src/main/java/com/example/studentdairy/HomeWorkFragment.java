package com.example.studentdairy;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HomeWorkFragment extends Fragment {

    TableLayout tableLayout;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_work, container, false);

        // Find the ImageView
        ImageView attarrow = view.findViewById(R.id.attarrow);

        // Click → Intent to MainActivity
        attarrow.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

        tableLayout = view.findViewById(R.id.tableLayout);

        // ✅ Initialize SharedPreferences properly (inside onCreateView)
        prefs = requireContext().getSharedPreferences("Homework", Activity.MODE_PRIVATE);

        // ✅ Load data
        loadHomeworkData();

        return view;
    }

    private void loadHomeworkData() {
        String url = "https://testing.trifrnd.net.in/ishwar/school/api/homework_api.php";

        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);

                if (jsonObject.has("data")) {
                    JSONArray dataArray = jsonObject.getJSONArray("data");

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject item = dataArray.getJSONObject(i);

                        String subject = item.getString("subject");
                        String date = item.getString("hw_date");
                        String fileUrl = item.getString("file_name");

                        addRow(i + 1, subject, date, fileUrl);
                    }
                } else {
                    Toast.makeText(getContext(), "No homework data found.", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show();
            }
        }, error -> Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show()) {

            // ✅ Send mobile from SharedPreferences
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                String mobile = prefs.getString("mobile", "");
                if (mobile.isEmpty()) {
                    Toast.makeText(getContext(), "No mobile number found in SharedPreferences", Toast.LENGTH_SHORT).show();
                }

                params.put("mobile", mobile);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(request);
    }

    private void addRow(int srNo, String subject, String date, String fileUrl) {
        TableRow row = new TableRow(getContext());
        row.setPadding(8, 8, 8, 8);

        // Serial Number
        TextView srView = new TextView(getContext());
        srView.setText(String.valueOf(srNo));
        srView.setPadding(8, 8, 8, 8);
        srView.setGravity(android.view.Gravity.CENTER);

        // Subject
        TextView subjectView = new TextView(getContext());
        subjectView.setText(subject);
        subjectView.setPadding(8, 8, 8, 8);
        subjectView.setGravity(android.view.Gravity.CENTER);

        // Date
        TextView dateView = new TextView(getContext());
        dateView.setText(date);
        dateView.setPadding(8, 8, 8, 8);
        dateView.setGravity(android.view.Gravity.CENTER);

        // File Button
        Button fileButton = new Button(getContext());
        fileButton.setText("View");
        fileButton.setPadding(8, 8, 8, 8);
        fileButton.setOnClickListener(v -> {
            String fullUrl = fileUrl.startsWith("http") ?
                    fileUrl : "https://testing.trifrnd.net.in/ishwar/school/" + fileUrl;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl));
            startActivity(intent);
        });

        row.addView(srView);
        row.addView(subjectView);
        row.addView(dateView);
        row.addView(fileButton);

        tableLayout.addView(row);
    }
}


//package com.example.studentdairy;
//
//import android.annotation.SuppressLint;
//import android.os.Bundle;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.google.android.material.tabs.TabLayout;
//
//public class HomeWorkFragment extends Fragment {
//    private TabLayout tabLayout;
//    public HomeWorkFragment() {}
//
//    @SuppressLint("MissingInflatedId")
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_home_work, container, false);
//
////        spinnerCourse = view.findViewById(R.id.spinnerCourse);
//        tabLayout = view.findViewById(R.id.tabLayout);
//
//
//        // Add tabs
//        tabLayout.addTab(tabLayout.newTab().setText("Mon"));
//        tabLayout.addTab(tabLayout.newTab().setText("Tue"));
//        tabLayout.addTab(tabLayout.newTab().setText("Wen"));
//        tabLayout.addTab(tabLayout.newTab().setText("Thu"));
//        tabLayout.addTab(tabLayout.newTab().setText("Fri"));
//        tabLayout.addTab(tabLayout.newTab().setText("Sat"));
//
//        return view;
//    }
//
//    }
//


