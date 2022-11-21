package com.themiezz.lms.helper_classes;

import android.graphics.Color;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

public class IAEM {

    private TextView textView;
    static CountDownTimer timer;
    static boolean isTimerRunning = false;
    public static int INFORMATIVE = 0;
    public static int WARNING = 1;
    public static int ERROR = 2;

    public IAEM(TextView textView) {
        this.textView = textView;
        this.textView.setTextColor(Color.parseColor("#D8000C"));
    }

    public void showMessage(String message) {
        textView.setVisibility(View.VISIBLE);
        textView.setText(message);
        if (isTimerRunning)
            timer.cancel();
        timer = new CountDownTimer(2500, 500) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                textView.setVisibility(View.GONE);
                isTimerRunning = false;
            }
        };
        timer.start();
    }

    public void showMessage(String message, int type) {
        if (type == INFORMATIVE) {
            textView.setTextColor(Color.GREEN);
            showMessage(message);
        } else if (type == WARNING) {
            textView.setTextColor(Color.YELLOW);
            showMessage(message);
        } else if (type == ERROR) {
            textView.setTextColor(Color.parseColor("#D8000C"));
            showMessage(message);
        }
    }

    public boolean isVisible() {
        return (textView.getVisibility() == View.VISIBLE);
    }

}
