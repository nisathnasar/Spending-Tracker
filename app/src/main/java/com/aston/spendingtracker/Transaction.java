package com.aston.spendingtracker;

import java.util.Date;

public class Transaction {
    String dateOfTransaction;
    String paymentType;
    String paymentDetails;
    String paidOut;
    String painIn;

    public String getDateOfTransaction() {
        return dateOfTransaction;
    }

    public String getPaymentType() {
        return paymentType;
    }


    public String getPaymentDetails() {
        return paymentDetails;
    }



    public String getPaidOut() {
        return paidOut;
    }



    public String getPainIn() {
        return painIn;
    }


    public String getBalance() {
        return balance;
    }


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

    @Override
    public String toString() {
        return "Transaction{" +
                "dateOfTransaction='" + dateOfTransaction + '\'' +
                ", paymentType='" + paymentType + '\'' +
                ", paymentDetails='" + paymentDetails + '\'' +
                ", paidOut='" + paidOut + '\'' +
                ", painIn='" + painIn + '\'' +
                ", balance='" + balance + '\'' +
                '}';
    }
}
