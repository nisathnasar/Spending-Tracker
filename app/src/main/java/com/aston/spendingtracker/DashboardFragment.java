package com.aston.spendingtracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.aston.spendingtracker.entity.Transaction;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;
import java.util.TreeMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment implements OnChartValueSelectedListener {

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

    LinkedList<Transaction> transactionList = new LinkedList<>();

    EditText filterET;

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

//        Fragment childFragment = new DashboardFragment();
//        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
//        transaction.replace(R.id.transaction_list_frame, childFragment).commit();

//        FragmentManager childFragMan = getChildFragmentManager();
//        FragmentTransaction childFragTrans = childFragMan.beginTransaction();
//        DashboardFragment fragB = new DashboardFragment ();
//        childFragTrans.add(R.id.transaction_list_frame, fragB);
//        childFragTrans.addToBackStack("B");
//        childFragTrans.commit();


        Button viewAllAnalyticsBtn = getView().findViewById(R.id.btn_more_analytics);
        viewAllAnalyticsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try{
                    FragmentChangeListener mParent = (FragmentChangeListener) getActivity();
                    mParent.onChange(2);
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
                    FragmentChangeListener mParent = (FragmentChangeListener) getActivity();
                    mParent.onChange(1);
                }
                catch(ClassCastException e){
                    System.out.println(e);
                }
            }
        });



        mRecyclerView = getView().findViewById(R.id.recyclerview);


        // Create an adapter and supply the data to be displayed.
        mAdapter = new MostRecentRVAdapter(getActivity(), transactionList);

        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //mRecyclerView.setLayoutFrozen(true);

        //mAdapter.setClickListener(this::onClick); // Bind the listener


        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        DatabaseReference mTransactionRef = mRootRef.child("Transaction");



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

//                        try {
//                            Transaction.sortTransactionListByDate(transactionList);
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
                    }

                }

                //finalMaximumBal[0] = maximumBal;

                //update max bal on db:
                mRootRef.child("MaximumBalance").setValue(maximumBal);

/*
                if(transactionList.size()==0){
                    getView().findViewById(R.id.frame_layout).setVisibility(View.GONE);
                    getView().findViewById(R.id.welcome_msg_tv).setVisibility(View.VISIBLE);
                    //Button welcomeMsgUploadBtn = getView().findViewById(R.id.welcome_msg_upload_bt);
                    //welcomeMsgUploadBtn.setVisibility(View.VISIBLE);

                } else{
                    getView().findViewById(R.id.frame_layout).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.welcome_msg_tv).setVisibility(View.GONE);
                }
*/

                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("db data retrieval fail: " + error);
            }
        });



        if(transactionList.size()==0){
            //getView().findViewById(R.id.frame_layout).setVisibility(View.GONE);
            //getView().findViewById(R.id.welcome_msg_tv).setVisibility(View.VISIBLE);

            //Button welcomeMsgUploadBtn = getView().findViewById(R.id.welcome_msg_upload_bt);
            //welcomeMsgUploadBtn.setVisibility(View.VISIBLE);

        }


        //chart population:
        //tvX = getView().findViewById(R.id.tvXMax);
        //tvY = getView().findViewById(R.id.tvYMax);

        //seekBarX = getView().findViewById(R.id.seekBar1);
        //seekBarX.setOnSeekBarChangeListener(this);

        //seekBarY = getView().findViewById(R.id.seekBar2);
//        seekBarY.setMax(180);
        //seekBarY.setMax(1763);
        //seekBarY.setOnSeekBarChangeListener(this);

        {   // // Chart Style // //
            chart = getView().findViewById(R.id.chart1);

            // background color
            chart.setBackgroundColor(Color.WHITE);

            // disable description text
            chart.getDescription().setEnabled(false);

            // enable touch gestures
            chart.setTouchEnabled(true);

            // set listeners
            chart.setOnChartValueSelectedListener(this);
            chart.setDrawGridBackground(false);

            // enable scaling and dragging
            chart.setDragEnabled(true);
            //chart.setScaleEnabled(true);//-----------------
            chart.setScaleXEnabled(true);
            chart.setScaleYEnabled(true);

            // force pinch zoom along both axis
            chart.setPinchZoom(true);
        }

        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);


            xAxis.setValueFormatter(new MyXAxisValueFormatter());

        }

        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = chart.getAxisLeft();

            // disable dual axis (only use LEFT axis)
            chart.getAxisRight().setEnabled(false);

        }


        {

            yAxis.setDrawLimitLinesBehindData(true);
            xAxis.setDrawLimitLinesBehindData(true);

        }

        setDataFromDB();

        // draw points over time
        chart.animateX(1500);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();

        // draw legend entries as lines
        l.setForm(Legend.LegendForm.LINE);


        //ListView transactionListView = getView().findViewById(R.id.transaction_list_view);


        //transactionListView.setAdapter(new MostRecentRVAdapter(getActivity(), transactionList));


    }




    private void setDataFromDB() {

        ArrayList<Entry> values = new ArrayList<>();

//        for (int i = 0; i < count; i++) {
//
//            float val = (float) (Math.random() * range) - 30;
//            values.add(new Entry(i, val, getResources().getDrawable(R.drawable.star)));
//
//        }



        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        DatabaseReference mTransactionRef = mRootRef.child("Transaction");


        mTransactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //transactionList.clear(); //------------------
                //values.clear();

                float i = 0;
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){

                    for(DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()){
                        Transaction transaction = dataSnapshot2.getValue(Transaction.class);
                        //System.out.println(transaction);
                        transaction.parseDBDate();
                        transaction.parseDBMonth();
//                        transactionList.add(transaction);

                        float x;
                        float y;
//
                        x = transaction.getDateInMilliseconds();
                        y = Float.valueOf(transaction.getBalance().trim());

                        try{
                            //values.add(new Entry(x, y, ((MainActivity)getActivity()).getResources().getDrawable(R.drawable.star)));
                            values.add(new Entry(x, y, requireActivity().getResources().getDrawable(R.drawable.star)));
                        }
                        catch (Exception e){
                            System.out.println(e);
                        }


//                        values.add(new Entry(i, y));

                        //Collections.sort(values, new EntryXComparator());

                        i++;
                        //System.out.println("x = " + x + "   , y = " + y + "    : parsed: " + Transaction.getParsedDateInMilliseconds(x));


                    }

                }




                LineDataSet set1;

                if (chart.getData() != null &&
                        chart.getData().getDataSetCount() > 0) {
                    set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);

                    set1.setValues(values);
                    set1.notifyDataSetChanged();
                    chart.getData().notifyDataChanged();
                    chart.notifyDataSetChanged();
                } else {
                    // create a dataset and give it a type
                    set1 = new LineDataSet(values, "DataSet 1");

                    set1.setDrawIcons(false);

                    // draw dashed line
                    set1.enableDashedLine(10f, 5f, 0f);

                    // black lines and points
                    set1.setColor(Color.BLACK);
                    set1.setCircleColor(Color.BLACK);

                    // line thickness and point size
                    set1.setLineWidth(1f);
                    set1.setCircleRadius(3f);

                    // draw points as solid circles
                    set1.setDrawCircleHole(false);

                    // customize legend entry
                    set1.setFormLineWidth(1f);
                    set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
                    set1.setFormSize(15.f);

                    // text size of values
                    set1.setValueTextSize(9f);

                    // draw selection line as dashed
                    set1.enableDashedHighlightLine(10f, 5f, 0f);

                    // set the filled area
                    set1.setDrawFilled(true);
                    set1.setFillFormatter(new IFillFormatter() {
                        @Override
                        public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                            return chart.getAxisLeft().getAxisMinimum();
                        }
                    });

                    // set color of filled area
                    if (Utils.getSDKInt() >= 18) {
                        // drawables only supported on api level 18 and above
                        Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.fade_red);
                        set1.setFillDrawable(drawable);
                    } else {
                        set1.setFillColor(Color.BLACK);
                    }

                    ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                    dataSets.add(set1); // add the data sets

                    // create a data object with the data sets
                    LineData data = new LineData(dataSets);

                    // set data
                    chart.setData( data);
                }





            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("db data retrieval fail: " + error);
            }
        });




//        for(Entry val : values){
//            System.out.println(val);
//        }


        for(int i  = 0; i < 100; i++){
            i += i*2;
            //values.add(new Entry(i, i, getResources().getDrawable(R.drawable.star)));
        }



    }


    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }



}