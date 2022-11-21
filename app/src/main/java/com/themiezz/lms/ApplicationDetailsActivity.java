package com.themiezz.lms;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.themiezz.lms.helper_classes.FirebaseHelper;
import com.themiezz.lms.helper_classes.Helper;
import com.themiezz.lms.helper_classes.StaticHelper;
import com.themiezz.lms.user_defined_classes.LeaveInfo;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ApplicationDetailsActivity extends AppCompatActivity implements TakeActionDialog.ActionListener {
    TextView tvNoOfDays, tvFromToDate, tvUserID, tvReason, tvLastLeaveTaken, tvAttendance, tvNoOfDaysUnit;
    EditText etRemarks;
    View parentView;
    Integer currMonth;
    String parentPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_details);

        parentView = findViewById(R.id.parentView);
        tvNoOfDays = findViewById(R.id.tvNoOfDays);
        tvFromToDate = findViewById(R.id.tvFromToDate);
        tvUserID = findViewById(R.id.tvUserID);
        tvReason = findViewById(R.id.tvReason);
        tvLastLeaveTaken = findViewById(R.id.tvLastLeaveTaken);
        tvAttendance = findViewById(R.id.tvAttendance);
        tvNoOfDaysUnit = findViewById(R.id.tvNoOfDaysUnit);
        etRemarks = findViewById(R.id.etRemarks);
        currMonth = Integer.parseInt(new SimpleDateFormat("MM", Locale.getDefault()).format(new Date()));

        long diff = Helper.getDays(StaticHelper.application.getFromDate(), StaticHelper.application.getToDate());
        if (diff < 10)
            tvNoOfDays.setText("0" + diff);
        else
            tvNoOfDays.setText(String.valueOf(diff));
        if (diff > 1)
            tvNoOfDaysUnit.setText("DAYS");

        tvFromToDate.setText(String.format("%s - %s", StaticHelper.application.getFromDate(), StaticHelper.application.getToDate()));
        tvUserID.setText(String.format("Applied by %s", StaticHelper.application.getStudentID()));
        tvReason.setText(StaticHelper.application.getReason());
        FirebaseHelper.LEAVE_INFO_DATABASE.child(StaticHelper.hod.getDepartment()).child(StaticHelper.application.getStudentID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    LeaveInfo leaveInfo = dataSnapshot.getValue(LeaveInfo.class);
                    if (leaveInfo != null) {
                        if (leaveInfo.getLastLeave() != null) {
                            tvLastLeaveTaken.setText(leaveInfo.getLastLeave());
                        }
                        if (leaveInfo.getLeaveDaysCount() != null) {
                            float attendance = (StaticHelper.settings.getWorkingDays() - leaveInfo.getLeaveDaysCount()) * 100 / StaticHelper.settings.getWorkingDays();
                            float newAttendance = (StaticHelper.settings.getWorkingDays() - leaveInfo.getLeaveDaysCount() - Helper.getDays(StaticHelper.application.getFromDate(), StaticHelper.application.getToDate())) * 100 / StaticHelper.settings.getWorkingDays();
                            BigDecimal bigDecimal = new BigDecimal(attendance);
                            bigDecimal = bigDecimal.setScale(1, BigDecimal.ROUND_CEILING);
                            BigDecimal bigDecimal2 = new BigDecimal(newAttendance);
                            bigDecimal2 = bigDecimal2.setScale(1, BigDecimal.ROUND_CEILING);
                            tvAttendance.setText(bigDecimal + "% --> " + bigDecimal2 + "%");
                        } else {
                            float newAttendance = (StaticHelper.settings.getWorkingDays() - Helper.getDays(StaticHelper.application.getFromDate(), StaticHelper.application.getToDate())) * 100 / StaticHelper.settings.getWorkingDays();
                            BigDecimal bigDecimal2 = new BigDecimal(newAttendance);
                            bigDecimal2 = bigDecimal2.setScale(1, BigDecimal.ROUND_CEILING);
                            tvAttendance.setText(100 + "% --> " + bigDecimal2 + "%");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        FirebaseHelper.STUDENTS_DATABASE.child(StaticHelper.application.getStudentID()).child("parentPhoneNumber").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                parentPhoneNumber = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        new FirebaseHelper(this).downloadAttachment(StaticHelper.application.getStudentID(), StaticHelper.application.getFromDate(), new FirebaseHelper.CompletionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailed() {

            }
        });
    }

    public void Decline() {
        String remarks = etRemarks.getText().toString();
        Map<String, Object> map = new HashMap<>();
        if (!remarks.equals(""))
            map.put("remarks", remarks);
        else
            map.put("remarks", "No Remarks");
        map.put("status", "Declined");
        DeclineApplication(map);
    }

    public void Accept() {
        String remarks = etRemarks.getText().toString();
        final Map<String, Object> map = new HashMap<>();
        map.put("status", "Accepted");
        if (!remarks.equals(""))
            map.put("remarks", remarks);
        else
            map.put("remarks", "No Remarks");
        AcceptApplication(map);
    }

    private void AcceptApplication(Map<String, Object> mapAccept) {
        final ProgressDialog progressDialog = ProgressDialog.show(this, null, "Updating...", true, false);
        final DatabaseReference reference = StaticHelper.applicationReference;
        final String fromDate = StaticHelper.application.getFromDate(), toDate = StaticHelper.application.getToDate(), studentID = StaticHelper.application.getStudentID();
        new FirebaseHelper(this).updateKey(reference, mapAccept, new FirebaseHelper.CompletionListener() {
            @Override
            public void onSuccess() {
                new FirebaseHelper(ApplicationDetailsActivity.this).incrementValue(FirebaseHelper.LEAVE_INFO_DATABASE.child(StaticHelper.hod.getDepartment()).child(studentID).child("timesAccepted"));
                new FirebaseHelper(ApplicationDetailsActivity.this).incrementValue(FirebaseHelper.LEAVE_INFO_DATABASE.child(StaticHelper.hod.getDepartment()).child(studentID).child("leaveDaysCount"), Helper.getDays(fromDate, toDate));
                progressDialog.dismiss();
                Toast.makeText(ApplicationDetailsActivity.this, "Application Accepted Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed() {
                progressDialog.dismiss();
                Snackbar.make(parentView, "Failed to Update", Snackbar.LENGTH_LONG).show();
            }
        });

    }

    private void DeclineApplication(Map<String, Object> mapReject) {
        final DatabaseReference reference = StaticHelper.applicationReference;
        final ProgressDialog progressDialog = ProgressDialog.show(this, null, "Updating...", true, false);
        new FirebaseHelper(this).updateKey(reference, mapReject, new FirebaseHelper.CompletionListener() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                Toast.makeText(ApplicationDetailsActivity.this, "Application Declined Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed() {
                progressDialog.dismiss();
                Snackbar.make(parentView, "Failed to Update", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public void TakeAnAction(View view) {
        TakeActionDialog takeActionDialog = new TakeActionDialog();
        takeActionDialog.show(getSupportFragmentManager(), "takeActionDialog");
    }

    public void CallParent() {
        if (parentPhoneNumber == null) {
            Snackbar.make(parentView, "Failed to Call", Snackbar.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + parentPhoneNumber.substring(3)));
        startActivity(intent);

    }

    public void ViewAttachment() {
        File localFile = new File(getFilesDir() + "/attachment/" + StaticHelper.application.getStudentID() + "_" + StaticHelper.application.getFromDate().replaceAll("\\W", ""));
        if (!localFile.exists())
            Toast.makeText(this, "No Attachment have been provided", Toast.LENGTH_SHORT).show();
        else {
            Intent intent = new Intent(this, AttachmentViewActivity.class);
            intent.putExtra("studentID", StaticHelper.application.getStudentID());
            intent.putExtra("dateApplied", StaticHelper.application.getFromDate());
            startActivity(intent);
        }
    }

    @Override
    public void onClick(int id) {
        if (id == TakeActionDialog.ACCEPT_APPLICATION) Accept();
        else if (id == TakeActionDialog.DECLINE_APPLICATION)
            Decline();
        else if (id == TakeActionDialog.CALL_PARENT)
            CallParent();
        else if (id == TakeActionDialog.VIEW_ATTACHMENT)
            ViewAttachment();
    }

}
