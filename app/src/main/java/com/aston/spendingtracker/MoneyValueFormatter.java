package com.aston.spendingtracker;

import com.aston.spendingtracker.entity.Transaction;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class MoneyValueFormatter extends ValueFormatter {

    @Override
    public String getFormattedValue(float value) {
        return "£"+value;
    }
}
