package com.laxman.foodgramdelivery.models;

public class DeliveryPersonDTO {
    private int userId;
    private String vehicleNumber;
    private String operatingArea;

    public DeliveryPersonDTO(int userId, String vehicleNumber, String operatingArea) {
        this.userId = userId;
        this.vehicleNumber = vehicleNumber;
        this.operatingArea = operatingArea;
    }
}