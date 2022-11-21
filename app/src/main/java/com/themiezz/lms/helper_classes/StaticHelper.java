package com.themiezz.lms.helper_classes;

import com.google.firebase.database.DatabaseReference;
import com.themiezz.lms.user_defined_classes.Admin;
import com.themiezz.lms.user_defined_classes.Application;
import com.themiezz.lms.user_defined_classes.HOD;
import com.themiezz.lms.user_defined_classes.Settings;
import com.themiezz.lms.user_defined_classes.Student;

public class StaticHelper {

    public static Student student;
    public static Student parcelableStudent;
    public static HOD hod;
    public static Admin admin;
    public static Application application;
    public static DatabaseReference applicationReference;
    public static Settings settings;

    static {
        student = null;
        parcelableStudent = null;
        hod = null;
        admin = null;
        application = null;
        applicationReference = null;
        settings = null;
    }

}
