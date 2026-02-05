package com.laxman.foodgramdelivery.adapters;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.laxman.foodgramdelivery.fragments.OrdersFragment;
import com.laxman.foodgramdelivery.fragments.AnalyticsFragment;
import com.laxman.foodgramdelivery.fragments.ProfileFragment;

public class ScreenSlidePagerAdapter extends FragmentStateAdapter {

    public ScreenSlidePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new OrdersFragment();
            case 1:
                return new AnalyticsFragment();
            case 2:
                return new ProfileFragment();
            default:
                return new OrdersFragment(); // fallback
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}