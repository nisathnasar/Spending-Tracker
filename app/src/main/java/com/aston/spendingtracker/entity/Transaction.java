package com.aston.spendingtracker.entity;

import java.util.Date;
import java.util.Locale;

public class Transaction {
    private int transactionID;
    private String dateOfTransaction;
    private String paymentType;
    private String paymentDetails;
    private String paidOut;
    private String painIn;
    private String balance;
    private static int TRANSACTIONCOUNT;

    public int getTransactionID(){
        return transactionID;
    }

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

    public Transaction(String dateOfTransaction, String paymentType, String paymentDetails, String paidOut, String painIn, String balance){
        this.dateOfTransaction = dateOfTransaction;
        this.paymentType = paymentType;
        this.paymentDetails = paymentDetails;
        this.paidOut = paidOut;
        this.painIn = painIn;
        this.balance = balance;
        transactionID = TRANSACTIONCOUNT;
        TRANSACTIONCOUNT++;

    }

    public Transaction(){

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


    /**
     * return int month for given 3 letter string i.e. "feb" returns 2
     *
     * @param month 3 letter string
     * @return integer 0 for jan
     */
    private int formatMonth(String month) {
        int result;
        switch (month.toLowerCase(Locale.ROOT)) {
            case "feb":
                result = 1;
                break;
            case "mar":
                result = 2;
                break;
            case "apr":
                result = 3;
                break;
            case "may":
                result = 4;
                break;
            case "jun":
                result = 5;
                break;
            case "jul":
                result = 6;
                break;
            case "aug":
                result = 7;
                break;
            case "sep":
                result = 8;
                break;
            case "oct":
                result = 9;
                break;
            case "nov":
                result = 10;
                break;
            case "dec":
                result = 11;
                break;
            default:
                result = 0;
                break;
        }
        return result;
    }


    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    public void setDateOfTransaction(String dateOfTransaction) {
        this.dateOfTransaction = dateOfTransaction;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public void setPaymentDetails(String paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public void setPaidOut(String paidOut) {
        this.paidOut = paidOut;
    }

    public void setPainIn(String painIn) {
        this.painIn = painIn;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }
}
