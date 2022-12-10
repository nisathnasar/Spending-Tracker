package com.aston.spendingtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ViewTransaction extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_transaction);

        Intent intent = getIntent();
        String message = intent.getStringExtra("detail");

        TextView detailsTV = findViewById(R.id.details_tv);
        detailsTV.setText(message);
    }
}