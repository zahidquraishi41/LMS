package com.themiezz.lms;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.themiezz.lms.helper_classes.ErrorHandler;
import com.themiezz.lms.helper_classes.FirebaseHelper;
import com.themiezz.lms.helper_classes.IAEM;
import com.themiezz.lms.helper_classes.StaticHelper;
import com.themiezz.lms.user_defined_classes.Settings;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {
    View parentView;
    EditText etWorkingDays;
    int day, month, year;
    TextView tvNo, tvYes, tvErrorMessage;
    Boolean allowRegistration;
    IAEM iaem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        etWorkingDays = findViewById(R.id.etWorkingDays);
        tvNo = findViewById(R.id.tvNO);
        tvYes = findViewById(R.id.tvYES);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);

        iaem = new IAEM(tvErrorMessage);
        day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        month = Calendar.getInstance().get(Calendar.MONTH);
        year = Calendar.getInstance().get(Calendar.YEAR);

        if (ErrorHandler.isEmpty(StaticHelper.settings)) {
            Toast.makeText(this, "An unexpected error has occurred", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        allowRegistration = StaticHelper.settings.getRegistrationOpen();
        if (StaticHelper.settings.getRegistrationOpen()) {
            AllowRegistration(tvYes);
        } else {
            DenyRegistration(tvNo);
        }
        etWorkingDays.setText(Integer.toString(StaticHelper.settings.getWorkingDays()));
    }

    public void DenyRegistration(View view) {
        tvNo.setBackgroundColor(Color.parseColor("#D81111"));
        tvYes.setBackgroundColor(Color.TRANSPARENT);
        allowRegistration = false;
    }

    public void AllowRegistration(View view) {
        tvYes.setBackgroundColor(Color.parseColor("#25AA2B"));
        tvNo.setBackgroundColor(Color.TRANSPARENT);
        allowRegistration = true;
    }

    public void Update(View view) {
        String workingDays = etWorkingDays.getText().toString();
        Integer integer;
        if (workingDays.equals("")) iaem.showMessage("Working days cannot be empty");
        else {
            try {
                integer = Integer.parseInt(workingDays);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                iaem.showMessage("Enter a valid number");
                return;
            }
        }
        if (iaem.isVisible())
            return;
        final ProgressDialog progressDialog = ProgressDialog.show(this, null, "Updating...", true, false);
        final Settings settings = new Settings(allowRegistration, Integer.parseInt(workingDays));
        new FirebaseHelper(this).saveSettings(settings, new FirebaseHelper.CompletionListener() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                StaticHelper.settings = settings;
                iaem.showMessage("Settings Saved Successfully", IAEM.INFORMATIVE);
            }

            @Override
            public void onFailed() {
                progressDialog.dismiss();
                iaem.showMessage("Failed to update settings");
            }
        });
    }

}
