package com.example.astrotalk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragment";

    private static final String CHAT_USERS_URL =
            "https://testing.trifrnd.net.in/ishwar/chat/chatUsers_fetch_api.php";

    private RecyclerView rvChats;
    private EditText etSearch;

    private ChatListAdapter adapter;
    private final List<ChatUser> allUsers = new ArrayList<>();
    private final List<ChatUser> displayedUsers = new ArrayList<>();

    // Logged-in user id (as String for API)
    private String myUserId = "";

    public ChatFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        rvChats = view.findViewById(R.id.rvChats);
        etSearch = view.findViewById(R.id.etSearch);

        // ✅ Read logged-in user id from SAME prefs used in LoginActivity
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userIdInt = prefs.getInt("userid", -1);
        if (userIdInt != -1) {
            myUserId = String.valueOf(userIdInt);
        } else {
            // Optional: show a message if not logged in
            Toast.makeText(requireContext(),
                    "User not logged in, userid not found",
                    Toast.LENGTH_SHORT).show();
        }

        // For testing only, you can force:
        // myUserId = "5"; // ishwar

        setupRecyclerView();
        setupSearch();
        fetchChatUsers();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new ChatListAdapter(displayedUsers, user -> {
            // ✅ Open ChatActivity when a chat item is clicked
            Intent i = new Intent(requireContext(), ChatActivity.class);
            i.putExtra("my_userid", myUserId);           // logged-in user
            i.putExtra("receiver_id", user.getUserid()); // from API
            i.putExtra("receiver_name", user.getName()); // display name
            startActivity(i);
        });

        rvChats.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvChats.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterUsers(String query) {
        String q = query.toLowerCase(Locale.ROOT).trim();
        displayedUsers.clear();

        if (q.isEmpty()) {
            displayedUsers.addAll(allUsers);
        } else {
            for (ChatUser u : allUsers) {
                if (u.getName().toLowerCase(Locale.ROOT).contains(q)) {
                    displayedUsers.add(u);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void fetchChatUsers() {
        if (getContext() == null) return;

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest request = new StringRequest(
                Request.Method.GET,
                CHAT_USERS_URL,
                response -> {
                    Log.d(TAG, "Response: " + response);
                    parseChatUsers(response);
                },
                error -> {
                    Log.e(TAG, "Error: " + error.getMessage());
                    if (isAdded()) {
                        Toast.makeText(requireContext(),
                                "Failed to load chats",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        queue.add(request);
    }

    private void parseChatUsers(String response) {
        try {
            JSONArray array = new JSONArray(response);

            allUsers.clear();
            displayedUsers.clear();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                String userid = obj.getString("userid");
                String name = obj.getString("name");

                ChatUser user = new ChatUser(userid, name);
                allUsers.add(user);
            }

            displayedUsers.addAll(allUsers);
            adapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
            if (isAdded()) {
                Toast.makeText(requireContext(),
                        "Parse error",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
