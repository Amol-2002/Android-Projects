package com.example.studentdairy;

import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

public class AttendanceFragment extends Fragment {

    EditText editTextFromDate, editTextToDate;
    Button btnFetchData;

    public AttendanceFragment() {
        // Required empty public constructor
    }

    public static AttendanceFragment newInstance(String param1, String param2) {
        AttendanceFragment fragment = new AttendanceFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_attendance, container, false);

        // Initialize views
        editTextFromDate = view.findViewById(R.id.editTextFromDate);
        editTextToDate = view.findViewById(R.id.editTextToDate);
        btnFetchData = view.findViewById(R.id.btnFetchData);

        // Show calendar when clicking From Date
        editTextFromDate.setOnClickListener(v -> showDatePicker(editTextFromDate));

        // Show calendar when clicking To Date
        editTextToDate.setOnClickListener(v -> showDatePicker(editTextToDate));

        // Button click
        btnFetchData.setOnClickListener(v -> {
            String fromDate = editTextFromDate.getText().toString();
            String toDate = editTextToDate.getText().toString();

            if (fromDate.isEmpty() || toDate.isEmpty()) {
                Toast.makeText(getContext(), "Please select both dates", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(),
                        "Fetching data from " + fromDate + " to " + toDate,
                        Toast.LENGTH_LONG).show();
                // TODO: Call your API here
            }
        });

        return view;
    }

    // Function to show calendar
    private void showDatePicker(EditText target) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog picker = new DatePickerDialog(
                getContext(),
                (view, y, m, d) -> {
                    String date = String.format("%02d/%02d/%04d", d, (m + 1), y);
                    target.setText(date);
                },
                year, month, day
        );
        picker.show();
    }
}
