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
import com.themiezz.lms.user_defined_classes.HOD;

public class HODListActivity extends AppCompatActivity implements HODListHolder.OnClickListener {
    RecyclerView rvHODList;
    FirebaseRecyclerOptions<HOD> options;
    FirebaseRecyclerAdapter<HOD, HODListHolder> adapter;
    View parentView;
    TextView tvZeroResult;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hod_list);

        tvZeroResult = findViewById(R.id.tvZeroResult);
        progressBar = findViewById(R.id.progressBar);
        rvHODList = findViewById(R.id.rvUserList);
        parentView = findViewById(R.id.parentView);
        Query query = FirebaseHelper.HOD_DATABASE;
        options = new FirebaseRecyclerOptions.Builder<HOD>()
                .setQuery(query, HOD.class).build();
        adapter = new FirebaseRecyclerAdapter<HOD, HODListHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull HODListHolder hodListHolder, int i, @NonNull HOD hod) {
                hodListHolder.setData(hod, HODListActivity.this);
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
            public HODListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_hod_list, parent, false);
                HODListHolder holder = new HODListHolder(view);
                holder.setListener(HODListActivity.this);
                return holder;

            }
        };

        rvHODList.setAdapter(adapter);
        rvHODList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
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
        builder.setMessage("Are you sure you want to delete " + adapter.getItem(position).getUsername() + "'s account. Deleting will also delete the following \n - " + adapter.getItem(position).getDepartment() + " Department" + "\n - Students under " + adapter.getItem(position).getDepartment() + " department");
        builder.setCancelable(true);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final ProgressDialog progressDialog = ProgressDialog.show(HODListActivity.this, null, "Removing...", true, false);
                new FirebaseHelper(HODListActivity.this).deleteHOD(adapter.getItem(position), new FirebaseHelper.CompletionListener() {
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

class HODListHolder extends RecyclerView.ViewHolder {
    private TextView tvName, tvUserID, tvDepartment;
    private ImageView ivProfilePicture;
    private HODListHolder.OnClickListener listener;

    HODListHolder(@NonNull View itemView) {
        super(itemView);
        tvName = itemView.findViewById(R.id.tvFullName);
        tvUserID = itemView.findViewById(R.id.tvUsername);
        tvDepartment = itemView.findViewById(R.id.tvDepartment);
        ivProfilePicture = itemView.findViewById(R.id.ivProfilePicture);
        ImageView ivDelete = itemView.findViewById(R.id.ivDelete);
        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(view, getAdapterPosition());
            }
        });
    }

    void setData(HOD hod, Context context) {
        tvName.setText(hod.getFullName());
        tvUserID.setText(hod.getUsername());
        tvDepartment.setText(hod.getDepartment());
        new FirebaseHelper(context).loadImageInto(ivProfilePicture, hod.getUsername(), FirebaseHelper.HOD);
    }

    void setListener(OnClickListener listener) {
        this.listener = listener;
    }

    public interface OnClickListener {
        void onClick(View view, int position);
    }

}