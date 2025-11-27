package com.example.classteacherportal;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AttendanceGridAdapter extends RecyclerView.Adapter<AttendanceGridAdapter.GridVH> {

    // 🔹 This is the class ViewAttendanceFragmentGrid is trying to use
    public static class StudentMonthItem {
        public String studentId;
        public String studentName;
        public String[] dayStatus; // length 31, "P", "A", or ""

        public StudentMonthItem(String studentId, String studentName, int daysInMonth) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.dayStatus = new String[31]; // indices 0..30 => days 1..31
            for (int i = 0; i < 31; i++) {
                dayStatus[i] = "";
            }
        }
    }

    private Context ctx;
    private List<StudentMonthItem> items;
    private int daysInMonth;
    private int highlightDay; // which day column to highlight (0 = none)

    public AttendanceGridAdapter(Context ctx,
                                 List<StudentMonthItem> items,
                                 int daysInMonth,
                                 int highlightDay) {
        this.ctx = ctx;
        this.items = items;
        this.daysInMonth = daysInMonth;
        this.highlightDay = highlightDay;
    }

    @NonNull
    @Override
    public GridVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.row_attendance_grid, parent, false);
        return new GridVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GridVH h, int position) {
        StudentMonthItem it = items.get(position);
        h.tvSrNo.setText(String.valueOf(position + 1));
        h.tvName.setText(it.studentName);

        for (int d = 1; d <= 31; d++) {
            TextView cell = h.dayCells[d - 1];

            // Hide / gray-out days beyond the month length
            if (d > daysInMonth) {
                cell.setText("");
                cell.setBackgroundColor(Color.parseColor("#F5F5F5"));
                cell.setTextColor(Color.TRANSPARENT);
                continue;
            }

            String status = it.dayStatus[d - 1]; // "P", "A", or ""
            cell.setText(status);

            // Reset style
            cell.setBackgroundResource(R.drawable.day_cell_bg);
            cell.setTextColor(Color.BLACK);

            if ("P".equalsIgnoreCase(status)) {
                cell.setTextColor(Color.parseColor("#1B5E20")); // green
            } else if ("A".equalsIgnoreCase(status)) {
                cell.setTextColor(Color.parseColor("#B71C1C")); // red
            }

            // Highlight chosen column (like red block in screenshot)
            if (highlightDay == d) {
                cell.setBackgroundColor(Color.parseColor("#FF5252")); // red
                cell.setTextColor(Color.WHITE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class GridVH extends RecyclerView.ViewHolder {
        TextView tvSrNo, tvName;
        TextView[] dayCells = new TextView[31];

        GridVH(@NonNull View itemView) {
            super(itemView);
            tvSrNo = itemView.findViewById(R.id.tvSrNo);
            tvName = itemView.findViewById(R.id.tvStudentName);

            dayCells[0]  = itemView.findViewById(R.id.tvD1);
            dayCells[1]  = itemView.findViewById(R.id.tvD2);
            dayCells[2]  = itemView.findViewById(R.id.tvD3);
            dayCells[3]  = itemView.findViewById(R.id.tvD4);
            dayCells[4]  = itemView.findViewById(R.id.tvD5);
            dayCells[5]  = itemView.findViewById(R.id.tvD6);
            dayCells[6]  = itemView.findViewById(R.id.tvD7);
            dayCells[7]  = itemView.findViewById(R.id.tvD8);
            dayCells[8]  = itemView.findViewById(R.id.tvD9);
            dayCells[9]  = itemView.findViewById(R.id.tvD10);
            dayCells[10] = itemView.findViewById(R.id.tvD11);
            dayCells[11] = itemView.findViewById(R.id.tvD12);
            dayCells[12] = itemView.findViewById(R.id.tvD13);
            dayCells[13] = itemView.findViewById(R.id.tvD14);
            dayCells[14] = itemView.findViewById(R.id.tvD15);
            dayCells[15] = itemView.findViewById(R.id.tvD16);
            dayCells[16] = itemView.findViewById(R.id.tvD17);
            dayCells[17] = itemView.findViewById(R.id.tvD18);
            dayCells[18] = itemView.findViewById(R.id.tvD19);
            dayCells[19] = itemView.findViewById(R.id.tvD20);
            dayCells[20] = itemView.findViewById(R.id.tvD21);
            dayCells[21] = itemView.findViewById(R.id.tvD22);
            dayCells[22] = itemView.findViewById(R.id.tvD23);
            dayCells[23] = itemView.findViewById(R.id.tvD24);
            dayCells[24] = itemView.findViewById(R.id.tvD25);
            dayCells[25] = itemView.findViewById(R.id.tvD26);
            dayCells[26] = itemView.findViewById(R.id.tvD27);
            dayCells[27] = itemView.findViewById(R.id.tvD28);
            dayCells[28] = itemView.findViewById(R.id.tvD29);
            dayCells[29] = itemView.findViewById(R.id.tvD30);
            dayCells[30] = itemView.findViewById(R.id.tvD31);
        }
    }
}
