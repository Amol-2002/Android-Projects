package com.example.astrotalk.Adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.astrotalk.ChatActivity;
import com.example.astrotalk.R;
import com.example.astrotalk.models.Astrologer;

import java.util.List;

public class AstrologerAdapter extends RecyclerView.Adapter<AstrologerAdapter.AstroViewHolder> {

    private List<Astrologer> astrologerList;

    public AstrologerAdapter(List<Astrologer> astrologerList) {
        this.astrologerList = astrologerList;
    }

    @NonNull
    @Override
    public AstroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_astrologer, parent, false);
        return new AstroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AstroViewHolder holder, int position) {
        Astrologer astrologer = astrologerList.get(position);

        // Set name
        holder.tvName.setText(astrologer.getName());

        // Set default profile image
        holder.imgAstrologer.setImageResource(R.drawable.profile);

        // Chat button click → open ChatActivity
        holder.btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ChatActivity.class);
            intent.putExtra("userid", astrologer.getUserid());
            intent.putExtra("username", astrologer.getName());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return astrologerList.size();
    }

    static class AstroViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAstrologer;
        TextView tvName;
        Button btnChat;

        AstroViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAstrologer = itemView.findViewById(R.id.imgAstrologer);
            tvName = itemView.findViewById(R.id.tvAstrologerName);
            btnChat = itemView.findViewById(R.id.btnChat);
        }
    }
}
