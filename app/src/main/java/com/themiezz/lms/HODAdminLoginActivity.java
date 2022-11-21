package com.themiezz.lms;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.themiezz.lms.helper_classes.FirebaseHelper;
import com.themiezz.lms.helper_classes.IAEM;

public class HODAdminLoginActivity extends AppCompatActivity {
    int acType;
    EditText etUsername, etPassword;
    IAEM iaem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hod_admin_login);

        acType = getIntent().getIntExtra("acType", 3);
        if (acType != FirebaseHelper.ADMIN && acType != FirebaseHelper.HOD) {
            Toast.makeText(this, "An unexpected error has occurred", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        iaem = new IAEM((TextView) findViewById(R.id.tvErrorMessage));
    }

    public void Login(View view) {
        final String username = etUsername.getText().toString(),
                password = etPassword.getText().toString();
        if (username.equals(""))
            iaem.showMessage("Username cannot be empty");
        else if (password.equals(""))
            iaem.showMessage("Password cannot be empty");
        if (iaem.isVisible())
            return;
        final ProgressDialog progressDialog = ProgressDialog.show(this,null,"Verifying...",true,false);
        new FirebaseHelper(HODAdminLoginActivity.this).verifyUser(acType, username, password, new FirebaseHelper.CompletionListener() {
            @Override
            public void onSuccess() {
                new FirebaseHelper(HODAdminLoginActivity.this).getInfo(acType, username, new FirebaseHelper.CompletionListener() {
                    @Override
                    public void onSuccess() {
                        if (acType == FirebaseHelper.ADMIN) {
                            new FirebaseHelper(HODAdminLoginActivity.this).saveAccount(username,password,FirebaseHelper.ADMIN);
                            progressDialog.dismiss();
                            startActivity(new Intent(HODAdminLoginActivity.this, AdminMainActivity.class));
                            finishAffinity();
                        } else {
                            new FirebaseHelper(HODAdminLoginActivity.this).saveAccount(username,password,FirebaseHelper.HOD);
                            progressDialog.dismiss();
                            startActivity(new Intent(HODAdminLoginActivity.this, HODMainActivity.class));
                            finishAffinity();
                        }
                    }

                    @Override
                    public void onFailed() {
                        progressDialog.dismiss();
                        iaem.showMessage("Failed to authenticate");
                    }
                });
            }

            @Override
            public void onFailed() {
                progressDialog.dismiss();
                iaem.showMessage("Invalid username or password");
            }
        });
    }

    public void ForgotPassword(View view) {
        startActivity(new Intent(this,ForgotPasswordActivity.class).putExtra("acType",acType));
    }

}
