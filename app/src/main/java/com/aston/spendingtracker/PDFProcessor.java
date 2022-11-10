package com.aston.spendingtracker;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

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

    public List<Integer> rowNumToJoin;
    public boolean paymentOut;
    public String date = "";
    private FileDescriptor fileDescriptor;
    private LinkedList<String> listTransaction;

    public PDFProcessor(Context context, Uri pathURI) throws IOException {

        int numOfPagesToExtractFrom = 2;
        listTransaction = new LinkedList<>();
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
        String text = stripper.getText(splitPages.get(0));
        String[] lines = text.split("\\r?\\n");

        List<String> rows = synthesiseList(lines);

        // Remove the first non transaction lines: removes the first 30 lines by traversing in reverse
        for (int i = 30; i > 1; i--) {
            rows.remove(rows.remove(rows.size() - 1));
        }

        // Remove the last non transaction lines
        for (int i = 2; i > -1; i--) {
            rows.remove(i);
        }

        rows = processLines(rows);

        //write to file
        for (String str : rows) {
            //fw.write(str+"\n");
            //addTextView(str+"\n");
            listTransaction.addLast(str + "\n");
        }

        //Page 2
        if (numOfPagesToExtractFrom > 1) {
            String text2 = stripper.getText(splitPages.get(1));
            String[] lines2 = text2.split("\\r?\\n");
            List<String> rows2 = synthesiseList(lines2);

            /*
             * Remove the last irrelevant lines: removes the first 16 lines by traversing in reverse
             */
            for (int i = 16; i > 1; i--) {
                rows2.remove(rows2.remove(rows2.size() - 1));
            }
            /*
             * Remove the first useless lines
             */
            for (int i = 1; i > -1; i--) {
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
            List<String> rows3 = synthesiseList(lines3);

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

    public List<String> synthesiseList(String[] strArr) {
        List<String> result = new ArrayList<>();
        Collections.addAll(result, strArr);

        /*
        for(String str : strArr){
            result.add(str);
        }*/

        System.out.println("synthesised list: " + result);
        return result;
    }

    public void printList(List<String> list) {

        for (int i = 0; i < list.size(); i++) {
            System.out.println(i + ". " + list.get(i));
        }
    }

    public boolean startsWithDate(String str) {
        return str.matches("^[0-9]{2}\\s[a-zA-Z]{3}\\s[0-9]{2}\\s[()a-zA-Z0-9_-].*$");
    }

    public boolean startsWithFormattedDate(String str) {
        return str.matches("^[0-9]{2}-[a-zA-Z]{3}-[0-9]{2}\\s[()a-zA-Z0-9_-].*$");
    }

    public boolean endsWithMoneyValue(String str) {
        return str.matches("[,()a-zA-Z0-9_-].*\\s[0-9]{1}[0-9]*\\.[0-9]{2}$");
    }

    public boolean endsWith1MoneyValue(String str) {
        return str.matches("[,()a-zA-Z0-9_-].*\\s[0-9]{1}[0-9]*\\.[0-9]{2}$") && !str.matches("[,()a-zA-Z0-9_-].*\\s[0-9]{1}[0-9]*\\.[0-9]{2}\\s[0-9]{1}[0-9]*\\.[0-9]{2}$");
    }

    public boolean endsWith2MoneyValues(String str) {
        return str.matches("[,()a-zA-Z0-9_-].*\\s[0-9]{1}[0-9]*\\.[0-9]{2}\\s[0-9]{1}[0-9]*\\.[0-9]{2}$");
    }

    /**
     * //System.out.println("10 aug 22 whatever d563 rfr".matches("^[0-9]{2}\\s[a-zA-Z]{3}\\s[0-9]{2}\\s[a-zA-Z0-9_-].*$"));
     * //System.out.println("STR@13:05 10.00".matches("[a-zA-Z0-9_-].*\\s[0-9]{1}[0-9]*\\.[0-9]{2}$"));
     * //System.out.println("STR@13:05 123,456,789.00".matches("[a-zA-Z0-9_-].*\\s[0-9]{1}[0-9]*,[0-9]{3}.*\\.[0-9]{2}$"));
     * //System.out.println("ererb, erbe, efr. 120,20.00". replaceAll(",", ""));
     */
    public List<String> processLines(List<String> rows) {

        rowNumToJoin = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {


            rows.set(i, rows.get(i).replaceAll(",", ""));
            rows.set(i, rows.get(i).replaceAll(" +", " "));
            rows.set(i, rows.get(i).trim());

            if (startsWithDate(rows.get(i))) {

                String[] words = rows.get(i).split(" ");
                StringBuilder reformattedLine = new StringBuilder(words[0] + "-" + words[1] + "-" + words[2] + " ");
                date = reformattedLine.toString().trim();


                for (int j = 3; j < words.length; j++) {
                    if (j != words.length - 1) {
                        reformattedLine.append(words[j]).append(" ");
                    } else {
                        reformattedLine.append(words[j]);
                    }
                }
                rows.set(i, reformattedLine.toString());
                rows.set(i, rows.get(i).trim());
                //System.out.println(i + ". starts with date:    " +rows.get(i));

                if (!endsWithMoneyValue(rows.get(i))) {
                    rowNumToJoin.add(i);
                }

            } else if (endsWithMoneyValue(rows.get(i))) {
                //System.out.println(i + ". ends with money: " + rows.get(i));
            } else {
                //System.out.println( i + ". regex fail:      " + rows.get(i));
                rowNumToJoin.add(i);

            }
        }
        /*
        for(int i=0; i<rowNumToJoin.size(); i++){
            System.out.println(rowNumToJoin.get(i));
        }*/

        /*
          join together broken lines
         */

        for (int i = rowNumToJoin.size(); i > 0; i--) {
            int rowNumber = rowNumToJoin.get(i - 1);
            if (rowNumber + 1 < rows.size()) {
                rows.set(rowNumber, rows.get(rowNumber) + " " + rows.get(rowNumber + 1));
                rows.remove(rowNumber + 1);
            }

        }

        //fill empty dates
        for (int i = 0; i < rows.size(); i++) {
            if (startsWithFormattedDate(rows.get(i))) {
                String[] words = rows.get(i).split(" ");
                date = words[0];
            }
            //if(!startsWithFormattedDate(rows.get(i))){
            else {
                String[] words = rows.get(i).split(" ");
                StringBuilder reformattedLine = new StringBuilder();
                reformattedLine.append(date).append(" ");
                for (int j = 0; j < words.length; j++) {

                    if (j != words.length - 1) {
                        reformattedLine.append(words[j]).append(" ");
                    } else {
                        reformattedLine.append(words[j]);
                    }
                }
                rows.set(i, reformattedLine.toString());
                rows.set(i, rows.get(i).trim());
            }
        }

        rows = addCommas(rows);
        return rows;
    }


    public boolean isPaymentOut(String word) {
        if ("BP".equals(word) || "CR".equals(word)) {
            return false;
        }
        return true;
    }

    public List<String> addCommas(List<String> rows) {
        //add commas
        for (int i = 0; i < rows.size(); i++) {
            if (startsWithFormattedDate(rows.get(i))) {
                String[] words = rows.get(i).split(" ");
                paymentOut = isPaymentOut(words[1]);
                //add comma to 'date, type, + rest'
                StringBuilder reformattedLine = new StringBuilder(words[0] + ", " + words[1] + ", ");

                for (int j = 2; j < words.length; j++) {
                    reformattedLine.append(words[j]).append(" ");
                }
                rows.set(i, reformattedLine.toString());
                rows.set(i, rows.get(i).trim());
            } else {

                String[] words = rows.get(i).split(" ");
                paymentOut = isPaymentOut(words[0]);
                //add blank cell if there is no date and comma: ' ,type,
                StringBuilder reformattedLine = new StringBuilder("," + words[0] + ", ");
                for (int j = 1; j < words.length; j++) {
                    reformattedLine.append(words[j]).append(" ");
                }
                rows.set(i, reformattedLine.toString());
                rows.set(i, rows.get(i).trim());
            }


            // if you get 2 money values, first is either a pay in or out, second is a balance
            if (endsWith2MoneyValues(rows.get(i))) {
                String[] words = rows.get(i).split(" ");
                StringBuilder reformattedLine = new StringBuilder();
                for (int j = 0; j < words.length - 2; j++) {
                    if (j != words.length - 3) {
                        reformattedLine.append(words[j]).append(" ");
                    } else {
                        reformattedLine.append(words[j]);
                    }
                }
                //add comma in the end 'rest, moneyValue, moneyValue'

                if (paymentOut) {
                    //add extra comma for payment out 'rest, moneyValue,, moneyValue'
                    reformattedLine.append(", ").append(words[words.length - 2]).append(",, ").append(words[words.length - 1]);
                } else {
                    //add extra comma for payment in 'rest,, moneyValue, moneyValue'
                    reformattedLine.append(",, ").append(words[words.length - 2]).append(", ").append(words[words.length - 1]);
                }

                rows.set(i, reformattedLine.toString());
                rows.set(i, rows.get(i).trim());
            }

            //if(endsWith1MoneyValue(rows.get(i))){
            else {
                String[] words = rows.get(i).split(" ");
                StringBuilder reformattedLine = new StringBuilder();
                for (int j = 0; j < words.length - 1; j++) {
                    if (j != words.length - 2) {
                        reformattedLine.append(words[j]).append(" ");
                    } else {
                        reformattedLine.append(words[j]);
                    }
                }
                //add comma in the end: 'rest, moneyvalue'
                //reformattedLine.append(", ").append(words[words.length-1]);
                if (paymentOut) {
                    reformattedLine.append(", ").append(words[words.length - 1]);
                } else {
                    reformattedLine.append(",, ").append(words[words.length - 1]);
                }
                rows.set(i, reformattedLine.toString());
                rows.set(i, rows.get(i).trim());
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