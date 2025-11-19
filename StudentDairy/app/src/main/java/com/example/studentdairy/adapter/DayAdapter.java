package com.example.studentdairy.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.studentdairy.R;
import com.example.studentdairy.model.Day;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.VH> {

    private List<Day> days;
    private Set<String> presentDates; // yyyy-MM-dd
    private Set<String> absentDates;  // yyyy-MM-dd
    private Set<String> holidayDates; // yyyy-MM-dd
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public DayAdapter(List<Day> days, Set<String> presentDates, Set<String> absentDates, Set<String> holidayDates) {
        this.days = days;
        this.presentDates = presentDates;
        this.absentDates = absentDates;
        this.holidayDates = holidayDates;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Day d = days.get(position);

        // Filler cell -> keep space but invisible
        if (!d.inMonth || d.dayNumber == 0) {
            holder.tv.setText("");
            holder.tv.setBackgroundColor(Color.TRANSPARENT);
            holder.tv.setClickable(false);
            holder.tv.setVisibility(View.INVISIBLE);
            return;
        }

        holder.tv.setVisibility(View.VISIBLE);
        holder.tv.setText(String.valueOf(d.dayNumber));
        holder.tv.setClickable(true);

        boolean isHoliday = holidayDates != null && holidayDates.contains(d.dateString);
        boolean isPresent = presentDates != null && presentDates.contains(d.dateString);
        boolean isAbsent = absentDates != null && absentDates.contains(d.dateString);


        try {
            Date date = sdf.parse(d.dateString);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int dow = cal.get(Calendar.DAY_OF_WEEK);

            // Priority: Present -> Absent -> Holiday/Sunday -> Default
            if ( dow == Calendar.SUNDAY) {
                holder.tv.setBackgroundResource(R.drawable.circle_sunday);
                holder.tv.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));
            }else if (isHoliday) {
                    holder.tv.setBackgroundResource(R.drawable.circle_holiday);
                    holder.tv.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));
            }else if (isPresent) {
                holder.tv.setBackgroundResource(R.drawable.circle_present);
                holder.tv.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));
            } else if (isAbsent) {
                holder.tv.setBackgroundResource(R.drawable.circle_absent);
                holder.tv.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));
            }else {
                holder.tv.setBackgroundResource(R.drawable.circle_default);
                holder.tv.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));
            }
        } catch (Exception e) {
            holder.tv.setBackgroundResource(R.drawable.circle_default);
            holder.tv.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));
        }
    }

    @Override
    public int getItemCount() {
        return days == null ? 0 : days.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv;
        VH(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tvDayNumber);
        }
    }
}
