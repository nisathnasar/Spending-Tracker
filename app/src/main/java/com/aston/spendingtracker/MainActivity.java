package com.aston.spendingtracker;

//import android.app.FragmentManager;
import androidx.fragment.app.FragmentManager;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

        import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

//import android.app.Fragment;

import androidx.fragment.app.FragmentTransaction;

        import com.aston.spendingtracker.authorization.LoginActivity;
import com.aston.spendingtracker.pdf.FileSelectorFragment;
        import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
        import com.google.firebase.auth.FirebaseAuth;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

import java.io.File;

public class MainActivity extends AppCompatActivity implements
        OnChartValueSelectedListener, FragmentChangeListener {

    // Request code for selecting a PDF document.
    private static final int PICK_PDF_FILE = 2;

    private static final int MY_REQUEST_CODE_PERMISSION = 1000;
    private static final int MY_RESULT_CODE_FILECHOOSER = 2000;

    private File root;
    private AssetManager assetManager;

//    PyObject pyobj;

    BottomNavigationView navBarView;




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


        navBarView = findViewById(R.id.bottom_navigation);
        navBarView.setVisibility(View.VISIBLE);


//        if(!Python.isStarted()){
//            Python.start(new AndroidPlatform(this));
//        }

        Intent intent = getIntent();
        boolean isSnapShotExists = intent.getBooleanExtra("snapShotExists", false);

        if(isSnapShotExists){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
            navBarView.setSelectedItemId(R.id.menu_dashboard);
        } else{

            navBarView.getMenu().getItem(0).setEnabled(false);
            navBarView.getMenu().getItem(1).setEnabled(false);
            navBarView.getMenu().getItem(2).setEnabled(false);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FileSelectorFragment()).commit();
            navBarView.setSelectedItemId(R.id.menu_upload);

        }



        //navBarView.setSelectedItemId(R.id.menu_dashboard);

        navBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.menu_dashboard:

                        replaceFragment(new DashboardFragment());

                        break;
                    case R.id.menu_list:

                        replaceFragment(new TransactionFragment());

                        break;
                    case R.id.menu_analytics:

                        replaceFragment(new AnalyticsFragment());

                        break;
                    case R.id.menu_upload:

                        replaceFragment(new FileSelectorFragment());
                        break;
                }
                return true;
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }



    @Override
    protected void onPause()
    {
        super.onPause();

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            signOut();
        }

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menufile, menu);

        return true;
    }


    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    @Override
    public void onChange(int newFragmentPosition) {

    }
}