package com.themiezz.lms.helper_classes;

import android.view.View;
import android.view.animation.Animation;

import java.util.ArrayList;
import java.util.Collections;

public class ViewStateChanger {

    private ArrayList<View> viewArrayList;
    private int VISIBILITY;
    public static final int VISIBLE = View.VISIBLE;
    public static final int INVISIBLE = View.INVISIBLE;
    public static final int GONE = View.GONE;

    public ViewStateChanger(View... views) {
        viewArrayList = new ArrayList<>();
        Collections.addAll(viewArrayList, views);
        VISIBILITY = views[0].getVisibility();
    }

    public void setVisible() {
        for (View view : viewArrayList) {
            view.setVisibility(View.VISIBLE);
        }
        VISIBILITY = VISIBLE;
    }

    public void setVisible(Animation animation) {
        for (View view : viewArrayList) {
            view.setVisibility(View.VISIBLE);
            view.startAnimation(animation);
        }
        VISIBILITY = VISIBLE;
    }

    public void setInvisible() {
        for (View view : viewArrayList) {
            view.setVisibility(View.INVISIBLE);
        }
        VISIBILITY = INVISIBLE;
    }

    public void setGone() {
        for (View view : viewArrayList) {
            view.setVisibility(View.GONE);
        }
        VISIBILITY = GONE;
    }

    public int getVisibility() {
        return VISIBILITY;
    }

    public void lockView() {
        for (View view : viewArrayList) {
            view.setEnabled(false);
            view.setClickable(false);
            view.setFocusable(false);
            view.setAlpha(.5f);
        }
    }

    public void unlockView() {
        for (View view : viewArrayList) {
            view.setEnabled(true);
            view.setClickable(true);
            view.setFocusable(true);
            view.setAlpha(1f);
        }
    }

}
