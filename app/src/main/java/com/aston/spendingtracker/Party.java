package com.aston.spendingtracker;

import com.aston.spendingtracker.entity.Transaction;

import java.util.ArrayList;

public class Party {

    private ArrayList<Transaction> listOfTransactions;


    public Party(String name) {

        //Firebase path cannot accept the following characters: '.', '#', '$', '[', or ']'
        String tmpName = name.replaceAll("\\.", "~");
        tmpName = tmpName.replaceAll("#", "~");
        tmpName = tmpName.replaceAll("\\$", "~");
        tmpName = tmpName.replaceAll("\\[", "~");
        tmpName = tmpName.replaceAll("]", "~");

        this.name = tmpName;


        listOfTransactions = new ArrayList<>();
    }

    public Party(){

    }

    public static String SanitiseName(String name){
        String tmpName = name.replaceAll("\\.", "~");
        tmpName = tmpName.replaceAll("#", "~");
        tmpName = tmpName.replaceAll("\\$", "~");
        tmpName = tmpName.replaceAll("\\[", "~");
        tmpName = tmpName.replaceAll("]", "~");
        return tmpName;
    }

    public void addTransaction(Transaction transaction){
        listOfTransactions.add(transaction);
    }



    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Transaction> getListOfTransactions() {
        return listOfTransactions;
    }

    public void setListOfTransactions(ArrayList<Transaction> listOfTransactions) {
        this.listOfTransactions = listOfTransactions;
    }



    private String name;


}
