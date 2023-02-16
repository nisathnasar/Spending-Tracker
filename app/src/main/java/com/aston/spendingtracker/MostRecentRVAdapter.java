package com.aston.spendingtracker;

import android.content.Context;

import com.aston.spendingtracker.entity.Transaction;

import java.util.LinkedList;

public class MostRecentRVAdapter extends RecyclerViewAdapter{

    private int limit = 4;
    LinkedList<Transaction> transactionList;

    public MostRecentRVAdapter(Context context, LinkedList<Transaction> transactionList) {
        super(context, transactionList);
        this.transactionList = transactionList;
    }

    @Override
    public int getItemCount() {

        if(transactionList.size() > limit){
            return limit;
        }
        else {
            return transactionList.size();
        }
    }
}
