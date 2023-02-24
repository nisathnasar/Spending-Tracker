package com.aston.spendingtracker.axisformatters;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;

public class PercentageValueFormatter extends ValueFormatter {

    @Override
    public String getFormattedValue(float value) {
        DecimalFormat df = new DecimalFormat("0");
        return df.format(value) + "%";
    }
}
