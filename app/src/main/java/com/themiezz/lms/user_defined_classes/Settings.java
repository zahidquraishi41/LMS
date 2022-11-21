package com.themiezz.lms.user_defined_classes;

public class Settings {

    private Boolean registrationOpen;
    private Integer workingDays;

    public Settings() {
    }

    public Settings(Boolean registrationOpen, Integer workingDays) {
        this.registrationOpen = registrationOpen;
        this.workingDays = workingDays;
    }

    public Boolean getRegistrationOpen() {
        return registrationOpen;
    }

    public void setRegistrationOpen(Boolean registrationOpen) {
        this.registrationOpen = registrationOpen;
    }

    public Integer getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(Integer workingDays) {
        this.workingDays = workingDays;
    }

}
