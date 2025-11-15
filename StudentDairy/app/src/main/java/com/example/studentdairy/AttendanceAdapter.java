package com.example.studentdairy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private List<AttendanceModel> list;

    public AttendanceAdapter(List<AttendanceModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceModel model = list.get(position);
        holder.tvDate.setText(model.getDate());
        holder.tvStatus.setText(model.getStatus());

        if (model.getStatus().equalsIgnoreCase("P")) {
            holder.tvStatus.setBackgroundResource(R.drawable.status_present_bg);
            holder.tvStatus.setText("Present");
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.status_absent_bg);
            holder.tvStatus.setText("Absent");
        }

        // Fade animation
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(500);
        holder.card.startAnimation(anim);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvStatus;
        CardView card;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            card = (CardView) itemView;
        }
    }
}
