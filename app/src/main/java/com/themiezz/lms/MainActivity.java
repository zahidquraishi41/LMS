package com.themiezz.lms;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.themiezz.lms.helper_classes.ErrorHandler;
import com.themiezz.lms.helper_classes.FirebaseHelper;
import com.themiezz.lms.helper_classes.StaticHelper;
import com.themiezz.lms.helper_classes.ViewStateChanger;
import com.themiezz.lms.user_defined_classes.Settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    boolean error;
    ImageView ivAboutUs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivAboutUs = findViewById(R.id.ivAboutUs);
        new FirebaseHelper(this).getSettings();
        error = false;
        ReadSavedAccount();
        if (error) new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                DisplayMain();
            }
        }, 1000);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    private void DisplayMain() {
        final ViewStateChanger viewStateChanger = new ViewStateChanger(findViewById(R.id.btnAdmin), findViewById(R.id.btnHOD), findViewById(R.id.btnStudent));
        TransitionManager.beginDelayedTransition((ViewGroup) findViewById(R.id.parentView));
        Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up);
        viewStateChanger.setVisible(animation);
        ivAboutUs.setVisibility(View.VISIBLE);
    }

    private void ReadSavedAccount() {
        File localFile = new File(getFilesDir() + "/" + "saved_ac.txt");
        if (!localFile.exists()) {
            error = true;
            return;
        }
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = openFileInput("saved_ac.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            StringBuilder[] stringBuilders = new StringBuilder[3];
            stringBuilders[0] = new StringBuilder();
            stringBuilders[1] = new StringBuilder();
            stringBuilders[2] = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String temp = bufferedReader.readLine();
            inputStreamReader.close();
            bufferedReader.close();
            if (temp == null) {
                error = true;
                return;
            }
            int pos = 0;
            for (int i = 0; i < temp.length(); i++) {
                if (temp.charAt(i) == '|') {
                    i++;
                    pos++;
                }
                stringBuilders[pos].append(temp.charAt(i));
            }
            if (stringBuilders[0].length() == 0 || stringBuilders[1].length() == 0 || stringBuilders[2].length() == 0) {
                error = true;
                return;
            }
            Authenticate(stringBuilders[0].toString(), stringBuilders[1].toString(), Integer.parseInt(stringBuilders[2].toString()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private void Authenticate(final String username, String password, final int acType) {
        if (acType != FirebaseHelper.ADMIN && acType != FirebaseHelper.HOD && acType != FirebaseHelper.STUDENT) {
            error = true;
            return;
        }
        new FirebaseHelper(this).verifyUser(acType, username, password, new FirebaseHelper.CompletionListener() {
            @Override
            public void onSuccess() {
                new FirebaseHelper(MainActivity.this).getInfo(acType, username, new FirebaseHelper.CompletionListener() {
                    @Override
                    public void onSuccess() {
                        if (acType == FirebaseHelper.STUDENT) {
                            if (ErrorHandler.isEmpty(StaticHelper.student))
                                DisplayMain();
                            else
                                startActivity(new Intent(MainActivity.this, StudentMainActivity.class));
                        } else if (acType == FirebaseHelper.HOD) {
                            if (ErrorHandler.isEmpty(StaticHelper.hod))
                                DisplayMain();
                            else
                                startActivity(new Intent(MainActivity.this, HODMainActivity.class));
                        } else {
                            if (ErrorHandler.isEmpty(StaticHelper.admin))
                                DisplayMain();
                            else
                                startActivity(new Intent(MainActivity.this, AdminMainActivity.class));
                        }
                        finish();
                    }

                    @Override
                    public void onFailed() {
                        error = true;
                    }
                });
            }

            @Override
            public void onFailed() {
                DisplayMain();
            }
        });
    }

    public void Student(View view) {
//        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, findViewById(R.id.imgLMSIcon), "imgLMSIcon");
        startActivity(new Intent(this, StudentLoginActivity.class));
//        Animation zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
//        Animation animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
//        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }

    public void HOD(View view) {
        startActivity(new Intent(this, HODAdminLoginActivity.class).putExtra("acType", FirebaseHelper.HOD));
    }

    public void Admin(View view) {
        startActivity(new Intent(this, HODAdminLoginActivity.class).putExtra("acType", FirebaseHelper.ADMIN));
    }

    public void AboutUs(View view) {
        startActivity(new Intent(this,AboutUsActivity.class));
    }

}
