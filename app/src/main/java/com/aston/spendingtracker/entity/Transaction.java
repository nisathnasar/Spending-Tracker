package com.aston.spendingtracker.entity;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private String paidIn;
    private String balance;
    private float dateInMilliseconds;
    private String partyID;
    private String category;

    private static int TRANSACTIONCOUNT;



    public Transaction(String dateOfTransaction, String paymentType, String paymentDetails, String paidOut, String paidIn, String balance, String partyID) throws ParseException {
        this.dateOfTransaction = dateOfTransaction;
        this.paymentType = paymentType;
        this.paymentDetails = paymentDetails;
        this.paidOut = paidOut;
        this.paidIn = paidIn;
        this.balance = balance;
        this.partyID = partyID;
        transactionID = TRANSACTIONCOUNT;
        TRANSACTIONCOUNT++;

        category = "";
    }

    public Transaction(){

    }

    public void parseDBDate(){
        String[] dateElements = dateOfTransaction.split("-");
        //date-month-year
        dateOfTransaction = dateElements[2] +"-"+ dateElements[1] +"-"+ dateElements[0];
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

    public float getDateInMilliseconds(){

        String[] elements;
        if(dateOfTransaction.contains("-")){
            //System.out.println("str contains dash: " + dateOfTransaction);
            elements = dateOfTransaction.split("-");
        } else{
            //System.out.println("str contains space: " + dateOfTransaction);
            elements = dateOfTransaction.split(" ");
        }

        int day = Integer.parseInt(elements[0]);
        int month = formatMonth(elements[1]);
        int year = Integer.parseInt(elements[2]);

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);

        return cal.getTimeInMillis();
    }

    public void setDateInMilliseconds(float str){
        dateInMilliseconds = str;

    }

    public static String getParsedDateInMilliseconds(float timeStamp){

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((long) timeStamp);

        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        Format f = new SimpleDateFormat("dd MMM yy");
        String strDate = f.format(calendar.getTime());

        String str = mDay + " " + mMonth + " " + mYear;

        return strDate;
        //return Transaction.parseDBMonth(str, " ");
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "dateOfTransaction='" + dateOfTransaction + '\'' +
                ", paymentType='" + paymentType + '\'' +
                ", paymentDetails='" + paymentDetails + '\'' +
                ", paidOut='" + paidOut + '\'' +
                ", painIn='" + paidIn + '\'' +
                ", balance='" + balance + '\'' +
                '}';
    }


    /**
     * return int month for given 3 letter string i.e. "feb" returns 2
     *
     * @param month 3 letter string
     * @return integer 0 for jan
     */
    public static int formatMonth(String month) {
        int result=0;
        switch (month.toLowerCase()) {
            case "jan":
                result = 1;
                break;
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

        }
        return result;
    }


    /**
     *
     */
    public void parseDBMonth() {
        String parsedDBDate = dateOfTransaction;
        String[] dateElements = parsedDBDate.trim().split("-");
        int month = Integer.parseInt(dateElements[1].trim());
        String result="";
        switch (month) {
            case 1:
                result = "January";
                break;
            case 2:
                result = "February";
                break;
            case 3:
                result = "March";
                break;
            case 4:
                result = "April";
                break;
            case 5:
                result = "May";
                break;
            case 6:
                result = "June";
                break;
            case 7:
                result = "July";
                break;
            case 8:
                result = "August";
                break;
            case 9:
                result = "September";
                break;
            case 10:
                result = "October";
                break;
            case 11:
                result = "November";
                break;
            case 12:
                result = "December";
                break;
        }
        dateOfTransaction = dateElements[0] + " " + result + " " + dateElements[2];
    }

    /**
     * TODO: implement a method to get current year.
     */
    public void parseDBYear() {
        String parsedDBDate = dateOfTransaction;
        String[] dateElements = parsedDBDate.trim().split(" ");
        //dateElements = parsedDBDate.trim().split("-");
        int year = Integer.parseInt(dateElements[2].trim());
        String result="";

        if(year>=0 && year<=23){
            result="20"+year;
        }else{
            result="19"+year;
        }

        dateOfTransaction = dateElements[0] + " " + dateElements[1] + " " + result;

    }


    public static String parseDBMonth(String parsedDBDate, String splitText) {
        //String parsedDBDate = dateOfTransaction;
        String[] dateElements = parsedDBDate.trim().split(splitText);
        int month = Integer.parseInt(dateElements[1].trim());
        String result="";
        switch (month) {
            case 1:
                result = "jan";
                break;
            case 2:
                result = "feb";
                break;
            case 3:
                result = "mar";
                break;
            case 4:
                result = "apr";
                break;
            case 5:
                result = "may";
                break;
            case 6:
                result = "jun";
                break;
            case 7:
                result = "jul";
                break;
            case 8:
                result = "aug";
                break;
            case 9:
                result = "sep";
                break;
            case 10:
                result = "oct";
                break;
            case 11:
                result = "nov";
                break;
            case 12:
                result = "dec";
                break;
        }
        return dateElements[0] + splitText + result + splitText + dateElements[2];
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

    public void setPaidIn(String paidIn) {
        this.paidIn = paidIn;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

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

    public String getPaidIn() {
        return paidIn;
    }

    public String getBalance() {
        return balance;
    }

    public String getPartyID() {
        return partyID;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    public void setPartyID(String partyID) {
        this.partyID = partyID;
    }

}
