package com.themiezz.lms.user_defined_classes;

public class LeaveInfo {

    private String lastLeave;
    private Integer timesApplied, timesAccepted,leaveDaysCount;

    public LeaveInfo() {
    }

    public LeaveInfo(String lastLeave, Integer timesApplied, Integer timesAccepted, Integer leaveDaysCount) {
        this.lastLeave = lastLeave;
        this.timesApplied = timesApplied;
        this.timesAccepted = timesAccepted;
        this.leaveDaysCount = leaveDaysCount;
    }

    public String getLastLeave() {
        return lastLeave;
    }

    public void setLastLeave(String lastLeave) {
        this.lastLeave = lastLeave;
    }

    public Integer getTimesApplied() {
        return timesApplied;
    }

    public void setTimesApplied(Integer timesApplied) {
        this.timesApplied = timesApplied;
    }

    public Integer getTimesAccepted() {
        return timesAccepted;
    }

    public void setTimesAccepted(Integer timesAccepted) {
        this.timesAccepted = timesAccepted;
    }

    public Integer getLeaveDaysCount() {
        return leaveDaysCount;
    }

    public void setLeaveDaysCount(Integer leaveDaysCount) {
        this.leaveDaysCount = leaveDaysCount;
    }
}
