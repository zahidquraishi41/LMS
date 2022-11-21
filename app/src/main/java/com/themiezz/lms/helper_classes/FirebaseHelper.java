package com.themiezz.lms.helper_classes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.themiezz.lms.user_defined_classes.Admin;
import com.themiezz.lms.user_defined_classes.Application;
import com.themiezz.lms.user_defined_classes.Settings;
import com.themiezz.lms.user_defined_classes.Student;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class FirebaseHelper {
    private Context context;
    private Handler handler;
    private ConnectionRunnable runnable;
    public final static int STUDENT = 0;
    public final static int HOD = 1;
    public final static int ADMIN = 2;
    public final static DatabaseReference STUDENTS_DATABASE = FirebaseDatabase.getInstance().getReference("students");
    public final static DatabaseReference HOD_DATABASE = FirebaseDatabase.getInstance().getReference("hod");
    public final static DatabaseReference ADMIN_DATABASE = FirebaseDatabase.getInstance().getReference("admin");
    public final static DatabaseReference APPLICATIONS_DATABASE = FirebaseDatabase.getInstance().getReference("applications");
    public final static DatabaseReference DEPARTMENT_DATABASE = FirebaseDatabase.getInstance().getReference("department");
    public final static DatabaseReference LEAVE_INFO_DATABASE = FirebaseDatabase.getInstance().getReference("leave_info");
    public final static DatabaseReference REGISTRATION_INFO_DATABASE = FirebaseDatabase.getInstance().getReference("registration_info");
    public final static DatabaseReference SETTINGS_DATABASE = FirebaseDatabase.getInstance().getReference("settings");
    public final static StorageReference STUDENT_STORAGE = FirebaseStorage.getInstance().getReference("student");
    public final static StorageReference HOD_STORAGE = FirebaseStorage.getInstance().getReference("hod");
    public final static StorageReference ADMIN_STORAGE = FirebaseStorage.getInstance().getReference("admin");
    public final static StorageReference ATTACHMENT_STORAGE = FirebaseStorage.getInstance().getReference().child("Attachments");
    private long count, total;
    private static ArrayList<String> downloadedPicUsersList = new ArrayList<>();

    public interface CompletionListener {
        void onSuccess();

        void onFailed();
    }

    public interface KeyExistListener {
        void onFound();

        void onNotFound();

        void onFailed();
    }

    public FirebaseHelper(Context context) {
        this.context = context;
        handler = new Handler();
    }

    public void incrementValue(final DatabaseReference reference) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    reference.setValue(1);
                } else {
                    Integer value = dataSnapshot.getValue(Integer.class);
                    if (value != null)
                        reference.setValue(value + 1);
                    else reference.setValue(1);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void incrementValue(final DatabaseReference reference, final int increment) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    reference.setValue(increment);
                } else {
                    Integer value = dataSnapshot.getValue(Integer.class);
                    if (value != null)
                        reference.setValue(value + increment);
                    else reference.setValue(increment);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void decrementValue(final DatabaseReference reference) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer value = dataSnapshot.getValue(Integer.class);
                    if (value != null)
                        reference.setValue(value - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void decrementValue(final DatabaseReference reference, final int decrement) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer value = dataSnapshot.getValue(Integer.class);
                    if (value != null)
                        reference.setValue(value - decrement);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void verifyUser(final int userType, final String username, final String password, final CompletionListener listener) {
        runnable = new ConnectionRunnable(context, handler, new ConnectionRunnable.ConnectionListener() {
            @Override
            public void onConnected() {
                final ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            listener.onFailed();
                        } else {
                            String pass = dataSnapshot.getValue(String.class);
                            if (pass != null)
                                if (pass.equals(password))
                                    listener.onSuccess();
                                else listener.onFailed();
                            else listener.onFailed();
                        }
                        handler.removeCallbacks(runnable);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listener.onFailed();
                        handler.removeCallbacks(runnable);
                    }
                };
                if (userType == STUDENT) {
                    STUDENTS_DATABASE.child(username).child("password").addListenerForSingleValueEvent(valueEventListener);
                } else if (userType == HOD)
                    HOD_DATABASE.child(username).child("password").addListenerForSingleValueEvent(valueEventListener);
                else if (userType == ADMIN) {
                    ADMIN_DATABASE.child(username).child("password").addListenerForSingleValueEvent(valueEventListener);
                } else {
                    listener.onFailed();
                    handler.removeCallbacks(runnable);
                }
            }
        });

        handler.post(runnable);

    }

    public void getSettings() {
        runnable = new ConnectionRunnable(context, handler, new ConnectionRunnable.ConnectionListener() {
            @Override
            public void onConnected() {
                SETTINGS_DATABASE.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            StaticHelper.settings = dataSnapshot.getValue(Settings.class);
                        handler.removeCallbacks(runnable);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        handler.removeCallbacks(runnable);
                    }
                });
            }
        });
        handler.post(runnable);
    }

    public void addUser(final Student data, final CompletionListener listener) {
        runnable = new ConnectionRunnable(context, handler, new ConnectionRunnable.ConnectionListener() {
            @Override
            public void onConnected() {
                STUDENTS_DATABASE.child(data.getStudentID()).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            incrementValue(REGISTRATION_INFO_DATABASE.child("totalStudents").child(data.getDepartment()));
                            listener.onSuccess();
                        } else
                            listener.onFailed();
                        handler.removeCallbacks(runnable);
                    }
                });
            }
        });
        handler.post(runnable);
    }

    public void saveAccount(String userID, String password, int acType) {
        String text = userID + "|" + password + "|" + acType;
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = context.openFileOutput("saved_ac.txt", MODE_PRIVATE);
            fileOutputStream.write(text.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void getInfo(final int acType, final String userID, final CompletionListener listener) {
        final ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (acType == FirebaseHelper.STUDENT)
                        StaticHelper.student = dataSnapshot.getValue(Student.class);
                    else if (acType == FirebaseHelper.HOD)
                        StaticHelper.hod = dataSnapshot.getValue(com.themiezz.lms.user_defined_classes.HOD.class);
                    else
                        StaticHelper.admin = dataSnapshot.getValue(Admin.class);
                    listener.onSuccess();
                } else listener.onFailed();
                handler.removeCallbacks(runnable);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailed();
                handler.removeCallbacks(runnable);
            }
        };
        runnable = new ConnectionRunnable(context, handler, new ConnectionRunnable.ConnectionListener() {
            @Override
            public void onConnected() {
                if (acType == FirebaseHelper.STUDENT)
                    STUDENTS_DATABASE.child(userID).addListenerForSingleValueEvent(valueEventListener);
                else if (acType == FirebaseHelper.HOD)
                    HOD_DATABASE.child(userID).addListenerForSingleValueEvent(valueEventListener);
                else if (acType == FirebaseHelper.ADMIN)
                    ADMIN_DATABASE.child(userID).addListenerForSingleValueEvent(valueEventListener);
                else {
                    listener.onFailed();
                    handler.removeCallbacks(runnable);
                }
            }
        });
        handler.post(runnable);
    }

    public void uploadPicture(final Uri imgUri, final String userID, final int acType, final CompletionListener listener) {
        final StorageReference storageReference;
        final File filePath, localFile;
        Bitmap bitmap;
        ByteArrayOutputStream byteArrayOutputStream;
        FileOutputStream fileOutputStream;
        final byte[] data;
        if (acType == STUDENT) {
            storageReference = STUDENT_STORAGE.child(userID);
            filePath = new File(context.getFilesDir() + "/student/");
            localFile = new File(context.getFilesDir() + "/student/" + userID);
        } else if (acType == HOD) {
            storageReference = HOD_STORAGE.child(userID);
            filePath = new File(context.getFilesDir() + "/hod/");
            localFile = new File(context.getFilesDir() + "/hod/" + userID);
        } else if (acType == ADMIN) {
            storageReference = ADMIN_STORAGE.child(userID);
            filePath = new File(context.getFilesDir() + "/admin/");
            localFile = new File(context.getFilesDir() + "/admin/" + userID);
        } else {
            listener.onFailed();
            handler.removeCallbacks(runnable);
            return;
        }
        if (!filePath.exists()) if (!filePath.mkdir()) {
            listener.onFailed();
            handler.removeCallbacks(runnable);
        }
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imgUri);
            float aspectRatio = bitmap.getWidth() / (float) bitmap.getHeight();
            int newWidth = 450;
            if (newWidth >= bitmap.getWidth())
                newWidth = bitmap.getWidth();
            int newHeight = Math.round(newWidth / aspectRatio);
            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
            byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            fileOutputStream = new FileOutputStream(localFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            data = byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            listener.onFailed();
            handler.removeCallbacks(runnable);
            return;
        }
        runnable = new ConnectionRunnable(context, handler, new ConnectionRunnable.ConnectionListener() {
            @Override
            public void onConnected() {
                storageReference.putBytes(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful())
                            listener.onSuccess();
                        else
                            listener.onFailed();
                        handler.removeCallbacks(runnable);
                    }
                });
            }
        });
        handler.post(runnable);
    }

    public void loadImageInto(final ImageView ivUserPicture, final String userID, int acType) {
        StorageReference storageReference;
        final File oldFile;
        final File newFile;
        final File filePath;

        if (acType == STUDENT) {
            try {
                storageReference = STUDENT_STORAGE.child(userID);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            filePath = new File(context.getFilesDir() + "/student/");
            oldFile = new File(context.getFilesDir() + "/student/" + userID);
        } else if (acType == HOD) {
            try {
                storageReference = HOD_STORAGE.child(userID);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            filePath = new File(context.getFilesDir() + "/hod/");
            oldFile = new File(context.getFilesDir() + "/hod/" + userID);
        } else if (acType == FirebaseHelper.ADMIN) {
            try {
                storageReference = ADMIN_STORAGE.child(userID);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            filePath = new File(context.getFilesDir() + "/admin/");
            oldFile = new File(context.getFilesDir() + "/admin/" + userID);
        } else return;

        if (oldFile.exists())
            try {
                ivUserPicture.setImageBitmap(BitmapFactory.decodeFile(oldFile.getAbsolutePath()));
                if (acType == FirebaseHelper.STUDENT)
                    if (!downloadedPicUsersList.contains(userID)) {
                        downloadedPicUsersList.add(userID);
                    } else {
                        return;
                    }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

        if (!filePath.exists()) if (!filePath.mkdir()) return;

        try {
            newFile = File.createTempFile("tempImage", "tmp", filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        storageReference.getFile(newFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                try {
                    if (newFile.renameTo(oldFile)) {
                        Bitmap bitmap = BitmapFactory.decodeFile(oldFile.getAbsolutePath());
                        FileOutputStream outputStream = new FileOutputStream(oldFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.flush();
                        outputStream.close();
                        try {
                            ivUserPicture.setImageBitmap(bitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void updateKey(final DatabaseReference reference, final Map<String, Object> map, final CompletionListener listener) {
        runnable = new ConnectionRunnable(context, handler, new ConnectionRunnable.ConnectionListener() {
            @Override
            public void onConnected() {
                reference.updateChildren(map, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null) listener.onSuccess();
                        else listener.onFailed();
                        handler.removeCallbacks(runnable);
                    }
                });
            }
        });
        handler.post(runnable);
    }

    public void updateKey(final DatabaseReference reference, final String key, final String value, CompletionListener listener) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        updateKey(reference, map, listener);
    }

    public void updateKey(final DatabaseReference reference, final String key, final String value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        reference.updateChildren(map);
    }

    public void deleteHOD(final com.themiezz.lms.user_defined_classes.HOD hod, final CompletionListener listener) {
        count = 0;
        total = 0;
        final DatabaseReference.CompletionListener completionListener = new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                count++;
            }
        };
        final Query studentQuery = STUDENTS_DATABASE.orderByChild("department").equalTo(hod.getDepartment());
        runnable = new ConnectionRunnable(context, handler, new ConnectionRunnable.ConnectionListener() {
            @Override
            public void onConnected() {
                studentQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        total = dataSnapshot.getChildrenCount() + 6;
                        DEPARTMENT_DATABASE.child(hod.getDepartment()).removeValue(completionListener);
                        HOD_DATABASE.child(hod.getUsername()).removeValue(completionListener);
                        LEAVE_INFO_DATABASE.child(hod.getDepartment()).removeValue(completionListener);
                        HOD_STORAGE.child(hod.getUsername()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                count++;
                            }
                        });
                        REGISTRATION_INFO_DATABASE.child("totalStudents").child(hod.getDepartment()).removeValue(completionListener);
                        decrementValue(REGISTRATION_INFO_DATABASE.child("totalDepartments"));
                        decrementValue(REGISTRATION_INFO_DATABASE.child("totalHODs"));
                        APPLICATIONS_DATABASE.child(hod.getDepartment()).removeValue(completionListener);
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Student student = snapshot.getValue(Student.class);
                            if (student != null) {
                                STUDENTS_DATABASE.child(student.getStudentID()).removeValue(completionListener);
                                STUDENT_STORAGE.child(student.getStudentID()).delete();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        count = -1;
                        listener.onFailed();
                    }
                });
            }
        });
        new Runnable() {
            @Override
            public void run() {
                if (count == -1) {
                    handler.removeCallbacks(runnable);
                    return;
                }
                if (total == 0) new Handler().postDelayed(this, 500);
                else if (count < total) new Handler().postDelayed(this, 500);
                else {
                    listener.onSuccess();
                    handler.removeCallbacks(runnable);
                }
            }
        }.run();
        handler.post(runnable);
    }

    public void deleteStudent(final String studentID, final String department, final CompletionListener listener) {
        count = 0;
        total = 0;
        final DatabaseReference.CompletionListener completionListener = new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                count++;
            }
        };
        final Query query = APPLICATIONS_DATABASE.child(department).orderByChild("studentID").equalTo(studentID);
        runnable = new ConnectionRunnable(context, handler, new ConnectionRunnable.ConnectionListener() {
            @Override
            public void onConnected() {
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        total = dataSnapshot.getChildrenCount() + 3;
                        STUDENTS_DATABASE.child(studentID).removeValue(completionListener);
                        STUDENT_STORAGE.child(studentID).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                count++;
                            }
                        });
                        decrementValue(REGISTRATION_INFO_DATABASE.child("totalStudents").child(department));
                        LEAVE_INFO_DATABASE.child(department).child(studentID).removeValue(completionListener);
                        if (dataSnapshot.getChildrenCount() != 0)
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                snapshot.getRef().removeValue(completionListener);
                            }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        count = -1;
                        listener.onFailed();
                        handler.removeCallbacks(runnable);
                    }
                });
            }
        });
        handler.post(runnable);
        new Runnable() {
            @Override
            public void run() {
                if (count == -1)
                    return;
                if (count == 0) new Handler().postDelayed(this, 500);
                else if (count < total) new Handler().postDelayed(this, 500);
                else {
                    listener.onSuccess();
                    handler.removeCallbacks(runnable);
                }
            }
        }.run();
    }

    public void keyExist(final DatabaseReference keyRef, final String value, final KeyExistListener listener) {
        runnable = new ConnectionRunnable(context, handler, new ConnectionRunnable.ConnectionListener() {
            @Override
            public void onConnected() {
                keyRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            if (Objects.requireNonNull(dataSnapshot.getValue(String.class)).equals(value))
                                listener.onFound();
                            else listener.onNotFound();
                        else listener.onNotFound();
                        handler.removeCallbacks(runnable);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listener.onFailed();
                        handler.removeCallbacks(runnable);
                    }
                });
            }
        });
        handler.post(runnable);
    }

    public void valueUsed(final DatabaseReference reference, final String key, final String value, final KeyExistListener listener) {
        runnable = new ConnectionRunnable(context, handler, new ConnectionRunnable.ConnectionListener() {
            @Override
            public void onConnected() {
                reference.orderByChild(key).equalTo(value).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() == 0)
                            listener.onNotFound();
                        else listener.onFound();
                        handler.removeCallbacks(runnable);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listener.onFailed();
                        handler.removeCallbacks(runnable);
                    }
                });
            }
        });
        handler.post(runnable);
    }

    public void addApplication(final Application application, final String department, final CompletionListener listener) {
        runnable = new ConnectionRunnable(context, handler, new ConnectionRunnable.ConnectionListener() {
            @Override
            public void onConnected() {
                APPLICATIONS_DATABASE.child(StaticHelper.student.getDepartment()).push().setValue(application, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            incrementValue(LEAVE_INFO_DATABASE.child(department).child(application.getStudentID()).child("timesApplied"));
                            listener.onSuccess();
                            StaticHelper.application = application;
                            StaticHelper.applicationReference = databaseReference;
                            handler.removeCallbacks(runnable);
                        } else {
                            listener.onFailed();
                            handler.removeCallbacks(runnable);
                        }
                    }
                });
            }
        });
        handler.post(runnable);
    }

    public void saveSettings(final Settings settings, final CompletionListener listener) {
        runnable = new ConnectionRunnable(context, handler, new ConnectionRunnable.ConnectionListener() {
            @Override
            public void onConnected() {
                SETTINGS_DATABASE.setValue(settings).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                            listener.onSuccess();
                        else
                            listener.onFailed();
                        handler.removeCallbacks(runnable);
                    }
                });
            }
        });
        handler.post(runnable);
    }

    public void uploadAttachment(Uri imgUri, String dateApplied, final CompletionListener listener) {
        dateApplied = dateApplied.replaceAll("\\W", "");

        File localFile = new File(context.getFilesDir() + "/attachment/" + StaticHelper.student.getStudentID() + "_" + dateApplied);
        File filePath = new File(context.getFilesDir() + "/attachment/");
        if (!filePath.exists()) {
            if (!filePath.mkdir()) {
                listener.onFailed();
                return;
            }
        }
        Bitmap bitmap;
        ByteArrayOutputStream byteArrayOutputStream;
        FileOutputStream fileOutputStream;
        final byte[] data;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imgUri);
            float aspectRatio = bitmap.getWidth() / (float) bitmap.getHeight();
            int newWidth = 1000;
            if (newWidth >= bitmap.getWidth())
                newWidth = bitmap.getWidth();
            int newHeight = Math.round(newWidth / aspectRatio);
            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
            byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            fileOutputStream = new FileOutputStream(localFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            data = byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            listener.onFailed();
            handler.removeCallbacks(runnable);
            return;
        }
        final String finalDateApplied = dateApplied;
        runnable = new ConnectionRunnable(context, handler, new ConnectionRunnable.ConnectionListener() {
            @Override
            public void onConnected() {
                ATTACHMENT_STORAGE.child(StaticHelper.student.getStudentID() + "_" + finalDateApplied).putBytes(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful())
                            listener.onSuccess();
                        else
                            listener.onFailed();
                        handler.removeCallbacks(runnable);
                    }
                });
            }
        });
        handler.post(runnable);
    }

    public void downloadAttachment(final String studentID, String dateApplied, final CompletionListener listener) {
        dateApplied = dateApplied.replaceAll("\\W", "");
        File file = new File(context.getFilesDir() + "/attachment/" + studentID + "_" + dateApplied);
        if (file.exists()) {
            listener.onSuccess();
            return;
        }
        try {
            file = File.createTempFile(studentID + "_" + dateApplied, null, new File(context.getFilesDir() + "/attachment/"));
        } catch (IOException e) {
            listener.onFailed();
            e.printStackTrace();
            return;
        }
        final File finalFile = file;
        runnable = new ConnectionRunnable(context, handler, new ConnectionRunnable.ConnectionListener() {
            @Override
            public void onConnected() {
                ATTACHMENT_STORAGE.getFile(finalFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                        if (task.isSuccessful())
                            listener.onSuccess();
                        else
                            listener.onFailed();
                        handler.removeCallbacks(runnable);
                    }
                });
            }
        });
        handler.post(runnable);
    }

}

class ConnectionRunnable implements Runnable {
    private Handler handler;
    private ConnectionListener listener;
    private Context context;
    private volatile static boolean isDialogShowing = false;
    private boolean isInProgress;

    ConnectionRunnable(Context context, Handler handler, ConnectionListener listener) {
        this.listener = listener;
        this.handler = handler;
        this.context = context;
        isInProgress = false;

    }

    @Override
    public void run() {
        if (!isDialogShowing) {
            if (ConnectionStatus.isConnected(context)) {
                if (!isInProgress) {
                    isInProgress = true;
                    listener.onConnected();
                }
            } else {
                ShowDialog();
            }
        }
        handler.postDelayed(this, 2000);
    }

    private void ShowDialog() {
        isDialogShowing = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("No Internet Connection");
        builder.setCancelable(false);
        builder.setMessage("We can not detect any internet connectivity. Please check your internet connection and try again");
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isDialogShowing = false;
                if (!ConnectionStatus.isConnected(context)) {
                    ShowDialog();
                }
            }
        });
        builder.show();
    }

    interface ConnectionListener {
        void onConnected();
    }

}