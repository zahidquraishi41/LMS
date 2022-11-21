package com.themiezz.lms.helper_classes;

import com.themiezz.lms.user_defined_classes.Admin;
import com.themiezz.lms.user_defined_classes.Application;
import com.themiezz.lms.user_defined_classes.HOD;
import com.themiezz.lms.user_defined_classes.Settings;
import com.themiezz.lms.user_defined_classes.Student;

public class ErrorHandler {

    static public boolean isEmpty(Application application) {
        if (application == null) return true;
        return application.getRemarks() == null || application.getReason() == null || application.getStudentID() == null || application.getStatus() == null || application.getDateApplied() == null || application.getFromDate() == null || application.getToDate() == null;
    }

    static public boolean isEmpty(Student student) {
        if (student == null) return true;
        return student.getPhoneNumber() == null || student.getStudentID() == null || student.getDepartment() == null || student.getFullName() == null || student.getPassword() == null;
    }

    static public boolean isEmpty(HOD hod) {
        if (hod == null) return true;
        return hod.getPhoneNumber() == null || hod.getUsername() == null || hod.getDepartment() == null || hod.getFullName() == null || hod.getPassword() == null;
    }

    static public boolean isEmpty(Admin admin) {
        if (admin == null) return true;
        return admin.getPhoneNumber() == null || admin.getUsername() == null || admin.getFullName() == null || admin.getPassword() == null;
    }

    static public boolean isEmpty(String... strings) {
        for (String s : strings) {
            if (s == null)
                return true;
            else if (s.equals(""))
                return true;
        }
        return false;
    }

    static public boolean isEmpty(Settings settings) {
        if (settings == null) return true;
        return settings.getRegistrationOpen() == null || settings.getWorkingDays() == null;
    }

    public static boolean isValidNumber(String phoneNumber) {
        boolean isValid = true;
        if (phoneNumber == null) return false;
        if (phoneNumber.equals("")) return false;
        if (phoneNumber.length() != 10) return false;
        if (phoneNumber.charAt(0) != '9' && phoneNumber.charAt(0) != '8' && phoneNumber.charAt(0) != '7' && phoneNumber.charAt(0) != '6')
            return false;
        for (int i = 0; i < phoneNumber.length(); i++) {
            if (phoneNumber.charAt(i) < '0' || phoneNumber.charAt(i) > '9') {
                isValid = false;
                break;
            }
        }
        return isValid;
    }

}
