package com.example.studentdairy.model;

public class Day {
    public int dayNumber; // 1..31 (0 for filler)
    public String dateString; // yyyy-MM-dd
    public boolean inMonth;

    public Day(int dayNumber, String dateString, boolean inMonth) {
        this.dayNumber = dayNumber;
        this.dateString = dateString;
        this.inMonth = inMonth;
    }
}
