package com.example.studentdairy;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ClassScheduleFragment extends Fragment {

    public ClassScheduleFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_class_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BottomNavigationView bottomNav = view.findViewById(R.id.bottom_navigation);


        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;

            int id = item.getItemId();
            if (id == R.id.nav_mon) {
                selected = new MondayFragment();
            } else if (id == R.id.nav_tue) {
                selected = new MondayFragment();
            } else if (id == R.id.nav_wed) {
                selected = new MondayFragment();
            } else if (id == R.id.nav_thu) {
                selected = new MondayFragment();
            } else if (id == R.id.nav_fri) {
                selected = new MondayFragment();
            } else if (id == R.id.nav_sat) {
                selected = new MondayFragment();
            }


            return true;
        });

    }


}
