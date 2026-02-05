package com.laxman.foodgramdelivery.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.laxman.foodgramdelivery.R;
import com.laxman.foodgramdelivery.models.Delivery;
import com.laxman.foodgramdelivery.models.Order;
import com.laxman.foodgramdelivery.models.dtos.ApiError;
import com.laxman.foodgramdelivery.network.OrderApi;
import com.laxman.foodgramdelivery.network.RetrofitClient;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_FOOTER = 1;

    private static List<Delivery> deliveries;

    public OrdersAdapter(List<Delivery> deliveries) {
        this.deliveries = deliveries;
        Log.d("OrdersAdapter", "Adapter initialized with " + deliveries.size() + " deliveries");
    }

    public void updateDeliveries(List<Delivery> newDeliveries) {
        this.deliveries = newDeliveries;
        Log.d("OrdersAdapter", "Deliveries updated, new size = " + newDeliveries.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == deliveries.size()) {
            return VIEW_TYPE_FOOTER;
        }
        return VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_FOOTER) {
            View view = new View(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) (100 * parent.getContext().getResources().getDisplayMetrics().density)));
            return new FooterViewHolder(view);
        }

        Log.d("OrdersAdapter", "Creating new ViewHolder");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FooterViewHolder) {
            return;
        }

        OrderViewHolder orderViewHolder = (OrderViewHolder) holder;

        Delivery delivery = deliveries.get(position);
        Order order = delivery.getOrder();

        Log.d("OrdersAdapter", "Binding deliveryId=" + delivery.getDeliveryId() +
                " orderId=" + order.getOrderId() +
                " status=" + order.getOrderStatus() +
                " restaurant=" + order.getRestaurantName() +
                " customer=" + order.getCustomerName());

        orderViewHolder.textOrderId.setText("Order #" + order.getOrderId());
        orderViewHolder.chipStatus.setText(order.getOrderStatus());
        orderViewHolder.textRestaurant.setText(order.getRestaurantName());
        orderViewHolder.textCustomer.setText(order.getCustomerName());
        orderViewHolder.textAmount.setText("₹" + order.getTotalAmount());

        // Hide cancel button if delivered or cancelled
        boolean isActionable = !"DELIVERED".equalsIgnoreCase(order.getOrderStatus())
                && !"CANCELLED".equalsIgnoreCase(order.getOrderStatus());
        orderViewHolder.buttonCancelOrder.setVisibility(isActionable ? View.VISIBLE : View.GONE);

        orderViewHolder.buttonCancelOrder.setOnClickListener(v -> {
            orderViewHolder.showCancelConfirmation(v, delivery);
        });
    }

    @Override
    public int getItemCount() {
        int count = deliveries != null ? deliveries.size() + 1 : 1; // +1 for footer
        Log.d("OrdersAdapter", "ItemCount=" + count);
        return count;
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView textOrderId, textRestaurant, textCustomer, textAmount;
        Button buttonCancelOrder;
        Chip chipStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textOrderId = itemView.findViewById(R.id.textOrderId);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            textRestaurant = itemView.findViewById(R.id.textRestaurant);
            textCustomer = itemView.findViewById(R.id.textCustomer);
            textAmount = itemView.findViewById(R.id.textAmount);
            buttonCancelOrder = itemView.findViewById(R.id.buttonCancelOrder);
            com.laxman.foodgramdelivery.utils.BounceTouchListener.attach(buttonCancelOrder);

            // Long press listener
            itemView.setOnLongClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < deliveries.size()) {
                    Delivery delivery = deliveries.get(position);
                    showOrderOptionsDialog(v, delivery);
                }
                return true; // consume the event
            });
        }

        void showOrderOptionsDialog(View v, Delivery delivery) {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(v.getContext())
                    .setTitle("Update Order #" + delivery.getOrder().getOrderId())
                    .setItems(new CharSequence[] { "Mark as Delivered", "Cancel Order" }, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                updateOrderStatus(delivery, v);
                                break;
                            case 1:
                                updateOrderStatus(delivery, v);
                                break;
                        }
                    })
                    .show();
        }

        private void updateOrderStatus(Delivery delivery, View v) {
            int orderId = delivery.getOrder().getOrderId();

            OrderApi api = RetrofitClient.getInstance(v.getContext()).create(OrderApi.class);
            api.markOrderDelivered(orderId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        delivery.getOrder().setOrderStatus("DELIVERED");
                        chipStatus.setText("DELIVERED");
                        Toast.makeText(v.getContext(), "Order marked as delivered", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorBody = null;
                        try {
                            errorBody = response.errorBody() != null ? response.errorBody().string() : "";
                        } catch (IOException e) {
                            Log.e("OrdersAdapter", "Error reading errorBody", e);
                            Toast.makeText(v.getContext(), "Unknown error occurred", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ApiError apiError = new Gson().fromJson(errorBody, ApiError.class);
                        Toast.makeText(v.getContext(), "" + apiError.getMessage(), Toast.LENGTH_LONG).show();

                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(v.getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        void showCancelConfirmation(View v, Delivery delivery) {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(v.getContext())
                    .setTitle("Cancel Order?")
                    .setMessage("Are you sure you want to cancel this order?")
                    .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                        cancelOrder(delivery, v);
                    })
                    .setNegativeButton("No", null)
                    .show();
        }

        private void cancelOrder(Delivery delivery, View v) {
            int orderId = delivery.getOrder().getOrderId();
            OrderApi api = RetrofitClient.getInstance(v.getContext()).create(OrderApi.class);

            api.cancelOrder(orderId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        delivery.getOrder().setOrderStatus("CANCELLED");
                        chipStatus.setText("CANCELLED");
                        buttonCancelOrder.setVisibility(View.GONE);
                        Toast.makeText(v.getContext(), "Order Cancelled", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(v.getContext(), "Failed to cancel: " + response.message(), Toast.LENGTH_SHORT)
                                .show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(v.getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
