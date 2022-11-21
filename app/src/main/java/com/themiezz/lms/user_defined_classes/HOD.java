package com.themiezz.lms.user_defined_classes;

public class HOD {
    private String username, password, phoneNumber, department, fullName;

    public HOD() {
    }

    public HOD(String username, String password, String phoneNumber, String department, String fullName) {
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.department = department;
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
