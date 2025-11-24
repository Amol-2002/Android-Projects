package com.example.studentdairy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PeriodAdapter extends RecyclerView.Adapter<PeriodAdapter.VH> {

    private List<TimetableFragment.Period> list;

    public PeriodAdapter(List<TimetableFragment.Period> list) {
        this.list = list;
    }

    public void updateList(List<TimetableFragment.Period> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        TimetableFragment.Period p = list.get(position);
        holder.tv1.setText((position+1) + ". " + " (" + p.start + " - " + p.end + ")"+" "+ p.subject +" "+ p.teacher );
//        holder.tv2.setText("Teacher: " + p.teacher);
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv1, tv2;
        public VH(View itemView) {
            super(itemView);
            tv1 = itemView.findViewById(android.R.id.text1);
            tv2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
