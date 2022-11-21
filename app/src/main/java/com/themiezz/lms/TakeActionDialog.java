package com.themiezz.lms;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class TakeActionDialog extends BottomSheetDialogFragment {
    public static final int ACCEPT_APPLICATION = 1;
    public static final int DECLINE_APPLICATION = 2;
    public static final int CALL_PARENT = 3;
    public static final int VIEW_ATTACHMENT = 4;

    private ActionListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.take_action_dialog, container, false);
        Button btnAcceptApplication = view.findViewById(R.id.btnAcceptApplication);
        Button btnDeclineApplication = view.findViewById(R.id.btnDeclineApplication);
        Button btnViewAttachment = view.findViewById(R.id.btnViewAttachment);
        Button btnCallParent = view.findViewById(R.id.btnCallParent);

        btnAcceptApplication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(ACCEPT_APPLICATION);
                dismiss();
            }
        });

        btnDeclineApplication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(DECLINE_APPLICATION);
                dismiss();
            }
        });

        btnViewAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(VIEW_ATTACHMENT);
                dismiss();
            }
        });

        btnCallParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(CALL_PARENT);
                dismiss();
            }
        });

        return view;
    }

    public interface ActionListener {
        void onClick(int id);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        listener = (ActionListener) context;
    }
}
