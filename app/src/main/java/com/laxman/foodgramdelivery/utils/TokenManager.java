package com.laxman.foodgramdelivery.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREFS_NAME = "foodgram_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_DELIVERY_PERSON_ID = "delivery_person_id";

    // Check if user is logged in
    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.contains(KEY_TOKEN);
    }

    // Save token
    public static void saveToken(Context context, String token) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    // Save user details
    public static void saveUserDetails(Context context, Long userId, Long deliveryPersonId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (userId != null) {
            editor.putLong(KEY_USER_ID, userId);
        }
        if (deliveryPersonId != null) {
            editor.putLong(KEY_DELIVERY_PERSON_ID, deliveryPersonId);
        }
        editor.apply();
    }

    // Get token
    public static String getToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_TOKEN, null);
    }

    // Clear token (for logout)
    public static void clearToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_TOKEN)
                .remove(KEY_USER_ID)
                .remove(KEY_DELIVERY_PERSON_ID)
                .apply();
    }

    public static Long getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long id = prefs.getLong(KEY_USER_ID, -1);
        return id != -1 ? id : null;
    }

    public static Long getDeliveryPersonId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long id = prefs.getLong(KEY_DELIVERY_PERSON_ID, -1);
        return id != -1 ? id : null;
    }
}