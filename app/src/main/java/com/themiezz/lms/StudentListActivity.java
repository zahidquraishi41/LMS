package com.themiezz.lms;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.Query;
import com.themiezz.lms.helper_classes.FirebaseHelper;
import com.themiezz.lms.helper_classes.StaticHelper;
import com.themiezz.lms.user_defined_classes.Student;

public class StudentListActivity extends AppCompatActivity implements StudentListHolder.OnClickListener {
    RecyclerView rvStudentList;
    FirebaseRecyclerOptions<Student> options;
    FirebaseRecyclerAdapter<Student, StudentListHolder> adapter;
    View parentView;
    TextView tvZeroResult;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        tvZeroResult = findViewById(R.id.tvZeroResult);
        progressBar = findViewById(R.id.progressBar);
        rvStudentList = findViewById(R.id.rvStudentList);
        parentView = findViewById(R.id.parentView);
        Query query = FirebaseHelper.STUDENTS_DATABASE.orderByChild("department").equalTo(StaticHelper.hod.getDepartment());
        options = new FirebaseRecyclerOptions.Builder<Student>()
                .setQuery(query, Student.class).build();
        adapter = new FirebaseRecyclerAdapter<Student, StudentListHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull StudentListHolder studentListHolder, int i, @NonNull Student student) {
                studentListHolder.setData(student, StudentListActivity.this);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                progressBar.setVisibility(View.GONE);
                if (adapter.getItemCount() == 0) {
                    tvZeroResult.setVisibility(View.VISIBLE);
                } else if (tvZeroResult.getVisibility() == View.VISIBLE) {
                    tvZeroResult.setVisibility(View.GONE);
                }
            }

            @NonNull
            @Override
            public StudentListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_student_list, parent, false);
                StudentListHolder holder = new StudentListHolder(view);
                holder.setListener(StudentListActivity.this);
                return holder;
            }
        };
        rvStudentList.setAdapter(adapter);
        rvStudentList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onClick(View view, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Confirmation");
        builder.setMessage("Are you sure you want to delete " + adapter.getItem(position).getStudentID() + "'s account.");
        builder.setCancelable(true);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final ProgressDialog progressDialog = ProgressDialog.show(StudentListActivity.this, null, "Removing...", true, false);
                new FirebaseHelper(StudentListActivity.this).deleteStudent(adapter.getItem(position).getStudentID(), adapter.getItem(position).getDepartment(), new FirebaseHelper.CompletionListener() {
                    @Override
                    public void onSuccess() {
                        progressDialog.dismiss();
                        Snackbar.make(parentView, "Removed Successfully", Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed() {
                        progressDialog.dismiss();
                        Snackbar.make(parentView, "Filed to Removed", Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();
    }


}

class StudentListHolder extends RecyclerView.ViewHolder {
    private TextView tvFullName, tvUserID;
    private ImageView ivProfilePicture;
    private StudentListHolder.OnClickListener listener;

    StudentListHolder(@NonNull View itemView) {
        super(itemView);
        tvFullName = itemView.findViewById(R.id.tvFullName);
        tvUserID = itemView.findViewById(R.id.tvStudentID);
        ivProfilePicture = itemView.findViewById(R.id.ivProfilePicture);
        ImageView ivDeleteIcon = itemView.findViewById(R.id.ivDelete);
        ivDeleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(view, getAdapterPosition());
            }
        });
    }

    void setData(Student student, Context context) {
        tvFullName.setText(student.getFullName());
        tvUserID.setText(student.getStudentID());
        new FirebaseHelper(context).loadImageInto(ivProfilePicture, student.getStudentID(), FirebaseHelper.STUDENT);
    }

    void setListener(OnClickListener listener) {
        this.listener = listener;
    }

    public interface OnClickListener {
        void onClick(View view, int position);
    }

}
