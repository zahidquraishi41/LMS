package com.themiezz.lms;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.themiezz.lms.helper_classes.ErrorHandler;
import com.themiezz.lms.helper_classes.FirebaseHelper;
import com.themiezz.lms.helper_classes.Helper;
import com.themiezz.lms.helper_classes.StaticHelper;
import com.themiezz.lms.user_defined_classes.Application;
import com.themiezz.lms.user_defined_classes.LeaveInfo;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.themiezz.lms.helper_classes.FirebaseHelper.APPLICATIONS_DATABASE;

public class StudentMainActivity extends AppCompatActivity {
    Uri imageUri;
    View parentView;
    TextView tvTotalLeave, tvLeaveAccepted, tvAttendance, tvFullName;
    LeaveInfo leaveInfo;
    ImageView ivProfilePic;
    Integer currMonth;
    long lastBackPressTime = 0;
    ValueEventListener pendingApplicationListener, leaveInfoListener;
    Query pendingApplicationQuery, leaveInfoQuery;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);

        tvFullName = findViewById(R.id.tvFullName);
        tvLeaveAccepted = findViewById(R.id.tvLeaveAccepted);
        tvTotalLeave = findViewById(R.id.tvTotalLeave);
        tvAttendance = findViewById(R.id.tvAttendance);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        parentView = findViewById(R.id.parentView);
        currMonth = Integer.parseInt(new SimpleDateFormat("MM", Locale.getDefault()).format(new Date()));
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setTitle(null);
        progressDialog.setCancelable(false);

        new FirebaseHelper(StudentMainActivity.this).loadImageInto(ivProfilePic, StaticHelper.student.getStudentID(), FirebaseHelper.STUDENT);
        pendingApplicationQuery = APPLICATIONS_DATABASE.child(StaticHelper.student.getDepartment()).orderByChild("studentID").equalTo(StaticHelper.student.getStudentID()).limitToLast(1);
        leaveInfoQuery = FirebaseHelper.LEAVE_INFO_DATABASE.child(StaticHelper.student.getDepartment()).child(StaticHelper.student.getStudentID());
        pendingApplicationListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    StaticHelper.application = snapshot.getValue(Application.class);
                    StaticHelper.applicationReference = snapshot.getRef();
                }
                if (StaticHelper.application != null)
                    if (isApplicationExpired()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("status", "Expired");
                        map.put("remarks", "Expired");
                        new FirebaseHelper(StudentMainActivity.this).updateKey(StaticHelper.applicationReference, map, new FirebaseHelper.CompletionListener() {
                            @Override
                            public void onSuccess() {
                                new FirebaseHelper(StudentMainActivity.this).incrementValue(FirebaseHelper.LEAVE_INFO_DATABASE.child(StaticHelper.student.getDepartment()).child(StaticHelper.student.getStudentID()).child("leaveExpired"));
                                StaticHelper.application.setStatus("Expired");
                                StaticHelper.application.setReason("Expired");
                            }

                            @Override
                            public void onFailed() {
                                Snackbar.make(parentView, "Failed to update application", Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        leaveInfoListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    leaveInfo = dataSnapshot.getValue(LeaveInfo.class);
                    if (leaveInfo != null) {
                        if (leaveInfo.getTimesApplied() != null)
                            tvTotalLeave.setText(String.format("%s", leaveInfo.getTimesApplied() + ""));
                        if (leaveInfo.getTimesAccepted() != null)
                            tvLeaveAccepted.setText(String.format("%s", leaveInfo.getTimesAccepted() + ""));
                        if (leaveInfo.getLeaveDaysCount() != null) {
                            float attendance = (StaticHelper.settings.getWorkingDays() - leaveInfo.getLeaveDaysCount()) * 100 / StaticHelper.settings.getWorkingDays();
                            BigDecimal bigDecimal = new BigDecimal(attendance);
                            bigDecimal = bigDecimal.setScale(1, BigDecimal.ROUND_CEILING);
                            tvAttendance.setText(String.format("%s%%", bigDecimal));
                        } else {
                            tvAttendance.setText("100%");
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
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
                new FirebaseHelper(this).uploadPicture(imageUri, StaticHelper.student.getStudentID(), FirebaseHelper.STUDENT, new FirebaseHelper.CompletionListener() {
                    @Override
                    public void onSuccess() {
                        progressDialog.dismiss();
                        Snackbar.make(parentView, "Picture Updated Successfully", Snackbar.LENGTH_SHORT).show();
                        new FirebaseHelper(StudentMainActivity.this).loadImageInto(ivProfilePic, StaticHelper.student.getStudentID(), FirebaseHelper.STUDENT);
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
    protected void onResume() {
        if (ErrorHandler.isEmpty(StaticHelper.student)) {
            Toast.makeText(this, "An unexpected error has occurred", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvFullName.setText(StaticHelper.student.getFullName());
        pendingApplicationQuery.addValueEventListener(pendingApplicationListener);
        leaveInfoQuery.addValueEventListener(leaveInfoListener);
        super.onResume();
    }

    @Override
    protected void onStop() {
        pendingApplicationQuery.removeEventListener(pendingApplicationListener);
        leaveInfoQuery.removeEventListener(leaveInfoListener);
        super.onStop();
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
        StaticHelper.student = null;
        StaticHelper.application = null;
        StaticHelper.applicationReference = null;
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void ApplicationHistory(View view) {
        startActivity(new Intent(this, ApplicationHistoryActivity.class));
    }

    public void ApplicationStatus(final View view) {
        if (StaticHelper.application == null) {
            Snackbar.make(parentView, "You have not applied any application", Snackbar.LENGTH_SHORT).show();
        } else {
            startActivity(new Intent(this, ApplicationStatusActivity.class));
        }
    }

    public void ApplyForLeave(final View view) {
        startActivity(new Intent(this, ApplyForLeaveActivity.class));
    }

    public void UpdateProfile(View view) {
        startActivity(new Intent(this, MyProfileActivity.class).putExtra("acType", FirebaseHelper.STUDENT));
    }

    public void UpdatePicture(View view) {
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

    private boolean isApplicationExpired() {
        int currDate = Integer.parseInt(new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()));
        if (currDate > Helper.DateToInt(StaticHelper.application.getFromDate()) && StaticHelper.application.getStatus().equals("Pending")) {
            return true;
        }
        if (currDate > Helper.DateToInt(StaticHelper.application.getToDate()) && StaticHelper.application.getStatus().equals("Pending")) {
            return true;
        } else return currDate > Helper.DateToInt(StaticHelper.application.getToDate());
    }

}
