package com.aston.spendingtracker;

import java.util.Date;

public class Transaction {
    String dateOfTransaction;
    String paymentType;
    String paymentDetails;
    String paidOut;
    String painIn;
    String balance;

    Transaction(){

    }

    Transaction(String dateOfTransaction, String paymentType, String paymentDetails, String paidOut, String painIn, String balance){
        this.dateOfTransaction = dateOfTransaction;
        this.paymentType = paymentType;
        this.paymentDetails = paymentDetails;
        this.paidOut = paidOut;
        this.painIn = painIn;
        this.balance = balance;
    }


}
