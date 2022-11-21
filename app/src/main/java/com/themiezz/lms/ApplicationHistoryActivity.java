package com.themiezz.lms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.themiezz.lms.helper_classes.FirebaseHelper;
import com.themiezz.lms.helper_classes.StaticHelper;
import com.themiezz.lms.user_defined_classes.Application;

public class ApplicationHistoryActivity extends AppCompatActivity implements ApplicationHolder.OnClickListener {
    RecyclerView rvApplicationList;
    FirebaseRecyclerOptions<Application> options;
    FirebaseRecyclerAdapter<Application, ApplicationHolder> adapter;
    Application application;
    DatabaseReference applicationRef;
    TextView tvZeroResult;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_history);

        tvZeroResult = findViewById(R.id.tvZeroResult);
        progressBar = findViewById(R.id.progressBar);
        application = StaticHelper.application;
        applicationRef = StaticHelper.applicationReference;
        rvApplicationList = findViewById(R.id.rvApplicationList);
        Query query = FirebaseHelper.APPLICATIONS_DATABASE.child(StaticHelper.student.getDepartment()).orderByChild("studentID").equalTo(StaticHelper.student.getStudentID());
        options = new FirebaseRecyclerOptions.Builder<Application>()
                .setQuery(query, Application.class).build();
        adapter = new FirebaseRecyclerAdapter<Application, ApplicationHolder>(options) {

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

            @Override
            protected void onBindViewHolder(@NonNull ApplicationHolder applicationHolder, int i, @NonNull Application application) {
                applicationHolder.setData(application, ApplicationHistoryActivity.this);
                new FirebaseHelper(ApplicationHistoryActivity.this);
            }

            @NonNull
            @Override
            public ApplicationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_std_application_history, parent, false);
                ApplicationHolder holder = new ApplicationHolder(view);
                holder.setListener(ApplicationHistoryActivity.this);
                return holder;
            }
        };

        rvApplicationList.setAdapter(adapter);
        rvApplicationList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onDestroy() {
        StaticHelper.application = application;
        StaticHelper.applicationReference = applicationRef;
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onClick(View view, int position) {
        StaticHelper.application = adapter.getItem(position);
        StaticHelper.applicationReference = adapter.getRef(position);
        startActivity(new Intent(this, ApplicationStatusActivity.class));
    }

}

class ApplicationHolder extends RecyclerView.ViewHolder {
    private TextView tvAppliedOn, tvReason;
    private ImageView ivApplicationStatus;
    private ApplicationHolder.OnClickListener listener;

    ApplicationHolder(@NonNull View itemView) {
        super(itemView);
        tvAppliedOn = itemView.findViewById(R.id.tvDateApplied);
        tvReason = itemView.findViewById(R.id.tvReason);
        ivApplicationStatus = itemView.findViewById(R.id.ivApplicationStatus);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(view, getAdapterPosition());
            }
        });
    }

    void setData(Application data, Context context) {
        tvAppliedOn.setText(data.getDateApplied());
        if (data.getReason().length() <= 20)
            tvReason.setText(data.getReason());
        else
            tvReason.setText(data.getReason().substring(0, 20));
        if (data.getStatus().equals("Pending"))
            ivApplicationStatus.setImageDrawable(context.getDrawable(R.drawable.application_pending));
        else if (data.getStatus().equals("Accepted"))
            ivApplicationStatus.setImageDrawable(context.getDrawable(R.drawable.application_accepted));
        else
            ivApplicationStatus.setImageDrawable(context.getDrawable(R.drawable.application_declined));
    }

    void setListener(OnClickListener listener) {
        this.listener = listener;
    }

    public interface OnClickListener {
        void onClick(View view, int position);
    }

}
