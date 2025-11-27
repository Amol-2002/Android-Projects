package com.example.classteacherportal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StudentFragment extends Fragment {

    private static final String URL = "https://testing.trifrnd.net.in/ishwar/school/api/class_std_api.php";
    private static final String REQ_TAG = "StudentListReq";

    private RecyclerView recyclerStudents;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private SearchView searchView;
    private ImageView ivBack;

    private StudentAdapter adapter;
    private ArrayList<StudentModel> list = new ArrayList<>();
    private RequestQueue requestQueue;
    private String staffId = "";

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_student, container, false);

        // views
        recyclerStudents = view.findViewById(R.id.recyclerStudents);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        searchView = view.findViewById(R.id.searchView);
        ivBack = view.findViewById(R.id.ivBack);




        // RecyclerView + adapter
        recyclerStudents.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new StudentAdapter(requireContext(), new ArrayList<>()); // start empty
        recyclerStudents.setAdapter(adapter);

        // Volley queue
        requestQueue = Volley.newRequestQueue(requireContext());

        // Back button
        ivBack.setOnClickListener(v -> {
            // prefer NavController if you use Navigation component:
            // NavHostFragment.findNavController(this).navigateUp();
            requireActivity().onBackPressed();
        });

        // Load staff_id from SharedPreferences (StudentProfile / userid)
        SharedPreferences sp = requireActivity().getSharedPreferences("StudentProfile", requireActivity().MODE_PRIVATE);
        staffId = sp.getString("userid", "");

        // Configure SearchView - real-time filtering by name or roll no
        setupSearchView();

        if (staffId == null || staffId.isEmpty()) {
            Toast.makeText(requireContext(), "Staff ID missing! Please login again.", Toast.LENGTH_LONG).show();
            showEmptyState(true, "Please login");
        } else {
            fetchStudents(staffId);
        }

        return view;
    }

    private void setupSearchView() {
        if (searchView == null) return;

        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Search by name or roll no");
        // optional: remove focus on start
        searchView.clearFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // run filter on submit as well
                adapter.getFilter().filter(query);
                searchView.clearFocus(); // hide keyboard
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        // Optional: handle close (X) pressed to reset list
        searchView.setOnCloseListener(() -> {
            adapter.getFilter().filter("");
            return false;
        });
    }

    private void fetchStudents(String staff_id) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        showEmptyState(false, null);
        list.clear();

        StringRequest req = new StringRequest(Request.Method.POST, URL,
                response -> {
                    try {
                        // expected server response: [[{...}, {...}]]
                        JSONArray outer = new JSONArray(response);
                        JSONArray arr = outer.getJSONArray(0);

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);

                            StudentModel m = new StudentModel();
                            m.setStudent_id(obj.optString("student_id", ""));
                            m.setStudent_name(obj.optString("student_name", ""));
                            m.setClass_id(obj.optString("class_id", ""));
                            m.setSection_id(obj.optString("section_id", ""));
                            m.setRoll_no(obj.optString("roll_no", ""));
                            m.setGender(obj.optString("gender", ""));

                            list.add(m);
                        }

                        // update adapter via helper method (keeps internal full list)
                        adapter.updateData(list);

                        if (progressBar != null) progressBar.setVisibility(View.GONE);

                        if (list.isEmpty()) {
                            showEmptyState(true, "No students found");
                        } else {
                            showEmptyState(false, null);
                        }

                    } catch (Exception e) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        showEmptyState(true, "Response parse error");
                        Toast.makeText(requireContext(), "Parse Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    showEmptyState(true, "Network error");
                    String msg = error.getMessage() == null ? "Network error" : error.getMessage();
                    Toast.makeText(requireContext(), "API Error: " + msg, Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("staff_id", staff_id);
                return map;
            }
        };

        req.setTag(REQ_TAG);
        requestQueue.add(req);
    }

    private void showEmptyState(boolean show, @Nullable String message) {
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
            if (message != null) tvEmptyState.setText(message);
        }
        if (recyclerStudents != null) recyclerStudents.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (requestQueue != null) {
            requestQueue.cancelAll(REQ_TAG);
            requestQueue = null;
        }
    }
}
