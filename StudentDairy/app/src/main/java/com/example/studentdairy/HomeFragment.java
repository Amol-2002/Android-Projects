package com.example.studentdairy;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Example cards & icons

      ImageView itemAttendance = view.findViewById(R.id.item_attendance);
      ImageView itemClassSchedule = view.findViewById(R.id.item_class_schedule);



        itemAttendance.setOnClickListener(v -> {
            Fragment attendanceFragment = new AttendanceFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, attendanceFragment)
                    .addToBackStack(null)
                    .commit();
        });

        itemClassSchedule.setOnClickListener(v -> {
            Fragment classScheduleFragment = new ClassScheduleFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, classScheduleFragment)
                    .addToBackStack(null)
                    .commit();
        });




        return view;
    }
}
