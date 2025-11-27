package com.example.classteacherportal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentAttendanceAdapter extends RecyclerView.Adapter<StudentAttendanceAdapter.AttViewHolder> {

    public static class StudentItem {
        public String studentId;
        public String studentName;
        public String status; // "P" or "A"

        public StudentItem(String studentId, String studentName) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.status = "P"; // default Present
        }
    }

    private Context context;
    private List<StudentItem> items;

    public StudentAttendanceAdapter(Context context, List<StudentItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public AttViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.row_student_attendance, parent, false);
        return new AttViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AttViewHolder holder, int position) {
        StudentItem it = items.get(position);
        holder.tvSrNo.setText(String.valueOf(position + 1));
        holder.tvName.setText(it.studentName);

        // set radio based on status
        if ("A".equals(it.status)) {
            holder.rbAbsent.setChecked(true);
        } else {
            holder.rbPresent.setChecked(true);
        }

        holder.rgStatus.setOnCheckedChangeListener(null);
        holder.rgStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == holder.rbPresent.getId()) {
                it.status = "P";
            } else if (checkedId == holder.rbAbsent.getId()) {
                it.status = "A";
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AttViewHolder extends RecyclerView.ViewHolder {
        TextView tvSrNo, tvName;
        RadioGroup rgStatus;
        RadioButton rbPresent, rbAbsent;

        AttViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSrNo = itemView.findViewById(R.id.tvSrNo);
            tvName = itemView.findViewById(R.id.tvStudentName);
            rgStatus = itemView.findViewById(R.id.rgStatus);
            rbPresent = itemView.findViewById(R.id.rbPresent);
            rbAbsent = itemView.findViewById(R.id.rbAbsent);
        }
    }
}
