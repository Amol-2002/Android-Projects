package com.example.classteacherportal;

public class StudentModel {
    private String student_id;
    private String student_name;
    private String class_id;
    private String section_id;
    private String roll_no;
    private String gender;

    public StudentModel() {}

    public StudentModel(String student_id, String student_name, String class_id,
                        String section_id, String roll_no, String gender) {
        this.student_id = student_id;
        this.student_name = student_name;
        this.class_id = class_id;
        this.section_id = section_id;
        this.roll_no = roll_no;
        this.gender = gender;
    }

    public String getStudent_id() { return student_id; }
    public void setStudent_id(String student_id) { this.student_id = student_id; }

    public String getStudent_name() { return student_name; }
    public void setStudent_name(String student_name) { this.student_name = student_name; }

    public String getClass_id() { return class_id; }
    public void setClass_id(String class_id) { this.class_id = class_id; }

    public String getSection_id() { return section_id; }
    public void setSection_id(String section_id) { this.section_id = section_id; }

    public String getRoll_no() { return roll_no; }
    public void setRoll_no(String roll_no) { this.roll_no = roll_no; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}
