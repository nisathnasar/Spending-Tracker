package com.aston.spendingtracker;


import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.aston.spendingtracker.entity.Transaction;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Analytics {

    Context context;

    public Analytics(Context context){
        this.context = context;
    }

    public ArrayList<Entry> getDataFromDB() {

        ArrayList<Entry> values = new ArrayList<>();

//        for (int i = 0; i < count; i++) {
//
//            float val = (float) (Math.random() * range) - 30;
//            values.add(new Entry(i, val, getResources().getDrawable(R.drawable.star)));
//
//        }



        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        DatabaseReference mTransactionRef = mRootRef.child("Transaction");


        mTransactionRef.addValueEventListener(new ValueEventListener() {
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

                        values.add(new Entry(x, y, context.getResources().getDrawable(R.drawable.star)));

//                        values.add(new Entry(i, y));

                        //Collections.sort(values, new EntryXComparator());

                        i++;
                        //System.out.println("x = " + x + "   , y = " + y + "    : parsed: " + Transaction.getParsedDateInMilliseconds(x));


                    }

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

    return values;

    }

}
