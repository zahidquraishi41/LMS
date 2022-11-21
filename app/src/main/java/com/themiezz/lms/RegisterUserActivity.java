package com.themiezz.lms;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.themiezz.lms.helper_classes.ErrorHandler;
import com.themiezz.lms.helper_classes.FirebaseHelper;
import com.themiezz.lms.helper_classes.IAEM;
import com.themiezz.lms.helper_classes.StaticHelper;
import com.themiezz.lms.helper_classes.ViewStateChanger;
import com.themiezz.lms.user_defined_classes.Department;
import com.themiezz.lms.user_defined_classes.HOD;
import com.themiezz.lms.user_defined_classes.Student;

import java.util.ArrayList;
import java.util.HashMap;

public class RegisterUserActivity extends AppCompatActivity {
    private static final String TAG = "ZQRegisterHOD";
    int acType;
    EditText etFullName, etUserID, etPassword, etPhoneNumber, etParentPhoneNumber, etDepartmentName;
    TextInputLayout tilFullName, tilUserID, tilPassword, tilPhoneNumber, tilParentPhoneNumber, tilDepartmentName;
    TextView tvForgotPassword;
    Spinner spnDepartment;
    Button btnLogin;
    String department;
    IAEM iaem;
    HashMap<String, String> departmentMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        acType = getIntent().getIntExtra("acType", 9);
        if (acType == 9) {
            Toast.makeText(this, "An unexpected error has occurred", Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else if (acType != FirebaseHelper.HOD && acType != FirebaseHelper.STUDENT) {
            Toast.makeText(this, "An unexpected error has occurred", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        iaem = new IAEM((TextView) findViewById(R.id.tvErrorMessage));
        etFullName = findViewById(R.id.etFullName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etParentPhoneNumber = findViewById(R.id.etParentPhoneNumber);
        etPassword = findViewById(R.id.etPassword);
        etUserID = findViewById(R.id.etUserID);
        etDepartmentName = findViewById(R.id.etDepartmentName);
        tilFullName = findViewById(R.id.tilFullName);
        tilPhoneNumber = findViewById(R.id.tilPhoneNumber);
        tilParentPhoneNumber = findViewById(R.id.tilParentPhoneNumber);
        tilPassword = findViewById(R.id.tilPassword);
        tilUserID = findViewById(R.id.tilUserID);
        tilDepartmentName = findViewById(R.id.tilDepartmentName);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnLogin = findViewById(R.id.btnLogin);
        spnDepartment = findViewById(R.id.spnDepartment);

        departmentMap = new HashMap<>();
        if (acType == FirebaseHelper.STUDENT) {
            new ViewStateChanger(spnDepartment).lockView();
            tilParentPhoneNumber.setVisibility(View.VISIBLE);
            tilUserID.setHint("Student ID");
        } else {
            tilUserID.setHint("Username");
        }
        final ArrayList<String> departmentList = new ArrayList<>();
        if (acType == FirebaseHelper.HOD)
            departmentList.add("Select Department");
        else {
            department = StaticHelper.hod.getDepartment();
            departmentList.add(StaticHelper.hod.getDepartment());
        }

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, departmentList);
        spnDepartment.setAdapter(arrayAdapter);
        if (acType == FirebaseHelper.HOD) {
            spnDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    department = adapterView.getItemAtPosition(i).toString();
                    if (department.equals("Add New Department")) {
                        tilDepartmentName.setVisibility(View.VISIBLE);
                    } else {
                        tilDepartmentName.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            FirebaseHelper.DEPARTMENT_DATABASE.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        // When no department is added
                        if (acType == FirebaseHelper.HOD) {
                            arrayAdapter.add("Add New Department");
                            arrayAdapter.notifyDataSetChanged();
                            return;
                        }
                    }
                    Department department;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        department = snapshot.getValue(Department.class);
                        if (department != null) {
                            departmentMap.put(department.getDepartmentName(), department.getHod());
                            arrayAdapter.add(department.getDepartmentName());
                        }
                    }
                    if (acType == FirebaseHelper.HOD)
                        arrayAdapter.add("Add New Department");
                    arrayAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    iaem.showMessage("Failed to retrieve department list");
                }
            });
        }
    }

    public void Submit(View view) {
        final String userID = etUserID.getText().toString();
        final String password = etPassword.getText().toString();
        final String phoneNumber = etPhoneNumber.getText().toString(),
                parentPhoneNumber = etParentPhoneNumber.getText().toString(),
                fullName = etFullName.getText().toString(),
                newDepartment = etDepartmentName.getText().toString();

        if (fullName.equals(""))
            iaem.showMessage("Full name cannot be empty");
        else if (TextUtils.isEmpty(userID)) {
            if (etUserID.getHint().equals("Student ID"))
                iaem.showMessage("Student ID cannot be empty");
            else
                iaem.showMessage("Username cannot be empty");
        } else if (!userID.matches("^[a-zA-Z0-9]+$")) {
            if (acType == FirebaseHelper.STUDENT)
                iaem.showMessage("Student ID must be alphanumeric");
            else {
                iaem.showMessage("Username must be alphanumeric");
            }
        } else if (TextUtils.isEmpty(password))
            iaem.showMessage("Password cannot be empty");
        else if (!ErrorHandler.isValidNumber(phoneNumber))
            iaem.showMessage("Invalid phone number");
        else if (department.equals("Add New Department")) {
            if (newDepartment.equals("")) {
                iaem.showMessage("Department name cannot be empty");
            }
        } else if (TextUtils.isEmpty(password))
            iaem.showMessage("Password field cannot be empty");
        else if (department.equals("Select Department"))
            iaem.showMessage("Please select a department");
        else {
            if (acType == FirebaseHelper.STUDENT)
                if (!ErrorHandler.isValidNumber(parentPhoneNumber))
                    iaem.showMessage("Invalid parent's number");
                else if (parentPhoneNumber.equals(phoneNumber))
                    iaem.showMessage("Student and Parent number cannot be same");
        }

        if (iaem.isVisible()) {
            return;
        }

        final ProgressDialog progressDialog = ProgressDialog.show(this, null, "Registering...", true, false);
        final FirebaseHelper.CompletionListener listener = new FirebaseHelper.CompletionListener() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                Toast.makeText(RegisterUserActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed() {
                progressDialog.dismiss();
                Toast.makeText(RegisterUserActivity.this, "Failed to Register!", Toast.LENGTH_SHORT).show();
            }
        };
        final FirebaseHelper.KeyExistListener phoneNumberListener = new FirebaseHelper.KeyExistListener() {
            @Override
            public void onFailed() {
                iaem.showMessage("Failed to connect to internet");
            }

            @Override
            public void onFound() {
                progressDialog.dismiss();
                iaem.showMessage("This phone number is already used");
            }

            @Override
            public void onNotFound() {
                String dept;
                if (acType == FirebaseHelper.STUDENT) {
                    progressDialog.dismiss();
                    RegisterStudent(new Student(userID, password, "+91" + phoneNumber, "+91" + parentPhoneNumber, fullName, department));
                } else {
                    if (department.equals("Add New Department"))
                        dept = newDepartment;
                    else
                        dept = department;
                    progressDialog.dismiss();
                    RegisterHOD(new HOD(userID, password, "+91" + phoneNumber, dept, fullName));
                }
            }
        };
        FirebaseHelper.KeyExistListener userIDListener = new FirebaseHelper.KeyExistListener() {
            @Override
            public void onFailed() {
                iaem.showMessage("Failed to connect to internet");
            }

            @Override
            public void onFound() {
                progressDialog.dismiss();
                if (acType == FirebaseHelper.STUDENT)
                    iaem.showMessage("This ID is already used");
                else
                    iaem.showMessage("This username is already used");
            }

            @Override
            public void onNotFound() {
                if (acType == FirebaseHelper.STUDENT)
                    new FirebaseHelper(RegisterUserActivity.this).valueUsed(FirebaseHelper.STUDENTS_DATABASE, "phoneNumber", "+91" + phoneNumber, phoneNumberListener);
                else
                    new FirebaseHelper(RegisterUserActivity.this).valueUsed(FirebaseHelper.HOD_DATABASE, "phoneNumber", "+91" + phoneNumber, phoneNumberListener);
            }
        };

        if (acType == FirebaseHelper.STUDENT)
            new FirebaseHelper(this).keyExist(FirebaseHelper.STUDENTS_DATABASE.child(userID).child("studentID"), userID, userIDListener);
        else
            new FirebaseHelper(this).keyExist(FirebaseHelper.HOD_DATABASE.child(userID).child("username"), userID, userIDListener);
    }

    private void RegisterStudent(Student student) {
        final ProgressDialog progressDialog = ProgressDialog.show(this, null, "Registering...", true, false);
        new FirebaseHelper(RegisterUserActivity.this).addUser(student, new FirebaseHelper.CompletionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(RegisterUserActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                finish();
            }

            @Override
            public void onFailed() {
                Toast.makeText(RegisterUserActivity.this, "Failed to Register", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();

            }
        });

    }

    private void RegisterHOD(HOD hod) {
        final ProgressDialog progressDialog = ProgressDialog.show(this, null, "Registering...", true, false);
        if (departmentMap.containsKey(hod.getDepartment())) {
            String s = departmentMap.get(hod.getDepartment());
            if (s != null) {
                new FirebaseHelper(RegisterUserActivity.this).updateKey(FirebaseHelper.DEPARTMENT_DATABASE.child(hod.getDepartment()), "hod", hod.getUsername());
                FirebaseHelper.HOD_DATABASE.child(s).removeValue();
                FirebaseHelper.HOD_DATABASE.child(hod.getUsername()).setValue(hod, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterUserActivity.this, "Successfully Registered", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            } else {
                progressDialog.dismiss();
                iaem.showMessage("Failed to connect to internet");
            }
        } else {
            FirebaseHelper.DEPARTMENT_DATABASE.child(hod.getDepartment()).setValue(new Department(hod.getDepartment(), hod.getUsername()));
            new FirebaseHelper(this).incrementValue(FirebaseHelper.REGISTRATION_INFO_DATABASE.child("totalDepartments"));
            new FirebaseHelper(this).incrementValue(FirebaseHelper.REGISTRATION_INFO_DATABASE.child("totalHODs"));
            FirebaseHelper.HOD_DATABASE.child(hod.getUsername()).setValue(hod, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterUserActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }


}
