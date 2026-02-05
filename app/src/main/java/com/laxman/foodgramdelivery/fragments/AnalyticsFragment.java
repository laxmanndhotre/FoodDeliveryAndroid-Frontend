package com.laxman.foodgramdelivery.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

// import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.laxman.foodgramdelivery.views.SquigglySwipeRefreshLayout;

import com.laxman.foodgramdelivery.R;
import com.laxman.foodgramdelivery.models.Delivery;
import com.laxman.foodgramdelivery.network.OrderApi;
import com.laxman.foodgramdelivery.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnalyticsFragment extends Fragment {

    private TextView textTotalEarnings;
    private TextView textCompletedDeliveries;
    private TextView textPendingDeliveries;
    private TextView textBestDayAmount;
    private TextView textBestDayDate;
    private SquigglySwipeRefreshLayout swipeRefreshLayout;

    public AnalyticsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        textTotalEarnings = view.findViewById(R.id.textTotalEarnings);
        textCompletedDeliveries = view.findViewById(R.id.textCompletedDeliveries);
        textPendingDeliveries = view.findViewById(R.id.textPendingDeliveries);
        textBestDayAmount = view.findViewById(R.id.textBestDayAmount);
        textBestDayDate = view.findViewById(R.id.textBestDayDate);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshAnalytics);

        swipeRefreshLayout.setOnRefreshListener(this::fetchAnalyticsData);

        fetchAnalyticsData();

        return view;
    }

    private void fetchAnalyticsData() {
        if (getContext() == null)
            return;

        android.util.Log.d("AnalyticsFragment", "Starting API call for orders...");
        OrderApi api = RetrofitClient.getInstance(getContext()).create(OrderApi.class);
        Long dpId = com.laxman.foodgramdelivery.utils.TokenManager.getDeliveryPersonId(getContext());
        if (dpId == null)
            return;
        // Fetch orders for the logged-in delivery person
        api.getOrdersForDeliveryPerson(dpId.intValue()).enqueue(new Callback<List<Delivery>>() {
            @Override
            public void onResponse(Call<List<Delivery>> call, Response<List<Delivery>> response) {
                android.util.Log.d("AnalyticsFragment", "API Response: Code=" + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    List<Delivery> list = response.body();
                    android.util.Log.d("AnalyticsFragment", "Received " + list.size() + " orders");
                    calculateAndDisplayStats(list);
                } else {
                    android.util.Log.e("AnalyticsFragment", "Response unsuccessful: " + response.message());
                    if (getContext() != null) {
                        android.widget.Toast.makeText(getContext(), "Failed to load analytics: " + response.code(),
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (swipeRefreshLayout != null)
                        swipeRefreshLayout.setRefreshing(false);
                }, 2000);
            }

            @Override
            public void onFailure(Call<List<Delivery>> call, Throwable t) {
                android.util.Log.e("AnalyticsFragment", "API Call Failed", t);
                if (getContext() != null) {
                    android.widget.Toast
                            .makeText(getContext(), "Error: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT)
                            .show();
                }
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (swipeRefreshLayout != null)
                        swipeRefreshLayout.setRefreshing(false);
                }, 2000);
            }

        });
    }

    private void calculateAndDisplayStats(List<Delivery> deliveries) {
        int completedCount = 0;
        int pendingCount = 0;
        java.util.Map<String, Integer> dailyEarnings = new java.util.HashMap<>();

        for (Delivery delivery : deliveries) {
            boolean isDelivered = "DELIVERED".equalsIgnoreCase(delivery.getOrder().getOrderStatus());

            if (isDelivered) {
                completedCount++;

                // Track daily earnings
                String date = delivery.getOrder().getOrderDate();
                // Simpledate format might be needed if date string is complex, assuming simple
                // YYYY-MM-DD or similar
                if (date != null) {
                    // Normalize date if needed, for now just use the string
                    int currentDaily = dailyEarnings.getOrDefault(date, 0);
                    dailyEarnings.put(date, currentDaily + 20);
                }
            } else {
                pendingCount++;
            }
        }

        int totalEarnings = completedCount * 20;

        // Find best day
        String bestDay = "--";
        int maxDailyEarning = 0;

        for (java.util.Map.Entry<String, Integer> entry : dailyEarnings.entrySet()) {
            if (entry.getValue() > maxDailyEarning) {
                maxDailyEarning = entry.getValue();
                bestDay = entry.getKey();
            }
        }

        // Update UI
        textTotalEarnings.setText("₹ " + totalEarnings);
        textCompletedDeliveries.setText(String.valueOf(completedCount));
        textPendingDeliveries.setText(String.valueOf(pendingCount));
        textBestDayAmount.setText("₹ " + maxDailyEarning);

        // Format best day slightly if possible, or just show it
        textBestDayDate.setText(bestDay);
    }
}