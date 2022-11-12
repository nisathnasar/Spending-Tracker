package com.aston.spendingtracker;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.multipdf.Splitter;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class PDFProcessor {

    private static final String TAG = "PDFProcessor";

    public List<Integer> rowIndexListToMergeWith;
    public boolean paymentOut;
    public String date = "";
    private FileDescriptor fileDescriptor;
    private LinkedList<String> listTransaction;
    private Transaction transaction;
    private String transactionDate, transactionPaymentType, transactionPaymentDetails, transactionPaidOut, transactionPaidIn, transactionBalance;
    private HSBCRegex hsbcRegex;

    public PDFProcessor(Context context, Uri pathURI) throws IOException {

        int numOfPagesToExtractFrom = 1;
        listTransaction = new LinkedList<>();
        hsbcRegex = new HSBCRegex();
        PDFBoxResourceLoader.init(context);
        ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(pathURI, "r");
        this.fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        InputStream fileStream = new FileInputStream(this.fileDescriptor);
        PDDocument document = PDDocument.load(fileStream);

//        PDDocument document = PDDocument.load(assetManager.open("sample_stmt.pdf"));
//        PyObject obj = pyobj.callAttr("extract_text");
//        String result = obj.toString();
//        FileWriter fw = new FileWriter(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/Created.csv");
//        FileWriter fw = new FileWriter(root.getAbsolutePath() + "/Created.csv");
//        Log.d("MainActivity.java", root.getAbsolutePath() + "/Created.csv");
//        write header
//        fw.write("Date, Type, Details, Pay Out, Pay In, Balance\n");

        activateSequence(document, numOfPagesToExtractFrom);
        parcelFileDescriptor.close();
    }

    private void activateSequence(PDDocument document, int numOfPagesToExtractFrom) throws IOException {

        Splitter splitter = new Splitter();
        List<PDDocument> splitPages = splitter.split(document);
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(splitPages.get(0)); // reads all of the page
        String[] lines = text.split("\\r?\\n"); // each line is broken into array

        List<String> rows = stringArrayToArrayList(lines); //change string array to arraylist

        // Remove the last non transaction lines: removes the first 30 lines by traversing in reverse
        for (int i = 29; i > 0; i--) {
            rows.remove(rows.size() - 1);
        }

        // Remove the first non transaction lines
        for (int i = 3; i >= 0; i--) {
            rows.remove(i);
        }

        rows = processLines(rows);

        for (String str : rows) {
            //fw.write(str+"\n"); //write to file
            listTransaction.addLast(str + "\n");
        }

        //Page 2
        if (numOfPagesToExtractFrom > 1) {
            String text2 = stripper.getText(splitPages.get(1));
            String[] lines2 = text2.split("\\r?\\n");
            List<String> rows2 = stringArrayToArrayList(lines2);

            /*
             * Remove the last irrelevant lines: removes the first 16 lines by traversing in reverse
             */
            for (int i = 16; i > 1; i--) {
                rows2.remove(rows2.remove(rows2.size() - 1));
            }
            /*
             * Remove the first useless lines
             */
            for (int i = 1; i >= 0; i--) {
                rows2.remove(i);
            }

            processLines(rows2);
            printList(rows2);

            //write to file
            for (String str : rows2) {
                //fw.write(str+"\n");
                //addTextView(str + "\n");
                listTransaction.addLast(str + "\n");
            }
        }

        //Page 3

        if (numOfPagesToExtractFrom > 2) {
            String text3 = stripper.getText(splitPages.get(2));
            String[] lines3 = text3.split("\\r?\\n");
            List<String> rows3 = stringArrayToArrayList(lines3);

            /*
             * Remove the last irrelevant lines: removes the first 16 lines by traversing in reverse
             */
            for (int i = 16; i > 1; i--) {
                rows3.remove(rows3.remove(rows3.size() - 1));
            }
            /*
             * Remove the first useless lines
             */
            for (int i = 1; i > -1; i--) {
                rows3.remove(i);
            }

            processLines(rows3);
            printList(rows3);

            //write to file
            for (String str : rows3) {
                //fw.write(str+"\n");
                //addTextView(str + "\n");
            }
        }

        document.close();
        //fw.close();

    }

    public LinkedList<String> getTransactionList() {
        return listTransaction;
    }

    public List<String> stringArrayToArrayList(String[] strArr) {
        List<String> result = new ArrayList<>();
        Collections.addAll(result, strArr);
        System.out.println("synthesised list: " + result);
        return result;
    }

    public void printList(List<String> list) {

        for (int i = 0; i < list.size(); i++) {
            System.out.println(i + ". " + list.get(i));
        }
    }


    /**
     * //System.out.println("10 aug 22 whatever d563 rfr".matches("^[0-9]{2}\\s[a-zA-Z]{3}\\s[0-9]{2}\\s[a-zA-Z0-9_-].*$"));
     * //System.out.println("STR@13:05 10.00".matches("[a-zA-Z0-9_-].*\\s[0-9]{1}[0-9]*\\.[0-9]{2}$"));
     * //System.out.println("STR@13:05 123,456,789.00".matches("[a-zA-Z0-9_-].*\\s[0-9]{1}[0-9]*,[0-9]{3}.*\\.[0-9]{2}$"));
     * //System.out.println("ererb, erbe, efr. 120,20.00". replaceAll(",", ""));
     */
    public List<String> processLines(List<String> rows) {

        rowIndexListToMergeWith = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {

            rows.set(i, rows.get(i).replaceAll(",", "")); //removes the commas in money values
            rows.set(i, rows.get(i).trim()); //removes space on both ends

            if (hsbcRegex.startsWithDate(rows.get(i))) { //if row starts with a date

                String[] words = rows.get(i).split(" "); //one word could be '29 Dec 20 ))) GOLDENS'
                StringBuilder reformattedLine = new StringBuilder(words[0] + "-" + words[1] + "-" + words[2] + " "); //add dashes between dates
                //date = reformattedLine.toString().trim();

                for (int j = 3; j < words.length; j++) {
                    if (j != words.length - 1) {
                        reformattedLine.append(words[j]).append(" "); //after each word, add space
                    } else {
                        reformattedLine.append(words[j]); //add no space after ending word
                    }
                }

                //bef: 29 Dec 20 ))) GOLDENS
                rows.set(i, reformattedLine.toString());
                //aft: 29-Dec-20 ))) GOLDENS
                rows.set(i, rows.get(i).trim());


                if (!hsbcRegex.endsWithMoneyValue(rows.get(i))) {  //if the row doesn't end with money value, it means this transaction is broken into multiple lines. add the index to rownumtojoin. this is to be merged 'this+next'
                    rowIndexListToMergeWith.add(i);
                }
            }
            else if (hsbcRegex.endsWithMoneyValue(rows.get(i))) { //if doesn't start with date nor ends with money value, ignore, it will be merged with a prev line: 'prev+this'
//                System.out.println(i + ". ends with money: " + rows.get(i));
            }
            else { //if the row doesn't start with a date nor end with money value, it means this transaction is broken into multiple lines. add the index to rownumtojoin. this is to be merged 'date+this+next'
//                System.out.println( i + ". regex fail:      " + rows.get(i));
                rowIndexListToMergeWith.add(i);
            }
        }

        //join together broken lines
        for (int i = rowIndexListToMergeWith.size(); i > 0; i--) { //iterate from last row
            int rowNumber = rowIndexListToMergeWith.get(i - 1); //get last index from array
            if (rowNumber + 1 < rows.size()) { //if not on last index of rows

                rows.set(rowNumber, rows.get(rowNumber) + " " + rows.get(rowNumber + 1)); //merge lines
                rows.remove(rowNumber + 1); // remove the line that's been merged with previous line

            }
        }

        //fill empty dates
        for (int i = 0; i < rows.size(); i++) {
            String[] columns = rows.get(i).split(" "); //split the line by word/column

            if (hsbcRegex.startsWithFormattedDate(rows.get(i))) { //try and get the date of the first transaction of the day
                date = columns[0];
            }
            //if(!startsWithFormattedDate(rows.get(i))){
            else {
                //String[] columns = rows.get(i).split(" ");
                StringBuilder newLineWithDateAdded = new StringBuilder();
                newLineWithDateAdded.append(date).append(" "); //Add the date to a new line


                for (int j = 0; j < columns.length; j++) {
                    newLineWithDateAdded.append(columns[j]).append(" "); //join up all the words/columns back up
                }
                rows.set(i, newLineWithDateAdded.toString().trim()); //set to rows list

            }
        }

        rows = addCommas(rows);
        return rows;
    }


    /**
     * TODO FATAL issue, sometimes vis turns out to be payment in
     * @param word
     * @return
     */
    public boolean isPaymentOut(String word) {
        if ("BP".equals(word) || "CR".equals(word)) {
            return false;
        }
        return true;
    }

    public List<String> addCommas(List<String> rows) {

        String date, type, details, paidOut, painIn, Balance;

        //add commas
        for (int i = 0; i < rows.size(); i++) {

            String[] words = rows.get(i).split(" "); //spaces disappear after split so re-append spaces


            if (hsbcRegex.startsWithFormattedDate(rows.get(i))) {

                paymentOut = isPaymentOut(words[1]);
                StringBuilder reformattedLine = new StringBuilder(words[0] + ", " + words[1] + ", "); //add coma 'date, type, '

                for (int j = 2; j < words.length; j++) {
                    reformattedLine.append(words[j]).append(" "); //append rest of the line 'date, type, + rest'
                }
                rows.set(i, reformattedLine.toString().trim());
                words = rows.get(i).split(" "); //update words array with modified rows
            }




            // if you get 2 money values, first is either a pay in or out, second is a balance
            if (hsbcRegex.endsWith2MoneyValues(rows.get(i))) {



                StringBuilder reformattedLine = new StringBuilder();
                for (int j = 0; j < words.length - 2; j++) { // break into columns append up to 'date, type, details'
                    if (j != words.length - 3) {
                        reformattedLine.append(words[j]).append(" ");
                    } else {
                        reformattedLine.append(words[j]);
                    }
                    System.out.println(reformattedLine);
                }


                //add comma in the end 'rest, moneyValue, moneyValue'
                if (paymentOut) {
                    //add extra comma for payment out 'rest, moneyValue,, moneyValue'
                    reformattedLine.append(", ").append(words[words.length - 2]).append(",, ").append(words[words.length - 1]);
                } else {
                    //add extra comma for payment in 'rest,, moneyValue, moneyValue'
                    reformattedLine.append(",, ").append(words[words.length - 2]).append(", ").append(words[words.length - 1]);
                }

                rows.set(i, reformattedLine.toString().trim());
            }

            //if(hsbcRegex.endsWith1MoneyValue(rows.get(i))){
            else {
                System.out.println("bef: " + rows.get(i));

                StringBuilder reformattedLine = new StringBuilder();
                for (int j = 0; j < words.length - 1; j++) { // break into columns append up to 'date, type, details'
                    if (j != words.length - 2) {
                        reformattedLine.append(words[j]).append(" ");
                    } else {
                        reformattedLine.append(words[j]);
                    }
                }

                //add comma in the end: 'rest, moneyvalue'
                if (paymentOut) {
                    reformattedLine.append(", ").append(words[words.length - 1]);
                } else {
                    reformattedLine.append(",, ").append(words[words.length - 1]);
                }
                rows.set(i, reformattedLine.toString().trim());

                System.out.println("aft: " + rows.get(i));
            }

        }
        return rows;
    }

    /**
     * return int month for given 3 letter string i.e. "feb" returns 2
     *
     * @param month 3 letter string
     * @return
     */
    private int formatMonth(String month) {
        int result = 0;
        switch (month.toLowerCase(Locale.ROOT)) {
            case "jan":
                result = 0;
                break;
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
}