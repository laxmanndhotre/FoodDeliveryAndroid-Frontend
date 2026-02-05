package com.laxman.foodgramdelivery.utils;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

public class BounceTouchListener implements View.OnTouchListener {

    private static final float SCALE_DOWN_FACTOR = 0.92f;
    private static final int DURATION = 100;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                animateScale(v, SCALE_DOWN_FACTOR);
                return true; // Consume to ensure we get UP event. Warning: might block ClickListener if not
                             // handled carefully.
            // Actually, standard OnTouchListener blocking clicks is bad.
            // Better approach: Let it return false but only animate.
            // OR wrap the click logic.

            case MotionEvent.ACTION_UP:
                animateScale(v, 1f);
                v.performClick(); // Manually trigger click
                return true;

            case MotionEvent.ACTION_CANCEL:
                animateScale(v, 1f);
                return true;
        }
        return false;
    }

    private void animateScale(View v, float scale) {
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat(View.SCALE_X, scale);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat(View.SCALE_Y, scale);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(v, pvhX, pvhY);
        animator.setDuration(DURATION);
        animator.setInterpolator(new OvershootInterpolator());
        animator.start();
    }

    // Static helper to attach to a view
    public static void attach(View view) {
        view.setOnTouchListener(new BounceTouchListener());
    }
}
