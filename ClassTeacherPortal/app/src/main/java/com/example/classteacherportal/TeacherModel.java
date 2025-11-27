package com.example.classteacherportal;


public class TeacherModel {
    private String user_id;
    private String teacher_name;
    private String departments;
    private String designation;
    private String joining_date;
    private String phone;
    private String gender;
    private String state;
    private String city;
    private String present_address;
    private String permanent_address;

    // getters & setters
    public String getUser_id() { return user_id; }
    public void setUser_id(String user_id) { this.user_id = user_id; }

    public String getTeacher_name() { return teacher_name; }
    public void setTeacher_name(String teacher_name) { this.teacher_name = teacher_name; }

    public String getDepartments() { return departments; }
    public void setDepartments(String departments) { this.departments = departments; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getJoining_date() { return joining_date; }
    public void setJoining_date(String joining_date) { this.joining_date = joining_date; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPresent_address() { return present_address; }
    public void setPresent_address(String present_address) { this.present_address = present_address; }

    public String getPermanent_address() { return permanent_address; }
    public void setPermanent_address(String permanent_address) { this.permanent_address = permanent_address; }
}
