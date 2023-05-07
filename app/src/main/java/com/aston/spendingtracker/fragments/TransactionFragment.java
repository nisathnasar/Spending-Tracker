package com.aston.spendingtracker.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.aston.spendingtracker.ItemClickListener;
import com.aston.spendingtracker.R;
import com.aston.spendingtracker.RecyclerViewAdapter;
import com.aston.spendingtracker.ViewTransaction;
import com.aston.spendingtracker.entity.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TransactionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransactionFragment extends Fragment implements ItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;

    LinkedList<Transaction> transactionList = new LinkedList<>();

    EditText filterET;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TransactionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TransactionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TransactionFragment newInstance(String param1, String param2) {
        TransactionFragment fragment = new TransactionFragment();
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
        return inflater.inflate(R.layout.fragment_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mRecyclerView = getView().findViewById(R.id.recyclerview);

        getView().findViewById(R.id.frame_layout).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.welcome_msg_tv).setVisibility(View.GONE);

        TreeMap<Timestamp, Transaction> transactionsMap = new TreeMap<>();

        // Create an adapter and supply the data to be displayed.
        mAdapter = new RecyclerViewAdapter(getActivity(), transactionList);

        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);


        mAdapter.setClickListener(this::onClick); // Bind the listener


        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        DatabaseReference mTransactionRef = mRootRef.child("Transaction");

        mTransactionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float maximumBal = 0;
                transactionList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    for(DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()){
                        Transaction transaction = dataSnapshot2.getValue(Transaction.class);
                        transaction.parseDBDate();
                        transaction.parseDBMonth();
                        transaction.parseDBYear();
                        transactionList.add(transaction);
                        if(maximumBal < Float.valueOf(transaction.getBalance())){
                            maximumBal = Float.valueOf(transaction.getBalance());
                        }
                    }
                }

                //update max bal on db:
                mRootRef.child("MaximumBalance").setValue(maximumBal);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        // yourMethod();

                        if(transactionList.size()==0){

                            try{
                                getView().findViewById(R.id.frame_layout).setVisibility(View.GONE);
                                getView().findViewById(R.id.welcome_msg_tv).setVisibility(View.VISIBLE);
                            }
                            catch (Exception e){
                            }

                        } else{
                            try{
                                getView().findViewById(R.id.frame_layout).setVisibility(View.VISIBLE);
                                getView().findViewById(R.id.welcome_msg_tv).setVisibility(View.GONE);
                            }
                            catch(Exception e){

                            }

                        }
                    }
                }, 3000);



                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("db data retrieval fail: " + error);
            }
        });



        filterET = getView().findViewById(R.id.filter_et);

        filterET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
            }
        });

//        if(transactionList.size()==0){
//            getView().findViewById(R.id.frame_layout).setVisibility(View.GONE);
//            getView().findViewById(R.id.welcome_msg_tv).setVisibility(View.VISIBLE);
//            //Button welcomeMsgUploadBtn = getView().findViewById(R.id.welcome_msg_upload_bt);
//            //welcomeMsgUploadBtn.setVisibility(View.VISIBLE);
//
//        }
    }

    private void filter(String text){
        if(!text.trim().isEmpty()){
            LinkedList<Transaction> filteredTransactionList = new LinkedList<>();

            for(Transaction t : transactionList){
                if(t.getPaymentDetails().toLowerCase().contains(text.toLowerCase())){
                    filteredTransactionList.add(t);
                }
            }

            mAdapter.filterList(filteredTransactionList);
        }else{
            mAdapter.filterList(transactionList);
        }

    }

    @Override
    public void onClick(View view, int position) {
        // The onClick implementation of the RecyclerView item click

        Intent i = new Intent(getActivity(), ViewTransaction.class);
        i.putExtra("detail", transactionList.get(position).getPaymentDetails());
        i.putExtra("type", transactionList.get(position).getPaymentType());
        i.putExtra("date", transactionList.get(position).getDateOfTransaction());
        i.putExtra("category", transactionList.get(position).getCategory());
        i.putExtra("balance", transactionList.get(position).getBalance());
        i.putExtra("paidOut", transactionList.get(position).getPaidOut());
        i.putExtra("paidIn", transactionList.get(position).getPaidIn());

        startActivity(i);

    }

}

