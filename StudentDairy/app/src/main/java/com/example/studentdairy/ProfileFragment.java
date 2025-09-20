package com.example.studentdairy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    private TextView txtMobile, txtName, txtStudentId, txtClassSection;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // UI init
        txtMobile = view.findViewById(R.id.txtMobile);
        txtName = view.findViewById(R.id.textView6);
        txtStudentId = view.findViewById(R.id.textView2);
        txtClassSection = view.findViewById(R.id.textView8);

        // 🔹 Find back arrow
        ImageView backArrow = view.findViewById(R.id.profilearrow);
        backArrow.setOnClickListener(v -> {
            Fragment attendanceFragment = new AttendanceFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, attendanceFragment)
                    .addToBackStack(null)
                    .commit();

            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.fragment_container);
        });

        // 🔹 Load profile from SharedPreferences
        loadProfileFromSession();

        return view;
    }

    private void loadProfileFromSession() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyPref", MODE_PRIVATE);
        String mobile = prefs.getString("userid", "N/A");
        String name = prefs.getString("username", "N/A");
        String studentId = prefs.getString("student_id", "N/A"); // optional
        String classSection = prefs.getString("class_section", "N/A"); // optional

        txtMobile.setText("Mobile: " + mobile);
        txtName.setText("Name: " + name);
        txtStudentId.setText("Student ID: " + studentId);
        txtClassSection.setText("Class/Section: " + classSection);
    }
}
