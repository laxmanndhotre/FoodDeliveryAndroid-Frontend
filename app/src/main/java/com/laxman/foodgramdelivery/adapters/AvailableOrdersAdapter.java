package com.laxman.foodgramdelivery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.laxman.foodgramdelivery.R;
import com.laxman.foodgramdelivery.models.Order;

import java.util.List;

public class AvailableOrdersAdapter extends RecyclerView.Adapter<AvailableOrdersAdapter.ViewHolder> {

    private List<Order> orders;
    private OnOrderAcceptListener listener;

    public AvailableOrdersAdapter(List<Order> orders, OnOrderAcceptListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    public void updateList(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_available_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.textRestaurantName.setText(order.getRestaurantName());
        holder.textCustomerName.setText("For: " + order.getCustomerName());
        holder.textOrderAmount.setText("₹ " + order.getTotalAmount());

        holder.buttonAccept.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAcceptOrder(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textRestaurantName, textCustomerName, textOrderAmount;
        Button buttonAccept;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textRestaurantName = itemView.findViewById(R.id.textRestaurantName);
            textCustomerName = itemView.findViewById(R.id.textCustomerName);
            textOrderAmount = itemView.findViewById(R.id.textOrderAmount);
            buttonAccept = itemView.findViewById(R.id.buttonAccept);
            com.laxman.foodgramdelivery.utils.BounceTouchListener.attach(buttonAccept);
        }
    }
}
