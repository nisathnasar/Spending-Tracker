package com.aston.spendingtracker;

import com.aston.spendingtracker.entity.Transaction;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;

public class MoneyValueFormatter extends ValueFormatter {

    @Override
    public String getFormattedValue(float value) {
        DecimalFormat df = new DecimalFormat("0.00");
        return "£"+df.format(value);
    }
}
