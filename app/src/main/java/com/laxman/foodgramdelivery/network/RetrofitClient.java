package com.laxman.foodgramdelivery.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        String url = original.url().toString();

                        // Use TokenManager consistently
                        String token = com.laxman.foodgramdelivery.utils.TokenManager.getToken(context);

                        Log.d("RetrofitClient", "Outgoing request: " + original.method() + " " + url);

                        Request.Builder builder = original.newBuilder();
                        if (token != null && !url.contains("/auth/register") && !url.contains("/auth/login")) {
                            builder.header("Authorization", "Bearer " + token);
                            Log.d("RetrofitClient", "Authorization header attached");
                        } else {
                            Log.d("RetrofitClient", "No JWT token found");
                        }

                        return chain.proceed(builder.build());
                    })
                    .addInterceptor(logging)
                    .build();
            String baseUrl="https://foodgram-spring-backend-for-delivery-person-production.up.railway.app/api/";
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;

    }
}