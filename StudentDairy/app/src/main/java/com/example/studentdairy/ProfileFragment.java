package com.example.studentdairy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Find the ImageView
        ImageView attarrow = view.findViewById(R.id.attarrow);

        // Click → Intent to MainActivity
        attarrow.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

        // ================= Parent Profile Card =================
        LinearLayout parentProfileCard = view.findViewById(R.id.card_personal_details);
        parentProfileCard.setOnClickListener(v -> {

            // Open ParentProfileFragment inside same Activity
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ParentProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // ================= Student Profile Card =================
        LinearLayout StudentProfileCard = view.findViewById(R.id.card_Student_profile);
        StudentProfileCard.setOnClickListener(v -> {

            // Open ParentProfileFragment inside same Activity
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new StudentProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // ================= Time Table Card =================//
        LinearLayout TimeTableCard = view.findViewById(R.id.card_Timetable);
        TimeTableCard.setOnClickListener(v -> {

            // Open ParentProfileFragment inside same Activity
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new TimeTableFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // ================= Holiday Card =================//
        LinearLayout HolidayCard = view.findViewById(R.id.card_holiday);
        HolidayCard.setOnClickListener(v -> {

            // Open ParentProfileFragment inside same Activity
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HolidayFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // ================= privacy policy  Card =================//
        LinearLayout privacypolicyCard= view.findViewById(R.id.card_privacy_policy);
        privacypolicyCard.setOnClickListener(v -> {

            // Open ParentProfileFragment inside same Activity
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new PrivacyPolicyFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // ================= support  Card =================//
        LinearLayout supportCard= view.findViewById(R.id.card_support);
        supportCard.setOnClickListener(v -> {

            // Open ParentProfileFragment inside same Activity
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new SupportFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // ================= change Password  Card =================//
        LinearLayout changepasswordCard= view.findViewById(R.id.card_change_password);
        changepasswordCard.setOnClickListener(v -> {

            // Open ParentProfileFragment inside same Activity
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new UpdatePasswordFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // ================= Shapre App Card =================//
        LinearLayout ShareAppCard= view.findViewById(R.id.card_share_app);
        ShareAppCard.setOnClickListener(v -> {

            // Open ParentProfileFragment inside same Activity
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ShareAppFragment())
                    .addToBackStack(null)
                    .commit();
        });

//         ================= log out Card =================//
//        LinearLayout logoutCard= view.findViewById(R.id.card_logout);
//        logoutCard.setOnClickListener(v -> {
//
//            // Open ParentProfileFragment inside same Activity
//            requireActivity()
//                    .getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, new LoginActivity())
//                    .addToBackStack(null)
//                    .commit();
//        });


        // ================= Log Out Card =================
        LinearLayout logoutCard = view.findViewById(R.id.card_logout);
        logoutCard.setOnClickListener(v -> {

            // Clear Student login session
            SharedPreferences studentPrefs = requireActivity()
                    .getSharedPreferences("StudentProfile", requireActivity().MODE_PRIVATE);
            studentPrefs.edit().clear().apply();

            // Clear Parent profile session
            SharedPreferences parentPrefs = requireActivity()
                    .getSharedPreferences("ParentProfile", requireActivity().MODE_PRIVATE);
            parentPrefs.edit().clear().apply();

            // Clear timetable/homework session
            SharedPreferences timePrefs = requireActivity()
                    .getSharedPreferences("Homework", requireActivity().MODE_PRIVATE);
            timePrefs.edit().clear().apply();

            // Open LoginActivity
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Close current activity
            requireActivity().finish();
        });


        return view;
    }
}
