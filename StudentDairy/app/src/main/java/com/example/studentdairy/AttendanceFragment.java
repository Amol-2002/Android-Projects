package com.example.studentdairy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
public class AttendanceFragment extends Fragment {

    private static final String ATTEND_URL = "https://trifrnd.co.in/school/api/data.php?apicall=attend";
    RecyclerView recyclerAttendance;
    List<AttendanceModel> attendanceList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_attendance, container, false);

        // Find the ImageView
        ImageView attarrow = view.findViewById(R.id.attarrow);

        // Click → Intent to MainActivity
        attarrow.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

        recyclerAttendance = view.findViewById(R.id.recyclerAttendance);
        recyclerAttendance.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedPreferences prefs = requireActivity().getSharedPreferences("StudentProfile", getContext().MODE_PRIVATE);
        String classId = prefs.getString("class_id", "1");
        String sectionId = prefs.getString("section_id", "A");
        String studentId = prefs.getString("student_id", "101");

        loadAttendance(classId, sectionId, studentId);

        return view;
    }


    private void loadAttendance(String classId, String sectionId, String studentId) {
        StringRequest request = new StringRequest(Request.Method.POST, ATTEND_URL,
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        attendanceList.clear();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String dateRaw = obj.getString("a_date");
                            String status = obj.getString("status");

                            // Format date to readable
                            SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                            SimpleDateFormat newFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                            String dateFormatted = newFormat.format(oldFormat.parse(dateRaw));

                            attendanceList.add(new AttendanceModel(dateFormatted, status));
                        }

                        recyclerAttendance.setAdapter(new AttendanceAdapter(attendanceList));

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Server Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
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

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(request);
    }
}
