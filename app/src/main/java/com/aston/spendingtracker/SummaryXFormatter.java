package com.aston.spendingtracker;

import com.aston.spendingtracker.entity.Transaction;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class SummaryXFormatter extends ValueFormatter {

    @Override
    public String getFormattedValue(float value) {
        String result = "";
        if(value == 0f){
            result = "Income";
        }
        else if (value == 1f){
            result = "Spending";
        }
        return result;

    }
}
