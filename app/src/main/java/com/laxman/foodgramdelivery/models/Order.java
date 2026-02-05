package com.laxman.foodgramdelivery.models;

import com.google.gson.annotations.SerializedName;

public class Order {
    @SerializedName("orderId")
    private int orderId;

    @SerializedName("orderStatus")
    private String orderStatus;

    @SerializedName("restaurantName")
    private String restaurantName;

    @SerializedName("customerName")
    private String customerName;

    @SerializedName("totalAmount")
    private double totalAmount;

    @SerializedName("orderDate")
    private String orderDate;

    // getters
    public int getOrderId() { return orderId; }
    public String getOrderStatus() { return orderStatus; }
    public String getRestaurantName() { return restaurantName; }
    public String getCustomerName() { return customerName; }
    public double getTotalAmount() { return totalAmount; }
    public String getOrderDate() { return orderDate; }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }
}