package com.laxman.foodgramdelivery.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingParent;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;

public class SquigglySwipeRefreshLayout extends FrameLayout implements NestedScrollingParent {

    private static final float DRAG_RATE = 0.8f; // Slightly less than 1:1 for initial resistance
    private static final int DRAG_MAX_DISTANCE = 400; // dp, increased for elastic range
    private static final int REFRESH_TRIGGER_DISTANCE = 120; // dp

    private View targetView; // The content view (RecyclerView)
    private SquigglyProgressView squigglyView;
    private int touchSlop;
    private float initialDownY;
    private boolean isBeingDragged;
    private boolean isRefreshing;
    private OnRefreshListener listener;

    // For nested scrolling
    private NestedScrollingParentHelper nestedScrollingParentHelper;

    public interface OnRefreshListener {
        void onRefresh();
    }

    public SquigglySwipeRefreshLayout(@NonNull Context context) {
        this(context, null);
    }

    public SquigglySwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        nestedScrollingParentHelper = new NestedScrollingParentHelper(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // Assume the last child is the content? Or find by ID?
        // We will create the SquigglyView programmatically or expect it as a child.
        // Let's create it programmatically to ensure it exists and separate from
        // content.

        squigglyView = new SquigglyProgressView(getContext());
        int size = (int) (60 * getResources().getDisplayMetrics().density);
        LayoutParams params = new LayoutParams(size, size);
        params.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        params.topMargin = -size; // Start hidden above
        addView(squigglyView, 0, params); // Add at bottom index but visually on top if translated?
        // Actually index 0 is behind others usually, but we translate content down, so
        // order matters less if no overlap.
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        this.listener = listener;
    }

    public void setRefreshing(boolean refreshing) {
        if (isRefreshing == refreshing)
            return;

        isRefreshing = refreshing;
        if (!refreshing) {
            animateToPosition(0);
        } else {
            // If triggered programmatically
            int triggerPx = (int) (REFRESH_TRIGGER_DISTANCE * getResources().getDisplayMetrics().density);
            animateToPosition(triggerPx);
        }
    }

    private void ensureTarget() {
        if (targetView == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child != squigglyView) {
                    targetView = child;
                    break;
                }
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();
        if (targetView == null || !isEnabled() || canChildScrollUp() || isRefreshing) {
            return false;
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialDownY = ev.getY();
                isBeingDragged = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float y = ev.getY();
                float yDiff = y - initialDownY;
                if (yDiff > touchSlop && !isBeingDragged) {
                    initialDownY = y; // Reset start point to current
                    isBeingDragged = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isBeingDragged = false;
                break;
        }
        return isBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isBeingDragged)
            return super.onTouchEvent(ev);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float y = ev.getY();
                float scrollTop = (y - initialDownY) * DRAG_RATE;
                if (scrollTop > 0) {
                    moveSpinner(scrollTop);
                } else {
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isBeingDragged = false;
                finishDrag();
                return false;
        }
        return true;
    }

    private void moveSpinner(float scrollTop) {
        float maxDrag = DRAG_MAX_DISTANCE * getResources().getDisplayMetrics().density;

        // Elastic effect: as scrollTop increases, the actual movement decreases
        // (damping)
        // y = final_distance * (1 - e^(-scrollTop / final_distance))
        // This gives a nice rubber-band effect that asymptotically approaches maxDrag

        // We want slightly more travel than before, so let's set asymptotic limit
        // higher
        float asymptoticLimit = maxDrag * 1.5f;

        // Apply damping
        // If scrollTop is small, it's linear. As it gets larger, it resists more.
        float boundedDrag = asymptoticLimit * (1.0f - (float) Math.exp(-scrollTop / asymptoticLimit));

        // Move content down
        if (targetView != null) {
            targetView.setTranslationY(boundedDrag);
        }

        // Move spinner down
        if (squigglyView != null) {
            squigglyView.setTranslationY(boundedDrag);
            squigglyView.setVisibility(View.VISIBLE);
        }
    }

    private void finishDrag() {
        float triggerPx = REFRESH_TRIGGER_DISTANCE * getResources().getDisplayMetrics().density;
        if (targetView.getTranslationY() > triggerPx) {
            isRefreshing = true;
            animateToPosition(triggerPx);
            if (listener != null)
                listener.onRefresh();
        } else {
            isRefreshing = false;
            animateToPosition(0);
        }
    }

    private void animateToPosition(float targetY) {
        if (targetView == null)
            return;

        float currentY = targetView.getTranslationY();
        ValueAnimator animator = ValueAnimator.ofFloat(currentY, targetY);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(val -> {
            float y = (float) val.getAnimatedValue();
            targetView.setTranslationY(y);
            if (squigglyView != null) {
                squigglyView.setTranslationY(y);
                if (y == 0 && !isRefreshing) {
                    squigglyView.setVisibility(View.GONE);
                } else {
                    squigglyView.setVisibility(View.VISIBLE);
                }
            }
        });
        animator.start();
    }

    public boolean canChildScrollUp() {
        if (targetView == null)
            return false;
        return canViewScrollUp(targetView);
    }

    private boolean canViewScrollUp(View view) {
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            // Check if the view itself can scroll
            if (view.canScrollVertically(-1)) {
                return true;
            }
            // If not, check its children
            for (int i = 0; i < group.getChildCount(); i++) {
                if (canViewScrollUp(group.getChildAt(i))) {
                    return true;
                }
            }
            return false;
        } else {
            return view.canScrollVertically(-1);
        }
    }

    // Nested Scrolling Interface Implementation
    // This allows the SwipeRefresh to work with NestedScrollingChild (like
    // RecyclerView) nicely.
    // ... Simplified implementation for now, assuming RecyclerView will pass
    // touches up if top reached.
}
