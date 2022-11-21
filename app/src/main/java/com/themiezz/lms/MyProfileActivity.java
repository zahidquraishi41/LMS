package com.themiezz.lms;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.themiezz.lms.helper_classes.ErrorHandler;
import com.themiezz.lms.helper_classes.FirebaseHelper;
import com.themiezz.lms.helper_classes.IAEM;
import com.themiezz.lms.helper_classes.StaticHelper;
import com.themiezz.lms.user_defined_classes.Student;

public class MyProfileActivity extends AppCompatActivity {
    EditText etFullName, etPhoneNumber, etParentPhoneNumber;
    LinearLayout llParentPhoneNumber;
    IAEM iaem;
    int acType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        etFullName = findViewById(R.id.etFullName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etParentPhoneNumber = findViewById(R.id.etParentPhoneNumber);
        llParentPhoneNumber = findViewById(R.id.llParentPhoneNumber);
        acType = getIntent().getIntExtra("acType", 9);
        if (acType == FirebaseHelper.STUDENT && StaticHelper.student == null) {
            DisplayErrorMessage();
            return;
        } else if (acType == FirebaseHelper.HOD && StaticHelper.hod == null) {
            DisplayErrorMessage();
            return;
        } else if (acType == FirebaseHelper.ADMIN && StaticHelper.admin == null) {
            DisplayErrorMessage();
            return;
        } else if (acType == 9) {
            DisplayErrorMessage();
            return;
        }
        if (acType != FirebaseHelper.STUDENT)
            llParentPhoneNumber.setVisibility(View.GONE);
        iaem = new IAEM((TextView) findViewById(R.id.tvErrorMessage));
        if (acType == FirebaseHelper.STUDENT) {
            etFullName.setText(StaticHelper.student.getFullName());
            etPhoneNumber.setText(StaticHelper.student.getPhoneNumber().substring(3));
            etParentPhoneNumber.setText(StaticHelper.student.getParentPhoneNumber().substring(3));
        } else if (acType == FirebaseHelper.HOD) {
            etFullName.setText(StaticHelper.hod.getFullName());
            etPhoneNumber.setText(StaticHelper.hod.getPhoneNumber().substring(3));
        } else {
            etFullName.setText(StaticHelper.admin.getFullName());
            etPhoneNumber.setText(StaticHelper.admin.getPhoneNumber().substring(3));
        }
    }

    private void DisplayErrorMessage() {
        Toast.makeText(this, "An unexpected error has occurred", Toast.LENGTH_SHORT).show();
        finish();
    }

    public void UpdateFullName(View view) {
        final String newName = etFullName.getText().toString();
        if (newName.equals("")) {
            iaem.showMessage("Full Name cannot be empty");
            return;
        }
        final ProgressDialog progressDialog = ProgressDialog.show(this, null, "Updating...", true, false);
        FirebaseHelper.CompletionListener listener = new FirebaseHelper.CompletionListener() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                iaem.showMessage("Name updated successfully", IAEM.INFORMATIVE);
                if (acType == FirebaseHelper.STUDENT) {
                    StaticHelper.student.setFullName(newName);
                } else if (acType == FirebaseHelper.HOD) {
                    StaticHelper.hod.setFullName(newName);
                } else {
                    StaticHelper.admin.setFullName(newName);
                }
            }

            @Override
            public void onFailed() {
                progressDialog.dismiss();
                iaem.showMessage("Failed to update name");
            }
        };
        if (acType == FirebaseHelper.STUDENT) {
            if (!newName.equals(StaticHelper.student.getFullName()))
                new FirebaseHelper(this).updateKey(FirebaseHelper.STUDENTS_DATABASE.child(StaticHelper.student.getStudentID()), "fullName", newName, listener);
            else progressDialog.dismiss();
        } else if (acType == FirebaseHelper.HOD) {
            if (!StaticHelper.hod.getFullName().equals(newName))
                new FirebaseHelper(this).updateKey(FirebaseHelper.STUDENTS_DATABASE.child(StaticHelper.hod.getUsername()), "fullName", newName, listener);
            else progressDialog.dismiss();
        } else {
            if (!StaticHelper.admin.getFullName().equals(newName))
                new FirebaseHelper(this).updateKey(FirebaseHelper.ADMIN_DATABASE.child(StaticHelper.admin.getUsername()), "fullName", newName, listener);
            else progressDialog.dismiss();
        }
    }

    public void UpdatePhoneNumber(View view) {
        String phoneNumber = etPhoneNumber.getText().toString();
        if (!ErrorHandler.isValidNumber(phoneNumber))
            iaem.showMessage("Invalid Number");

        StaticHelper.parcelableStudent = new Student();
        StaticHelper.parcelableStudent.setPhoneNumber(phoneNumber);
        Intent intent = new Intent(this, VerifyNumberActivity.class);
        intent.putExtra("intent", VerifyNumberActivity.UPDATE_NUMBER);
        intent.putExtra("acType", acType);
        intent.putExtra("phoneNumber", "+91" + phoneNumber);
        if (acType == FirebaseHelper.STUDENT) {
            if (StaticHelper.student.getPhoneNumber().substring(3).equals(phoneNumber))
                return;
            else if (StaticHelper.student.getParentPhoneNumber().substring(3).equals(phoneNumber)) {
                iaem.showMessage("Your phone number cannot be same as your parent's number");
                return;
            } else intent.putExtra("userID", StaticHelper.student.getStudentID());
        } else if (acType == FirebaseHelper.HOD) {
            if (StaticHelper.hod.getPhoneNumber().substring(3).equals(phoneNumber))
                return;
            else
                intent.putExtra("userID", StaticHelper.hod.getUsername());
        } else {
            if (StaticHelper.admin.getPhoneNumber().substring(3).equals(phoneNumber))
                return;
            else
                intent.putExtra("userID", StaticHelper.admin.getUsername());
        }
        startActivity(intent);
    }

    public void UpdatePassword(View view) {
        startActivity(new Intent(this, ChangePasswordActivity.class).putExtra("acType", acType));
    }

    public void UpdateParentNumber(View view) {
        String parentNumber = etParentPhoneNumber.getText().toString();
        if (!ErrorHandler.isValidNumber(parentNumber)) {
            iaem.showMessage("Invalid Number");
        } else if (parentNumber.equals(StaticHelper.student.getParentPhoneNumber().substring(3)))
            return;
        else if (parentNumber.equals(StaticHelper.student.getPhoneNumber().substring(3)))
            iaem.showMessage("Parent's phone number cannot be same as yours");
        if (iaem.isVisible())
            return;

        final ProgressDialog progressDialog = ProgressDialog.show(this, null, "Updating...", true, false);
        new FirebaseHelper(this).updateKey(FirebaseHelper.STUDENTS_DATABASE.child(StaticHelper.student.getStudentID()), "parentPhoneNumber", "+91" + parentNumber, new FirebaseHelper.CompletionListener() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                iaem.showMessage("Number updated successfully");
            }

            @Override
            public void onFailed() {
                progressDialog.dismiss();
                iaem.showMessage("Failed to update number");
            }
        });
    }

}
