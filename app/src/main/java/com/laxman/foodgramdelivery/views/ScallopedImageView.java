package com.laxman.foodgramdelivery.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ScallopedImageView extends androidx.appcompat.widget.AppCompatImageView {

    private Path path;
    private RectF rect;

    public ScallopedImageView(@NonNull Context context) {
        super(context);
        init();
    }

    public ScallopedImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScallopedImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        path = new Path();
        rect = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updatePath(w, h);
    }

    private void updatePath(int w, int h) {
        path.reset();
        rect.set(0, 0, w, h);

        float centerX = w / 2f;
        float centerY = h / 2f;

        // Radius is slightly smaller than half width to ensure it fits with stroke if
        // needed
        float radius = Math.min(w, h) / 2f;

        // Generate a scalloped/flower shape
        // Parametric equation for a wavy circle:
        // x = (R + A*cos(k*theta)) * cos(theta)
        // y = (R + A*cos(k*theta)) * sin(theta)

        int petals = 8; // Number of bumps
        float amplitude = radius * 0.1f; // Depth of the wave
        float baseRadius = radius - amplitude;

        path.moveTo(centerX + (baseRadius + amplitude), centerY); // Start at theta=0

        for (int i = 1; i <= 360; i++) {
            float angle = (float) Math.toRadians(i);
            float r = baseRadius + amplitude * (float) Math.cos(petals * angle);

            float x = centerX + r * (float) Math.cos(angle);
            float y = centerY + r * (float) Math.sin(angle);

            path.lineTo(x, y);
        }
        path.close();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.clipPath(path);
        super.onDraw(canvas);
        canvas.restore();
    }
}
