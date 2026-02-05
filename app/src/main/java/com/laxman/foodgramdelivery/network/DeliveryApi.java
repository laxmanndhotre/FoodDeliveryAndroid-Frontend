package com.laxman.foodgramdelivery.network;

import com.laxman.foodgramdelivery.models.AuthResponse;
import com.laxman.foodgramdelivery.models.DeliveryPersonDTO;
import com.laxman.foodgramdelivery.models.DeliveryPersonProfileDto;
import com.laxman.foodgramdelivery.models.DeliveryPersonResponse;
import com.laxman.foodgramdelivery.models.LoginRequest;
import com.laxman.foodgramdelivery.models.Order;
import com.laxman.foodgramdelivery.models.RegisterRequest;
import com.laxman.foodgramdelivery.models.RegisterResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface DeliveryApi {

//    @POST("delivery-person/auth/login")
//    Call<AuthResponse> login(
//            @Field("email") String email,
//            @Field("password") String password
//    );
    @POST("delivery-person/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("delivery-person/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("delivery-person/profile")
    Call<DeliveryPersonResponse> createProfile(@Body DeliveryPersonDTO dto);

    @GET("delivery-person/{dpId}/user/{userId}/profile")
    Call<DeliveryPersonProfileDto> getProfile(@Path("dpId") Long dpId, @Path("userId") Long userId);

    @PUT("delivery-person/{dpId}/profile")
    Call<DeliveryPersonProfileDto> updateProfile(@Path("dpId") Long dpId,
                                                 @Body DeliveryPersonProfileDto dto);




}