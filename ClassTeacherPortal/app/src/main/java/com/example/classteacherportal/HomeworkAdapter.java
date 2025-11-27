package com.example.classteacherportal;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeworkAdapter extends RecyclerView.Adapter<HomeworkAdapter.VH> {

    private final List<HomeworkItem> list;
    private final Context ctx;
    private final String baseUrl = "https://testing.trifrnd.net.in/ishwar/school/"; // adjust if needed

    public HomeworkAdapter(Context ctx, List<HomeworkItem> list) {
        this.ctx = ctx;
        this.list = list;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_homework, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        HomeworkItem it = list.get(position);
        holder.tvSubject.setText(it.subject);
        holder.tvDate.setText(it.hwDate);
        holder.tvFile.setText(it.fileName != null && !it.fileName.isEmpty() ? "View attachment" : "No attachment");

        holder.tvFile.setOnClickListener(v -> {
            if (it.fileName == null || it.fileName.isEmpty()) return;
            String full = it.fileName.startsWith("http") ? it.fileName : (baseUrl + it.fileName.replaceFirst("^\\.*/?", ""));
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(full));
                ctx.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvSubject, tvDate, tvFile;
        VH(View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.subject);
            tvDate = itemView.findViewById(R.id.hw_date);
            tvFile = itemView.findViewById(R.id.file_name);
        }
    }

    // simple data holder
    public static class HomeworkItem {
        public String subject;
        public String hwDate; // in yyyy-MM-dd
        public String fileName;
        public HomeworkItem(String s, String d, String f) { subject = s; hwDate = d; fileName = f; }
    }
}