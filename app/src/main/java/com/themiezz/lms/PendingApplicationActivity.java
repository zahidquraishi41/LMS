package com.themiezz.lms;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.themiezz.lms.helper_classes.FirebaseHelper;
import com.themiezz.lms.helper_classes.Helper;
import com.themiezz.lms.helper_classes.StaticHelper;
import com.themiezz.lms.user_defined_classes.Application;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class PendingApplicationActivity extends AppCompatActivity implements PendingApplicationHolder.OnClickListener {
    private static final String TAG = "ZQPending";
    RecyclerView rvApplicationList;
    FirebaseRecyclerOptions<Application> options;
    FirebaseRecyclerAdapter<Application, PendingApplicationHolder> adapter;
    Map<String, Object> mapAccept, mapReject, mapUndo, mapExpired;
    View parentView;
    Integer currMonth;
    HashMap<String, String> fullNameMap;
    TextView tvZeroResult;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_application);

        tvZeroResult = findViewById(R.id.tvZeroResult);
        progressBar = findViewById(R.id.progressBar);
        parentView = findViewById(R.id.parentView);
        fullNameMap = new HashMap<>();
        mapAccept = new HashMap<>();
        mapReject = new HashMap<>();
        mapUndo = new HashMap<>();
        mapExpired = new HashMap<>();
        mapAccept.put("status", "Accepted");
        mapAccept.put("remarks", "None");
        mapReject.put("status", "Rejected");
        mapReject.put("remarks", "None");
        mapUndo.put("status", "Pending");
        mapUndo.put("remarks", "Pending");
        mapExpired.put("status", "Expired");
        mapExpired.put("remarks", "Expired");
        currMonth = Integer.parseInt(new SimpleDateFormat("MM", Locale.getDefault()).format(new Date()));
        rvApplicationList = findViewById(R.id.rvApplicationList);
        Query query = FirebaseHelper.APPLICATIONS_DATABASE.child(StaticHelper.hod.getDepartment()).orderByChild("status").equalTo("Pending");
        options = new FirebaseRecyclerOptions.Builder<Application>()
                .setQuery(query, Application.class).build();
        adapter = new FirebaseRecyclerAdapter<Application, PendingApplicationHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final PendingApplicationHolder holder, int i, @NonNull final Application application) {
                if (!isApplicationExpired(application)) {
                    holder.setData("...", application.getStudentID(), application.getFromDate() + " - " + application.getToDate());
                    if (!fullNameMap.containsKey(application.getStudentID()))
                        FirebaseHelper.STUDENTS_DATABASE.child(application.getStudentID()).child("fullName").addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String fullName = dataSnapshot.getValue(String.class);
                                fullNameMap.put(application.getStudentID(), fullName);
                                holder.setFullName(fullName);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    else
                        holder.setData(fullNameMap.get(application.getStudentID()), application.getStudentID(), application.getFromDate() + " - " + application.getToDate());
                    new FirebaseHelper(PendingApplicationActivity.this).loadImageInto(holder.ivStudentPicture, application.getStudentID(), FirebaseHelper.STUDENT);
                } else {
                    adapter.getRef(i).updateChildren(mapExpired);
                }
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
            public PendingApplicationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_pending_application, parent, false);
                PendingApplicationHolder holder = new PendingApplicationHolder(view);
                holder.setListener(PendingApplicationActivity.this);
                return holder;
            }
        };

        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(PendingApplicationActivity.this, c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(PendingApplicationActivity.this, R.color.colorGreen))
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(PendingApplicationActivity.this, R.color.colorRed))
                        .addSwipeRightActionIcon(R.drawable.ic_delete_white_24dp)
                        .addSwipeLeftActionIcon(R.drawable.ic_accept_white_24dp)
                        .addSwipeRightLabel("Decline")
                        .addSwipeLeftLabel("Accept")
                        .setSwipeLeftLabelColor(Color.WHITE)
                        .setSwipeRightLabelColor(Color.WHITE)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                switch (direction) {
                    case ItemTouchHelper.LEFT:
                        AcceptApplication(position);
                        break;
                    case ItemTouchHelper.RIGHT:
                        DeclineApplication(position);
                        break;
                }
            }
        };

        rvApplicationList.setAdapter(adapter);
        rvApplicationList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rvApplicationList);
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
    public void onClick(View view, int position) {
        StaticHelper.application = adapter.getItem(position);
        StaticHelper.applicationReference = adapter.getRef(position);
        startActivity(new Intent(this, ApplicationDetailsActivity.class));
    }

    public boolean isApplicationExpired(Application application) {
        int currDate = Integer.parseInt(new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()));
        if (currDate > Helper.DateToInt(application.getFromDate()) && application.getStatus().equals("Pending")) {
            return true;
        }
        if (currDate > Helper.DateToInt(application.getToDate()) && application.getStatus().equals("Pending")) {
            return true;
        } else return currDate > Helper.DateToInt(application.getToDate());
    }

    private void AcceptApplication(int position) {
        final DatabaseReference reference = adapter.getRef(position);
        final String fromDate = adapter.getItem(position).getFromDate(), toDate = adapter.getItem(position).getToDate(), studentID = adapter.getItem(position).getStudentID();

        new FirebaseHelper(this).updateKey(reference, mapAccept, new FirebaseHelper.CompletionListener() {
            @Override
            public void onSuccess() {
                new FirebaseHelper(PendingApplicationActivity.this).incrementValue(FirebaseHelper.LEAVE_INFO_DATABASE.child(StaticHelper.hod.getDepartment()).child(studentID).child("timesAccepted"));
                new FirebaseHelper(PendingApplicationActivity.this).incrementValue(FirebaseHelper.LEAVE_INFO_DATABASE.child(StaticHelper.hod.getDepartment()).child(studentID).child("leaveDaysCount"), Helper.getDays(fromDate, toDate));
                Snackbar.make(parentView, "Application Accepted Successfully", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new FirebaseHelper(PendingApplicationActivity.this).updateKey(reference, mapUndo, new FirebaseHelper.CompletionListener() {
                                    @Override
                                    public void onSuccess() {
                                        new FirebaseHelper(PendingApplicationActivity.this).decrementValue(FirebaseHelper.LEAVE_INFO_DATABASE.child(StaticHelper.hod.getDepartment()).child(studentID).child("timesAccepted"));
                                        new FirebaseHelper(PendingApplicationActivity.this).decrementValue(FirebaseHelper.LEAVE_INFO_DATABASE.child(StaticHelper.hod.getDepartment()).child(studentID).child("leaveDaysCount"), Helper.getDays(fromDate, toDate));
                                    }

                                    @Override
                                    public void onFailed() {
                                        Snackbar.make(parentView, "Failed to Undo", Snackbar.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }).show();
            }

            @Override
            public void onFailed() {
                Snackbar.make(parentView, "Failed to Update", Snackbar.LENGTH_LONG).show();
            }
        });

    }

    private void DeclineApplication(final int position) {
        final DatabaseReference reference = adapter.getRef(position);
        new FirebaseHelper(this).updateKey(reference, mapReject, new FirebaseHelper.CompletionListener() {
            @Override
            public void onSuccess() {
                Snackbar.make(parentView, "Application Declined Successfully", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new FirebaseHelper(PendingApplicationActivity.this).updateKey(reference, mapUndo, new FirebaseHelper.CompletionListener() {
                                    @Override
                                    public void onSuccess() {
                                    }

                                    @Override
                                    public void onFailed() {
                                        Snackbar.make(parentView, "Failed to Undo", Snackbar.LENGTH_LONG).show();
                                    }
                                });
                            }
                        })
                        .show();
            }

            @Override
            public void onFailed() {
                Snackbar.make(parentView, "Failed to Update", Snackbar.LENGTH_LONG).show();
            }
        });
    }

}

class PendingApplicationHolder extends RecyclerView.ViewHolder {
    private TextView tvStudentName, tvStudentId, tvFromToDate;
    ImageView ivStudentPicture;
    private PendingApplicationHolder.OnClickListener listener;

    PendingApplicationHolder(@NonNull View itemView) {
        super(itemView);
        tvStudentName = itemView.findViewById(R.id.tvStudentName);
        tvStudentId = itemView.findViewById(R.id.tvStudentId);
        tvFromToDate = itemView.findViewById(R.id.tvFromToDate);
        ivStudentPicture = itemView.findViewById(R.id.ivStudentPicture);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(view, getAdapterPosition());
            }
        });
    }

    void setData(String studentName, String studentId, String fromToDate) {
        tvStudentName.setText(studentName);
        tvStudentId.setText(studentId);
        tvFromToDate.setText(fromToDate);
    }

    void setFullName(String fullName) {
        if (fullName != null)
            tvStudentName.setText(fullName);
    }

    void setListener(OnClickListener listener) {
        this.listener = listener;
    }

    public interface OnClickListener {
        void onClick(View view, int position);
    }

}
