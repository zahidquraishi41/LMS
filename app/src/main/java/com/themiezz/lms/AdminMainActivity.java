package com.themiezz.lms;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import java.io.File;

public class AdminMainActivity extends AppCompatActivity {
    private static final String TAG = "ZQAdminMainActivity";
    Uri imageUri;
    LinearLayout parentView;
    ImageView ivProfilePic;
    TextView tvFullName;
    ValueEventListener totalHODsListener, totalStudentsListener, totalDepartmentsListener;
    long lastBackPressTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        ivProfilePic = findViewById(R.id.ivProfilePic);
        parentView = findViewById(R.id.parentView);
        tvFullName = findViewById(R.id.tvFullName);
        totalHODsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                TextView tvTotalHODs = findViewById(R.id.tvTotalHODs);
                Integer integer = dataSnapshot.getValue(Integer.class);
                if (integer != null)
                    tvTotalHODs.setText(String.valueOf(integer));
                else
                    tvTotalHODs.setText(String.valueOf(0));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        totalStudentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                TextView tvTotalStudents = findViewById(R.id.tvTotalStudents);
                Integer integer = 0;
                if (!dataSnapshot.exists()) {
                    tvTotalStudents.setText(String.valueOf(integer));
                    return;
                }
                if (dataSnapshot.getChildrenCount() == 0) {
                    tvTotalStudents.setText(String.valueOf(integer));
                    return;
                }
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Integer value = snapshot.getValue(Integer.class);
                    if (value != null)
                        integer += value;

                }
                tvTotalStudents.setText(String.valueOf(integer));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        totalDepartmentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer integer = dataSnapshot.getValue(Integer.class);
                TextView tvTotalDepartments = findViewById(R.id.tvTotalDepartments);
                if (integer != null)
                    tvTotalDepartments.setText(String.valueOf(integer));
                else tvTotalDepartments.setText(String.valueOf(0));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        FirebaseHelper.REGISTRATION_INFO_DATABASE.child("totalHODs").addValueEventListener(totalHODsListener);
        FirebaseHelper.REGISTRATION_INFO_DATABASE.child("totalStudents").addValueEventListener(totalStudentsListener);
        FirebaseHelper.REGISTRATION_INFO_DATABASE.child("totalDepartments").addValueEventListener(totalDepartmentsListener);
        new FirebaseHelper(this).loadImageInto(ivProfilePic, StaticHelper.admin.getUsername(), FirebaseHelper.ADMIN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ErrorHandler.isEmpty(StaticHelper.admin)) {
            Toast.makeText(this, "An unexpected error has occurred", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvFullName.setText(StaticHelper.admin.getFullName());
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
                new FirebaseHelper(this).uploadPicture(imageUri, StaticHelper.admin.getUsername(), FirebaseHelper.ADMIN, new FirebaseHelper.CompletionListener() {
                    @Override
                    public void onSuccess() {
                        progressDialog.dismiss();
                        Snackbar.make(parentView, "Picture Updated Successfully", Snackbar.LENGTH_SHORT).show();
                        new FirebaseHelper(AdminMainActivity.this).loadImageInto(ivProfilePic, StaticHelper.admin.getUsername(), FirebaseHelper.ADMIN);
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

    public void MyProfile(View view) {
        startActivity(new Intent(this, MyProfileActivity.class).putExtra("acType", FirebaseHelper.ADMIN));
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
        StaticHelper.admin = null;
        FirebaseHelper.REGISTRATION_INFO_DATABASE.child("totalHODs").removeEventListener(totalHODsListener);
        FirebaseHelper.REGISTRATION_INFO_DATABASE.child("totalStudents").removeEventListener(totalStudentsListener);
        FirebaseHelper.REGISTRATION_INFO_DATABASE.child("totalDepartments").removeEventListener(totalDepartmentsListener);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();

    }

    public void AddHOD(View view) {
        startActivity(new Intent(this, RegisterUserActivity.class).putExtra("acType", FirebaseHelper.HOD));
    }

    public void RemoveHOD(View view) {
        startActivity(new Intent(this, HODListActivity.class));
    }

    public void Settings(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

}
