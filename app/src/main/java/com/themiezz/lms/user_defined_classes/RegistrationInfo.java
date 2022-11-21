package com.themiezz.lms.user_defined_classes;

public class RegistrationInfo {
    private Integer totalStudents, totalHODs, totalDepartments;

    public RegistrationInfo(){}

    public RegistrationInfo(Integer totalStudents, Integer totalHODs, Integer totalDepartments) {
        this.totalStudents = totalStudents;
        this.totalHODs = totalHODs;
        this.totalDepartments = totalDepartments;
    }

    public Integer getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(Integer totalStudents) {
        this.totalStudents = totalStudents;
    }

    public Integer getTotalHODs() {
        return totalHODs;
    }

    public void setTotalHODs(Integer totalHODs) {
        this.totalHODs = totalHODs;
    }

    public Integer getTotalDepartments() {
        return totalDepartments;
    }

    public void setTotalDepartments(Integer totalDepartments) {
        this.totalDepartments = totalDepartments;
    }
}
