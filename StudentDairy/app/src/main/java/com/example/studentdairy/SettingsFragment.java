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

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // ================= Back Arrow =================
        ImageView backArrow = view.findViewById(R.id.setarrow);
        backArrow.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // ================= Disable Other Cards =================
        int[] disabledCardIds = {
                R.id.card_personal_details,
                R.id.card_contact_details,
                R.id.card_postal_details,
                R.id.card_about_us,
                R.id.card_privacy_policy,
                R.id.card_support,
                R.id.card_change_password,
                R.id.card_share_app,
                R.id.card_rate_app
        };

        for (int id : disabledCardIds) {
            LinearLayout card = view.findViewById(id);
            card.setOnClickListener(v -> {
                // Optional: show a Toast like "Coming Soon"
            });
        }

        // ================= Log Out Card =================
        LinearLayout logoutCard = view.findViewById(R.id.card_logout);
        logoutCard.setOnClickListener(v -> {
            // 1️⃣ Remove saved userid to log out
            SharedPreferences prefs = requireActivity().getSharedPreferences("MyPref", getActivity().MODE_PRIVATE);
            prefs.edit().remove("userid").apply(); // important

            // 2️⃣ Start LoginActivity and clear back stack
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // 3️⃣ Finish current activity
            requireActivity().finish();
        });

        return view;
    }
}
