package com.laxman.foodgramdelivery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.laxman.foodgramdelivery.models.LoginRequest;
import com.laxman.foodgramdelivery.network.DeliveryApi;
import com.laxman.foodgramdelivery.network.RetrofitClient;
import com.laxman.foodgramdelivery.utils.TokenManager;
import com.laxman.foodgramdelivery.models.AuthResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private android.widget.TextView textViewRegister;
    private DeliveryApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);

        textViewRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        api = RetrofitClient.getInstance(this).create(DeliveryApi.class);

        buttonLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        Log.d("LoginActivity", "Attempting login with email=" + email);

        if (email.isEmpty() || password.isEmpty()) {
            Log.w("LoginActivity", "Email or password is empty");
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }
        LoginRequest request = new LoginRequest(email, password);
        api.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                Log.d("LoginActivity", "Received response: code=" + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    Long userId = response.body().getUserId();
                    Long deliveryPersonId = response.body().getDeliveryPersonId();
                    Log.i("LoginActivity", "Login successful. Token=" + token + ", UserId=" + userId
                            + ", DeliveryPersonId=" + deliveryPersonId);

                    TokenManager.saveToken(LoginActivity.this, token);
                    TokenManager.saveUserDetails(LoginActivity.this, userId, deliveryPersonId);
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                } else {
                    Log.e("LoginActivity", "Login failed. Response code=" + response.code()
                            + ", message=" + response.message()
                            + ", errorBody="
                            + (response.errorBody() != null ? response.errorBody().toString() : "null"));
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Log.e("LoginActivity", "Login request failed: " + t.getMessage(), t);
                Toast.makeText(LoginActivity.this, "Login failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}