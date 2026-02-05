package com.laxman.foodgramdelivery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.laxman.foodgramdelivery.models.RegisterRequest;
import com.laxman.foodgramdelivery.models.RegisterResponse;
import com.laxman.foodgramdelivery.network.DeliveryApi;
import com.laxman.foodgramdelivery.network.RetrofitClient;
import com.laxman.foodgramdelivery.network.RetrofitClient;
import com.laxman.foodgramdelivery.utils.TokenManager;
import com.laxman.foodgramdelivery.models.LoginRequest;
import com.laxman.foodgramdelivery.models.AuthResponse;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextFullName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextPhone;
    private String editTextRole;
    private Button buttonRegister;
    private DeliveryApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextRole = "delivery_person";
        buttonRegister = findViewById(R.id.buttonRegister);

        api = RetrofitClient.getInstance(this).create(DeliveryApi.class);

        TextView textLoginInstead = findViewById(R.id.textLoginInstead);
        textLoginInstead.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        buttonRegister.setOnClickListener(v -> {
            registerUser();
        });

    }

    private void registerUser() {
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String role = editTextRole;

        RegisterRequest request = new RegisterRequest(fullName, email, password, phone, role);

        api.register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();
                    Log.d("RegisterActivity", "Message=" + registerResponse.getMessage()
                            + ", UserId=" + registerResponse.getUserId()
                            + ", Role=" + registerResponse.getRole());

                    TokenManager.saveToken(RegisterActivity.this, registerResponse.getToken());
                    // Automatically login to check for existing profile
                    performLogin(email, password);
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Log.e("RegisterActivity", "Error: " + t.getMessage());
                Toast.makeText(RegisterActivity.this, "Registration failed: " + t.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void performLogin(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);
        api.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    Long userId = response.body().getUserId();
                    Long deliveryPersonId = response.body().getDeliveryPersonId();

                    TokenManager.saveToken(RegisterActivity.this, token);
                    TokenManager.saveUserDetails(RegisterActivity.this, userId, deliveryPersonId);

                    Log.d("RegisterActivity", "Auto-login successful. dpId=" + deliveryPersonId);

                    // Profile exists or skipped, go to Home
                    Toast.makeText(RegisterActivity.this, "Welcome " + response.body().getUserId(), Toast.LENGTH_SHORT)
                            .show();
                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                } else {
                    Log.e("RegisterActivity", "Auto-login failed: " + response.code());
                    Toast.makeText(RegisterActivity.this, "Registration successful, please login.", Toast.LENGTH_SHORT)
                            .show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Log.e("RegisterActivity", "Auto-login error: " + t.getMessage());
                Toast.makeText(RegisterActivity.this, "Registration successful, please login.", Toast.LENGTH_SHORT)
                        .show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

}