package com.example.astrotalk;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String SEND_MESSAGE_URL =
            "https://testing.trifrnd.net.in/ishwar/chat/send_msg_api.php";
    private static final String FETCH_MESSAGE_URL =
            "https://testing.trifrnd.net.in/ishwar/chat/sendMsg_fetch_api.php";

    // Auto-refresh interval
    private static final long REFRESH_INTERVAL = 2000;

    private String myUserId;
    private String receiverId;
    private String receiverName;

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageView btnSend, btnBack;
    private TextView tvUserName;

    private MessageAdapter adapter;
    private final List<ChatMessage> messageList = new ArrayList<>();
    private RequestQueue queue;

    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private boolean isLoadingMessages = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        setContentView(R.layout.activity_chat);

        String userid = getIntent().getStringExtra("userid");
        String username = getIntent().getStringExtra("username");


        myUserId = getIntent().getStringExtra("my_userid");
        receiverId = getIntent().getStringExtra("receiver_id");
        receiverName = getIntent().getStringExtra("receiver_name");

        if (myUserId == null) myUserId = "";
        if (receiverId == null) receiverId = "";
        if (receiverName == null) receiverName = "User";

        initViews();
        setupRecyclerView();
        setupClicks();

        queue = Volley.newRequestQueue(this);

        // first load
        loadMessagesFromServer();

        // auto-refresh setup
        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = () -> {
            loadMessagesFromServer();
            refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        tvUserName = findViewById(R.id.tvUserName);

        tvUserName.setText(receiverName);
    }

    private void setupRecyclerView() {
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rvMessages.setLayoutManager(lm);

        adapter = new MessageAdapter(messageList);
        rvMessages.setAdapter(adapter);
    }

    private void setupClicks() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(msg)) sendMessage(msg);
        });
    }

    private void sendMessage(String messageText) {
        if (myUserId.isEmpty() || receiverId.isEmpty()) return;

        JSONObject body = new JSONObject();
        try {
            body.put("userid", myUserId);
            body.put("receiver_id", receiverId);
            body.put("message", messageText);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        btnSend.setEnabled(false);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                SEND_MESSAGE_URL,
                body,
                response -> {
                    btnSend.setEnabled(true);
                    String nowTs = new java.text.SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            java.util.Locale.getDefault()
                    ).format(new java.util.Date());

                    addMessageToList(messageText, true, nowTs);
                },
                error -> {
                    btnSend.setEnabled(true);
                    Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                }
        );

        etMessage.setText("");
        queue.add(request);
    }

    private void addMessageToList(String text, boolean isSentByMe, String timestamp) {
        ChatMessage chatMessage = new ChatMessage(text, isSentByMe, timestamp);
        messageList.add(chatMessage);
        adapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.scrollToPosition(messageList.size() - 1);
    }

    /** 🔥 SMART AUTO-SCROLL (WHATSAPP STYLE) */
    private boolean isUserAtBottom() {
        if (rvMessages.getLayoutManager() == null) return true;
        LinearLayoutManager lm = (LinearLayoutManager) rvMessages.getLayoutManager();
        int lastVisible = lm.findLastVisibleItemPosition();
        return lastVisible >= messageList.size() - 2; // threshold of 2
    }

    private void loadMessagesFromServer() {
        if (myUserId.isEmpty() || receiverId.isEmpty()) return;
        if (isLoadingMessages) return;
        isLoadingMessages = true;

        JSONObject body = new JSONObject();
        try {
            body.put("userid", myUserId);
            body.put("receiver_id", receiverId);
        } catch (JSONException e) {
            e.printStackTrace();
            isLoadingMessages = false;
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                FETCH_MESSAGE_URL,
                body,
                response -> {
                    isLoadingMessages = false;

                    boolean status = response.optBoolean("status", false);
                    if (!status) return;

                    List<ChatMessage> tempList = new ArrayList<>();
                    try {
                        for (int i = 0; i < response.getJSONArray("chat").length(); i++) {
                            JSONObject obj = response.getJSONArray("chat").getJSONObject(i);

                            String senderId = obj.optString("sender_id", "");
                            String message = obj.optString("message", "");
                            String timestamp = obj.optString("timestamp", "");

                            boolean isSentByMe = senderId.equals(myUserId);
                            tempList.add(new ChatMessage(message, isSentByMe, timestamp));
                        }
                    } catch (JSONException ignored) {}

                    boolean shouldAutoScroll = isUserAtBottom();

                    messageList.clear();
                    messageList.addAll(tempList);
                    adapter.notifyDataSetChanged();

                    if (shouldAutoScroll && !messageList.isEmpty()) {
                        rvMessages.scrollToPosition(messageList.size() - 1);
                    }
                },
                error -> isLoadingMessages = false
        );

        queue.add(request);
    }
}
