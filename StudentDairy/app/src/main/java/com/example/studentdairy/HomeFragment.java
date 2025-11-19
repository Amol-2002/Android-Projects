package com.example.studentdairy;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
        TextView parentNameTxt = view.findViewById(R.id.etParentName);

        SharedPreferences prefs = requireContext().getSharedPreferences("ParentProfile", getActivity().MODE_PRIVATE);
        String parentName = prefs.getString("parent_fullname", "Parent");

        parentNameTxt.setText(parentName);

        // Example cards & icons

      ImageView itemAttendance = view.findViewById(R.id.item_attendance);
      ImageView itemClassSchedule = view.findViewById(R.id.item_class_schedule);
      ImageView itemOnlineClass = view.findViewById(R.id.onlineClasses_icon);
      ImageView itemFeesPaid = view.findViewById(R.id.feespaid_icon);
        ImageView itemRegisterSubject = view.findViewById(R.id.registersubjects_icon);
        ImageView itemCertificateIcon = view.findViewById(R.id.Certificate_icon);
        ImageView itemCalenderIcon = view.findViewById(R.id.Calender_icon);
        ImageView itemItleIcon = view.findViewById(R.id.itle_icon);
        ImageView itemRailwayPass = view.findViewById(R.id.railway_icon);







        itemAttendance.setOnClickListener(v -> {
            Fragment parentProfileFragment = new ParentProfileFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, parentProfileFragment)
                    .addToBackStack(null)
                    .commit();
        });

        itemClassSchedule.setOnClickListener(v -> {
            Fragment studentProfileFragment  = new StudentProfileFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, studentProfileFragment)
                    .addToBackStack(null)
                    .commit();
        });

        itemOnlineClass.setOnClickListener(v -> {
            Fragment holidayFragment = new HolidayFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, holidayFragment)
                    .addToBackStack(null)
                    .commit();
        });


        itemFeesPaid.setOnClickListener(v -> {
            Fragment timeTableFragment  = new TimetableFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, timeTableFragment)
                    .addToBackStack(null)
                    .commit();
        });


        itemRegisterSubject.setOnClickListener(v -> {
            Fragment homeWorkFragment  = new HomeWorkFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, homeWorkFragment)
                    .addToBackStack(null)
                    .commit();
        });

        itemCertificateIcon.setOnClickListener(v -> {
            Fragment certificateFragment = new CertificateFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, certificateFragment)
                    .addToBackStack(null)
                    .commit();
        });

        itemCalenderIcon.setOnClickListener(v -> {
            Fragment calenderFragment = new CalenderFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, calenderFragment)
                    .addToBackStack(null)
                    .commit();
        });

        itemItleIcon.setOnClickListener(v -> {
            Fragment updatePasswordFragment = new UpdatePasswordFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, updatePasswordFragment)
                    .addToBackStack(null)
                    .commit();
        });

        itemRailwayPass.setOnClickListener(v -> {
            Fragment attendanceFragment = new AttendanceFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, attendanceFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
