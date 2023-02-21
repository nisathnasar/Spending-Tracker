package com.aston.spendingtracker;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.aston.spendingtracker.authorization.LoginActivity;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

import java.io.File;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener,
        OnChartValueSelectedListener, FragmentChangeListener {

    // Request code for selecting a PDF document.
    private static final int PICK_PDF_FILE = 2;

    private static final int MY_REQUEST_CODE_PERMISSION = 1000;
    private static final int MY_RESULT_CODE_FILECHOOSER = 2000;

    private File root;
    private AssetManager assetManager;

//    PyObject pyobj;

    ViewPager2 pager;
    PagerAdapter adapter;
    TabLayout mTabLayout;
    TabItem transactionItem, analyticsItem, uploadItem;

    public static final int DASHBOARD_POSITION = 0;
    public static final int TRANSACTION_LIST_POSITION = 1;
    public static final int ANALYTICS_POSITION = 2;
    public static final int UPLOAD_POSITION = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            signOut();
        }

        pager = findViewById(R.id.viewPager);
        mTabLayout = findViewById(R.id.tab_layout);
        //transactionItem = findViewById(R.id.AnalyticsItem);
        //uploadItem = findViewById(R.id.UploadItem);

        adapter = new PagerAdapter(getSupportFragmentManager(), getLifecycle() , mTabLayout.getTabCount());
        pager.setAdapter(adapter);



        new TabLayoutMediator(mTabLayout, pager, (tab, position) -> {
            if(position == 0){
                tab.setText("Dashboard");
                tab.setIcon(R.drawable.dashboard_icon);
            } else if (position == 1){
                tab.setText("List");
                tab.setIcon(R.drawable.reorder_icon);
            } else if (position == 2){
                tab.setText("Analytics");
                tab.setIcon(R.drawable.bar_chart_icon);
            } else if (position == 3){
                tab.setText("Upload");
                tab.setIcon(R.drawable.description_icon);
            }
            else{
                tab.setText("Tab " + (position+1));
            }

        }).attach();

        //pager.setUserInputEnabled(false);

        if(!Python.isStarted()){
            Python.start(new AndroidPlatform(this));
        }

//        Python py = Python.getInstance();
//        pyobj = py.getModule("listoftransactions");


    }

    @Override
    protected void onPause()
    {
        super.onPause();

//        if(transactionDataVisible){
//            // save RecyclerView state
//            mBundleRecyclerViewState = new Bundle();
//            Parcelable listState = mRecyclerView.getLayoutManager().onSaveInstanceState();
//            mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);
//        }

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
//        if(transactionDataVisible){
//            // save RecyclerView state
//            mBundleRecyclerViewState = new Bundle();
//            Parcelable listState = mRecyclerView.getLayoutManager().onSaveInstanceState();
//            mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);
//
//        }

    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            signOut();
        }

        // restore RecyclerView state
//        if (mBundleRecyclerViewState != null && mRecyclerView != null) {
//            Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
//            Objects.requireNonNull(mRecyclerView.getLayoutManager()).onRestoreInstanceState(listState);
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setup();
    }

    /**
     * Initializes variables used for convenience
     */
    private void setup() {
        // Enable Android asset loading
        PDFBoxResourceLoader.init(getApplicationContext());
        // Find the root of the external storage.

        root = getApplicationContext().getCacheDir();
        assetManager = getAssets();
    }

    @Override
    public void onBackPressed() {
        // Disable back button
    }

    public void signOut(){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_logout:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                signOut();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void switchFragment(int i){
        pager.setCurrentItem(i);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menufile, menu);

//        MenuItem searchItem = menu.findItem(R.id.action_search);
//        SearchView searchView = (SearchView) searchItem.getActionView();
//
//        searchView.setOnSearchClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });

        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    @Override
    public void onChange(int newFragmentPosition) {

        pager.setCurrentItem(newFragmentPosition);
    }
}