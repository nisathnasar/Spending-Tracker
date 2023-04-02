package com.aston.spendingtracker.tutorial;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.aston.spendingtracker.tutorial.Page1Fragment;
import com.aston.spendingtracker.tutorial.Page2Fragment;
import com.aston.spendingtracker.tutorial.Page3Fragment;
import com.aston.spendingtracker.tutorial.Page4Fragment;
import com.aston.spendingtracker.tutorial.Page5Fragment;

public class TutorialPagerAdapter extends FragmentStateAdapter {

    private int tabs;



    public TutorialPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, int tabs) {
        super(fragmentManager, lifecycle);
        this.tabs = tabs;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                Fragment page1Fragment = new Page1Fragment();
                return page1Fragment;
            case 1:
                Fragment page2Fragment = new Page2Fragment();
                return page2Fragment;
            case 2:
                Fragment page3Fragment = new Page3Fragment();
                return page3Fragment;
            case 3:
                Fragment page4Fragment = new Page4Fragment();
                return page4Fragment;
            case 4:
                Fragment page5Fragment = new Page5Fragment();
                return page5Fragment;
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return tabs;
    }


}
