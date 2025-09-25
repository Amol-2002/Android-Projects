package com.example.studentdairy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import static android.content.Context.MODE_PRIVATE;

public class IdcardFragment extends Fragment {

    private TextView txtMobile, txtName, txtStudentId, txtClassSection;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_idcard, container, false);


        // 🔹 Handle back arrow
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
