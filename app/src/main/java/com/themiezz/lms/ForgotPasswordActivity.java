package com.themiezz.lms;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.themiezz.lms.helper_classes.ConnectionStatus;
import com.themiezz.lms.helper_classes.FirebaseHelper;
import com.themiezz.lms.helper_classes.IAEM;
import com.themiezz.lms.helper_classes.ViewStateChanger;

import java.util.concurrent.TimeUnit;

public class ForgotPasswordActivity extends AppCompatActivity {
    TextView tvWelcomeText;
    Button btnSendOTP, btnVerifyOTP, btnChangePassword;
    TextInputLayout tilUserID, tilOTP, tilPassword;
    EditText etUserID, etOTP, etPassword;
    ViewStateChanger verificationView, sendView, changePasswordView;
    Runnable runnable;
    Handler handler;
    int timeLeft = 60;
    String codeSent, userID, userPhoneNumber;
    int acType;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    FirebaseAuth firebaseAuth;
    LinearLayout parentView;
    IAEM iaem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        acType = getIntent().getIntExtra("acType", 9);
        if (acType != FirebaseHelper.STUDENT && acType != FirebaseHelper.HOD && acType != FirebaseHelper.ADMIN) {
            Toast.makeText(this, "An unexpected error has occurred", Toast.LENGTH_SHORT).show();
            finish();
        }
        parentView = findViewById(R.id.parentView);
        tvWelcomeText = findViewById(R.id.tvWelcomeText);
        btnSendOTP = findViewById(R.id.btnSendOTP);
        btnVerifyOTP = findViewById(R.id.btnVerifyOTP);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        tilUserID = findViewById(R.id.tilUserID);
        tilOTP = findViewById(R.id.tilOTP);
        tilPassword = findViewById(R.id.tilNewPassword);
        etUserID = findViewById(R.id.etUserID);
        etOTP = findViewById(R.id.etOTP);
        etPassword = findViewById(R.id.etNewPassword);

        if (acType == FirebaseHelper.STUDENT)
            tilUserID.setHint("Student ID");
        else
            tilUserID.setHint("Username");
        handler = new Handler();
        iaem = new IAEM((TextView) findViewById(R.id.tvErrorMessage));
        firebaseAuth = FirebaseAuth.getInstance();

        verificationView = new ViewStateChanger(tilOTP, btnVerifyOTP);
        sendView = new ViewStateChanger(btnSendOTP, tilUserID);
        changePasswordView = new ViewStateChanger(btnChangePassword, tilPassword);


        runnable = new Runnable() {
            @Override
            public void run() {
                if (timeLeft == 0) {
                    verificationView.setGone();
                    sendView.setVisible();
                } else {
                    timeLeft--;
                    handler.postDelayed(this, 1000);
                }
            }
        };
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                codeSent = s;
                verificationView.setVisible();
                sendView.setGone();
                handler.post(runnable);
                iaem.showMessage("An OTP sent to ******" + userPhoneNumber.substring(9), IAEM.INFORMATIVE);
            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                onVerified();
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    iaem.showMessage("Invalid phone number");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(ForgotPasswordActivity.this, "Limit reached, please try again later", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (e instanceof FirebaseNetworkException) {
                    iaem.showMessage("Slow internet connection! Please wait...");
                }
            }
        };
    }

    private void onVerified() {
        handler.removeCallbacks(runnable);
        verificationView.setGone();
        sendView.setGone();
        changePasswordView.setVisible();
    }

    public void SendOTP(View view) {
        userID = etUserID.getText().toString();
        if (userID.equals("")) {
            Snackbar.make(view, "Phone number cannot be empty", Snackbar.LENGTH_SHORT);
            return;
        }
        final ProgressDialog progressDialog = ProgressDialog.show(this, null, "Verifying...", true, false);
        final ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                if (!dataSnapshot.exists()) {
                    if (acType == FirebaseHelper.STUDENT)
                        iaem.showMessage("Invalid Student ID");
                    else
                        iaem.showMessage("Invalid username");
                    return;
                }
                userPhoneNumber = dataSnapshot.getValue(String.class);
                PhoneAuthProvider.getInstance().verifyPhoneNumber(userPhoneNumber, 60, TimeUnit.SECONDS, ForgotPasswordActivity.this, callbacks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                iaem.showMessage("Failed to connect to internet");
            }
        };
        if (ConnectionStatus.isInternetAvailable()) {
            if (acType == FirebaseHelper.STUDENT) {
                FirebaseHelper.STUDENTS_DATABASE.child(userID).child("phoneNumber").addListenerForSingleValueEvent(valueEventListener);
            } else if (acType == FirebaseHelper.HOD) {
                FirebaseHelper.HOD_DATABASE.child(userID).child("phoneNumber").addListenerForSingleValueEvent(valueEventListener);
            } else {
                FirebaseHelper.ADMIN_DATABASE.child(userID).child("phoneNumber").addListenerForSingleValueEvent(valueEventListener);
            }
        } else {
            progressDialog.dismiss();
            iaem.showMessage("Cannot connect to the internet");
        }
    }

    public void VerifyOTP(View view) {
        String otp = etOTP.getText().toString();
        if (otp.equals(""))
            iaem.showMessage("Enter OTP");
        else if (otp.length() != 6)
            iaem.showMessage("Invalid OTP");
        if (iaem.isVisible())
            return;
        final ProgressDialog progressDialog = ProgressDialog.show(this, null, "Verifying...", true, false);
        PhoneAuthCredential credential;
        try {
            credential = PhoneAuthProvider.getCredential(codeSent, otp);
        } catch (Exception e) {
            progressDialog.dismiss();
            iaem.showMessage("Invalid OTP");
            e.printStackTrace();
            return;
        }
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    try {
                        firebaseAuth.signOut();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressDialog.dismiss();
                    onVerified();
                } else {
                    progressDialog.dismiss();
                    if (task.getException() == null) {
                        iaem.showMessage("Invalid OTP");
                        return;
                    }
                    if (task.getException().getMessage() == null) {
                        iaem.showMessage("Invalid OTP");
                        return;
                    }
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        if (task.getException().getMessage().substring(0, 12).equals("The sms code")) {
                            Toast.makeText(ForgotPasswordActivity.this, "You have exceeded the maximum number of attempts", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            iaem.showMessage("Invalid OTP");
                        }
                    } else if (task.getException() instanceof FirebaseNetworkException) {
                        Toast.makeText(ForgotPasswordActivity.this, "Some problem has occurred while connecting to server", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                        String error = ((FirebaseAuthException) task.getException()).getErrorCode();
                        switch (error) {
                            case "ERROR_USER_DISABLED":
                                Toast.makeText(ForgotPasswordActivity.this, "User is disabled", Toast.LENGTH_SHORT).show();
                                finish();
                                break;
                            case "ERROR_USER_TOKEN_EXPIRED":
                                iaem.showMessage("OTP expired");
                                break;
                        }
                    } else {
                        iaem.showMessage("Invalid OTP");
                    }
                }
            }
        });
    }

    public void ChangePassword(View view) {
        String newPassword = etPassword.getText().toString();
        if (newPassword.equals("")) {
            iaem.showMessage("Password cannot be empty");
            return;
        }

        final ProgressDialog progressDialog = ProgressDialog.show(this, null, "Resetting Password...", true, false);
        FirebaseHelper.CompletionListener listener = new FirebaseHelper.CompletionListener() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                Toast.makeText(ForgotPasswordActivity.this, "Password reset successful", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed() {
                progressDialog.dismiss();
                iaem.showMessage("Failed to reset password");
            }
        };
        if (acType == FirebaseHelper.STUDENT)
            new FirebaseHelper(this).updateKey(FirebaseHelper.STUDENTS_DATABASE.child(userID), "password", newPassword, listener);
        else if (acType == FirebaseHelper.HOD)
            new FirebaseHelper(this).updateKey(FirebaseHelper.HOD_DATABASE.child(userID), "password", newPassword, listener);
        else
            new FirebaseHelper(this).updateKey(FirebaseHelper.ADMIN_DATABASE.child(userID), "password", newPassword, listener);
    }

}
