package com.example.studentdairy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studentdairy.R;
import com.example.studentdairy.model.PeriodModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter holds a flat list of ListItem objects:
 * - String day header (wrapped into a HeaderHolder)
 * - PeriodModel (wrapped into a PeriodHolder)
 *
 * Two view types: HEADER = 0, PERIOD = 1
 */
public class TimetableAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_PERIOD = 1;

    // flattened data: either String (day header) or PeriodModel
    private final List<Object> items = new ArrayList<>();

    public void setItems(List<Object> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override public int getItemViewType(int position) {
        Object obj = items.get(position);
        return (obj instanceof String) ? TYPE_HEADER : TYPE_PERIOD;
    }

    @Override public int getItemCount() { return items.size(); }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View v = inf.inflate(R.layout.item_day_header, parent, false);
            return new HeaderHolder(v);
        } else {
            View v = inf.inflate(R.layout.item_period, parent, false);
            return new PeriodHolder(v);
        }
    }

    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            ((HeaderHolder) holder).bind((String) items.get(position));
        } else {
            ((PeriodHolder) holder).bind((PeriodModel) items.get(position));
        }
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        HeaderHolder(View v) {
            super(v);
            tvDay = v.findViewById(R.id.tvDayHeader);
        }
        void bind(String day) {
            tvDay.setText(day);
        }
    }

    static class PeriodHolder extends RecyclerView.ViewHolder {
        TextView tvPeriodId, tvSubject, tvTeacher, tvTime;
        PeriodHolder(View v) {
            super(v);
            tvPeriodId = v.findViewById(R.id.tvPeriodId);
            tvSubject = v.findViewById(R.id.tvSubject);
            tvTeacher = v.findViewById(R.id.tvTeacher);
            tvTime = v.findViewById(R.id.tvTime);
        }
        void bind(PeriodModel p) {
            tvPeriodId.setText(String.valueOf(p.getPeriodId()));
            tvSubject.setText(p.getSubject());
            tvTeacher.setText(p.getTeacherName());
            tvTime.setText(p.getStartTime() + " - " + p.getEndTime());
        }
    }
}
