package com.laxman.foodgramdelivery;

import android.app.Application;
import com.google.android.material.color.DynamicColors;

public class FoodGramApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
