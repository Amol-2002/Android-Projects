package com.example.studentdairy.model;

public class PeriodModel {
    private int periodId;
    private String day;
    private String subject;
    private String teacherName;
    private String startTime;
    private String endTime;

    public PeriodModel(int periodId, String day, String subject, String teacherName, String startTime, String endTime) {
        this.periodId = periodId;
        this.day = day;
        this.subject = subject;
        this.teacherName = teacherName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getPeriodId() { return periodId; }
    public String getDay() { return day; }
    public String getSubject() { return subject; }
    public String getTeacherName() { return teacherName; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
}
