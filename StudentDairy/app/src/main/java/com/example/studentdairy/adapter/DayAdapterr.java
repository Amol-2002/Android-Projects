package com.example.studentdairy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentdairy.R;
import com.example.studentdairy.model.DayItem;
import com.example.studentdairy.model.PeriodModel;

import java.util.List;

public class DayAdapterr extends RecyclerView.Adapter<DayAdapterr.DayHolder> {

    private final List<DayItem> items;

    public DayAdapterr(List<DayItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public DayHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day_card, parent, false);
        return new DayHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DayHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class DayHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;
        LinearLayout llPeriods;
        LayoutInflater inflater;

        DayHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvDayCardHeader);
            llPeriods = itemView.findViewById(R.id.llPeriodsContainer);
            inflater = LayoutInflater.from(itemView.getContext());
        }

        void bind(DayItem dayItem) {
            tvHeader.setText(dayItem.getDayName());
            llPeriods.removeAllViews();

            for (PeriodModel p : dayItem.getPeriods()) {
                View v = inflater.inflate(R.layout.item_period_small, llPeriods, false);
                TextView tvId = v.findViewById(R.id.tvPeriodSmallId);
                TextView tvSub = v.findViewById(R.id.tvPeriodSmallSub);
                TextView tvTeacher = v.findViewById(R.id.tvPeriodSmallTeacher);
                TextView tvTime = v.findViewById(R.id.tvPeriodSmallTime);

                tvId.setText(String.valueOf(p.getPeriodId()));
                tvSub.setText(p.getSubject());
                tvTeacher.setText(p.getTeacherName());
                tvTime.setText((p.getStartTime() != null ? p.getStartTime() : "") + " - " + (p.getEndTime() != null ? p.getEndTime() : ""));

                llPeriods.addView(v);
            }
        }
    }
}
