package com.aston.spendingtracker.fragments;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.aston.spendingtracker.MostRecentRVAdapter;
import com.aston.spendingtracker.R;
import com.aston.spendingtracker.RecyclerViewAdapter;
import com.aston.spendingtracker.axisformatters.MoneyValueFormatter;
import com.aston.spendingtracker.axisformatters.SummaryXFormatter;
import com.aston.spendingtracker.entity.Transaction;
import com.aston.spendingtracker.entity.User;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;

    private LineChart chart;
    private BarChart summaryBarChart;

    LinkedList<Transaction> transactionList = new LinkedList<>();

    EditText filterET;

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    DatabaseReference mTransactionRef = mRootRef.child("Transaction");

    public DashboardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DashboardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DashboardFragment newInstance(String param1, String param2) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);




        setButtonListeners();

        setWelcomeMessage();

        populateTransactionHistory();

        displayBarChart();

        setBalanceSummary();

    }

    private void setWelcomeMessage(){
        DatabaseReference mUser = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                User user = snapshot.getValue(User.class);
                String firstNameLowerCase = user.firstName;
                String formattedName = firstNameLowerCase.substring(0,1).toUpperCase() + firstNameLowerCase.substring(1);

                String res = "Welcome back " + formattedName + "!";
                TextView welcomeMsgDashboardTV = getActivity().findViewById(R.id.welcome_mgs_dashboard_tv);
                welcomeMsgDashboardTV.setText(res);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setBalanceSummary(){

        TextView balTV = getView().findViewById(R.id.tv_summary);
        TextView balanceChip = getView().findViewById(R.id.balance_chip);

        mTransactionRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    for(DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                        Transaction transaction = dataSnapshot2.getValue(Transaction.class);
                        System.out.println(transaction.getDateOfTransaction());

                        transaction.parseDBDate();
                        transaction.parseDBMonth();
                        transaction.parseDBYear();

                        String balanceSummaryTitleString = "Balance as of " + transaction.getDateOfTransaction() + ":";

                        SpannableStringBuilder str = new SpannableStringBuilder(balanceSummaryTitleString);
                        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),13 , str.length()-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        balTV.setText(str);

                        String balanceString = "£"+transaction.getBalance();
                        balanceChip.setText(balanceString);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void displayBarChart(){

        // bar chart code:

        summaryBarChart = getView().findViewById(R.id.summaryBarChart);
        //summaryBarChart.setOnChartValueSelectedListener(this);

        summaryBarChart.setDrawBarShadow(false);
        summaryBarChart.setDrawValueAboveBar(true);

        summaryBarChart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        summaryBarChart.setMaxVisibleValueCount(100);

        // scaling can now only be done on x- and y-axis separately
        summaryBarChart.setPinchZoom(false);

        summaryBarChart.setDrawGridBackground(false);
        summaryBarChart.setTouchEnabled(false);
        summaryBarChart.setExtraBottomOffset(20);
        //summaryBarChart.setExtraTopOffset(20);

        // chart.setDrawYLabels(false);


        //IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(chart);


        XAxis xAxis = summaryBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setTypeface(tfLight);
        xAxis.setDrawGridLines(false);
        //xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);


        ValueFormatter xAxisFormatter = new SummaryXFormatter();
        xAxis.setValueFormatter(xAxisFormatter);




        //IAxisValueFormatter custom = new MyAxisValueFormatter();

        YAxis leftAxis = summaryBarChart.getAxisLeft();
        //leftAxis.setTypeface(tfLight);
        //leftAxis.setLabelCount(8, false);
        //leftAxis.setSpaceTop(15f);
        leftAxis.setValueFormatter(new MoneyValueFormatter());
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(5f);

        leftAxis.setEnabled(false);





        //leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = summaryBarChart.getAxisRight();
        rightAxis.setDrawGridLines(false);

        rightAxis.setEnabled(false);

        //rightAxis.setTypeface(tfLight);
        //rightAxis.setLabelCount(8, false);

        //rightAxis.setValueFormatter(custom);

        //rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        Legend l = summaryBarChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);
        l.setEnabled(false);


        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

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


        setDataForSummaryBarChart();
    }

    private void setDataForSummaryBarChart(){

        ArrayList<BarEntry> values = new ArrayList<>();

        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        DatabaseReference mTransactionRef = mRootRef.child("Transaction");

        mTransactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                final ArrayList<String> xAxisLabel = new ArrayList<>();

                Float income = 0f;
                Float spending = 0f;

                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {

                        Transaction transaction = dataSnapshot2.getValue(Transaction.class);
                        String paidOut = transaction.getPaidOut();
                        if(!paidOut.isEmpty()){
                            spending += Float.valueOf(paidOut);
                        } else{
                            String paidIn = transaction.getPaidIn();
                            System.out.println("paidin  -----: " + paidIn);
                            income += Float.valueOf(paidIn);
                        }
                    }
                }

//                values.add(new BarEntry(1, income, getResources().getDrawable(R.drawable.star)));
//                values.add(new BarEntry(2, spending, getResources().getDrawable(R.drawable.star)));

                Float[] yValArray = new Float[2];
                yValArray[0] = income;
                yValArray[1] = spending;


                for(int i = 0; i < 2; i++){
                    values.add(new BarEntry(i, yValArray[i], getResources().getDrawable(R.drawable.star)));
                }

                SummaryXFormatter xFormatter = new SummaryXFormatter();
                xAxisLabel.add(xFormatter.getFormattedValue(1));
                xAxisLabel.add(xFormatter.getFormattedValue(2));

                System.out.println(values);

                BarDataSet set1;



                if (summaryBarChart.getData() != null &&
                        summaryBarChart.getData().getDataSetCount() > 0) {
                    set1 = (BarDataSet) summaryBarChart.getData().getDataSetByIndex(0);

                    set1.setValues(values);
                    summaryBarChart.getData().notifyDataChanged();
                    summaryBarChart.notifyDataSetChanged();

                } else {
                    set1 = new BarDataSet(values, "Spending");

                    set1.setDrawIcons(false);

                    XAxis xAxis = summaryBarChart.getXAxis();

//                    xAxis.setAxisMinimum(0 + 0.5f); //to center the bars inside the vertical grid lines we need + 0.5 step
//                    xAxis.setAxisMaximum(values.size() - 1.0f + 0.5f); //to center the bars inside the vertical grid lines we need + 0.5 step
                    xAxis.setLabelCount(xAxisLabel.size(), true); //draw x labels for 13 vertical grid lines

                    xAxis.setGranularityEnabled(true);
                    xAxis.setGranularity(1.0f);
                    xAxis.setLabelCount(2);

                    xAxis.setTextSize(15);


//                    xAxis.setValueFormatter(new ValueFormatter() {
//                        @Override
//                        public String getFormattedValue(float value) {
//                            return xAxisLabel.get((int) value);
//                        }
//                    });


                    YAxis yAxis = summaryBarChart.getAxisLeft();
                    //yAxis.setAxisMaximum(maxY);

                    //chart.setVisibleYRangeMaximum(maxY, YAxis.AxisDependency.LEFT);



                    ArrayList<IBarDataSet> dataSets = new ArrayList<>();
                    dataSets.add(set1);

                    BarData data = new BarData(dataSets);
                    data.setValueTextSize(15f);
                    data.setDrawValues(true);
                    //data.setValueTypeface(tfLight);
                    data.setBarWidth(0.9f);

                    int nightModeFlags = getView().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

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

                    set1.setColors(new int[] {Color.parseColor("#006C4A"),
                            Color.parseColor("#6c0d00")});

                    summaryBarChart.setData(data);

                }

                summaryBarChart.invalidate();
                summaryBarChart.refreshDrawableState();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setButtonListeners(){

        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        Button viewAllAnalyticsBtn = getView().findViewById(R.id.btn_more_analytics);
        viewAllAnalyticsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try{
                    bottomNavigationView.setSelectedItemId(R.id.menu_analytics);

//                    FragmentChangeListener mParent = (FragmentChangeListener) getActivity();
//                    mParent.onChange(2);
                }
                catch(ClassCastException e){
                    System.out.println(e);
                }


            }
        });

        Button viewAllTransactionsBtn = getView().findViewById(R.id.btn_all_transactions);
        viewAllTransactionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{

                    bottomNavigationView.setSelectedItemId(R.id.menu_list);

//                    FragmentChangeListener mParent = (FragmentChangeListener) getActivity();
//                    mParent.onChange(1);
                }
                catch(ClassCastException e){
                    System.out.println(e);
                }
            }
        });

    }

    private void populateTransactionHistory(){

        mRecyclerView = getView().findViewById(R.id.recyclerview);

        mRecyclerView.setClickable(false);

        // Create an adapter and supply the data to be displayed.
        mAdapter = new MostRecentRVAdapter(getActivity(), transactionList);

        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());


        linearLayoutManager = new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        mRecyclerView.setLayoutManager(linearLayoutManager);


        mTransactionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float maximumBal = 0;

                transactionList.clear(); //------------------
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    for(DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()){
                        Transaction transaction = dataSnapshot2.getValue(Transaction.class);
                        //System.out.println(transaction);
                        transaction.parseDBDate();
                        transaction.parseDBMonth();
                        transaction.parseDBYear();
                        transactionList.add(transaction);

                        if(maximumBal < Float.valueOf(transaction.getBalance())){
                            maximumBal = Float.valueOf(transaction.getBalance());
                        }

                    }

                }

                //update max bal on db:
                mRootRef.child("MaximumBalance").setValue(maximumBal);


                Collections.reverse(transactionList);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("db data retrieval fail: " + error);
            }
        });

    }


}