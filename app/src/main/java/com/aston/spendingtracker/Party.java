package com.aston.spendingtracker;

import com.aston.spendingtracker.entity.Transaction;

import java.util.ArrayList;
import java.util.UUID;

public class Party {

    //private ArrayList<Transaction> listOfTransactions = new ArrayList<>();

    private String name;

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    private String uniqueID;


    public Party(String name) {

        //Firebase path cannot accept the following characters: '.', '#', '$', '[', or ']'
        String tmpName = name.replaceAll("\\.", "~");
        tmpName = tmpName.replaceAll("#", "~");
        tmpName = tmpName.replaceAll("\\$", "~");
        tmpName = tmpName.replaceAll("\\[", "~");
        tmpName = tmpName.replaceAll("]", "~");

        this.name = tmpName;

        uniqueID = UUID.randomUUID().toString();

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

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
