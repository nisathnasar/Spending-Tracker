package com.aston.spendingtracker;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.aston.spendingtracker.pdf.FileSelectorFragment;

public class PagerAdapter extends FragmentStateAdapter {

    private int tabs;

    public PagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, int tabs) {
        super(fragmentManager, lifecycle);
        this.tabs = tabs;
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                Fragment transactionFragment = new TransactionFragment();
                return transactionFragment;
            case 1:
                Fragment analyticsFragment = new AnalyticsFragment();
                return analyticsFragment;
            case 2:
                Fragment fileSelector = new FileSelectorFragment();
                return fileSelector;
            default:
                return null;
        }

    }

    @Override
    public int getItemCount() {
        return tabs;
    }
}