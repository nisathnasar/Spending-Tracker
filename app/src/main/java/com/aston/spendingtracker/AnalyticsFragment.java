package com.aston.spendingtracker;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.renderscript.Sampler;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.aston.spendingtracker.entity.Transaction;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.SeekBar.OnSeekBarChangeListener;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AnalyticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AnalyticsFragment extends Fragment implements
        OnChartValueSelectedListener {

    private LineChart chart;
    private SeekBar seekBarX, seekBarY;
    private TextView tvX, tvY;
    LinkedList<Transaction> transactionList = new LinkedList<>();

    private PieChart pieChart;

    private static final int MAX_X_VALUE = 7;
    private static final int MAX_Y_VALUE = 50;
    private static final int MIN_Y_VALUE = 5;
    private static final String SET_LABEL = "App Downloads";
    private static final String[] DAYS = { "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT" };

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AnalyticsFragment() {
        // Required empty public constructor

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AnalyticsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AnalyticsFragment newInstance(String param1, String param2) {
        AnalyticsFragment fragment = new AnalyticsFragment();
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
        return inflater.inflate(R.layout.fragment_analytics, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        //getActivity().setTitle("LineChartActivity1");

        displayLineChart();


        //piechart

        pieChart = getView().findViewById(R.id.pie_chart);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 0, 5, 0);

        pieChart.setDragDecelerationFrictionCoef(0.95f);

        //pieChart.setCenterTextTypeface(tfLight);
//        pieChart.setCenterText(generateCenterSpannableText());

        pieChart.setDrawHoleEnabled(true);
        //pieChart.setHoleColor(Color.WHITE);

        pieChart.setHoleColor(R.color.md_theme_dark_background);
        pieChart.setTransparentCircleColor(R.color.md_theme_dark_background);
        pieChart.setTransparentCircleAlpha(110);

        pieChart.setHoleRadius(35f);
        pieChart.setTransparentCircleRadius(40f);

        pieChart.setDrawCenterText(true);

        pieChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        pieChart.setDrawEntryLabels(false);


        // pieChart.setUnit(" €");
        // chart.setDrawUnitsInChart(true);

        // add a selection listener
        pieChart.setOnChartValueSelectedListener(this);


        pieChart.animateY(1400, Easing.EaseInOutQuad);
        // chart.spin(2000, 0, 360);

        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(20f);

        int nightModeFlags = getView().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                l.setTextColor(Color.WHITE);
            case Configuration.UI_MODE_NIGHT_NO:
                l.setTextColor(Color.BLACK);
                break;
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                break;
        }

        l.setTextSize(12f);



        // entry label styling
        pieChart.setEntryLabelColor(Color.WHITE);
        //pieChart.setEntryLabelTypeface(tfRegular);
        pieChart.setEntryLabelTextSize(12f);

        setPieChartData();


        MaterialSwitch labelSwitch = getView().findViewById(R.id.labels_toggle);

        labelSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    pieChart.setDrawEntryLabels(true);
                    pieChart.invalidate();
                    pieChart.refreshDrawableState();
                }else{
                    pieChart.setDrawEntryLabels(false);
                    pieChart.invalidate();
                    pieChart.refreshDrawableState();
                }
            }
        });

    }

    private SpannableString generateCenterSpannableText(String val) {
        SpannableString s = new SpannableString("£"+val);
        s.setSpan(new RelativeSizeSpan(1.7f), 0, s.length(), 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), 0, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);


//        SpannableString s = new SpannableString("Spending \nby category");
//        s.setSpan(new RelativeSizeSpan(1.7f), 0, 9, 0);
//        s.setSpan(new StyleSpan(Typeface.NORMAL), 9, s.length() - 9, 0);
//        s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length() - 13, 0);
//        s.setSpan(new ForegroundColorSpan(Color.GRAY), 9, s.length() - 9, 0);
//        s.setSpan(new RelativeSizeSpan(.8f), 9, s.length() - 9, 0);
//        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 9, s.length(), 0);
//        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 9, s.length(), 0);
        return s;
    }


    private void setPieChartData() {
        ArrayList<PieEntry> entries = new ArrayList<>();

        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        DatabaseReference mTransactionRef = mRootRef.child("Transaction");

        mTransactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                double totalSpending = 0;

                Map<String, ArrayList<Transaction>> transactionCatMap = new HashMap<>();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {


                    for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                        Transaction transaction = dataSnapshot2.getValue(Transaction.class);

                        String category = transaction.getCategory();

                        if(!category.isEmpty()){

                            if(transactionCatMap.containsKey(category)){
                                ArrayList<Transaction> transactions = transactionCatMap.get(category);
                                transactions.add(transaction);
                                transactionCatMap.put(category, transactions);
                            }else{
                                ArrayList<Transaction> newList = new ArrayList<>();
                                newList.add(transaction);
                                transactionCatMap.put(category, newList);
                            }
                        }
                        else{
                            ArrayList<Transaction> newList = new ArrayList<>();
                            newList.add(transaction);
                            transactionCatMap.put("other", newList);
                        }

                    }
                }

                Map<String, Double> catSpendingMap = new HashMap<>();

                for(String key : transactionCatMap.keySet()){

                    for(Transaction t: transactionCatMap.get(key)){
                        String paidOutStr = t.getPaidOut();

                        if(!paidOutStr.isEmpty()){
                            totalSpending += Double.parseDouble(t.getPaidOut());
                        }

                    }

                    if(totalSpending != 0){
                        catSpendingMap.put(key, totalSpending);
                    }

                }


                for(String key : catSpendingMap.keySet()){
                    System.out.println(key + " " + catSpendingMap.get(key));
                    double val = catSpendingMap.get(key);
                    entries.add(new PieEntry((float) (val),
                            key,
                            getResources().getDrawable(R.drawable.star)));

                }

                {
                    PieDataSet dataSet = new PieDataSet(entries, "");

                    dataSet.setDrawIcons(false);

                    dataSet.setSliceSpace(3f);
                    dataSet.setIconsOffset(new MPPointF(0, 40));
                    dataSet.setSelectionShift(5f);

                    // add a lot of colors

                    ArrayList<Integer> colors = new ArrayList<>();

//                    for (int c : ColorTemplate.VORDIPLOM_COLORS)
//                        colors.add(c);

//                    for (int c : ColorTemplate.JOYFUL_COLORS)
//                        colors.add(c);
//
//                    for (int c : ColorTemplate.COLORFUL_COLORS)
//                        colors.add(c);
//
                    for (int c : ColorTemplate.LIBERTY_COLORS)
                        colors.add(c);
//
                    for (int c : ColorTemplate.PASTEL_COLORS)
                        colors.add(c);

                    colors.add(ColorTemplate.getHoloBlue());

                    dataSet.setColors(colors);
                    dataSet.setSelectionShift(12f);




                    PieData data = new PieData(dataSet);
                    data.setValueFormatter(new MoneyValueFormatter());
                    data.setValueTextSize(11f);
                    data.setValueTextColor(Color.WHITE);
                    //data.setValueTypeface(tfLight);
                    DecimalFormat df = new DecimalFormat("0.00");

                    pieChart.setCenterText(generateCenterSpannableText(df.format(totalSpending)));

                    pieChart.setData(data);


                    // undo all highlights
                    pieChart.highlightValues(null);

                    pieChart.invalidate();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }





    private void displayLineChart(){

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

            // create marker to display box when values are selected
            //MyMarkerView mv = new MyMarkerView(getActivity(), R.layout.custom_marker_view);

            // Set the marker to the chart
            //mv.setChartView(chart);
            //chart.setMarker(mv);

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

            // vertical grid lines
            //xAxis.enableGridDashedLine(10f, 10f, 0f);
        }

        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = chart.getAxisLeft();

            // disable dual axis (only use LEFT axis)
            chart.getAxisRight().setEnabled(false);

            // horizontal grid lines
            //yAxis.enableGridDashedLine(10f, 10f, 0f);

            // axis range
//            yAxis.setAxisMaximum(2535f);
//            yAxis.setAxisMinimum(0f);
        }


        //setData(45, 180);
        setDataFromDB();

        // draw points over time
        chart.animateX(1500);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();

        // draw legend entries as lines
        l.setForm(Legend.LegendForm.LINE);
        //l.setForm(Legend.LegendForm.CIRCLE);

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

                        values.add(new Entry(x, y, getResources().getDrawable(R.drawable.star)));

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

                    set1.setDrawValues(false);

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