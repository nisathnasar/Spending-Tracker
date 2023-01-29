package com.aston.spendingtracker;

import com.aston.spendingtracker.entity.Transaction;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class MyXAxisValueFormatter extends ValueFormatter {


    public String getXValue(String dateInMillisecons, int index, ViewPortHandler viewPortHandler) {
        try {

//            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");
//            return sdf.format(new Date(Long.parseLong(dateInMillisecons)));

        } catch (Exception e) {

            return  dateInMillisecons;
        }
        return "none";
    }

    @Override
    public String getFormattedValue(float value) {
        return Transaction.getParsedDateInMilliseconds(value);
        //return String.valueOf(value);

    }

}