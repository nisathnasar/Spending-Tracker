package com.aston.spendingtracker;

import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.aston.spendingtracker.authorization.LoginActivity;
import com.aston.spendingtracker.entity.Transaction;
import com.aston.spendingtracker.pdf.FileSelectorFragment;
import com.aston.spendingtracker.pdf.PDFProcessor;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    // Request code for selecting a PDF document.
    private static final int PICK_PDF_FILE = 2;

    private static final int MY_REQUEST_CODE_PERMISSION = 1000;
    private static final int MY_RESULT_CODE_FILECHOOSER = 2000;

    private File root;
    private AssetManager assetManager;

//    PyObject pyobj;

    ViewPager2 pager;
    TabLayout mTabLayout;
    TabItem transactionItem, analyticsItem, uploadItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            signOut();
        }

        pager = findViewById(R.id.viewPager);
        mTabLayout = findViewById(R.id.tab_layout);
        transactionItem = findViewById(R.id.AnalyticsItem);
        uploadItem = findViewById(R.id.UploadItem);

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), getLifecycle() , mTabLayout.getTabCount());
        pager.setAdapter(adapter);

        new TabLayoutMediator(mTabLayout, pager, (tab, position) -> {
            if(position == 0){
                tab.setText("List");
            } else if (position == 1){
                tab.setText("Analytics");
            } else if (position == 2){
                tab.setText("Upload");
            }
            else{
                tab.setText("Tab " + (position+1));
            }

        }).attach();

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

}