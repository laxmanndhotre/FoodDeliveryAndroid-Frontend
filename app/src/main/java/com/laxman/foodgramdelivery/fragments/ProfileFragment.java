package com.laxman.foodgramdelivery.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.laxman.foodgramdelivery.views.SquigglySwipeRefreshLayout;
import com.laxman.foodgramdelivery.R;
import com.laxman.foodgramdelivery.LoginActivity;
import com.laxman.foodgramdelivery.models.DeliveryPersonProfileDto;
import com.laxman.foodgramdelivery.network.DeliveryApi;
import com.laxman.foodgramdelivery.network.RetrofitClient;
import com.laxman.foodgramdelivery.network.OrderApi;
import com.laxman.foodgramdelivery.models.Delivery;
import com.laxman.foodgramdelivery.utils.TokenManager;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView textName, textEmail, textPhone, textVehicle, textArea, textStatus, textEarnings;
    private Button logoutButton, editProfileButton;
    private DeliveryApi api;
    private OrderApi orderApi;
    private SquigglySwipeRefreshLayout swipeRefreshLayout;

    private DeliveryPersonProfileDto currentProfile;

    private Long deliveryPersonId;
    private Long userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        textName = view.findViewById(R.id.textName);
        textEmail = view.findViewById(R.id.textEmail);
        textPhone = view.findViewById(R.id.textPhone);
        textVehicle = view.findViewById(R.id.textVehicle);
        textArea = view.findViewById(R.id.textArea);
        textStatus = view.findViewById(R.id.textStatus);
        textEarnings = view.findViewById(R.id.textEarnings);

        logoutButton = view.findViewById(R.id.buttonLogout);
        editProfileButton = view.findViewById(R.id.buttonEditProfile);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshProfile);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchProfile();
            fetchEarnings();
        });

        api = RetrofitClient.getInstance(requireContext()).create(DeliveryApi.class);
        orderApi = RetrofitClient.getInstance(requireContext()).create(OrderApi.class);

        logoutButton.setOnClickListener(v -> logout());
        editProfileButton.setOnClickListener(v -> {
            if (currentProfile != null) {
                editProfile(currentProfile); // pass the latest profile object
            } else {
                Toast.makeText(requireContext(), "Profile not loaded yet", Toast.LENGTH_SHORT).show();
            }
        });

        com.laxman.foodgramdelivery.utils.BounceTouchListener.attach(logoutButton);
        com.laxman.foodgramdelivery.utils.BounceTouchListener.attach(editProfileButton);

        deliveryPersonId = TokenManager.getDeliveryPersonId(requireContext());
        userId = TokenManager.getUserId(requireContext());

        if (deliveryPersonId == null) {
            // Try to recover profile using userId
            if (userId != null) {
                attemptProfileRecovery(userId);
            } else {
                Toast.makeText(requireContext(), "User details not found. Please login again.", Toast.LENGTH_SHORT)
                        .show();
                logout();
            }
            return view;
        }

        fetchProfile();
        fetchEarnings();

        return view;
    }

    private void fetchProfile() {
        api.getProfile(deliveryPersonId, userId).enqueue(new Callback<DeliveryPersonProfileDto>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<DeliveryPersonProfileDto> call,
                    @NonNull Response<DeliveryPersonProfileDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProfile = response.body(); // save profile for editing
                    DeliveryPersonProfileDto profile = response.body();
                    textName.setText("Name: " + profile.getFullName());
                    textEmail.setText("Email: " + profile.getEmail());
                    textPhone.setText("Phone: " + profile.getPhone());
                    textVehicle.setText("Vehicle: " + profile.getVehicleNumber());
                    textArea.setText("Area: " + profile.getOperatingArea());
                    textStatus.setText("Status: " + profile.getStatus());
                }
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (swipeRefreshLayout != null)
                        swipeRefreshLayout.setRefreshing(false);
                }, 2000);
            }

            @Override
            public void onFailure(@NonNull Call<DeliveryPersonProfileDto> call, Throwable t) {
                Log.e("ProfileFragment", "Failed to fetch profile", t);
                swipeRefreshLayout.setRefreshing(false);
            }

        });
    }

    private void fetchEarnings() {
        // Using dynamic delivery person ID form shared preferences
        if (deliveryPersonId == null)
            return;

        orderApi.getOrdersForDeliveryPerson(Math.toIntExact(deliveryPersonId)).enqueue(new Callback<List<Delivery>>() {
            @Override
            public void onResponse(Call<List<Delivery>> call, Response<List<Delivery>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Delivery> deliveries = response.body();
                    int completedCount = 0;
                    for (Delivery delivery : deliveries) {
                        if ("DELIVERED".equalsIgnoreCase(delivery.getOrder().getOrderStatus())) {
                            completedCount++;
                        }
                    }
                    int totalEarnings = completedCount * 20;
                    textEarnings.setText("Earnings: ₹" + totalEarnings);
                }
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    // if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false); //
                    // Already handled in fetchProfile mostly, or harmless
                    // Ideally we should sync these calls, but for now we delay both independently
                }, 2000);
            }

            @Override
            public void onFailure(Call<List<Delivery>> call, Throwable t) {
                Log.e("ProfileFragment", "Failed to fetch earnings", t);
                swipeRefreshLayout.setRefreshing(false);
            }

        });
    }

    private void editProfile(DeliveryPersonProfileDto currentProfile) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_profile, null);

        TextInputEditText editFullName = dialogView.findViewById(R.id.editFullName);
        TextInputEditText editPhone = dialogView.findViewById(R.id.editPhone);
        TextInputEditText editVehicle = dialogView.findViewById(R.id.editVehicle);
        TextInputEditText editArea = dialogView.findViewById(R.id.editArea);

        // Pre-fill
        editFullName.setText(currentProfile.getFullName());
        editPhone.setText(currentProfile.getPhone());
        editVehicle.setText(currentProfile.getVehicleNumber());
        editArea.setText(currentProfile.getOperatingArea());

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(deliveryPersonId == null ? "Create Profile" : "Edit Profile")
                .setView(dialogView)
                .setPositiveButton("Save", (dialogInterface, which) -> {
                    String fullName = Objects.requireNonNull(editFullName.getText()).toString();
                    String phone = Objects.requireNonNull(editPhone.getText()).toString();
                    String vehicle = Objects.requireNonNull(editVehicle.getText()).toString();
                    String area = Objects.requireNonNull(editArea.getText()).toString();

                    if (deliveryPersonId == null) {
                        // Create Profile
                        com.laxman.foodgramdelivery.models.DeliveryPersonDTO dto = new com.laxman.foodgramdelivery.models.DeliveryPersonDTO(
                                Math.toIntExact(userId), vehicle, area);

                        api.createProfile(dto)
                                .enqueue(new Callback<com.laxman.foodgramdelivery.models.DeliveryPersonResponse>() {
                                    @Override
                                    public void onResponse(
                                            @NonNull Call<com.laxman.foodgramdelivery.models.DeliveryPersonResponse> call,
                                            @NonNull Response<com.laxman.foodgramdelivery.models.DeliveryPersonResponse> response) {
                                        if (response.isSuccessful() && response.body() != null) {
                                            // Save new ID
                                            deliveryPersonId = (long) response.body().getId();
                                            TokenManager.saveUserDetails(requireContext(), userId, deliveryPersonId);
                                            Toast.makeText(requireContext(), "Profile created!", Toast.LENGTH_SHORT)
                                                    .show();
                                            fetchProfile();
                                        } else {
                                            Toast.makeText(requireContext(),
                                                    "Failed to create profile: " + response.code(), Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(
                                            @NonNull Call<com.laxman.foodgramdelivery.models.DeliveryPersonResponse> call,
                                            @NonNull Throwable t) {
                                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                });

                    } else {
                        // Update Profile
                        currentProfile.setFullName(fullName);
                        currentProfile.setPhone(phone);
                        currentProfile.setVehicleNumber(vehicle);
                        currentProfile.setOperatingArea(area);

                        api.updateProfile(deliveryPersonId, currentProfile)
                                .enqueue(new Callback<DeliveryPersonProfileDto>() {

                                    @Override
                                    public void onResponse(@NonNull Call<DeliveryPersonProfileDto> call,
                                            @NonNull Response<DeliveryPersonProfileDto> response) {
                                        if (response.isSuccessful()) {
                                            Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT)
                                                    .show();
                                            fetchProfile(); // refresh UI
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<DeliveryPersonProfileDto> call,
                                            @NonNull Throwable t) {
                                        Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show();
                                    }

                                });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();

        // Fix for invisible button text
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(requireContext().getColor(R.color.my_light_primary));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(requireContext().getColor(R.color.text_secondary));
    }

    private void attemptProfileRecovery(Long userId) {
        // Try to fetch profile with a dummy dpId (0L)
        api.getProfile(0L, userId).enqueue(new Callback<DeliveryPersonProfileDto>() {
            @Override
            public void onResponse(@NonNull Call<DeliveryPersonProfileDto> call,
                    @NonNull Response<DeliveryPersonProfileDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DeliveryPersonProfileDto profile = response.body();
                    deliveryPersonId = profile.getDeliveryPersonId();
                    // Save recovered ID
                    TokenManager.saveUserDetails(requireContext(), userId, deliveryPersonId);

                    // Proceed to load data
                    fetchProfile();
                    fetchEarnings();
                } else {
                    // Genuine missing profile or error
                    textName.setText("Profile Incomplete");
                    textStatus.setText("Status: Pending creation");

                    // Initialize empty profile so edit button works
                    currentProfile = new DeliveryPersonProfileDto();
                    currentProfile.setFullName("");
                    currentProfile.setPhone("");
                    currentProfile.setVehicleNumber("");
                    currentProfile.setOperatingArea("");

                    Toast.makeText(requireContext(), "Profile incomplete. Click Edit to create one.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeliveryPersonProfileDto> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error checking profile: " + t.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void logout() {
        TokenManager.clearToken(requireContext());
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}