package com.laxman.foodgramdelivery.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.laxman.foodgramdelivery.R;

public class SquigglyProgressView extends View {

    private Paint paint;
    private Path path;
    private float phase = 0f;
    private float amplitude = 20f; // Height of the wave
    private float frequency = 1f; // Wiggles per width (roughly)
    private float speed = 10f;
    private int waveColor = Color.parseColor("#6200EE");
    private float strokeWidth = 8f;
    private ValueAnimator animator;

    public SquigglyProgressView(Context context) {
        super(context);
        init(null);
    }

    public SquigglyProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SquigglyProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        // Resolve colorPrimary from theme
        android.util.TypedValue typedValue = new android.util.TypedValue();
        android.content.res.Resources.Theme theme = getContext().getTheme();
        // Try resolving android.R.attr.colorPrimary (usually works for Material themes
        // too)
        if (theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)) {
            waveColor = typedValue.data;
        } else {
            // Fallback to the resource explicitly if attribute resolution fails
            waveColor = androidx.core.content.ContextCompat.getColor(getContext(), R.color.my_light_primary);
        }

        if (attrs != null) {
            // Check for custom attributes if we had defined styleable (skipping for now to
            // keep it simple, just using defaults or setters)
        }

        paint.setColor(waveColor);
        paint.setStrokeWidth(strokeWidth);
        path = new Path();

        startAnimation();
    }

    private void startAnimation() {
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1000); // 1 second per cycle
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            phase = (float) animation.getAnimatedValue() * (float) (2 * Math.PI);
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (animator != null && !animator.isRunning()) {
            animator.start();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        float centerX = width / 2f;
        float centerY = height / 2f;

        // Radius of the main circle
        float maxRadius = Math.min(width, height) / 2f;
        float baseRadius = maxRadius * 0.7f; // Leave room for squiggles

        path.reset();

        // Draw circular sine wave (Star/Flower shape but animating phase makes it
        // spin/wiggle)
        // r = R + A * sin(N * theta + phase)

        int points = 360; // Resolution
        int waves = 8; // Number of squiggles around the circle

        for (int i = 0; i <= points; i++) {
            double angleRad = Math.toRadians(i);

            // Calculate radius at this angle
            double r = baseRadius + amplitude * Math.sin(waves * angleRad + phase);

            float x = (float) (centerX + r * Math.cos(angleRad));
            float y = (float) (centerY + r * Math.sin(angleRad));

            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.close(); // Close the loop

        canvas.drawPath(path, paint);
    }

    public void setWaveColor(int color) {
        this.waveColor = color;
        paint.setColor(color);
        invalidate();
    }
}
