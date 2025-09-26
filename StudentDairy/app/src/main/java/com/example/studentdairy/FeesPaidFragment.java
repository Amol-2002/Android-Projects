package com.example.studentdairy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;

public class FeesPaidFragment extends Fragment {

    private Spinner spinnerCourse;
    private TabLayout tabLayout;
    private TextView tvNoData;

    public FeesPaidFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fees_paid, container, false);

        spinnerCourse = view.findViewById(R.id.spinnerCourse);
        tabLayout = view.findViewById(R.id.tabLayout);
        tvNoData = view.findViewById(R.id.tvNoData);

        // Spinner items
        String[] courses = {"ELECTRONICS ENGINEERING - 8", "COMPUTER ENGINEERING - 6", "IT ENGINEERING - 4"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, courses);
        spinnerCourse.setAdapter(adapter);

        // Add tabs
        tabLayout.addTab(tabLayout.newTab().setText("ACADEMIC FEES"));
        tabLayout.addTab(tabLayout.newTab().setText("SCHOLARSHIP"));
        tabLayout.addTab(tabLayout.newTab().setText("OTHER FEES"));

        // Default - show "not found"
        tvNoData.setText("Fees paid details not found");

        return view;
    }
}
