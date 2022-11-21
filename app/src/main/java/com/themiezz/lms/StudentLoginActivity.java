package com.themiezz.lms;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.themiezz.lms.helper_classes.ErrorHandler;
import com.themiezz.lms.helper_classes.FirebaseHelper;
import com.themiezz.lms.helper_classes.IAEM;
import com.themiezz.lms.helper_classes.StaticHelper;
import com.themiezz.lms.helper_classes.ViewStateChanger;
import com.themiezz.lms.user_defined_classes.Department;
import com.themiezz.lms.user_defined_classes.Student;

import java.util.ArrayList;

public class StudentLoginActivity extends AppCompatActivity {
    EditText etFullName, etStudentID, etPassword, etPhoneNumber, etParentPhoneNumber;
    TextInputLayout tilFullName, tilStudentID, tilPassword, tilPhoneNumber, tilParentPhoneNumber;
    TextView tvForgotPassword,tvWelcomeText;
    Spinner spnDepartment;
    Button btnLogin;
    LinearLayout llSignUp;
    ViewStateChanger signUpViews, nonSignUpViews, defViews;
    String department;
    IAEM iaem;
    ImageView ivLMSIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login);

        iaem = new IAEM((TextView) findViewById(R.id.tvErrorMessage));
        etFullName = findViewById(R.id.etFullName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etParentPhoneNumber = findViewById(R.id.etParentPhoneNumber);
        etPassword = findViewById(R.id.etPassword);
        etStudentID = findViewById(R.id.etStudentID);
        tilFullName = findViewById(R.id.tilFullName);
        tilPhoneNumber = findViewById(R.id.tilPhoneNumber);
        tilParentPhoneNumber = findViewById(R.id.tilParentPhoneNumber);
        tilPassword = findViewById(R.id.tilPassword);
        tilStudentID = findViewById(R.id.tilStudentID);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        ivLMSIcon = findViewById(R.id.ivLMSIcon);
        tvWelcomeText = findViewById(R.id.tvWelcomeText);
        btnLogin = findViewById(R.id.btnLogin);
        spnDepartment = findViewById(R.id.spnDepartment);
        llSignUp = findViewById(R.id.llSignUp);
        signUpViews = new ViewStateChanger(tilFullName, tilPhoneNumber, tilParentPhoneNumber, spnDepartment);
        nonSignUpViews = new ViewStateChanger(llSignUp, tvForgotPassword,ivLMSIcon);
        defViews = new ViewStateChanger(tvForgotPassword, tilStudentID, tilPassword, btnLogin);

        ArrayList<String> departmentList = new ArrayList<>();
        departmentList.add("Select Department");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, departmentList);
        spnDepartment.setAdapter(arrayAdapter);
        spnDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                department = adapterView.getItemAtPosition(i).toString();
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
                    return;
                }
                Department department;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    department = snapshot.getValue(Department.class);
                    if (department != null) {
                        arrayAdapter.add(department.getDepartmentName());
                    }
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                iaem.showMessage("Failed to retrieve department list");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (StaticHelper.parcelableStudent != null) {
            etStudentID.setText(StaticHelper.parcelableStudent.getStudentID());
            etPassword.setText(StaticHelper.parcelableStudent.getPassword());
            StaticHelper.parcelableStudent = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (signUpViews.getVisibility() == ViewStateChanger.VISIBLE) {
            btnLogin.setText("Login");
            signUpViews.setGone();
            nonSignUpViews.setVisible();
            tvWelcomeText.setText("Welcome Back");
        } else {
            defViews.setInvisible();
            super.onBackPressed();
        }
    }

    public void SignUp(View view) {
        if (StaticHelper.settings == null)
            iaem.showMessage("There was a problem when connecting with internet");
        else if (!StaticHelper.settings.getRegistrationOpen())
            iaem.showMessage("Registration is closed by administrator");
        if (iaem.isVisible())
            return;
        etFullName.setText("");
        etPhoneNumber.setText("");
        btnLogin.setText("Continue");
        nonSignUpViews.setGone();
        signUpViews.setVisible();
        tvWelcomeText.setText("Registration");
    }

    public void LoginOrRegister(final View view) {
        if (btnLogin.getText().toString().equals("Login"))
            Login();
        else Register();
    }

    public void ForgotPassword(View view) {
        startActivity(new Intent(this, ForgotPasswordActivity.class).putExtra("acType", FirebaseHelper.STUDENT));
    }

    private void Login() {
        final String userId = etStudentID.getText().toString();
        final String password = etPassword.getText().toString();
        if (TextUtils.isEmpty(userId)) {
            iaem.showMessage("Student ID cannot be empty");
        } else if (!userId.matches("^[a-zA-Z0-9]+$"))
            iaem.showMessage("Student ID must be alphanumeric");
        else if (TextUtils.isEmpty(password))
            iaem.showMessage("Password cannot be empty");
        if (iaem.isVisible())
            return;
        final ProgressDialog progressDialog = ProgressDialog.show(this, null, "Verifying...", true, false);
        new FirebaseHelper(this).verifyUser(FirebaseHelper.STUDENT, userId, password, new FirebaseHelper.CompletionListener() {
            @Override
            public void onSuccess() {
                new FirebaseHelper(StudentLoginActivity.this).getInfo(FirebaseHelper.STUDENT, userId, new FirebaseHelper.CompletionListener() {
                    @Override
                    public void onSuccess() {
                        progressDialog.dismiss();
                        new FirebaseHelper(StudentLoginActivity.this).saveAccount(userId, password, FirebaseHelper.STUDENT);
                        startActivity(new Intent(StudentLoginActivity.this, StudentMainActivity.class));
                        finishAffinity();
                    }

                    @Override
                    public void onFailed() {
                        progressDialog.dismiss();
                        iaem.showMessage("Invalid Student ID or Password");
                    }
                });
            }

            @Override
            public void onFailed() {
                progressDialog.dismiss();
                iaem.showMessage("Invalid Student ID or Password");
            }
        });
    }

    private void Register() {
        final String fullName = etFullName.getText().toString(),
                userId = etStudentID.getText().toString(),
                phoneNumber = etPhoneNumber.getText().toString(),
                parentPhoneNumber = etParentPhoneNumber.getText().toString(),
                password = etPassword.getText().toString();

        if (fullName.equals(""))
            iaem.showMessage("Full name cannot be empty");
        else if (TextUtils.isEmpty(userId))
            iaem.showMessage("Student ID cannot be empty");
        else if (!userId.matches("^[a-zA-Z0-9]+$"))
            iaem.showMessage("Student ID must be alphanumeric");
        else if (!ErrorHandler.isValidNumber(phoneNumber))
            iaem.showMessage("Invalid phone number");
        else if (!ErrorHandler.isValidNumber(parentPhoneNumber))
            iaem.showMessage("Invalid phone number");
        else if (parentPhoneNumber.equals(phoneNumber))
            iaem.showMessage("Student and Parent number cannot be same");
        else if (TextUtils.isEmpty(password))
            iaem.showMessage("Password field cannot be empty");
        else if (department.equals("Select Department"))
            iaem.showMessage("Please select a department");

        if (iaem.isVisible()) {
            return;
        }

        final ProgressDialog progressDialog = ProgressDialog.show(this, null, "Verifying...", true, false);
        final FirebaseHelper.KeyExistListener phoneNumberListener = new FirebaseHelper.KeyExistListener() {
            @Override
            public void onFailed() {
                progressDialog.dismiss();
                iaem.showMessage("Failed to connect to internet");
            }

            @Override
            public void onFound() {
                progressDialog.dismiss();
                iaem.showMessage("This phone number is already used");
            }

            @Override
            public void onNotFound() {
                progressDialog.dismiss();
                if (!iaem.isVisible()) {
                    StaticHelper.parcelableStudent = new Student(userId, password, "+91" + phoneNumber, "+91" + parentPhoneNumber, fullName, department);
                    Intent intent = new Intent(StudentLoginActivity.this, VerifyNumberActivity.class);
                    intent.putExtra("intent", VerifyNumberActivity.REGISTER_STUDENT);
                    intent.putExtra("phoneNumber", "+91" + phoneNumber);
                    intent.putExtra("acType", FirebaseHelper.STUDENT);
                    intent.putExtra("userID", userId);
                    startActivity(intent);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onBackPressed();
                        }
                    }, 1000);
                }
            }
        };
        FirebaseHelper.KeyExistListener userIdListener = new FirebaseHelper.KeyExistListener() {
            @Override
            public void onFound() {
                progressDialog.dismiss();
                iaem.showMessage("This ID is already used");
            }

            @Override
            public void onNotFound() {
                new FirebaseHelper(StudentLoginActivity.this).valueUsed(FirebaseHelper.STUDENTS_DATABASE, "phoneNumber", "+91" + phoneNumber, phoneNumberListener);
            }

            @Override
            public void onFailed() {
                iaem.showMessage("Failed to connect to internet");
                progressDialog.dismiss();
            }
        };
        new FirebaseHelper(this).keyExist(FirebaseHelper.STUDENTS_DATABASE.child(userId).child("studentID"), userId, userIdListener);

    }

}
