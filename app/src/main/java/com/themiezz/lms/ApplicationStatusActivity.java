package com.themiezz.lms;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.themiezz.lms.helper_classes.ConnectionStatus;
import com.themiezz.lms.helper_classes.FirebaseHelper;
import com.themiezz.lms.helper_classes.Helper;
import com.themiezz.lms.helper_classes.StaticHelper;
import com.themiezz.lms.helper_classes.ViewStateChanger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ApplicationStatusActivity extends AppCompatActivity {
    View parentView;
    String currDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_status);

        parentView = findViewById(R.id.parentView);
        EditText etAppliedOn, etFromDate, etToDate, etReason, etStatus, etRemarks;
        final ImageButton ibAttachment;
        etFromDate = findViewById(R.id.etFromDate);
        etToDate = findViewById(R.id.etToDate);
        etReason = findViewById(R.id.etReason);
        etAppliedOn = findViewById(R.id.etAppliedOn);
        etStatus = findViewById(R.id.etStatus);
        etRemarks = findViewById(R.id.tvRemarks);
        ibAttachment = findViewById(R.id.ibAttachment);

        etReason.setKeyListener(null);
        etRemarks.setKeyListener(null);
        etFromDate.setKeyListener(null);
        etToDate.setKeyListener(null);
        etReason.setKeyListener(null);
        etAppliedOn.setKeyListener(null);
        etStatus.setKeyListener(null);
        new ViewStateChanger(ibAttachment).lockView();

        etReason.setMovementMethod(new ScrollingMovementMethod());
        etRemarks.setMovementMethod(new ScrollingMovementMethod());
        currDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());


        if (StaticHelper.application.getStatus().equals("Pending"))
            etStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.application_pending, 0);
        else if (StaticHelper.application.getStatus().equals("Accepted"))
            etStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.application_accepted, 0);
        else
            etStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.application_declined, 0);

        etAppliedOn.setText(StaticHelper.application.getDateApplied());
        etFromDate.setText(StaticHelper.application.getFromDate());
        etToDate.setText(StaticHelper.application.getToDate());
        etReason.setText(StaticHelper.application.getReason());
        etStatus.setText(StaticHelper.application.getStatus());
        etRemarks.setText(StaticHelper.application.getRemarks());
        Button cancelApplication = findViewById(R.id.btnCancelApplication);
        if (!StaticHelper.application.getStatus().equals("Pending"))
            new ViewStateChanger(cancelApplication).lockView();
        if (StaticHelper.application.getStatus().equals("Accepted") && Helper.DateToInt(currDate) <= Helper.DateToInt(StaticHelper.application.getToDate())) {
            new ViewStateChanger(cancelApplication).unlockView();
        }

        if (StaticHelper.application.getStatus().equals("Accepted") || StaticHelper.application.getStatus().equals("Pending"))
            new FirebaseHelper(this).downloadAttachment(StaticHelper.application.getStudentID(), StaticHelper.application.getFromDate(), new FirebaseHelper.CompletionListener() {
                @Override
                public void onSuccess() {
                    new ViewStateChanger(ibAttachment).unlockView();
                }

                @Override
                public void onFailed() {

                }
            });
    }

    public void CancelApplication(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel Application");
        builder.setMessage("Are you sure you want to cancel this application?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (StaticHelper.application.getStatus().equals("Pending"))
                    RemoveApplication();
                else if (Helper.DateToInt(currDate) < Helper.DateToInt(StaticHelper.application.getFromDate()))
                    RemoveApplication();
                else
                    CancelApplication();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();
    }

    private void RemoveApplication() {
        if (ConnectionStatus.isConnected(this)) {
            final String fromDate = StaticHelper.application.getFromDate(),
                    toDate = StaticHelper.application.getToDate();
            final ProgressDialog progressDialog = ProgressDialog.show(ApplicationStatusActivity.this, null, "Removing...", true, false);
            if (StaticHelper.application.getStatus().equals("Accepted")) {
                final DatabaseReference reference = FirebaseHelper.LEAVE_INFO_DATABASE.child(StaticHelper.student.getDepartment()).child(StaticHelper.student.getStudentID()).child("leaveDaysCount");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Integer leaveDaysCount = dataSnapshot.getValue(Integer.class);
                        if (leaveDaysCount != null) {
                            leaveDaysCount -= (Helper.getDays(fromDate, toDate));
                            reference.setValue(leaveDaysCount);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
            StaticHelper.applicationReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        String dateApplied = StaticHelper.application.getFromDate().replaceAll("\\W", "");
                        new FirebaseHelper(ApplicationStatusActivity.this).decrementValue(FirebaseHelper.LEAVE_INFO_DATABASE.child(StaticHelper.student.getDepartment()).child(StaticHelper.student.getStudentID()).child("totalLeave"));
                        FirebaseHelper.ATTACHMENT_STORAGE.child(StaticHelper.student.getStudentID()+ "_" + dateApplied).delete();
                        StaticHelper.application = null;
                        StaticHelper.applicationReference = null;
                        progressDialog.dismiss();
                        finish();
                    } else {
                        progressDialog.dismiss();
                        Snackbar.make(parentView, "Failed to remove application", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Snackbar.make(parentView, "Failed to connect to server", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void CancelApplication() {
        if (ConnectionStatus.isConnected(this)) {
            final ProgressDialog progressDialog = ProgressDialog.show(ApplicationStatusActivity.this, null, "Removing...", true, false);
            final DatabaseReference reference = FirebaseHelper.LEAVE_INFO_DATABASE.child(StaticHelper.student.getDepartment()).child(StaticHelper.student.getStudentID()).child("leaveDaysCount");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Integer leaveDaysCount = dataSnapshot.getValue(Integer.class);
                    leaveDaysCount -= (Helper.getDays(currDate, StaticHelper.application.getToDate()) - 1);
                    reference.setValue(leaveDaysCount).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            StaticHelper.applicationReference.child("status").setValue("Cancelled");
                            StaticHelper.applicationReference.child("toDate").setValue(currDate);
                            progressDialog.dismiss();
                            StaticHelper.application = null;
                            StaticHelper.applicationReference = null;
                            finish();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Snackbar.make(parentView, "Failed to Update", Snackbar.LENGTH_SHORT).show();
                }
            });
        } else {
            Snackbar.make(parentView, "Failed to connect to server", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void ViewAttachment(View view) {
        File localFile = new File(getFilesDir() + "/attachment/" + StaticHelper.application.getStudentID() + "_" + StaticHelper.application.getDateApplied().replaceAll("\\W", ""));
        if (!localFile.exists())
            Toast.makeText(this, "No Attachment have been provided", Toast.LENGTH_SHORT).show();
        else {
            Intent intent = new Intent(this, AttachmentViewActivity.class);
            intent.putExtra("studentID", StaticHelper.application.getStudentID());
            intent.putExtra("dateApplied", StaticHelper.application.getFromDate());
            startActivity(intent);
        }
    }

}
