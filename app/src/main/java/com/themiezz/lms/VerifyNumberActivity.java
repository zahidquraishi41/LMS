package com.themiezz.lms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.themiezz.lms.helper_classes.ConnectionStatus;
import com.themiezz.lms.helper_classes.FirebaseHelper;
import com.themiezz.lms.helper_classes.IAEM;
import com.themiezz.lms.helper_classes.StaticHelper;
import com.themiezz.lms.helper_classes.ViewStateChanger;

import java.text.MessageFormat;

public class VerifyNumberActivity extends AppCompatActivity {
    public final static int REGISTER_STUDENT = 1;
    public final static int UPDATE_NUMBER = 2;

    EditText etPhoneNumber, etOTP;
    TextInputLayout tilPassword;
    Button btnResendOTP, btnVerifyOTP;
    FirebaseAuth firebaseAuth;
    Runnable runnable;
    Handler handler;
    int timeLeft = 60;
    ViewStateChanger verificationView, sendView;
    IAEM iaem;
    String codeSent, verificationNumber, userID;
    int acType;
    int intent;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_number);

        acType = getIntent().getIntExtra("acType", 9);
        intent = getIntent().getIntExtra("intent", 9);
        verificationNumber = getIntent().getStringExtra("phoneNumber");
        userID = getIntent().getStringExtra("userID");
        if (verificationNumber == null) {
            DisplayErrorMessage();
            return;
        }

        etOTP = findViewById(R.id.etOTP);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        tilPassword = findViewById(R.id.tilPassword);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnResendOTP = findViewById(R.id.btnResendOTP);
        btnVerifyOTP = findViewById(R.id.btnVerifyOTP);
        firebaseAuth = FirebaseAuth.getInstance();

        new ViewStateChanger(etPhoneNumber).lockView();
        verificationView = new ViewStateChanger(btnVerifyOTP, etOTP);
        sendView = new ViewStateChanger(btnResendOTP);
        iaem = new IAEM((TextView) findViewById(R.id.tvErrorMessage));
        etPhoneNumber.setText(verificationNumber.substring(3));
        sendView.lockView();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (timeLeft == 1) {
                    btnResendOTP.setText("Send OTP");
                    etOTP.setText("");
                    verificationView.lockView();
                    sendView.unlockView();
                } else {
                    btnResendOTP.setText(MessageFormat.format("Send OTP ({0})", timeLeft));
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
                verificationView.unlockView();
                sendView.lockView();
                handler.post(runnable);
                Toast.makeText(VerifyNumberActivity.this, "OTP Sent", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                onVerified();
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(VerifyNumberActivity.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                    StaticHelper.parcelableStudent = null;
                    finish();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(VerifyNumberActivity.this, "Limit reached, please try again later", Toast.LENGTH_SHORT).show();
                    StaticHelper.parcelableStudent = null;
                    finish();
                } else if (e instanceof FirebaseNetworkException) {
                    iaem.showMessage("Slow internet connection! Please wait...");
                } else {
                    Toast.makeText(VerifyNumberActivity.this, "Invalid code", Toast.LENGTH_SHORT).show();
                }
            }
        };
        ResendOTP(btnResendOTP);
    }

    public void VerifyOTP(View view) {
        String otp = etOTP.getText().toString();
        if (otp.equals(""))
            iaem.showMessage("Enter OTP");
        else if (otp.length() != 6)
            iaem.showMessage("Invalid OTP");
        if (iaem.isVisible())
            return;
        PhoneAuthCredential credential;
        try {
            credential = PhoneAuthProvider.getCredential(codeSent, otp);
        } catch (Exception e) {
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
                    onVerified();
                } else {
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
                            Toast.makeText(VerifyNumberActivity.this, "You have exceeded the maximum number of attempts", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            iaem.showMessage("Invalid OTP");
                        }
                    } else if (task.getException() instanceof FirebaseNetworkException) {
                        Toast.makeText(VerifyNumberActivity.this, "Some problem has occurred while connecting to server", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                        String error = ((FirebaseAuthException) task.getException()).getErrorCode();
                        switch (error) {
                            case "ERROR_USER_DISABLED":
                                Toast.makeText(VerifyNumberActivity.this, "User is disabled", Toast.LENGTH_SHORT).show();
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

    public void ResendOTP(View view) {
        if (ConnectionStatus.isInternetAvailable())
            PhoneAuthProvider.getInstance().verifyPhoneNumber(verificationNumber, 60, java.util.concurrent.TimeUnit.SECONDS, this, callbacks);
        else {
            iaem.showMessage("Internet is not available");
            verificationView.lockView();
            sendView.unlockView();
        }
    }

    private void DisplayErrorMessage() {
        Toast.makeText(this, "An unexpected error has occurred", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void onVerified() {
        if (intent == UPDATE_NUMBER) {
            UpdatePhoneNumber();
        }
        if (intent == REGISTER_STUDENT) {
            RegisterStudent();
        }
    }

    private void RegisterStudent() {
        final ProgressDialog progressDialog = ProgressDialog.show(this, null, "Verifying...", true, false);
        new FirebaseHelper(VerifyNumberActivity.this).addUser(StaticHelper.parcelableStudent, new FirebaseHelper.CompletionListener() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                Toast.makeText(VerifyNumberActivity.this, "Verification Success", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed() {
                progressDialog.dismiss();
                Toast.makeText(VerifyNumberActivity.this, "Failed to register", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void UpdatePhoneNumber() {
        final ProgressDialog progressDialog = ProgressDialog.show(this, null, "Updating...", true, false);
        FirebaseHelper.CompletionListener listener = new FirebaseHelper.CompletionListener() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                Toast.makeText(VerifyNumberActivity.this, "Phone number updated successfully", Toast.LENGTH_SHORT).show();
                if (acType == FirebaseHelper.STUDENT) {
                    StaticHelper.student.setPhoneNumber(verificationNumber);
                } else if (acType == FirebaseHelper.HOD) {
                    StaticHelper.hod.setPhoneNumber(verificationNumber);
                } else {
                    StaticHelper.admin.setPhoneNumber(verificationNumber);
                }
                finish();
            }

            @Override
            public void onFailed() {
                progressDialog.dismiss();
                iaem.showMessage("Failed to update phone number");
            }
        };

        if (acType == FirebaseHelper.STUDENT)
            new FirebaseHelper(this).updateKey(FirebaseHelper.STUDENTS_DATABASE.child(StaticHelper.student.getStudentID()), "phoneNumber", verificationNumber, listener);
        else if (acType == FirebaseHelper.HOD)
            new FirebaseHelper(this).updateKey(FirebaseHelper.HOD_DATABASE.child(StaticHelper.hod.getUsername()), "phoneNumber", verificationNumber, listener);
        else
            new FirebaseHelper(this).updateKey(FirebaseHelper.ADMIN_DATABASE.child(StaticHelper.admin.getUsername()), "phoneNumber", verificationNumber, listener);
    }

}
