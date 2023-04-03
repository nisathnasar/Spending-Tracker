package com.aston.spendingtracker.pdf;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aston.spendingtracker.Category;
import com.aston.spendingtracker.Party;
import com.aston.spendingtracker.PartyKeywordsToCategory;
import com.aston.spendingtracker.entity.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.multipdf.Splitter;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class HSBCPDFProcessor implements PDFProcessor{

    private static final String TAG = "HSBCPDFProcessor";

    public List<Integer> rowIndexListToMergeWith;
    public boolean paymentOut;
    public String date = "";

    private LinkedList<Transaction> listTransactionItems;

    private HSBCRegex hsbcRegex;
    private double balanceCarriedForward;
    private int numOfPagesToExtractFrom;

    Context thisContext;
    Uri pathURI;
    DatabaseReference mrootRef;
    DatabaseReference dbRefContainsDateCommencing, dbRefTransaction;

    boolean partyExists = false;


    public HSBCPDFProcessor(Context context, Uri pathURI) {

        numOfPagesToExtractFrom = 1;
        thisContext = context;
        this.pathURI = pathURI;


    }

    @Override
    public void processPDF() throws IOException, ParseException {
        listTransactionItems = new LinkedList<>();
        hsbcRegex = new HSBCRegex();
        PDFBoxResourceLoader.init(thisContext);



        ParcelFileDescriptor parcelFileDescriptor = thisContext.getContentResolver().openFileDescriptor(pathURI, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        InputStream fileStream = new FileInputStream(fileDescriptor);
        PDDocument document = PDDocument.load(fileStream);

//        PDDocument document = PDDocument.load(assetManager.open("sample_stmt.pdf"));
//        PyObject obj = pyobj.callAttr("extract_text");
//        String result = obj.toString();

        //File path = thisContext.getFilesDir();

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        System.out.println(path);
        try{
            FileOutputStream writer = new FileOutputStream(new File(path, "someFileName.csv"));

            writer.write("date, type, balance".getBytes());
            writer.close();
        }
        catch (Exception e){
            System.out.println(e);
        }

//        FileWriter fw = new FileWriter(thisContext.getFilesDir() + "/Created.csv");
//        FileWriter fw = new FileWriter(root.getAbsolutePath() + "/Created.csv");

//        Log.d("MainActivity.java", root.getAbsolutePath() + "/Created.csv");
//        write header
//        fw.write("Date, Type, Details, Pay Out, Pay In, Balance\n");

        activateSequence(document, numOfPagesToExtractFrom);
        parcelFileDescriptor.close();
        fileStream.close();
        document.close();


    }

    private void activateSequence(PDDocument document, int numOfPagesToExtractFrom) throws IOException, ParseException {

        Splitter splitter = new Splitter();
        List<PDDocument> splitPages = splitter.split(document); //split pages
        numOfPagesToExtractFrom = splitPages.size();

        //numOfPagesToExtractFrom = 2;

        PDFTextStripper stripper = new PDFTextStripper();

        mrootRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        dbRefContainsDateCommencing = mrootRef.child("ContainsDateCommencing");
        dbRefTransaction = mrootRef.child("Transaction");


        for(int e = 0; e<numOfPagesToExtractFrom; e++){
            String text = stripper.getText(splitPages.get(e)); // reads all of the page
            String[] lines = text.split("\\r?\\n"); // each line is broken into array
            List<String> rows = stringArrayToArrayList(lines); //change string array to arraylist

            int lastIndexToKeep = -1;
            int firstIndexToKeep = 0;
            boolean firstDateFound = false;
            System.out.println("-------------------text read on first page--------------------------");
            for (int i = 0; i < rows.size(); i++) {
                String row = rows.get(i);
                //System.out.println(row);

                //identify when to stop reading
                if(row.matches(".*BALANCE CARRIED FORWARD\\s[()a-zA-Z0-9_-].*$")){
                    lastIndexToKeep = i-2;

                    //get carry forward balance
                    if(e == 0){
                        //String line = rows.get(rows.size() - 28).replaceAll(",", "");
                        String[] splitlines = row.replaceAll(",", "").split(" ");
                        balanceCarriedForward = Double.parseDouble(splitlines[3]);
                        //System.out.println("-----------------: " + balanceCarriedForward);
                    }
                    else{
                        //get carry forward balance
                        balanceCarriedForward = -1;

                        String[] splitlines = row.replaceAll(",", "").split(" ");
                        for(String line : splitlines){
                            if(line.trim().matches("[0-9]{1}[0-9]*\\.[0-9]{2}$")){
                                balanceCarriedForward = Double.parseDouble(line);
                                //System.out.println("-----------------: " + balanceCarriedForward);
                            }
                        }
                    }
                }

                //identify when to start reading
                if(HSBCRegex.startsWithDate(row)){
                    firstDateFound = true;
                }
                if(!firstDateFound){
                    firstIndexToKeep++;
                }

            }
            System.out.println("---------------------------------------------");

            if(lastIndexToKeep == -1){
                Toast.makeText(thisContext, "page "+ (e+1) + " doesn't have any transaction data.", Toast.LENGTH_SHORT).show();
                break;
                //throw new NullPointerException("regex fail");
            }

            // Remove the last non transaction lines: removes the first 30 lines by traversing in reverse
            // Doing last lines first to avoid index changes
            while(rows.size()-1 != lastIndexToKeep){
                rows.remove(rows.size() - 1);
            }


            // Remove the first non transaction lines
            //rows.subList(0, 4).clear();
            rows.subList(0, firstIndexToKeep).clear();

            String[] preprocessedRowWords = rows.get(0).split(" ");
            //dateCommencing  = (preprocessedRowWords[0] + "-" + preprocessedRowWords[1] + "-" + preprocessedRowWords[2]).trim();
            //System.out.println("dateCommencing assigned for first page: " + dateCommencing);

            rows = processLines(rows);


            addToDataBase(rows);
        }

        document.close();

        document.close();
    }


    public LinkedList<Transaction> getTransactionListItems() {
        return listTransactionItems;
    }

    public List<String> stringArrayToArrayList(String[] strArr) {
        List<String> result = new ArrayList<>();
        Collections.addAll(result, strArr);
        System.out.println("synthesised list: " + result);
        return result;
    }


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
            } else if (!hsbcRegex.endsWithMoneyValue(rows.get(i))) { //if the row doesn't start with a date nor end with money value, it means this transaction is broken into multiple lines. add the index to rownumtojoin. this is to be merged 'date+this+next'
//                System.out.println( i + ". regex fail:      " + rows.get(i));
                rowIndexListToMergeWith.add(i);
            }
            //if doesn't start with date nor ends with money value, ignore, it will be merged with a prev line: 'prev+this'

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


                for (String column : columns) {
                    newLineWithDateAdded.append(column).append(" "); //join up all the words/columns back up
                }
                rows.set(i, newLineWithDateAdded.toString().trim()); //set to rows list

            }
        }

        rows = addCommas(rows);

        //get date of first transaction for unique ID
//        dateCommencing = rows.get(0).split(",")[0].trim();

        addBalanceToAllLines(rows);

        return rows;
    }


    /**
     * TODO FATAL issue, sometimes vis turns out to be payment in
     */
    public boolean isPaymentOut(String word) {
        return !"BP".equals(word) && !"CR".equals(word);
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
                }

                //add comma in the end 'rest, moneyValue, moneyValue'
                if (paymentOut) {
                    //add extra comma for payment out 'rest, moneyValue,, moneyValue'
                    reformattedLine.append(", ").append(words[words.length - 2]).append(", , ").append(words[words.length - 1]);
                } else {
                    //add extra comma for payment in 'rest,, moneyValue, moneyValue'
                    reformattedLine.append(", , ").append(words[words.length - 2]).append(", ").append(words[words.length - 1]);
                }

                rows.set(i, reformattedLine.toString().trim());
            }

            //if(hsbcRegex.endsWith1MoneyValue(rows.get(i))){
            else {
                //System.out.println("bef: " + rows.get(i));

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
                rows.set(i, reformattedLine.toString());

                //System.out.println("aft: " + rows.get(i));
            }

        }
        return rows;
    }

    private List<String> addBalanceToAllLines(List<String> rows) {

        //get last transaction
        //get the balance
        //for each transaction from last
        //if there is a line before this
        //if payment out,
        //lastBalance = lastBalance + payment out of same transaction
        //else if payment in
        //lastBalance = lastBalance - payment in of same transaction
        //set lastBalance as previous balance


        System.out.println("-------------addbalance last row=" + rows.get(rows.size() - 1));
        //double lastBalance = Double.parseDouble(rows.get(rows.size()-1).split(", ")[5]);
        double lastBalance = balanceCarriedForward;

        System.out.println("-------------lastBalance=" + lastBalance);

        for (int i = 0; i < rows.size(); i++) {
            int currIndexOfRows = rows.size() - i - 1;

            //words.length == 4 if doesn't have balance, ==6 if has balance and has paid in or out
            String[] words = rows.get(currIndexOfRows).split(", "); //break into columns without commas and spaces in the end.

            DecimalFormat df = new DecimalFormat("0.00");

            if (words.length == 6) {
                if (isPaymentOut(words[1])) {
                    lastBalance = lastBalance + Double.parseDouble(words[3].trim());
                } else {
                    lastBalance = lastBalance - Double.parseDouble(words[4].trim());
                }
            } else {
                if(isPaymentOut(words[1])){
                    rows.set(currIndexOfRows, words[0] + ", " + words[1] + ", " + words[2] + ", " + words[3] + ", , " + df.format(lastBalance));
                }
                else{
                    rows.set(currIndexOfRows, words[0] + ", " + words[1] + ", " + words[2] + ", " + words[3] + ", " + df.format(lastBalance));
                }


                if (isPaymentOut(words[1])) {
                    lastBalance = lastBalance + Double.parseDouble(words[3].trim());
                } else {
                    lastBalance = lastBalance - Double.parseDouble(words[3].trim());
                }
            }


        }

        return rows;
    }

    String partyUID = "";

    private void addToDataBase(List<String> rows) throws ParseException {

        for (String str : rows) {

            //fw.write(str+"\n"); //write to file
            //listTransaction.addLast(str + "\n");

            String[] words = str.split(",");

            //reconstructed date to match date order on db
            String[] dateElements = words[0].trim().split("-");
            String reconstructedDate = dateElements[2] + "-" + Transaction.formatMonth(dateElements[1].trim()) + "-" + dateElements[0];


            Transaction transaction = new Transaction(
                    //new Timestamp(Transaction.getDateObjectFromString(words[0].trim()).getTime()),
                    //words[0].trim(),
                    reconstructedDate,
                    words[1].trim(),
                    words[2].trim(),
                    words[3].trim(),
                    words[4].trim(),
                    words[5].trim(),
                    partyUID);

            PartyKeywordsToCategory pKC = new PartyKeywordsToCategory();

            HashMap<String, String> keywordsToCategoryMap = pKC.getKeywordsToCategoryMap();

            for(String keyword : keywordsToCategoryMap.keySet()){
                if(transaction.getPaymentDetails().toLowerCase().contains(keyword)){
                    transaction.setCategory(keywordsToCategoryMap.get(keyword));
                    break;
                }
            }
            if(transaction.getCategory().equals("")){
                transaction.setCategory("other");
            }




            //extract party
            //extractParty(transaction);

            dbRefTransaction
                    .child(reconstructedDate)
                    .child(Integer.toString(transaction.getTransactionID()))
                    .setValue(transaction);


            listTransactionItems.add(transaction);

        }

        //add datcommencing to database to prevent future duplicate entries


    }

    /**
     * Get list of all parties (both senders and recievers) from database.
     * If description is a new user, then add as a new user.
     *
     * should be called before items are added to database, because we need to check
     */
    public void extractParty(Transaction t){

        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        DatabaseReference mPartyRef = mRootRef.child("Party");


        //try different kind of listener
        mPartyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                System.out.println("valueEventListener is being run ....................... for " + t.getPaymentDetails());

                System.out.println(snapshot.toString());

                String sanitisedPaymentDetails = Party.SanitiseName(t.getPaymentDetails());
                if(snapshot.hasChild(sanitisedPaymentDetails)){
                    System.out.println("from inside snapshot.haschild, party exists ......................... " + t.getPaymentDetails());

                    //snapshot.child(sanitisedPaymentDetails).getValue(Party.class).addTransaction(t);

                }
                else{

                    Party p = new Party(t.getPaymentDetails());
                    //p.addTransaction(t);
                    mPartyRef.child(p.getName()).setValue(p);
                    mPartyRef.push();

                    //System.out.println("Party added: " + p.getName() + ", " + p.getListOfTransactions().toString());
                }

                System.out.println(snapshot.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // ...
            }
        });

/*
        if(!isPartyExists(t)){
            Party p = new Party(t.getPaymentDetails());
            p.addTransaction(t);

            mPartyRef.child(p.getName()).setValue(p);


            System.out.println("Party added: " + p.getName() + ", " + p.getListOfTransactions().toString());
        }
        else{
            //find the party and add transaction
            System.out.println("Party exists: " + t.getPaymentDetails() + ", " + t);

            mPartyRef.child(t.getPaymentDetails()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Party p = dataSnapshot.getValue(Party.class);

                        p.addTransaction(t);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
*/
    }

    public boolean isPartyExists(Transaction transaction){

        partyExists = false;

        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        DatabaseReference mPartyRef = mRootRef.child("Party");

        //try different kind of listener
        mPartyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.hasChild(Party.SanitiseName(transaction.getPaymentDetails()))){
                    System.out.println("from inside snapshot.haschild, party exists ......................... " + transaction.getPaymentDetails());
                    partyExists = true;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // ...
            }
        });


        if(partyExists){
            System.out.println(transaction.getPaymentDetails()  + ": exists in db");
        }
        else{
            System.out.println(transaction.getPaymentDetails()  + ": does not exist in db");
        }

        return partyExists;
    }

}