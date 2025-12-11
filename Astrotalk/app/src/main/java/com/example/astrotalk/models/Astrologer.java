package com.example.astrotalk.models;
public class Astrologer {
    private String userid;
    private String name;

    public Astrologer(String userid, String name) {
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
