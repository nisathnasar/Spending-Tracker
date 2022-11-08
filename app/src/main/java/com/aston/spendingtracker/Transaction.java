package com.aston.spendingtracker;

import java.util.Date;

public class Transaction {
    Date dateOfTransaction;
    String paymentType;
    String paymentDetails;
    double paidOut;
    double painIn;
    double balance;

    Transaction(){

    }

    Transaction(Date dateOfTransaction, String paymentType, String paymentDetails, double paidOut, double painIn, double balance){
        this.dateOfTransaction = dateOfTransaction;
        this.paymentType = paymentType;
        this.paymentDetails = paymentDetails;
        this.paidOut = paidOut;
        this.painIn = painIn;
        this.balance = balance;
    }


}
