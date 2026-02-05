package com.laxman.foodgramdelivery;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.laxman.foodgramdelivery.utils.TokenManager;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String token = TokenManager.getToken(SplashScreen.this);

            Intent intent;
            if (token != null && !token.isEmpty()) {
                // Token exists → go to Home
                intent = new Intent(SplashScreen.this, HomeActivity.class);
            } else {
                // No token → go to Register/Login
                intent = new Intent(SplashScreen.this, RegisterActivity.class);
                // or LoginActivity if you prefer login first
            }

            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, 2000); // 2 seconds splash delay
    }
}