package com.example.studentdairy;

public class Homework {
    private String subject;
    private String hwDate;
    private String fileName;

    public Homework(String subject, String hwDate, String fileName) {
        this.subject = subject;
        this.hwDate = hwDate;
        this.fileName = fileName;
    }

    public String getSubject() { return subject; }
    public String getHwDate() { return hwDate; }
    public String getFileName() { return fileName; }
}
