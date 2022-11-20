package com.aston.spendingtracker;

import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

    private FileSelectorFragment fragment;
//    private Button buttonShowInfo;

    private File root;
    private AssetManager assetManager;
    private TextView tv;
    private LinearLayout linearLayout;

    private final LinkedList<String> mWordList = new LinkedList<>();

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;


    private Boolean transactionDataVisible = false;

    private final String KEY_RECYCLER_STATE = "recycler_state";
    private static Bundle mBundleRecyclerViewState;

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

//        FragmentManager fragmentManager = this.getSupportFragmentManager();
//        this.fragment = (FileSelectorFragment) fragmentManager.findFragmentById(R.id.fragment_fileChooser);

//        this.buttonShowInfo = this.findViewById(R.id.button_showInfo);
//
//        this.buttonShowInfo.setOnClickListener(v -> {
//            try {
//                stripText();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }
//        });


        // Get a handle to the RecyclerView.
        //mRecyclerView = findViewById(R.id.recyclerview);


        if(!Python.isStarted()){
            Python.start(new AndroidPlatform(this));
        }

//        Python py = Python.getInstance();
//        pyobj = py.getModule("listoftransactions");


//        LinkedList<Transaction> transactionList = new LinkedList<>();
//
//        // Create an adapter and supply the data to be displayed.
//        mAdapter = new RecyclerViewAdapter(this, transactionList);
//        // Connect the adapter with the RecyclerView.
//        mRecyclerView.setAdapter(mAdapter);
//        // Give the RecyclerView a default layout manager.
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
//        DatabaseReference mTransactionRef = mRootRef.child("Transaction");
//
//
//        mTransactionRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
//                    for(DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()){
//                        Transaction transaction = dataSnapshot2.getValue(Transaction.class);
//                        System.out.println(transaction);
//                        transactionList.add(transaction);
//                    }
//
//                }
//                mAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                System.out.println("db data retrieval fail: " + error);
//            }
//        });

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
        //tv = (TextView) findViewById(R.id.stripped_tv);
    }

    private void stripText() throws IOException, URISyntaxException {
        String path = this.fragment.getPath();
        //Toast.makeText(this, "Path: " + path, Toast.LENGTH_LONG).show();

        Uri pathURI = this.fragment.getPathURI();

        PDFProcessor pdfProcessor = new PDFProcessor(getApplicationContext(), pathURI);

        //pdftocsv.activateSequence(r);

//        // Create an adapter and supply the data to be displayed.
//        mAdapter = new RecyclerViewAdapter(this, pdfProcessor.getTransactionListItems());
//        // Connect the adapter with the RecyclerView.
//        mRecyclerView.setAdapter(mAdapter);
//        // Give the RecyclerView a default layout manager.
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        transactionDataVisible = true;
    }

    private void addTextView(String text){
        TextView valueTV = new TextView(this);
        valueTV.setText(text);
        valueTV.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(valueTV);
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

}