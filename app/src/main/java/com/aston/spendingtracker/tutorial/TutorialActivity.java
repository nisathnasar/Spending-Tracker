package com.aston.spendingtracker.tutorial;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.View;

import com.aston.spendingtracker.FragmentChangeListener;
import com.aston.spendingtracker.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class TutorialActivity extends AppCompatActivity implements FragmentChangeListener {

    private ViewPager2 pager;
    private TabLayout mTabLayout;
    private TutorialPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        pager = findViewById(R.id.view_pager);
        mTabLayout = findViewById(R.id.tabLayout);
        //transactionItem = findViewById(R.id.AnalyticsItem);
        //uploadItem = findViewById(R.id.UploadItem);
        adapter = new TutorialPagerAdapter(getSupportFragmentManager(), getLifecycle() , mTabLayout.getTabCount());
        pager.setAdapter(adapter);


        new TabLayoutMediator(mTabLayout, pager, (tab, position) -> {
            if(position == 0){
                tab.setText("");
                //tab.setIcon(R.drawable.dashboard_icon);
            } else if (position == 1){
                tab.setText("");
//                tab.setIcon(R.drawable.reorder_icon);
            } else if (position == 2){
                tab.setText("");
//                tab.setIcon(R.drawable.bar_chart_icon);
            } else if (position == 3){
                tab.setText("");
//                tab.setIcon(R.drawable.description_icon);
            } else if (position == 4){
                tab.setText("");
            }
            else{
//                tab.setText("Tab " + (position+1));
            }

        }).attach();

    }

    @Override
    public void onChange(int newFragmentPosition) {
        pager.setCurrentItem(newFragmentPosition);
    }
}