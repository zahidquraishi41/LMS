package com.themiezz.lms;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.themiezz.lms.helper_classes.FirebaseHelper;
import com.themiezz.lms.helper_classes.Helper;
import com.themiezz.lms.helper_classes.IAEM;
import com.themiezz.lms.helper_classes.StaticHelper;
import com.themiezz.lms.user_defined_classes.Application;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ApplyForLeaveActivity extends AppCompatActivity {
    EditText etFromDate, etToDate, etReason, etAttachment;
    RelativeLayout rlAttachmentPreview;
    ImageView ivAttachmentPreview;
    IAEM iaem;
    DatePickerDialog datePickerDialog;
    int day, month, year;
    String currDate;
    Uri imageUri;
    boolean delete = false;
    Integer leaveDaysCount;
    ArrayList<Application> applications;
    boolean isApplicationsLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_for_leave);

        etToDate = findViewById(R.id.etToDate);
        etFromDate = findViewById(R.id.etFromDate);
        etReason = findViewById(R.id.etReason);
        etAttachment = findViewById(R.id.etAttachment);
        ivAttachmentPreview = findViewById(R.id.ivAttachmentPreview);
        rlAttachmentPreview = findViewById(R.id.rlAttachmentPreview);

        leaveDaysCount = 0;
        isApplicationsLoaded = false;
        applications = new ArrayList<>();
        iaem = new IAEM((TextView) findViewById(R.id.tvErrorMessage));
        day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        month = Calendar.getInstance().get(Calendar.MONTH);
        year = Calendar.getInstance().get(Calendar.YEAR);
        currDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        FirebaseHelper.APPLICATIONS_DATABASE.child(StaticHelper.student.getDepartment())
                .orderByChild("studentID")
                .equalTo(StaticHelper.student.getStudentID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("ZQ", "onDataChange: " + dataSnapshot.toString());
                        for (DataSnapshot snapshot : dataSnapshot.getChildren())
                            applications.add(snapshot.getValue(Application.class));
                        isApplicationsLoaded = true;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        isApplicationsLoaded = true;
                    }
                });

        etFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FromDate(view);
            }
        });
        etToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToDate(view);
            }
        });
        etAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectAttachment();
            }
        });
        ivAttachmentPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ApplyForLeaveActivity.this, AttachmentViewActivity.class);
                intent.putExtra("studentID", StaticHelper.student.getStudentID());
                intent.putExtra("dateApplied", etFromDate.getText().toString());
                intent.putExtra("imgUri", imageUri);
                startActivity(intent);
            }
        });
        FirebaseHelper.LEAVE_INFO_DATABASE
                .child(StaticHelper.student.getDepartment())
                .child(StaticHelper.student.getStudentID())
                .child("leaveDaysCount")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        leaveDaysCount = dataSnapshot.getValue(Integer.class);
                        if (leaveDaysCount == null)
                            leaveDaysCount = 0;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    public void Apply(View view) {
        if (!isApplicationsLoaded) {
            iaem.showMessage("Please Wait...");
            return;
        }
        final String reason = etReason.getText().toString(),
                fromDate = etFromDate.getText().toString(),
                toDate = etToDate.getText().toString();
        if (fromDate.equals(""))
            iaem.showMessage("Please select from date");
        else if (toDate.equals(""))
            iaem.showMessage("Please select to date");
        else if (reason.equals(""))
            iaem.showMessage("Reason cannot be empty");
        else if (!applications.isEmpty()) {
            Log.d("ZQ", "Apply: not empty");
            if (Helper.isBetween(fromDate, toDate, applications))
                iaem.showMessage("You already have an application on this date");
        }
        if (iaem.isVisible())
            return;

        if (Helper.getDays(fromDate, toDate) == 0) {
            iaem.showMessage("Please select valid date");
            return;
        }
        int percentage = (StaticHelper.settings.getWorkingDays() - leaveDaysCount) * 100 / StaticHelper.settings.getWorkingDays();
        int newPercentage = (StaticHelper.settings.getWorkingDays() - leaveDaysCount - Helper.getDays(fromDate, toDate)) * 100 / StaticHelper.settings.getWorkingDays();
        if (percentage < 75 && etAttachment.getText().toString().equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Warning");
            builder.setMessage("Your Attendance is below 75%. You must have to provide an attachment in order to apply");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            builder.show();
            return;
        }
        if (newPercentage < 75 && etAttachment.getText().toString().equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Warning");
            builder.setMessage("Your Attendance will go below 75%. You must have to provide an attachment in order to apply");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            builder.show();
            return;
        }
        if (Helper.getDays(fromDate, toDate) > 3 && etAttachment.getText().toString().equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Warning");
            builder.setMessage("You have to provide an attachment when taking leave for more than 3 days");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            builder.show();
            return;
        }
        if (etAttachment.getText().toString().equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // TODO update message and title
            builder.setMessage("You have not provided any Attachment\nAre you sure you want to continue");
            builder.setTitle("Confirmation");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    final ProgressDialog progressDialog = ProgressDialog.show(ApplyForLeaveActivity.this, null, "Applying...", true, false);
                    new FirebaseHelper(ApplyForLeaveActivity.this).addApplication(new Application(currDate, fromDate, toDate, reason, "Pending", "Pending", StaticHelper.student.getStudentID()), StaticHelper.student.getDepartment(), new FirebaseHelper.CompletionListener() {
                        @Override
                        public void onSuccess() {
                            progressDialog.dismiss();
                            Toast.makeText(ApplyForLeaveActivity.this, "Applied Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onFailed() {
                            progressDialog.dismiss();
                            iaem.showMessage("Failed to apply");
                        }
                    });
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            builder.show();
        } else {
            final ProgressDialog progressDialog = ProgressDialog.show(ApplyForLeaveActivity.this, null, "Applying...", true, false);
            new FirebaseHelper(ApplyForLeaveActivity.this).uploadAttachment(imageUri, fromDate, new FirebaseHelper.CompletionListener() {
                @Override
                public void onSuccess() {
                    new FirebaseHelper(ApplyForLeaveActivity.this).addApplication(new Application(currDate, fromDate, toDate, reason, "Pending", "Pending", StaticHelper.student.getStudentID()), StaticHelper.student.getDepartment(), new FirebaseHelper.CompletionListener() {
                        @Override
                        public void onSuccess() {
                            progressDialog.dismiss();
                            Toast.makeText(ApplyForLeaveActivity.this, "Applied Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onFailed() {
                            progressDialog.dismiss();
                            iaem.showMessage("Failed to apply");
                        }
                    });
                }

                @Override
                public void onFailed() {
                    iaem.showMessage("Failed to upload attachment");
                    progressDialog.dismiss();
                }
            });

        }
    }

    public void ToDate(View view) {
        if (etFromDate.getText().toString().equals("")) {
            iaem.showMessage("Select from date first");
            return;
        }
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month++;
                String prevToDate = etToDate.getText().toString();
                if (day < 10 && month < 10) {
                    etToDate.setText("0" + day + "/" + "0" + month + "/" + year);
                } else if (day < 10) {
                    etToDate.setText("0" + day + "/" + month + "/" + year);
                } else if (month < 10) {
                    etToDate.setText(day + "/" + "0" + month + "/" + year);
                } else {
                    etToDate.setText(day + "/" + month + "/" + year);
                }
                String toDate = etToDate.getText().toString();
                String fromDate = etFromDate.getText().toString();
                if (Helper.isHoliday(toDate)) {
                    iaem.showMessage("Please select a valid date", IAEM.WARNING);
                    if (!prevToDate.equals(""))
                        etToDate.setText(prevToDate);
                    else
                        etToDate.setText("");
                    return;
                }
                if (Helper.DateToInt(toDate) < Helper.DateToInt(fromDate)) {
                    if (Helper.DateToInt(toDate) < Helper.DateToInt(currDate)) {
                        iaem.showMessage("Please select a valid date", IAEM.WARNING);
                        if (!prevToDate.equals(""))
                            etToDate.setText(prevToDate);
                        else
                            etToDate.setText("");
                    } else {
                        etFromDate.setText("");
                    }
                    return;
                }
                if (Helper.getDays(fromDate, toDate) > 15) {
                    iaem.showMessage("You cannot apply for more than 15 days", IAEM.WARNING);
                    if (!prevToDate.equals(""))
                        etToDate.setText(prevToDate);
                    else
                        etToDate.setText("");
                    return;
                }

                float newAttendance = ((StaticHelper.settings.getWorkingDays() - leaveDaysCount - Helper.getDays(fromDate, toDate)) * 100) / StaticHelper.settings.getWorkingDays();
                BigDecimal bigDecimal = new BigDecimal(newAttendance);
                bigDecimal = bigDecimal.setScale(1, BigDecimal.ROUND_CEILING);
                iaem.showMessage("Your attendance will become " + bigDecimal + "%", IAEM.INFORMATIVE);
            }
        }, year, month, day);
        datePickerDialog.show();

    }

    public void FromDate(View view) {
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month++;
                String prevFromDate = etFromDate.getText().toString();
                if (day < 10 && month < 10) {
                    etFromDate.setText("0" + day + "/" + "0" + month + "/" + year);
                } else if (day < 10) {
                    etFromDate.setText("0" + day + "/" + month + "/" + year);
                } else if (month < 10) {
                    etFromDate.setText(day + "/" + "0" + month + "/" + year);
                } else {
                    etFromDate.setText(day + "/" + month + "/" + year);
                }
                String fromDate = etFromDate.getText().toString();
                String toDate = etToDate.getText().toString();
                if (Helper.isHoliday(fromDate)) {
                    iaem.showMessage("Please select a valid date", IAEM.WARNING);
                    if (!prevFromDate.equals(""))
                        etFromDate.setText(prevFromDate);
                    else
                        etFromDate.setText("");
                    return;
                }
                if (Helper.DateToInt(fromDate) < Helper.DateToInt(currDate)) {
                    iaem.showMessage("Please select a valid date", IAEM.WARNING);
                    if (!prevFromDate.equals(""))
                        etFromDate.setText(prevFromDate);
                    else
                        etFromDate.setText("");
                    return;
                }
                if (!toDate.equals("")) {
                    if (Helper.DateToInt(fromDate) > Helper.DateToInt(toDate)) {
                        etToDate.setText("");
                    }
                }
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    public void SelectAttachment() {
        delete = true;
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
                if (imageUri != null) {
                    ivAttachmentPreview.setImageURI(imageUri);
                    rlAttachmentPreview.setVisibility(View.VISIBLE);
                    String fileName = getFileName(imageUri);
                    etAttachment.setText(fileName);
                }
            }
    }

    public void DeleteAttachment(View view) {
        rlAttachmentPreview.setVisibility(View.GONE);
        imageUri = null;
        etAttachment.setText("");
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri == null) return "file";
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result == null) return "file";
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

}