package com.laxman.foodgramdelivery.models;


import com.google.gson.annotations.SerializedName;

public class Delivery {
    @SerializedName("deliveryId")
    private int deliveryId;

    @SerializedName("deliveryPersonId")
    private int deliveryPersonId;

    @SerializedName("deliveryStatus")
    private String deliveryStatus;

    @SerializedName("deliveryTime")
    private String deliveryTime;

    @SerializedName("order")
    private Order order;

    // getters and setters
    public int getDeliveryId() { return deliveryId; }
    public int getDeliveryPersonId() { return deliveryPersonId; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public String getDeliveryTime() { return deliveryTime; }
    public Order getOrder() { return order; }
}