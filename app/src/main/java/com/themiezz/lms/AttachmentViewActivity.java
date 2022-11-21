package com.themiezz.lms;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class AttachmentViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attachment_view);

        ImageView ivAttachmentPreview = findViewById(R.id.ivAttachmentPreview);
        Intent intent = getIntent();
        String studentID = intent.getStringExtra("studentID");
        String dateApplied = intent.getStringExtra("dateApplied");
        Uri uri = intent.getParcelableExtra("imgUri");
        if (uri != null)
            ivAttachmentPreview.setImageURI(uri);
        else {
            if (studentID == null || dateApplied == null) {
                Toast.makeText(this, "An unexpected error has occurred", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            dateApplied = dateApplied.replaceAll("\\W","");
            File attachmentFile = new File(getFilesDir() + "/attachment/" + studentID + "_" + dateApplied);
            if (!attachmentFile.exists()) {
                Toast.makeText(this, "An unexpected error has occurred", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            try {
                ivAttachmentPreview.setImageURI(Uri.fromFile(attachmentFile));
            } catch (Exception e) {
                Toast.makeText(this, "An unexpected error has occurred", Toast.LENGTH_SHORT).show();
                finish();
            }
        }


    }

}
