package com.example.classteacherportal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * StudentAdapter with Filterable implementation.
 */
public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> implements Filterable {

    private final Context context;
    private final ArrayList<StudentModel> filteredList;
    private final ArrayList<StudentModel> fullList; // original copy for filtering

    public StudentAdapter(Context context, ArrayList<StudentModel> list) {
        this.context = context;
        this.filteredList = new ArrayList<>(list);
        this.fullList = new ArrayList<>(list);
    }

    // Call this to update adapter data (from fragment)
    public void updateData(ArrayList<StudentModel> newList) {
        fullList.clear();
        fullList.addAll(newList);

        filteredList.clear();
        filteredList.addAll(newList);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_student, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentAdapter.ViewHolder h, int pos) {
        StudentModel m = filteredList.get(pos);
        h.tvStudentName.setText(m.getStudent_name());
        h.tvRoll.setText("Roll No: " + m.getRoll_no());
        h.tvGender.setText(m.getGender());
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {
        return studentFilter;
    }

    private final Filter studentFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String query = (constraint == null) ? "" : constraint.toString().trim().toLowerCase();
            FilterResults results = new FilterResults();
            ArrayList<StudentModel> filtered = new ArrayList<>();

            if (query.isEmpty()) {
                filtered.addAll(fullList);
            } else {
                for (StudentModel s : fullList) {
                    String name = s.getStudent_name() == null ? "" : s.getStudent_name().toLowerCase();
                    String roll = s.getRoll_no() == null ? "" : s.getRoll_no().toLowerCase();

                    if (name.contains(query) || roll.contains(query)) {
                        filtered.add(s);
                    }
                }
            }

            results.values = filtered;
            results.count = filtered.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList.clear();
            //noinspection unchecked
            filteredList.addAll((List<StudentModel>) results.values);
            notifyDataSetChanged();
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvRoll, tvGender;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvRoll = itemView.findViewById(R.id.tvRoll);
            tvGender = itemView.findViewById(R.id.tvGender);
        }
    }
}
