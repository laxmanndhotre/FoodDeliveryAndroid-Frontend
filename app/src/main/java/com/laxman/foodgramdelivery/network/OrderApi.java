package com.laxman.foodgramdelivery.network;

import com.laxman.foodgramdelivery.models.Delivery;
import com.laxman.foodgramdelivery.models.Order;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface OrderApi {

    // Fetch all orders assigned to a delivery person
    @GET("delivery-person/{id}/orders")
    Call<List<Delivery>> getOrdersForDeliveryPerson(@Path("id") int deliveryPersonId);

    @PATCH("delivery-person/orders/{orderId}/delivered")
    Call<Void> markOrderDelivered(@Path("orderId") int orderId);

    @PATCH("delivery-person/orders/{orderId}/cancel")
    Call<Void> cancelOrder(@Path("orderId") int orderId);

    @GET("delivery-person/available-orders")
    Call<List<Order>> getAvailableOrders();

    @POST("delivery-person/{dpId}/accept-order/{orderId}")
    Call<Delivery> acceptOrder(@Path("dpId") long dpId, @Path("orderId") int orderId);

}