package com.aston.spendingtracker;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aston.spendingtracker.entity.Transaction;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.ToIntBiFunction;

public class AddToCategoryFragment extends DialogFragment {

    Context context;
    int selectedItem = -1;

    String detail;
    String categoryStr;

    ArrayList<String> categoriesList;

    public void setDetail(String detail){
        this.detail = detail;
    }

    public void setCategoriesList(ArrayList<String> categoriesList){
        this.categoriesList = categoriesList;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        //selectedItems = new ArrayList();  // Where we track the selected items
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());

        // Set the dialog title
        builder.setTitle("Select a category");

        final EditText input = new EditText(getActivity());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setHint("Type new category (Select Other)");
        input.setMaxLines(1);
        builder.setView(input);

        CharSequence[] categoryListCharSeq = categoriesList.toArray(new CharSequence[categoriesList.size()]);



        // Specify the list array, the items to be selected by default (null for none),
        // and the listener through which to receive callbacks when items are selected
        builder.setSingleChoiceItems(categoryListCharSeq, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //selectedItems.add(which);
                            selectedItem = which;

                        }

                    })
                // Set the action buttons
                .setPositiveButton("apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the selectedItems results somewhere
                        // or return them to the component that opened the dialog

                        if(selectedItem == -1){
                            selectedItem = 0;
                        }

                        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        DatabaseReference mTransactionRef = mRootRef.child("Transaction");
                        DatabaseReference mCategoriesRef = mRootRef.child("Categories");

                        categoryStr = categoriesList.get(selectedItem);

                        //categoryStr = getResources().getStringArray(R.array.categories)[selectedItem];

                        System.out.println(categoryStr + " ---------- " + input.getText().toString());

                        boolean newItem = false;

                        if(categoryStr.equalsIgnoreCase("other")){
                            categoryStr = input.getText().toString();
                            newItem = true;

                            if(categoryStr.isEmpty()){
                                categoryStr = "Unspecified";
                                newItem = false;
                            }
                        }

                        Toast.makeText(getActivity(), categoryStr, Toast.LENGTH_SHORT).show();

                        //check if the externally passed information is null or empty because Android recreates fragments sometimes, i.e. during a crash and will not contain the arguments.
                        if(detail != null || !detail.isEmpty()){



                            mTransactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                        for(DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()){
                                            Transaction transaction = dataSnapshot2.getValue(Transaction.class);


                                            if(transaction.getPaymentDetails().equals(detail)){

                                                transaction.setCategory(categoryStr);

//                                                Map<String, Object> map2 = new HashMap<>();
//                                                map2.put(dataSnapshot2.getKey(), transaction);
//
//                                                Map<String, Object> map = new HashMap<>();
//                                                map.put(dataSnapshot.getKey(), map2);


                                                mTransactionRef.child(transaction.getDateOfTransaction()).child(dataSnapshot2.getKey()).setValue(transaction);

                                                //mTransactionRef.updateChildren(map);

                                            }


                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                            //add new category to database

                            if(newItem){
                                //DatabaseReference mCategoriesRef = mRootRef.child("Categories");
                                mCategoriesRef.child(UUID.randomUUID().toString()).setValue(new Category(categoryStr));
                            }


                        }
                        else{
                            Toast.makeText(getActivity(), "Category not added, please try again later.", Toast.LENGTH_SHORT).show();
                        }



                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        return builder.create();
    }


}
