package com.aston.spendingtracker;

import static android.view.View.GONE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aston.spendingtracker.entity.Transaction;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TreeMap;

public class ViewTransaction extends AppCompatActivity implements OnChartValueSelectedListener {

    LinkedList<Transaction> transactionList = new LinkedList<>();

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private BarChart chart;

    private String detail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_transaction);

        TextView title_tv = findViewById(R.id.title_tv);
        TextView transactionTextView = findViewById(R.id.transaction_tv);
        TextView balanceTextView = findViewById(R.id.balance_tv);
        TextView balanceBeforeTextView = findViewById(R.id.balance_before_tv);
        TextView transactionTypeTextView = findViewById(R.id.type_tv);
        TextView dateOfTransactionTextView = findViewById(R.id.date_of_transaction_tv);
        TextView partyCategoryTextView = findViewById(R.id.category_tv);

        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

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
                    transactionTextView.setBackgroundColor(Color.parseColor("#53b075"));
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
                    transactionTextView.setBackgroundColor(Color.parseColor("#5c859c"));
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

        String partyCategory = intent.getStringExtra("category");
        partyCategoryTextView.setText(partyCategory);


        mRecyclerView = findViewById(R.id.recyclerviewfiltered);


        // Create an adapter and supply the data to be displayed.
        mAdapter = new RecyclerViewAdapter(this, transactionList);

        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        DatabaseReference mTransactionRef = mRootRef.child("Transaction");

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
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


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
        xAxis.setTextColor(Color.WHITE);

        ValueFormatter xAxisFormatter = new MyXAxisValueFormatter();
        //xAxis.setValueFormatter(xAxisFormatter);




        //IAxisValueFormatter custom = new MyAxisValueFormatter();

        YAxis leftAxis = chart.getAxisLeft();
        //leftAxis.setTypeface(tfLight);
        //leftAxis.setLabelCount(8, false);
        leftAxis.setSpaceTop(15f);
        leftAxis.setValueFormatter(new MoneyValueFormatter());
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setTextColor(Color.WHITE);
        //leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);

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
        l.setTextColor(Color.WHITE);

//        XYMarkerView mv = new XYMarkerView(this, new MyXAxisValueFormatter());
        /*
        XYMarkerView mv = new XYMarkerView(this, xAxisFormatter);
        mv.setChartView(chart); // For bounds control
        chart.setMarker(mv); // Set the marker to the chart
*/
        // setting data
//        seekBarY.setProgress(50);
//        seekBarX.setProgress(12);


        setData();




        Button btnCategory = findViewById(R.id.btn_add_to_category);
        btnCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AddToCategoryFragment frg = new AddToCategoryFragment();
                frg.show(getFragmentManager(), "idk");

            }
        });




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



                int i = 0;
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    for(DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()){
                        Transaction transaction = dataSnapshot2.getValue(Transaction.class);

                        if(transaction.getPaymentDetails().equals(detail)) {

                            if (!transaction.getPaidOut().isEmpty()) {


                                x = transaction.getDateInMilliseconds();
                                y = Float.valueOf(transaction.getPaidOut().trim());

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

                    xAxis.setAxisMinimum(0 + 0.5f); //to center the bars inside the vertical grid lines we need + 0.5 step
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


                    ArrayList<IBarDataSet> dataSets = new ArrayList<>();
                    dataSets.add(set1);

                    BarData data = new BarData(dataSets);
                    data.setValueTextSize(10f);
                    //data.setValueTypeface(tfLight);
                    data.setBarWidth(0.9f);
                    data.setValueTextColor(Color.WHITE);

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
}