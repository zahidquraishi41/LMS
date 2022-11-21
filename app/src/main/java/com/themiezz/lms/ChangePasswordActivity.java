package com.themiezz.lms;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.themiezz.lms.helper_classes.FirebaseHelper;
import com.themiezz.lms.helper_classes.IAEM;
import com.themiezz.lms.helper_classes.StaticHelper;

import java.io.File;

public class ChangePasswordActivity extends AppCompatActivity {
    int acType;
    EditText etOldPassword, etNewPassword;
    IAEM iaem;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(null);
        progressDialog.setMessage("Verifying...");
        progressDialog.setCancelable(false);
        acType = getIntent().getIntExtra("acType", 9);
        if (acType == 9) {
            finish();
            return;
        }
        etNewPassword = findViewById(R.id.etNewPassword);
        etOldPassword = findViewById(R.id.etOldPassword);
        iaem = new IAEM((TextView) findViewById(R.id.tvErrorMessage));
    }

    public void ChangePassword(View view) {
        final String oldPassword = etOldPassword.getText().toString(),
                newPassword = etNewPassword.getText().toString();
        if (oldPassword.equals("")) {
            iaem.showMessage("Please enter old password");
        } else if (newPassword.equals("")) {
            iaem.showMessage("Please enter new password");
        }
        if (iaem.isVisible())
            return;
        progressDialog.show();
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String password = dataSnapshot.getValue(String.class);
                if (password != null) {
                    if (password.equals(oldPassword)) {
                        ChangePassword(newPassword);
                    } else {
                        progressDialog.dismiss();
                        iaem.showMessage("Incorrect password");
                    }
                } else {
                    progressDialog.dismiss();
                    iaem.showMessage("Failed to connect to internet");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                iaem.showMessage("Failed to connect to internet");
            }
        };
        if (acType == FirebaseHelper.STUDENT) {
            FirebaseHelper.STUDENTS_DATABASE.child(StaticHelper.student.getStudentID()).child("password").addListenerForSingleValueEvent(valueEventListener);
        } else if (acType == FirebaseHelper.HOD) {
            FirebaseHelper.STUDENTS_DATABASE.child(StaticHelper.hod.getUsername()).child("password").addListenerForSingleValueEvent(valueEventListener);
        } else {
            FirebaseHelper.ADMIN_DATABASE.child(StaticHelper.admin.getUsername()).child("password").addListenerForSingleValueEvent(valueEventListener);
        }
    }

    private void ChangePassword(final String newPassword) {
        FirebaseHelper.CompletionListener listener = new FirebaseHelper.CompletionListener() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                File file = new File(getFilesDir() + "/" + "saved_ac.txt");
                if (file.exists())
                    file.delete();
                new FirebaseHelper(ChangePasswordActivity.this).saveAccount("ASD", newPassword, acType);
                Toast.makeText(ChangePasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed() {
                progressDialog.dismiss();
                iaem.showMessage("Failed to change password");
            }
        };

        if (acType == FirebaseHelper.STUDENT)
            new FirebaseHelper(this).updateKey(FirebaseHelper.STUDENTS_DATABASE.child(StaticHelper.student.getStudentID()), "password", newPassword, listener);
        else if (acType == FirebaseHelper.HOD)
            new FirebaseHelper(this).updateKey(FirebaseHelper.HOD_DATABASE.child(StaticHelper.hod.getUsername()), "password", newPassword, listener);
        else
            new FirebaseHelper(this).updateKey(FirebaseHelper.ADMIN_DATABASE.child(StaticHelper.admin.getUsername()), "password", newPassword, listener);
    }

}
