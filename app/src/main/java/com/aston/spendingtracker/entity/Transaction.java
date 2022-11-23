package com.aston.spendingtracker.entity;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

public class Transaction {
    private int transactionID;
    private String dateOfTransaction;
    //private Timestamp dateOfTransaction;
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

    public Transaction(String dateOfTransaction, String paymentType, String paymentDetails, String paidOut, String painIn, String balance) throws ParseException {
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

    public static void sortTransactionListByDate(LinkedList<Transaction> tList) throws ParseException {

        for(Transaction t : tList){

            //System.out.println(getDateObjectFromString(t.getDateOfTransaction()));
        }
    }

    public static Date getDateObjectFromString(String date) throws ParseException {
        String[] elements = date.split("-");

        int day = Integer.parseInt(elements[0]);
        int month = formatMonth(elements[1]);
        int year = Integer.parseInt(elements[2]);

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH);
        //date = elements[0] +"-"+ String.valueOf(month)  +"-"+ elements[2] ;
        return sdf.parse(date);

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
    private static int formatMonth(String month) {
        int result;
        switch (month.toLowerCase(Locale.ROOT)) {
            case "jan":
                result = 1;
            case "feb":
                result = 2;
                break;
            case "mar":
                result = 3;
                break;
            case "apr":
                result = 4;
                break;
            case "may":
                result = 5;
                break;
            case "jun":
                result = 6;
                break;
            case "jul":
                result = 7;
                break;
            case "aug":
                result = 8;
                break;
            case "sep":
                result = 9;
                break;
            case "oct":
                result = 10;
                break;
            case "nov":
                result = 11;
                break;
            case "dec":
                result = 12;
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

    public void setDateOfTransaction(String dateOfTransaction) throws ParseException {
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
