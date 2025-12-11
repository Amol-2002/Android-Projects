package com.example.astrotalk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    public interface OnChatClickListener {
        void onChatClick(ChatUser user);
    }

    private List<ChatUser> users;
    private OnChatClickListener listener;

    public ChatListAdapter(List<ChatUser> users, OnChatClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatUser user = users.get(position);
        holder.tvName.setText(user.getName());
        holder.tvLastMessage.setText("Tap to chat");
        holder.tvTime.setText(""); // set time if you have it

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onChatClick(user);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateList(List<ChatUser> newList) {
        this.users = newList;
        notifyDataSetChanged();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMessage, tvTime;
        ImageView imgProfile;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            imgProfile = itemView.findViewById(R.id.imgProfile);
        }
    }
}
