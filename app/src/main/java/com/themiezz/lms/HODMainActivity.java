package com.themiezz.lms;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.themiezz.lms.helper_classes.ErrorHandler;
import com.themiezz.lms.helper_classes.FirebaseHelper;
import com.themiezz.lms.helper_classes.StaticHelper;
import com.themiezz.lms.user_defined_classes.LeaveInfo;

import java.io.File;

public class HODMainActivity extends AppCompatActivity {
    private static final String TAG = "ZQHODMainActivity";
    LinearLayout parentView;
    TextView tvLeaveAccepted, tvTotalLeave, tvTotalStudents, tvFullName;
    Uri imageUri;
    ImageView ivProfilePic;
    ValueEventListener totalStudentListener, leaveInfoListener;
    long lastBackPressTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hod_main);

        parentView = findViewById(R.id.parentView);
        tvLeaveAccepted = findViewById(R.id.tvLeaveAccepted);
        tvTotalLeave = findViewById(R.id.tvTotalLeave);
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvFullName = findViewById(R.id.tvFullName);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        totalStudentListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer integer = dataSnapshot.getValue(Integer.class);
                if (integer != null)
                    tvTotalStudents.setText(String.valueOf(integer));
                else
                    tvTotalStudents.setText(String.valueOf(0));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        leaveInfoListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer totalLeave = 0, leaveAccepted = 0;
                LeaveInfo leaveInfo;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    leaveInfo = snapshot.getValue(LeaveInfo.class);
                    if (leaveInfo != null) {
                        if (leaveInfo.getTimesApplied() != null)
                            totalLeave += leaveInfo.getTimesApplied();
                        if (leaveInfo.getTimesAccepted() != null)
                            leaveAccepted += leaveInfo.getTimesAccepted();
                    }
                }
                tvLeaveAccepted.setText(String.valueOf(leaveAccepted));
                tvTotalLeave.setText(String.valueOf(totalLeave));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        FirebaseHelper.LEAVE_INFO_DATABASE.child(StaticHelper.hod.getDepartment()).addValueEventListener(leaveInfoListener);
        FirebaseHelper.REGISTRATION_INFO_DATABASE.child("totalStudents").child(StaticHelper.hod.getDepartment()).addValueEventListener(totalStudentListener);
        new FirebaseHelper(this).loadImageInto(ivProfilePic, StaticHelper.hod.getUsername(), FirebaseHelper.HOD);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ErrorHandler.isEmpty(StaticHelper.hod)) {
            Toast.makeText(this, "An unexpected error has occurred", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvFullName.setText(StaticHelper.hod.getFullName());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK)
            if (data != null) {
                imageUri = data.getData();
                final ProgressDialog progressDialog = ProgressDialog.show(this, null, "Uploading...", true, false);
                new FirebaseHelper(this).uploadPicture(imageUri, StaticHelper.hod.getUsername(), FirebaseHelper.HOD, new FirebaseHelper.CompletionListener() {
                    @Override
                    public void onSuccess() {
                        progressDialog.dismiss();
                        Snackbar.make(parentView, "Picture Updated Successfully", Snackbar.LENGTH_SHORT).show();
                        new FirebaseHelper(HODMainActivity.this).loadImageInto(ivProfilePic, StaticHelper.hod.getUsername(), FirebaseHelper.HOD);
                    }

                    @Override
                    public void onFailed() {
                        Snackbar.make(parentView, "Failed to update picture", Snackbar.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            }
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - lastBackPressTime > 1000) {
            Snackbar.make(parentView, "Press again to exit", Snackbar.LENGTH_SHORT).show();
            lastBackPressTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }


    public void MyProfile(View view) {
        startActivity(new Intent(this, MyProfileActivity.class).putExtra("acType", FirebaseHelper.HOD));
    }

    public void AddStudent(View view) {
        startActivity(new Intent(this, RegisterUserActivity.class).putExtra("acType", FirebaseHelper.STUDENT));
    }

    public void Logout(View view) {
        File file = new File(getFilesDir() + "/" + "saved_ac.txt");
        if (file.exists()) {
            if (!file.delete()) {
                Snackbar.make(parentView, "An error occurred", Snackbar.LENGTH_SHORT).setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Logout(view);
                    }
                }).show();
                return;
            }
        }
        FirebaseHelper.LEAVE_INFO_DATABASE.child(StaticHelper.hod.getDepartment()).removeEventListener(leaveInfoListener);
        FirebaseHelper.REGISTRATION_INFO_DATABASE.child("totalStudents").child(StaticHelper.hod.getDepartment()).removeEventListener(totalStudentListener);
        StaticHelper.hod = null;
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();

    }

    public void PendingApplications(View view) {
        startActivity(new Intent(this, PendingApplicationActivity.class));
    }

    public void RemoveStudent(View view) {
        startActivity(new Intent(this, StudentListActivity.class));
    }

    public void UpdateProfilePic(View view) {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(permission, 1001);
        } else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, 1);
        }
    }

}
