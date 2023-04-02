package com.aston.spendingtracker;

import static android.view.View.GONE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aston.spendingtracker.axisformatters.MoneyValueFormatter;
import com.aston.spendingtracker.axisformatters.MyXAxisValueFormatter;
import com.aston.spendingtracker.entity.Transaction;
import com.aston.spendingtracker.fragments.AddToCategoryFragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ViewTransaction extends AppCompatActivity implements OnChartValueSelectedListener, AddToCategoryFragment.NoticeDialogListener{

    LinkedList<Transaction> transactionList = new LinkedList<>();

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private BarChart chart;

    private int nightModeFlags;
    
    private TextView title_tv;
    private TextView transactionTextView;
    private TextView balanceTextView;
    private TextView balanceBeforeTextView;
    private TextView transactionTypeTextView;
    private TextView dateOfTransactionTextView;

    private ChipGroup catChipGroup;
    private Chip partyCategoryTextView;
    
    private Button editCatBtn;
    private Button clearCatBtn;
    private Button deleteCatBtn;

    private String detail;
    private String partyCategory;
    View rootLayout;

    DatabaseReference mRootRef, mTransactionRef;

    ArrayList<String> categoriesList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_transaction);

        rootLayout = findViewById(R.id.constraint_layout);
        
        title_tv = findViewById(R.id.title_tv);
        transactionTextView = findViewById(R.id.transaction_tv);
        balanceTextView = findViewById(R.id.balance_tv);
        balanceBeforeTextView = findViewById(R.id.balance_before_tv);
        transactionTypeTextView = findViewById(R.id.type_tv);
        dateOfTransactionTextView = findViewById(R.id.date_of_transaction_tv);
        partyCategoryTextView = findViewById(R.id.category_tv);

        catChipGroup = findViewById(R.id.cat_chip_group);

        editCatBtn = findViewById(R.id.edit_cat_btn);
        editCatBtn.setVisibility(GONE);

        clearCatBtn = findViewById(R.id.clear_cat_btn);
        clearCatBtn.setVisibility(GONE);

        deleteCatBtn = findViewById(R.id.delete_cat_btn);
        deleteCatBtn.setVisibility(GONE);

        nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        setFields();

        setHistoryTransactions();

        displayHistoryBarChart();

        setData();

        setButtonListeners();

        createNewCategoriesInDatabaseIfNone();

        retrieveListOfCategoriesFromDB();

    }

    private void setFields() {
        Intent intent = getIntent();
        detail = intent.getStringExtra("detail");
        title_tv.setText(detail);

        String dateOfTransaction = intent.getStringExtra("date");
        dateOfTransactionTextView.setText(dateOfTransaction);

        String paidIn = intent.getStringExtra("paidIn");
        String paidOut = intent.getStringExtra("paidOut");

        String balance = intent.getStringExtra("balance");
        String balanceString = "Balance after: £" + balance;
        balanceTextView.setText(balanceString);

        double balanceBefore;

        String resultTransactionString = "";
        if(!paidIn.isEmpty()){
            resultTransactionString = "+£" + paidIn;
            transactionTextView.setText(resultTransactionString);

            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    transactionTextView.setTextColor(Color.parseColor("#82b889"));
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    transactionTextView.setTextColor(Color.parseColor("#53b075"));
                    break;
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    break;
            }

            balanceBefore =  Double.parseDouble(balance) - Double.parseDouble(paidIn);
        } else{
            resultTransactionString = "-£" + paidOut;
            transactionTextView.setText(resultTransactionString);

            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    transactionTextView.setTextColor(Color.parseColor("#d98b8b"));
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    //transactionTextView.setBackgroundColor(Color.parseColor("#5c859c"));
                    break;
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    break;
            }
            balanceBefore = Double.parseDouble(balance) + Double.parseDouble(paidOut);
        }

        DecimalFormat df = new DecimalFormat("0.00");
        String balanceBeforeFormattedString = "Balance before: £" + df.format(balanceBefore);
        balanceBeforeTextView.setText(balanceBeforeFormattedString);


        String transactionType = intent.getStringExtra("type");
        String transactionTypeString = "Type of transaction: " + getFullBankAbbreviation(transactionType);
        transactionTypeTextView.setText(transactionTypeString);

        partyCategory = intent.getStringExtra("category");
        if(partyCategory.isEmpty()){
            partyCategoryTextView.setVisibility(GONE);
        }
        else{
            partyCategoryTextView.setText(partyCategory);
        }
    }

    private void setHistoryTransactions() {
        mRecyclerView = findViewById(R.id.recyclerviewfiltered);

        // Create an adapter and supply the data to be displayed.
        mAdapter = new RecyclerViewAdapter(this, transactionList);

        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        mRecyclerView.setLayoutManager(linearLayoutManager);

        mRootRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mTransactionRef = mRootRef.child("Transaction");

        mTransactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    for(DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()){
                        Transaction transaction = dataSnapshot2.getValue(Transaction.class);


                        if(transaction.getPaymentDetails().equals(detail)){

                            transaction.parseDBDate();
                            transaction.parseDBMonth();
                            transaction.parseDBYear();
                            transactionList.add(transaction);
                        }

                    }
                }
                Collections.reverse(transactionList);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void retrieveListOfCategoriesFromDB() {
        //now retrieve the list of categories from the database and store in an arraylist

        DatabaseReference mCategoriesRef = mRootRef.child("Categories");
        mCategoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){

                    Category category = dataSnapshot.getValue(Category.class);

                    //TODO: set caps for every first character
                    //do not add 'other'
                    if(!category.getCategory().toLowerCase().equals("other")){
                        categoriesList.add(category.getCategory());
                        System.out.println("executed " + category.getCategory());
                    }

                }

                //add other
                categoriesList.add("other");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createNewCategoriesInDatabaseIfNone() {

        //if a list of categories does not exist in database, create them:
        mRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild("Categories")){
                    ArrayList<String> categories = new ArrayList<>();
                    categories.add("entertainment");
                    categories.add("utility");
                    categories.add("other");


                    for(Integer i =0; i<categories.size(); i++){

                        mRootRef.child("Categories").child(i.toString()).setValue(new Category(categories.get(i)));
                    }
                }
                else{
                    System.out.println("Categories exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setButtonListeners(){

        catChipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {

                if(checkedIds.size()>0){
                    editCatBtn.setVisibility(View.VISIBLE);

                    if(!partyCategory.equals("other")){
                        clearCatBtn.setVisibility(View.VISIBLE);
                        deleteCatBtn.setVisibility(View.VISIBLE);
                    }

                }else{
                    editCatBtn.setVisibility(View.GONE);

                    //if(!partyCategory.equals("other")){
                        clearCatBtn.setVisibility(View.GONE);
                        deleteCatBtn.setVisibility(View.GONE);
                    //}

                }


            }
        });

        editCatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayChangeCategoryDialog("Change category for '" + detail + "'");


            }
        });

        clearCatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ViewTransaction.this);
                //AlertDialog.Builder builder = new AlertDialog.Builder(ViewTransaction.this);
                builder.setTitle("Remove '" + partyCategory + "' from '" + detail + "'");
                builder.setMessage("Are you sure you want to remove this venue from this category? \n\nImpact:\nThis will automatically add this venue to 'other' category")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                mTransactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                            for(DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()){
                                                Transaction transaction = dataSnapshot2.getValue(Transaction.class);

                                                if(transaction.getPaymentDetails().equals(detail)){

                                                    transaction.setCategory("other");

                                                    mTransactionRef.child(transaction.getDateOfTransaction()).child(dataSnapshot2.getKey()).setValue(transaction);

                                                    partyCategoryTextView.setText("other");

                                                    partyCategory = "other";
                                                    clearCatBtn.setVisibility(View.GONE);
                                                    deleteCatBtn.setVisibility(View.GONE);

                                                    String msg = "Successfully removed category '" + partyCategory + "' from this venue. ";
                                                    Snackbar.make(rootLayout, msg , Snackbar.LENGTH_SHORT)
                                                            .show();

                                                    retrieveListOfCategoriesFromDB();

                                                }

                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                // Create the AlertDialog object and return it
                builder.show();
            }
        });

        deleteCatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ViewTransaction.this);
                //AlertDialog.Builder builder = new AlertDialog.Builder(ViewTransaction.this);
                builder.setTitle("Warning: Completely delete '" + partyCategory +"'" );
                builder.setMessage("Are you sure want to completely remove this category from the category listing? \n\nImpact:\nThis will automatically add venues from this category to 'other' category")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                //set category to other
                                mTransactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                            for(DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()){
                                                Transaction transaction = dataSnapshot2.getValue(Transaction.class);

                                                if(transaction.getPaymentDetails().equals(detail)){

                                                    transaction.setCategory("other");


                                                    mTransactionRef.child(transaction.getDateOfTransaction()).child(dataSnapshot2.getKey()).setValue(transaction);

                                                }
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                //remove category now

                                mRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild("Categories")) {
                                            System.out.println("Categories exist");

                                            Iterable<DataSnapshot> categories = snapshot.child("Categories").getChildren();

                                            Iterator<DataSnapshot> iterator = categories.iterator();
                                            for (DataSnapshot category : categories) {
                                                System.out.println(category + " -----------------------------------");

                                                Category value = category.getValue(Category.class);

                                                if(value.getCategory().equals(partyCategory)){
                                                    mRootRef.child("Categories").child(category.getKey()).removeValue();

                                                    partyCategoryTextView.setText("other");

                                                    partyCategory = "other";
                                                    clearCatBtn.setVisibility(View.GONE);
                                                    deleteCatBtn.setVisibility(View.GONE);

                                                    String msg = "Successfully deleted category '" + partyCategory + "'. ";
                                                    Snackbar.make(rootLayout, msg , Snackbar.LENGTH_SHORT)
                                                            .show();
                                                }

                                            }

                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                // Create the AlertDialog object and return it
                builder.show();
            }
        });

    }
    
    private void displayChangeCategoryDialog(String title){

        AddToCategoryFragment frg = new AddToCategoryFragment();
        frg.setTitle(title);
        frg.setDetail(detail);
        frg.setCategoriesList(categoriesList);
        frg.show(getFragmentManager(), "idk");

    }

    private void displayHistoryBarChart(){
        // bar chart code:

        chart = findViewById(R.id.unitBarChart);
        chart.setOnChartValueSelectedListener(this);
        chart.setDrawBarShadow(false);

        chart.setDrawValueAboveBar(true);

        chart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart.setMaxVisibleValueCount(100);

        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false);

        chart.setDrawGridBackground(false);
        // chart.setDrawYLabels(false);


        //IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(chart);


        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setTypeface(tfLight);
        xAxis.setDrawGridLines(false);
        //xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);


        ValueFormatter xAxisFormatter = new MyXAxisValueFormatter();
        //xAxis.setValueFormatter(xAxisFormatter);

        //IAxisValueFormatter custom = new MyAxisValueFormatter();

        YAxis leftAxis = chart.getAxisLeft();
        //leftAxis.setTypeface(tfLight);
        //leftAxis.setLabelCount(8, false);
        //leftAxis.setSpaceTop(15f);
        leftAxis.setValueFormatter(new MoneyValueFormatter());
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(5f);

        //leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setEnabled(false);

        //rightAxis.setTypeface(tfLight);
        //rightAxis.setLabelCount(8, false);

        //rightAxis.setValueFormatter(custom);

        //rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                leftAxis.setTextColor(Color.WHITE);
                xAxis.setTextColor(Color.WHITE);
                l.setTextColor(Color.WHITE);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                leftAxis.setTextColor(Color.BLACK);
                xAxis.setTextColor(Color.BLACK);
                l.setTextColor(Color.BLACK);
                break;
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                break;
        }


//        XYMarkerView mv = new XYMarkerView(this, new MyXAxisValueFormatter());
        /*
        XYMarkerView mv = new XYMarkerView(this, xAxisFormatter);
        mv.setChartView(chart); // For bounds control
        chart.setMarker(mv); // Set the marker to the chart
*/
        // setting data
//        seekBarY.setProgress(50);
//        seekBarX.setProgress(12);
    }

    private void setData() {


        ArrayList<BarEntry> values = new ArrayList<>();

        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        DatabaseReference mTransactionRef = mRootRef.child("Transaction");

        mTransactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float x;
                float y;
                MyXAxisValueFormatter xFormatter = new MyXAxisValueFormatter();

                //initialize x Axis Labels (labels for 13 vertical grid lines)
                final ArrayList<String> xAxisLabel = new ArrayList<>();


                float maxY = Float.MIN_VALUE;

                int i = 0;
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    for(DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()){
                        Transaction transaction = dataSnapshot2.getValue(Transaction.class);

                        if(transaction.getPaymentDetails().equals(detail)) {

                            if (!transaction.getPaidOut().isEmpty()) {


                                x = transaction.getDateInMilliseconds();
                                y = Float.valueOf(transaction.getPaidOut().trim());

                                if(y>maxY){
                                    maxY = y;
                                }

                                System.out.println("x: " + xFormatter.getFormattedValue(x) + "    |    y: " + y);
                                values.add(new BarEntry(i, y, getResources().getDrawable(R.drawable.star)));

                                xAxisLabel.add(xFormatter.getFormattedValue(x));

                                i++;
                            }
                        }
                    }
                }

                //xAxisLabel.add(""); //empty label for the last vertical grid line on Y-Right Axis

                BarDataSet set1;

                if(values.size() < 2){
                    findViewById(R.id.unitBarChart).setVisibility(GONE);
                }
                else if (chart.getData() != null &&
                        chart.getData().getDataSetCount() > 0) {
                    set1 = (BarDataSet) chart.getData().getDataSetByIndex(0);
                    set1.setValues(values);
                    chart.getData().notifyDataChanged();
                    chart.notifyDataSetChanged();

                } else {
                    set1 = new BarDataSet(values, "Spending");

                    set1.setDrawIcons(false);

                    XAxis xAxis = chart.getXAxis();

                    xAxis.setAxisMinimum(0 - 0.5f); //to center the bars inside the vertical grid lines we need + 0.5 step
                    xAxis.setAxisMaximum(values.size() - 1.0f + 0.5f); //to center the bars inside the vertical grid lines we need + 0.5 step
                    xAxis.setLabelCount(xAxisLabel.size(), true); //draw x labels for 13 vertical grid lines

                    xAxis.setGranularityEnabled(true);
                    xAxis.setGranularity(1.0f);
                    xAxis.setLabelCount(5);


                    xAxis.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            return xAxisLabel.get((int) value);
                        }
                    });

                    YAxis yAxis = chart.getAxisLeft();
                    //yAxis.setAxisMaximum(maxY);

                    chart.setVisibleYRangeMaximum(maxY, YAxis.AxisDependency.LEFT);



                    ArrayList<IBarDataSet> dataSets = new ArrayList<>();
                    dataSets.add(set1);

                    BarData data = new BarData(dataSets);
                    data.setValueTextSize(10f);
                    //data.setValueTypeface(tfLight);
                    data.setBarWidth(0.9f);

                    int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

                    switch (nightModeFlags) {
                        case Configuration.UI_MODE_NIGHT_YES:
                            data.setValueTextColor(Color.WHITE);
                            break;
                        case Configuration.UI_MODE_NIGHT_NO:
                            data.setValueTextColor(Color.BLACK);
                            break;
                        case Configuration.UI_MODE_NIGHT_UNDEFINED:
                            break;
                    }

                    //data.setValueTextColor(Color.WHITE);
                    data.setValueFormatter(new MoneyValueFormatter());

                    chart.setData(data);

                }

                chart.invalidate();
                chart.refreshDrawableState();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error){

            }
        });



    }



    /**
     * Converts given bank abbreviation to full string. If not recognised, it will return the same text.
     * @param abbreviation the abbreviation to be converted
     * @return the full text
     */
    private String getFullBankAbbreviation(String abbreviation){
        String result = "";
        switch(abbreviation.toLowerCase()){
            case "cr":
                result = "Credit";
                break;
            case "dd":
                result = "Direct Debit";
                break;
            case "dr":
                result = "Debit";
                break;
            case "trf":
                result = "Transfer";
                break;
            case "so":
                result = "Standing Order";
                break;
            case "chq":
                result = "Cheque";
                break;
            case "atm":
                result = "Cash Machine";
                break;
            case "bacs":
                result = "Wage";
                break;
            case "vis":
                result = "Visa";
                break;
            case ")))":
                result = "Visa";
                break;
            case "bp":
                result = "Bill Payment";
                break;
            default:
                result = abbreviation.toUpperCase();
                break;
        }
        return result;
    }


    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }



    //pass extra message back up to parent activity to prevent locking tabs
    @Nullable
    @Override
    public Intent getParentActivityIntent() {

        //boolean mSubt=getIntent().getBooleanExtra("snapShotExists",true);

        Intent intent=new Intent(this,MainActivity.class);
        intent.putExtra("snapShotExists", true);
        return intent;

        //return super.getParentActivityIntent();
    }

    public void refreshCategoryField(){

        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        DatabaseReference mTransactionRef = mRootRef.child("Transaction");

        mTransactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    for(DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()){
                        Transaction transaction = dataSnapshot2.getValue(Transaction.class);

                        String category = transaction.getCategory();

                        if(transaction.getPaymentDetails().equals(detail) && !category.isEmpty()){

                            Chip partyCategoryTextView = findViewById(R.id.category_tv);
                            partyCategoryTextView.setVisibility(View.VISIBLE);
                            partyCategoryTextView.setText(category);

                            break;
                        }


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String newCategory) {

        partyCategoryTextView.setText(newCategory);

        partyCategory = newCategory;
        clearCatBtn.setVisibility(View.VISIBLE);
        deleteCatBtn.setVisibility(View.VISIBLE);

        String msg = "Successfully changed category to '" + newCategory + "'. ";
        Snackbar.make(rootLayout, msg , Snackbar.LENGTH_SHORT)
                .show();

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }
}