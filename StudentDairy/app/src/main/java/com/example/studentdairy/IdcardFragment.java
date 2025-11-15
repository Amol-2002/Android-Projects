package com.example.studentdairy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import static android.content.Context.MODE_PRIVATE;

public class IdcardFragment extends Fragment {

    private TextView tvMobileNumber, tvPermanentAddress, tvClassSection,
            tvRollNo, tvGender, tvName;
    private ImageView imgProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_idcard, container, false);

        tvName = view.findViewById(R.id.tvName);
        tvRollNo = view.findViewById(R.id.tvRollNo);
        tvClassSection = view.findViewById(R.id.tvClassSection);
        tvGender = view.findViewById(R.id.tvGender);
        tvMobileNumber = view.findViewById(R.id.tvMobileNumber);
        tvPermanentAddress = view.findViewById(R.id.tvPermanentAddress);
        imgProfile = view.findViewById(R.id.imgProfile);

        SharedPreferences prefs = requireActivity().getSharedPreferences("StudentProfile", MODE_PRIVATE);

        // ---- FIXED KEYS ----
        String first = prefs.getString("first_name", "");
        String middle = prefs.getString("middle_name", "");
        String last = prefs.getString("last_name", "");
        tvName.setText(first + " " + middle + " " + last);

        tvRollNo.setText(prefs.getString("roll_no", ""));
        tvClassSection.setText(prefs.getString("class_id", ""));   // FIXED
        tvGender.setText(prefs.getString("gender", ""));
        tvMobileNumber.setText(prefs.getString("mobile", ""));     // WORKS
        tvPermanentAddress.setText(prefs.getString("permanent_address", ""));

        String photo = prefs.getString("photo", "");
        if (!photo.isEmpty()) {
            Glide.with(getContext()).load(photo).into(imgProfile);
        }

        ImageView backArrow = view.findViewById(R.id.profilearrow);
        backArrow.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new MessageFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
