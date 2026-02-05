package com.laxman.foodgramdelivery.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
// import androidx.swiperefreshlayout.widget.SwipeRefreshLayout; // Removed
import com.laxman.foodgramdelivery.views.SquigglySwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.laxman.foodgramdelivery.R;
import com.laxman.foodgramdelivery.adapters.AvailableOrdersAdapter;
import com.laxman.foodgramdelivery.adapters.OrdersAdapter;
import com.laxman.foodgramdelivery.models.Delivery;
import com.laxman.foodgramdelivery.models.Order;
import com.laxman.foodgramdelivery.network.OrderApi;
import com.laxman.foodgramdelivery.network.RetrofitClient;
import com.laxman.foodgramdelivery.utils.TokenManager;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersFragment extends Fragment {

    private static final String TAG = "OrdersFragment";

    private RecyclerView recyclerAvailableOrders;
    private RecyclerView recyclerMyOrders;
    private AvailableOrdersAdapter availableOrdersAdapter;
    private OrdersAdapter myOrdersAdapter;
    private SquigglySwipeRefreshLayout swipeRefreshLayout;
    private ShimmerFrameLayout shimmerLayout;
    private MaterialButtonToggleGroup toggleOrderType;
    private TextView textEmpty;
    private com.laxman.foodgramdelivery.views.SquigglyProgressView squigglyProgressView;

    private boolean isShowingAvailableOrders = true; // Default tab

    public OrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        // Attach Bounce Listener to Toggle Buttons (Need to find them first if not
        // exposed by group easily, or just by ID from View)
        com.laxman.foodgramdelivery.utils.BounceTouchListener.attach(view.findViewById(R.id.btnAvailableOrders));
        com.laxman.foodgramdelivery.utils.BounceTouchListener.attach(view.findViewById(R.id.btnMyOrders));

        // Initialize Views
        toggleOrderType = view.findViewById(R.id.toggleOrderType);
        recyclerAvailableOrders = view.findViewById(R.id.recyclerAvailableOrders);
        recyclerMyOrders = view.findViewById(R.id.recyclerOrders);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        squigglyProgressView = view.findViewById(R.id.squigglyLoader);
        textEmpty = view.findViewById(R.id.textEmpty);

        // Setup RecyclerViews
        recyclerAvailableOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        availableOrdersAdapter = new AvailableOrdersAdapter(Collections.emptyList(), this::acceptOrder);
        recyclerAvailableOrders.setAdapter(availableOrdersAdapter);

        recyclerMyOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        myOrdersAdapter = new OrdersAdapter(Collections.emptyList());
        recyclerMyOrders.setAdapter(myOrdersAdapter);

        // Setup Toggle Listener
        toggleOrderType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnAvailableOrders) {
                    isShowingAvailableOrders = true;
                    recyclerAvailableOrders.setVisibility(View.VISIBLE);
                    recyclerMyOrders.setVisibility(View.GONE);
                    fetchOrders();
                } else if (checkedId == R.id.btnMyOrders) {
                    isShowingAvailableOrders = false;
                    recyclerAvailableOrders.setVisibility(View.GONE);
                    recyclerMyOrders.setVisibility(View.VISIBLE);
                    fetchOrders();
                }
            }
        });

        // Setup Swipe Refresh
        swipeRefreshLayout.setOnRefreshListener(this::fetchOrders);

        // Initial Fetch
        fetchOrders();

        return view;
    }

    private void fetchOrders() {
        if (getContext() == null)
            return;

        swipeRefreshLayout.setRefreshing(false); // We use squiggly for initial load, keep swipe for refresh
        squigglyProgressView.setVisibility(View.VISIBLE);
        textEmpty.setVisibility(View.GONE);

        OrderApi api = RetrofitClient.getInstance(getContext()).create(OrderApi.class);

        if (isShowingAvailableOrders) {
            // Fetch Available Orders
            api.getAvailableOrders().enqueue(new Callback<List<Order>>() {
                @Override
                public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                    }, 2000);

                    squigglyProgressView.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        List<Order> orders = response.body();
                        availableOrdersAdapter.updateList(orders);
                        if (orders.isEmpty()) {
                            textEmpty.setVisibility(View.VISIBLE);
                            textEmpty.setText("No available orders at the moment.");
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to load available orders", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Order>> call, Throwable t) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                    }, 2000);

                    squigglyProgressView.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            // Fetch My Assigned Orders
            Long dpId = TokenManager.getDeliveryPersonId(getContext());
            if (dpId == null) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Error: Delivery Person ID not found", Toast.LENGTH_SHORT).show();
                return;
            }

            api.getOrdersForDeliveryPerson(dpId.intValue()).enqueue(new Callback<List<Delivery>>() {
                @Override
                public void onResponse(Call<List<Delivery>> call, Response<List<Delivery>> response) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                    }, 2000);

                    squigglyProgressView.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        List<Delivery> deliveries = response.body();
                        myOrdersAdapter.updateDeliveries(deliveries);
                        if (deliveries.isEmpty()) {
                            textEmpty.setVisibility(View.VISIBLE);
                            textEmpty.setText("You haven't accepted any orders yet.");
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to load your orders", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Delivery>> call, Throwable t) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                    }, 2000);

                    squigglyProgressView.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void acceptOrder(Order order) {
        if (getContext() == null)
            return;

        Long dpId = TokenManager.getDeliveryPersonId(getContext());
        if (dpId == null) {
            Toast.makeText(getContext(), "Error: Delivery Person ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        OrderApi api = RetrofitClient.getInstance(getContext()).create(OrderApi.class);
        api.acceptOrder(dpId, order.getOrderId()).enqueue(new Callback<Delivery>() {
            @Override
            public void onResponse(Call<Delivery> call, Response<Delivery> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Order Accepted!", Toast.LENGTH_SHORT).show();
                    // Switch to My Orders tab and refresh
                    toggleOrderType.check(R.id.btnMyOrders);
                } else {
                    Toast.makeText(getContext(), "Failed to accept order: " + response.code(), Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<Delivery> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
