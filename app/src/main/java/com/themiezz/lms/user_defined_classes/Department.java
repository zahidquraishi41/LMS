package com.themiezz.lms.user_defined_classes;

public class Department {

    private String departmentName, hod;

    public Department(){
    }

    public Department(String departmentName, String managerId) {
        this.departmentName = departmentName;
        this.hod = managerId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getHod() {
        return hod;
    }

    public void setHod(String hod) {
        this.hod = hod;
    }
}
