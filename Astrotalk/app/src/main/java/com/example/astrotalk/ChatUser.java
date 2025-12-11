package com.example.astrotalk;

public class ChatUser {
    private String userid;
    private String name;

    public ChatUser(String userid, String name) {
        this.userid = userid;
        this.name = name;
    }

    public String getUserid() {
        return userid;
    }

    public String getName() {
        return name;
    }
}
