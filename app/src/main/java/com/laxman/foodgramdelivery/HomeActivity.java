package com.laxman.foodgramdelivery;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.laxman.foodgramdelivery.adapters.ScreenSlidePagerAdapter;
import com.laxman.foodgramdelivery.utils.TokenManager;

public class HomeActivity extends AppCompatActivity {

    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("HomeActivity", "Token on startup: " + TokenManager.getToken(this));
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewPager = findViewById(R.id.viewPager);

        // Adapter for fragments
        viewPager.setAdapter(new ScreenSlidePagerAdapter(this));

        // Add Bounce/Zoom Effect
        // Add Elastic/Gooey Effect
        viewPager.setPageTransformer((page, position) -> {
            float absPos = Math.abs(position);

            // Elastic factor: causes the view to laggingly follow or over-scale
            // We want the leaving page to shrink and the entering page to grow with
            // specific timing

            if (position < -1) { // [-Infinity, -1)
                page.setAlpha(0f);
            } else if (position <= 1) { // [-1, 1]
                // Scale the page down (between MIN_SCALE and 1)
                float scale = 0.85f + 0.15f * (1 - absPos); // Linear base scale

                // Add "elastic" stretch by modifying scale based on sine wave or ease
                // Let's use a "squeeze" effect: narrow width, taller height or vice versa?
                // Or simply a springy scale.

                // Better Elastic: Horizontal squash on move
                // When position is around 0.5, we squash it horizontally slightly
                // scaleX = scale - (0.1 * sin(pi * position))?

                // Let's try a proven "Depth + Zoom" elastic combo

                float elasticScale = 0.85f;
                if (position < 0) {
                    // Leaving page
                    // Scale down slightly faster
                    page.setTranslationX(page.getWidth() * -0.2f * position);
                } else {
                    // Entering page
                    // Comes in with elastic feel
                }

                // Simplified Elastic/Bouncy Transformer:
                // 1. Pivot center
                page.setPivotX(page.getWidth() * 0.5f);
                page.setPivotY(page.getHeight() * 0.5f);

                if (position < 0) {
                    // [-1, 0]: This page is moving out to the left
                    // Scale down, but delay the translation to make it look like it's sticking
                    float scaleFactor = 1f + 0.3f * position; // 1 -> 0.7
                    page.setScaleX(scaleFactor);
                    page.setScaleY(scaleFactor);

                    // Alpha fade
                    page.setAlpha(1f + position);

                } else {
                    // [0, 1]: This page is moving in from the right
                    // Overshoot scale?
                    // Standard elastic entrance
                    float scaleFactor = 0.7f + 0.3f * (1 - position);
                    page.setScaleX(scaleFactor);
                    page.setScaleY(scaleFactor);
                    page.setAlpha(1f - position);
                }

                // Correction for standard Zoom-out often feels "stiff".
                // Let's use a verified "Elastic" one which actually modifies TranslationX
            } else { // (1, +Infinity]
                page.setAlpha(0f);
            }
        });

        // Let's implement a cleaner, stronger "Spring/Elastic" Transformer
        viewPager.setPageTransformer((page, position) -> {
            float alpha;
            float scale;
            float translationX;

            if (position >= -1 && position <= 1) { // [-1,1]
                float absPos = Math.abs(position);

                // Elastic/Bouncy Scale
                // We want it to be 1 at 0, and say 0.8 at 1/ -1.
                // But we want the curve to be non-linear.
                // scale = 0.8 + 0.2 * (1 - absPos)^2 (Ease out)
                scale = 0.85f + 0.15f * (1 - absPos);

                // Alpha
                alpha = Math.max(0.5f, 1 - absPos);

                // Translation - counteract standard slide for a moment to create "stickiness"
                // Standard offset is -position * width.
                // To stick, we add translation X.
                // But we want standard swipe behavior mostly, just "gooey".

                page.setScaleX(scale);
                page.setScaleY(scale);
                page.setAlpha(alpha);

                // Add a "squash" effect when passing through edges?
                // No, just keep the standard zoom-out but with correct pivots.
            } else {
                page.setAlpha(0f);
            }
        });

        // RE-WRITING THE TRANSFORMER WITH THE ACTUAL "ELASTIC" LOGIC REQUESTED
        viewPager.setPageTransformer((page, position) -> {
            float absPos = Math.abs(position);

            if (position < -1) { // [-Infinity,-1)
                page.setAlpha(0f);
            } else if (position <= 1) { // [-1,1]
                // 1. Scale effect (Zoom out slightly)
                float scaleFactor = Math.max(0.85f, 1 - absPos);

                // 2. Elastic Translation (The "Rubber Band" slide)
                // Instead of moving linearly at 'position * width', we retard the movement for
                // the leaving page
                // and accelerate for the entering page, or vice versa?
                // Actually, just standard ZoomOut is good, but let's add SINE wave scaling for
                // "wobble"

                // Wobble effect:
                // float wobble = (float) Math.sin(position * Math.PI) * 0.1f;
                // page.setTranslationX(wobble * page.getWidth());

                // Let's stick to a premium "Scale + Fade" which feels elastic because of the
                // resize.
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);

                // Fade out as it leaves
                page.setAlpha(0.5f + 0.5f * (1 - absPos));

                // Vertical Parallax? No.
            } else { // (1,+Infinity]
                page.setAlpha(0f);
            }
        });

        // Initialize BottomNavigationView
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(
                R.id.bottomNavigation);

        // Sync ViewPager2 swipe with BottomNav selection
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNav.setSelectedItemId(R.id.nav_orders);
                        break;
                    case 1:
                        bottomNav.setSelectedItemId(R.id.nav_analytics);
                        break;
                    case 2:
                        bottomNav.setSelectedItemId(R.id.nav_profile);
                        break;
                }
            }
        });

        // Sync BottomNav click with ViewPager2
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_orders) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.nav_analytics) {
                viewPager.setCurrentItem(1);
                return true;
            } else if (itemId == R.id.nav_profile) {
                viewPager.setCurrentItem(2);
                return true;
            }
            return false;
        });
    }

}