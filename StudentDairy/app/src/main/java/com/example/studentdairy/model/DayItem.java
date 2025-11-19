package com.example.studentdairy.model;

import java.util.List;

public class DayItem {
    private final String dayName;
    private final List<PeriodModel> periods;

    public DayItem(String dayName, List<PeriodModel> periods) {
        this.dayName = dayName;
        this.periods = periods;
    }

    public String getDayName() { return dayName; }
    public List<PeriodModel> getPeriods() { return periods; }
}
