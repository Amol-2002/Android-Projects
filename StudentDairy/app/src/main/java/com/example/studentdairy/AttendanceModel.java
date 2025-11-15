package com.example.studentdairy;

public class AttendanceModel {
    private String date, status;

    public AttendanceModel(String date, String status) {
        this.date = date;
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }
}
