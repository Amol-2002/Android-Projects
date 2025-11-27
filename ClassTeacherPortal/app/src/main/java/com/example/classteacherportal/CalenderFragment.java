package com.example.classteacherportal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

public class CalenderFragment extends Fragment {

    private CalendarView calendarView;
    private TextView tvSelectedDate;

    public CalenderFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calender, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);

        // Find the ImageView
        ImageView attarrow = view.findViewById(R.id.attarrow);

        // Click → Intent to MainActivity
        attarrow.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

        // ✅ Always open on today's date
        Calendar today = Calendar.getInstance();
        calendarView.setDate(today.getTimeInMillis(), true, true);

        // ✅ Show today's date by default
        String todayDate = today.get(Calendar.DAY_OF_MONTH) + "/" +
                (today.get(Calendar.MONTH) + 1) + "/" +
                today.get(Calendar.YEAR);
        tvSelectedDate.setText("Selected Date: " + todayDate);

        // ✅ Update when user selects another date
        calendarView.setOnDateChangeListener((calendarView, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            tvSelectedDate.setText("Selected Date: " + date);
        });

        return view;
    }
}