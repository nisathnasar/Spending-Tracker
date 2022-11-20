package com.aston.spendingtracker;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aston.spendingtracker.entity.Transaction;

import java.util.LinkedList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.WordViewHolder> {

    //private final LinkedList<String> mWordList;
    private final LinkedList<Transaction> mTransactionList;
    private LayoutInflater mInflater;
    Context context;

    public RecyclerViewAdapter(Context context, LinkedList<Transaction> transactionList){
        mInflater = LayoutInflater.from(context);
        //this.mWordList = wordList;
        this.mTransactionList = transactionList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new WordViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.WordViewHolder holder, int position) {
        //String mCurrent = mWordList.get(position);
        //holder.detailsView.setText(mCurrent);
        String paidOut = mTransactionList.get(position).getPaidOut().trim();
        String paidIn = mTransactionList.get(position).getPainIn().trim();
        String mtransaction;

        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if(!paidOut.equals("")){
            mtransaction = "-£" + paidOut;

            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    holder.linearLayoutRV.setBackgroundColor(Color.parseColor("#5c859c"));
                    break;
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    break;
            }

        } else{
            mtransaction = "+£" + paidIn;

            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    holder.linearLayoutRV.setBackgroundColor(Color.parseColor("#53b075"));
                    break;
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    break;
            }
        }
        holder.transactionView.setText(mtransaction);

        String mDetails = mTransactionList.get(position).getPaymentDetails();
        holder.detailsView.setText(mDetails);

        String mBalance = "£" + mTransactionList.get(position).getBalance();
        holder.balanceView.setText(mBalance);

        String mDate = mTransactionList.get(position).getDateOfTransaction();
        holder.dateView.setText(mDate);

    }

    @Override
    public int getItemCount() {
        return mTransactionList.size();
    }

    class WordViewHolder extends RecyclerView.ViewHolder{
        public final TextView detailsView, transactionView, balanceView, dateView;
        public LinearLayout linearLayoutRV;
        final RecyclerViewAdapter mAdapter;
        public WordViewHolder(View itemView, RecyclerViewAdapter adapter) {
            super(itemView);
            detailsView = itemView.findViewById(R.id.details_tv);
            transactionView = itemView.findViewById(R.id.transaction_tv);
            balanceView = itemView.findViewById(R.id.balance_tv);
            dateView = itemView.findViewById(R.id.date_tv);
            linearLayoutRV = itemView.findViewById(R.id.linearLayoutRV);
            this.mAdapter = adapter;
        }
    }




}
