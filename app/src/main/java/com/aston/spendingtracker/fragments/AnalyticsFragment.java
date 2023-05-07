package com.aston.spendingtracker.fragments;

import static android.view.View.GONE;
import static com.github.mikephil.charting.utils.ColorTemplate.rgb;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aston.spendingtracker.R;
import com.aston.spendingtracker.axisformatters.MoneyValueFormatter;
import com.aston.spendingtracker.axisformatters.MyXAxisValueFormatter;
import com.aston.spendingtracker.axisformatters.PercentageValueFormatter;
import com.aston.spendingtracker.entity.Transaction;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
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
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AnalyticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AnalyticsFragment extends Fragment implements
        OnChartValueSelectedListener {

    private LineChart lineChart;
    private SeekBar seekBarX, seekBarY;
    private BarChart weeklyBarChart;
    private TextView tvX, tvY;
    LinkedList<Transaction> transactionList = new LinkedList<>();

    private PieChart pieChartSpendingByCategory, pieChartIncomeSources;


    public static final int[] DARK_COLOURS_FOR_WHITE_TEXT = {
            rgb("#ad3e07"), rgb("#243354"),
            rgb("#056341"), rgb("#053963"),
            rgb("#4a0563"), rgb("#630544"),
            rgb("#23454a"), rgb("#9a37c4")
    };



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

        displayWeeklyOutgoing();

        displayLineChartOverview();

        displayPieChartSpendingByCategory();

        displayPieChartIncomeSources();

        implementSwitches();

    }

    private void implementSwitches(){
        MaterialSwitch labelSwitch = getView().findViewById(R.id.labels_toggle);

        labelSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    pieChartSpendingByCategory.setDrawEntryLabels(true);
                    pieChartSpendingByCategory.invalidate();
                    pieChartSpendingByCategory.refreshDrawableState();
                }else{
                    pieChartSpendingByCategory.setDrawEntryLabels(false);
                    pieChartSpendingByCategory.invalidate();
                    pieChartSpendingByCategory.refreshDrawableState();
                }
            }
        });

        MaterialSwitch percentageSwitchPieChartSpendingByCategory = getView().findViewById(R.id.percentage_toggle_spending_by_category);

        percentageSwitchPieChartSpendingByCategory.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    pieChartSpendingByCategory.setUsePercentValues(true);
                    setPieChartDataSpendingByCategory(new PercentageValueFormatter());

                }else{
                    pieChartSpendingByCategory.setUsePercentValues(false);
                    setPieChartDataSpendingByCategory(new MoneyValueFormatter());

                }
                pieChartIncomeSources.invalidate();
                pieChartIncomeSources.refreshDrawableState();
            }
        });




        MaterialSwitch labelSwitchPieChartIncomeSource = getView().findViewById(R.id.labels_toggle_income_sources);

        labelSwitchPieChartIncomeSource.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    pieChartIncomeSources.setDrawEntryLabels(true);
                }else{
                    pieChartIncomeSources.setDrawEntryLabels(false);

                }
                pieChartIncomeSources.invalidate();
                pieChartIncomeSources.refreshDrawableState();
            }
        });

        MaterialSwitch percentageSwitchPieChartIncomeSource = getView().findViewById(R.id.percentage_toggle_income_sources);

        percentageSwitchPieChartIncomeSource.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    pieChartIncomeSources.setUsePercentValues(true);
                    setPieChartDataIncomeSources(new PercentageValueFormatter());

                }else{
                    pieChartIncomeSources.setUsePercentValues(false);
                    setPieChartDataIncomeSources(new MoneyValueFormatter());

                }
                pieChartIncomeSources.invalidate();
                pieChartIncomeSources.refreshDrawableState();
            }
        });

    }

    private void displayWeeklyOutgoing(){

        // bar chart code:

        weeklyBarChart = getView().findViewById(R.id.weekly_bar_chart);
//        weeklyBarChart.setOnChartValueSelectedListener(this);
        weeklyBarChart.setTouchEnabled(false);
        weeklyBarChart.setDrawBarShadow(false);
        weeklyBarChart.setDrawValueAboveBar(true);

        weeklyBarChart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        weeklyBarChart.setMaxVisibleValueCount(100);

        // scaling can now only be done on x- and y-axis separately
        weeklyBarChart.setPinchZoom(false);

        weeklyBarChart.setDrawGridBackground(false);
        // chart.setDrawYLabels(false);

        //weeklyBarChart.setBackgroundColor(Color.BLACK);

        //IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(chart);


        XAxis xAxis = weeklyBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setTypeface(tfLight);
        xAxis.setDrawGridLines(false);
        //xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);


        ValueFormatter xAxisFormatter = new MyXAxisValueFormatter();
        //xAxis.setValueFormatter(xAxisFormatter);




        //IAxisValueFormatter custom = new MyAxisValueFormatter();

        YAxis leftAxis = weeklyBarChart.getAxisLeft();
        //leftAxis.setTypeface(tfLight);
        //leftAxis.setLabelCount(8, false);
        //leftAxis.setSpaceTop(15f);
        leftAxis.setValueFormatter(new MoneyValueFormatter());
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(5f);

        leftAxis.setEnabled(false);



        //leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = weeklyBarChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setEnabled(false);

        //rightAxis.setTypeface(tfLight);
        //rightAxis.setLabelCount(8, false);

        //rightAxis.setValueFormatter(custom);

        //rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        Legend l = weeklyBarChart.getLegend();
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


        setWeeklyBarChart();

    }

    String lastDateString;
    Transaction lastDateTransactionObj;
    Date lastDateObj;
    String lastDateDayOfTheWeek;
    String nearestPrevMonday;
    Calendar calendar;

    private void setWeeklyBarChart(){

        ArrayList<BarEntry> values = new ArrayList<>();

        ArrayList<Transaction> allTransactions = new ArrayList<>();

        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        DatabaseReference mTransactionRef = mRootRef.child("Transaction");

        mTransactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                        Transaction transaction = dataSnapshot2.getValue(Transaction.class);
                        transaction.parseDBDate();
                        transaction.parseDBMonth();
                        transaction.parseDBYear();

                        allTransactions.add(transaction);

//                        lastDate = transaction.getDateOfTransaction();
//                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
//
//                        Date firstDateObj;
//                        try {
//                            firstDateObj = sdf.parse(lastDate);
//                            sdf.applyPattern("EEEE");
//
//                            lastDateDayOfTheWeek = sdf.format(firstDateObj);
//
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
                    }
                }

                Collections.reverse(allTransactions);
                //System.out.println(allTransactions);

                lastDateTransactionObj = allTransactions.get(0);


                MyXAxisValueFormatter xFormatter = new MyXAxisValueFormatter();

                //initialize x Axis Labels (labels for 13 vertical grid lines)
                final ArrayList<String> xAxisLabel = new ArrayList<>();


                float maxY = Float.MIN_VALUE;

                DateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
                try {
                    lastDateObj = formatter.parse(lastDateTransactionObj.getDateOfTransaction());
                    calendar = Calendar.getInstance();
                    calendar.setTime(lastDateObj);

                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    int daysToSubtract = dayOfWeek - Calendar.MONDAY;

                    if(daysToSubtract < 0){
                        daysToSubtract += 7;
                    }

                    calendar.add(Calendar.DAY_OF_MONTH, -daysToSubtract);

                    Date lastWeekStartDate = calendar.getTime();

                    ArrayList<Transaction> lastWeekTransactions = new ArrayList<>();

                    for(Transaction transaction : allTransactions){
                        Date thisDate = formatter.parse(transaction.getDateOfTransaction());

                        if(thisDate.after(lastWeekStartDate) || thisDate.equals(lastWeekStartDate)){
                            lastWeekTransactions.add(transaction);
                        } else{
                            break;
                        }

                    }

                    for(Transaction transaction : lastWeekTransactions){
                        System.out.println(transaction.getDateOfTransaction() + " : " +transaction);
                    }

                    HashMap<String, ArrayList<Transaction>> dateTransactionMap = new HashMap<>();


                    for(int i = 0; i < 7; i++){
                        calendar.add(Calendar.DATE, i);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
                        String iterDate = sdf.format(calendar.getTime());

                        dateTransactionMap.put(iterDate, new ArrayList<>());
                    }

                    for(Transaction transaction : lastWeekTransactions){

                        if(dateTransactionMap.containsKey(transaction.getDateOfTransaction())){
                            ArrayList<Transaction> existingList = dateTransactionMap.get(transaction.getDateOfTransaction());
                            existingList.add(transaction);
                            dateTransactionMap.put(transaction.getDateOfTransaction(), existingList);

                        }
                        else{
                            ArrayList<Transaction> newList = new ArrayList<>();
                            newList.add(transaction);
                            dateTransactionMap.put(transaction.getDateOfTransaction(), newList);
                        }

                    }

                    ArrayList<String> weekDaysLabelList = new ArrayList<>();
                    weekDaysLabelList.add("M");
                    weekDaysLabelList.add("T");
                    weekDaysLabelList.add("W");
                    weekDaysLabelList.add("T");
                    weekDaysLabelList.add("F");
                    weekDaysLabelList.add("S");
                    weekDaysLabelList.add("S");

                    int i = 0;

                    HashMap<Float, Float> dateTotalMap = new HashMap<>();
                    for(String dateStr : dateTransactionMap.keySet()){
                        float totalForTheDate = 0;

                        for(Transaction transaction : dateTransactionMap.get(dateStr)){
                            if(!transaction.getPaidOut().isEmpty()){
                                totalForTheDate += Float.parseFloat(transaction.getPaidOut());
                            }


                        }

                        int sizeOfArrList = dateTransactionMap.get(dateStr).size();

                        if(sizeOfArrList!=0){
                            dateTotalMap.put(dateTransactionMap.get(dateStr).get(0).getDateInMilliseconds(), totalForTheDate);
                        }
                        else{
                            //dateTotalMap.put(dateTransactionMap.get(dateStr).get(0).getDateInMilliseconds(), totalForTheDate);
                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
                        Date d = sdf.parse(dateStr);
                        //d.getTime()
//                        float x = dateTransactionMap.get(dateStr).get(0).getDateInMilliseconds();
                        float x = d.getTime();
                        float y = totalForTheDate;

                        System.out.println("x: " + x + "y: " + y);

                        values.add(new BarEntry(i, y, getResources().getDrawable(R.drawable.star)));

//                        xAxisLabel.add(xFormatter.getFormattedValue(x));
                        if(i < weekDaysLabelList.size()){
                            xAxisLabel.add(weekDaysLabelList.get(i));
                        }


                        i++;
                    }


                    for(Transaction transaction : lastWeekTransactions){
                        float x = transaction.getDateInMilliseconds();
                        float y;

                        if(!transaction.getPaidIn().isEmpty()){
                        }
                        else{
                            y = Float.valueOf(transaction.getPaidOut());
                        }


//                        values.add(new BarEntry(x, y, getResources().getDrawable(R.drawable.star)));
//                        xAxisLabel.add(xFormatter.getFormattedValue(x));
                    }



                } catch (ParseException e) {
                    e.printStackTrace();
                }






                //xAxisLabel.add(""); //empty label for the last vertical grid line on Y-Right Axis

                BarDataSet set1;

                if(values.size() < 2){
                    weeklyBarChart.setVisibility(GONE);
                }
                else if (weeklyBarChart.getData() != null &&
                        weeklyBarChart.getData().getDataSetCount() > 0) {
                    set1 = (BarDataSet) weeklyBarChart.getData().getDataSetByIndex(0);
                    set1.setValues(values);
                    weeklyBarChart.getData().notifyDataChanged();
                    weeklyBarChart.notifyDataSetChanged();

                } else {
                    set1 = new BarDataSet(values, "Spending");

                    set1.setDrawIcons(false);

                    XAxis xAxis = weeklyBarChart.getXAxis();

                    //xAxis.setAxisMinimum(0 + 0.5f); //to center the bars inside the vertical grid lines we need + 0.5 step
                    //xAxis.setAxisMaximum(values.size() - 1.0f + 0.5f); //to center the bars inside the vertical grid lines we need + 0.5 step
                    //xAxis.setLabelCount(xAxisLabel.size(), true); //draw x labels for 13 vertical grid lines

                    xAxis.setGranularityEnabled(true);
                    xAxis.setGranularity(1.0f);
                    xAxis.setLabelCount(7);


                    xAxis.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            return xAxisLabel.get((int) value);
                        }
                    });

                    //xAxis.setValueFormatter(new MyXAxisValueFormatter());

                    YAxis yAxis = weeklyBarChart.getAxisLeft();

                    //yAxis.setAxisMaximum(maxY);

                    //weeklyBarChart.setVisibleYRangeMaximum(maxY, YAxis.AxisDependency.LEFT);

                    weeklyBarChart.animateY(500);


                    ArrayList<IBarDataSet> dataSets = new ArrayList<>();
                    dataSets.add(set1);

                    BarData data = new BarData(dataSets);

                    data.setValueTextSize(10f);
                    //data.setValueTypeface(tfLight);
                    //data.setBarWidth(0.9f);


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


                    weeklyBarChart.setData(data);

                }

                weeklyBarChart.invalidate();
                weeklyBarChart.refreshDrawableState();





            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





    }

    private void displayPieChartIncomeSources(){

        //piechart

        pieChartIncomeSources = getView().findViewById(R.id.pie_chart_income_sources);
        pieChartIncomeSources.setUsePercentValues(false);
        pieChartIncomeSources.getDescription().setEnabled(false);
        pieChartIncomeSources.setExtraOffsets(5, 0, 5, 0);
        pieChartIncomeSources.setDragDecelerationFrictionCoef(0.95f);

        //pieChart.setCenterTextTypeface(tfLight);
//        pieChart.setCenterText(generateCenterSpannableText());

        pieChartIncomeSources.setDrawHoleEnabled(true);
        //pieChart.setHoleColor(Color.WHITE);

        pieChartIncomeSources.setHoleColor(R.color.md_theme_dark_background);
        pieChartIncomeSources.setTransparentCircleColor(R.color.md_theme_dark_background);
        pieChartIncomeSources.setTransparentCircleAlpha(110);

        pieChartIncomeSources.setHoleRadius(35f);
        pieChartIncomeSources.setTransparentCircleRadius(40f);

        pieChartIncomeSources.setDrawCenterText(true);

        pieChartIncomeSources.setRotationAngle(0);
        // enable rotation of the chart by touch
        pieChartIncomeSources.setRotationEnabled(true);
        pieChartIncomeSources.setHighlightPerTapEnabled(true);

        pieChartIncomeSources.setDrawEntryLabels(false);


        // pieChart.setUnit(" €");
        // chart.setDrawUnitsInChart(true);

        // add a selection listener
        pieChartIncomeSources.setOnChartValueSelectedListener(this);


        //pieChartIncomeSources.animateY(1400, Easing.EaseInOutQuad);
        // chart.spin(2000, 0, 360);

        Legend l = pieChartIncomeSources.getLegend();
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
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                l.setTextColor(Color.BLACK);
                break;
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                break;
        }

        l.setTextSize(12f);



        // entry label styling
        pieChartIncomeSources.setEntryLabelColor(Color.WHITE);
        //pieChart.setEntryLabelTypeface(tfRegular);
        pieChartIncomeSources.setEntryLabelTextSize(12f);



        setPieChartDataIncomeSources(new MoneyValueFormatter());


    }

    private void setPieChartDataIncomeSources(ValueFormatter valueFormatter){

        ArrayList<PieEntry> entries = new ArrayList<>();

        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        DatabaseReference mTransactionRef = mRootRef.child("Transaction");

        mTransactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {



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

                Map<String, Double> catIncomeMap = new HashMap<>();
                double totalIncome = 0;
                for(String key : transactionCatMap.keySet()){
                    double totalIncomeByCat = 0;
                    for(Transaction t: transactionCatMap.get(key)){
                        String paidInStr = t.getPaidIn();

                        if(!paidInStr.isEmpty()){
                            double paidIn = Double.parseDouble(t.getPaidIn());
                            totalIncome += paidIn;
                            totalIncomeByCat += paidIn;
                        }

                    }

                    if(totalIncomeByCat != 0){
                        catIncomeMap.put(key, totalIncomeByCat);
                    }

                }


                for(String key : catIncomeMap.keySet()){
                    System.out.println(key + " " + catIncomeMap.get(key));
                    double val = catIncomeMap.get(key);
                    float val2 = (float) val;
                    entries.add(new PieEntry( val2, key, getResources().getDrawable(R.drawable.star)));

                    System.out.println("-----------for key: " + key + ", double is: " + val + " , float is: " +  val2);
                }


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

                for(int c : DARK_COLOURS_FOR_WHITE_TEXT)
                    colors.add(c);

                for (int c : ColorTemplate.LIBERTY_COLORS)
                    colors.add(c);

                for (int c : ColorTemplate.PASTEL_COLORS)
                    colors.add(c);

                colors.add(ColorTemplate.getHoloBlue());

                dataSet.setColors(colors);
                dataSet.setSelectionShift(12f);




                PieData data = new PieData(dataSet);
                data.setValueFormatter(valueFormatter);
                data.setValueTextSize(11f);
                data.setValueTextColor(Color.WHITE);
                //data.setValueTypeface(tfLight);

                DecimalFormat df = new DecimalFormat("0.00");
                pieChartIncomeSources.setCenterText(generateCenterSpannableText(df.format(totalIncome)));

                pieChartIncomeSources.setData(data);


                // undo all highlights
                pieChartIncomeSources.highlightValues(null);

                pieChartIncomeSources.invalidate();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

    private void displayPieChartSpendingByCategory(){

        //piechart

        pieChartSpendingByCategory = getView().findViewById(R.id.pie_chart);
        pieChartSpendingByCategory.setUsePercentValues(false);
        pieChartSpendingByCategory.getDescription().setEnabled(false);


        pieChartSpendingByCategory.setExtraOffsets(5, 0, 5, 0);

        pieChartSpendingByCategory.setDragDecelerationFrictionCoef(0.95f);

        //pieChart.setCenterTextTypeface(tfLight);
//        pieChart.setCenterText(generateCenterSpannableText());

        pieChartSpendingByCategory.setDrawHoleEnabled(true);
        //pieChart.setHoleColor(Color.WHITE);

        pieChartSpendingByCategory.setHoleColor(R.color.md_theme_dark_background);
        pieChartSpendingByCategory.setTransparentCircleColor(R.color.md_theme_dark_background);
        pieChartSpendingByCategory.setTransparentCircleAlpha(110);

        pieChartSpendingByCategory.setHoleRadius(35f);
        pieChartSpendingByCategory.setTransparentCircleRadius(40f);

        pieChartSpendingByCategory.setDrawCenterText(true);

        pieChartSpendingByCategory.setRotationAngle(0);
        // enable rotation of the chart by touch
        pieChartSpendingByCategory.setRotationEnabled(true);
        pieChartSpendingByCategory.setHighlightPerTapEnabled(true);

        pieChartSpendingByCategory.setDrawEntryLabels(false);


        // pieChart.setUnit(" €");
        // chart.setDrawUnitsInChart(true);

        // add a selection listener
        pieChartSpendingByCategory.setOnChartValueSelectedListener(this);


        //pieChartSpendingByCategory.animateY(1400, Easing.EaseInOutQuad);
        // chart.spin(2000, 0, 360);

        Legend l = pieChartSpendingByCategory.getLegend();
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
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                l.setTextColor(Color.BLACK);
                break;
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                break;
        }

        l.setTextSize(12f);



        // entry label styling
        pieChartSpendingByCategory.setEntryLabelColor(Color.WHITE);
        //pieChart.setEntryLabelTypeface(tfRegular);
        pieChartSpendingByCategory.setEntryLabelTextSize(12f);



        setPieChartDataSpendingByCategory(new MoneyValueFormatter());

    }

    private void setPieChartDataSpendingByCategory(ValueFormatter valueFormatter) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        DatabaseReference mTransactionRef = mRootRef.child("Transaction");

        mTransactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ArrayList<Transaction> listOfTransactionsWithoutCategory = new ArrayList<>();


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
                                ArrayList<Transaction> newListOfTransactionByCat = new ArrayList<>();
                                newListOfTransactionByCat.add(transaction);
                                transactionCatMap.put(category, newListOfTransactionByCat);
                            }
                        }
                        else{

                            listOfTransactionsWithoutCategory.add(transaction);
                            transactionCatMap.put("other", listOfTransactionsWithoutCategory);
                        }

                    }
                }

                Map<String, Double> catSpendingMap = new HashMap<>();
                double totalSpending = 0;
                for(String key : transactionCatMap.keySet()){
                    double totalSpendingByCat = 0;
                    for(Transaction t: transactionCatMap.get(key)){
                        String paidOutStr = t.getPaidOut();

                        if(!paidOutStr.isEmpty()){
                            double paidOut = Double.parseDouble(t.getPaidOut());
                            totalSpending += paidOut;
                            totalSpendingByCat += paidOut;
                        }

                    }

                    if(totalSpendingByCat != 0){
                        catSpendingMap.put(key, totalSpendingByCat);
                    }

                }


                for(String key : catSpendingMap.keySet()){
                    System.out.println(key + " " + catSpendingMap.get(key));
                    double val = catSpendingMap.get(key);
                    entries.add(new PieEntry((float) (val), key, getResources().getDrawable(R.drawable.star)));

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
                    for(int c : DARK_COLOURS_FOR_WHITE_TEXT)
                        colors.add(c);

                    for (int c : ColorTemplate.LIBERTY_COLORS)
                        colors.add(c);

                    for (int c : ColorTemplate.PASTEL_COLORS)
                        colors.add(c);

                    colors.add(ColorTemplate.getHoloBlue());

                    dataSet.setColors(colors);
                    dataSet.setSelectionShift(12f);




                    PieData data = new PieData(dataSet);
                    data.setValueFormatter(valueFormatter);
                    data.setValueTextSize(11f);
                    data.setValueTextColor(Color.WHITE);

                    //data.setValueTypeface(tfLight);
                    DecimalFormat df = new DecimalFormat("0.00");

                    pieChartSpendingByCategory.setCenterText(generateCenterSpannableText(df.format(totalSpending)));

                    pieChartSpendingByCategory.setData(data);


                    // undo all highlights
                    pieChartSpendingByCategory.highlightValues(null);

                    pieChartSpendingByCategory.invalidate();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void displayLineChartOverview(){

        {   // // Chart Style // //
            lineChart = getView().findViewById(R.id.chart1);

            // background color
            lineChart.setBackgroundColor(Color.WHITE);

            lineChart.setBackgroundColor(Color.TRANSPARENT);

            // disable description text
            lineChart.getDescription().setEnabled(false);

            // enable touch gestures
            lineChart.setTouchEnabled(true);

            // set listeners
            lineChart.setOnChartValueSelectedListener(this);
            lineChart.setDrawGridBackground(false);

            // create marker to display box when values are selected
            //MyMarkerView mv = new MyMarkerView(getActivity(), R.layout.custom_marker_view);

            // Set the marker to the chart
            //mv.setChartView(chart);
            //chart.setMarker(mv);

            // enable scaling and dragging
            lineChart.setDragEnabled(true);
            //chart.setScaleEnabled(true);//-----------------
            lineChart.setScaleXEnabled(true);
            lineChart.setScaleYEnabled(true);

            // force pinch zoom along both axis
            lineChart.setPinchZoom(true);
        }

        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);


            xAxis.setValueFormatter(new MyXAxisValueFormatter());
            xAxis.setDrawGridLines(false);
            // vertical grid lines
            //xAxis.enableGridDashedLine(10f, 10f, 0f);

            xAxis.setLabelCount(4);


        }

        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = lineChart.getAxisLeft();

            // disable dual axis (only use LEFT axis)
            lineChart.getAxisRight().setEnabled(false);

            yAxis.setDrawGridLines(false);
            // horizontal grid lines
            //yAxis.enableGridDashedLine(10f, 10f, 0f);

            // axis range
//            yAxis.setAxisMaximum(2535f);
//            yAxis.setAxisMinimum(0f);
        }

        int nightModeFlags = getView().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                xAxis.setTextColor(Color.WHITE);
                yAxis.setTextColor(Color.WHITE);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                xAxis.setTextColor(Color.BLACK);
                yAxis.setTextColor(Color.BLACK);
                break;
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                break;
        }

        //setData(45, 180);
        setDataForLineChart();

        // draw points over time
        lineChart.animateX(1500);

        // get the legend (only possible after setting data)
        Legend l = lineChart.getLegend();

        // draw legend entries as lines
        l.setForm(Legend.LegendForm.LINE);
        //l.setForm(Legend.LegendForm.CIRCLE);

        l.setEnabled(false);

    }

    private void setDataForLineChart() {

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

                if (lineChart.getData() != null &&
                        lineChart.getData().getDataSetCount() > 0) {
                    set1 = (LineDataSet) lineChart.getData().getDataSetByIndex(0);

                    set1.setValues(values);
                    set1.notifyDataSetChanged();
                    lineChart.getData().notifyDataChanged();
                    lineChart.notifyDataSetChanged();
                } else {
                    // create a dataset and give it a type
                    set1 = new LineDataSet(values, "DataSet 1");

                    set1.setDrawIcons(false);

                    // draw dashed line
                    //set1.enableDashedLine(10f, 5f, 0f);

                    // black lines and points


                    int nightModeFlags = getView().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

                    switch (nightModeFlags) {
                        case Configuration.UI_MODE_NIGHT_YES:
                            set1.setColor(Color.WHITE);
                            set1.setCircleColor(Color.WHITE);
                            break;
                        case Configuration.UI_MODE_NIGHT_NO:
                            set1.setColor(Color.BLACK);
                            set1.setCircleColor(Color.BLACK);
                            break;
                        case Configuration.UI_MODE_NIGHT_UNDEFINED:
                            break;
                    }

                    // line thickness and point size
                    set1.setLineWidth(2f);
                    set1.setCircleRadius(5f);

                    ((LineDataSet) set1).setCircleColors(ColorTemplate.VORDIPLOM_COLORS);

                    // draw points as solid circles
                    set1.setDrawCircleHole(true);

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
                            return lineChart.getAxisLeft().getAxisMinimum();
                        }
                    });

                    // set color of filled area
                    if (Utils.getSDKInt() >= 18) {
                        // drawables only supported on api level 18 and above
//                        Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.fade_red);
//                        set1.setFillDrawable(drawable);
                    } else {
                        set1.setFillColor(Color.BLACK);
                    }

                    ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                    dataSets.add(set1); // add the data sets

                    // create a data object with the data sets
                    LineData data = new LineData(dataSets);

                    // set data
                    lineChart.setData( data);
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

    @Override
    public void onResume() {
        super.onResume();

        pieChartSpendingByCategory.animateY(500, Easing.EaseInOutQuad);

        pieChartIncomeSources.animateY(500, Easing.EaseInOutQuad);
        weeklyBarChart.animateY(500);

        lineChart.animateY(500, Easing.EaseInOutQuad);

    }
}