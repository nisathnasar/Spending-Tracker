package com.aston.spendingtracker.pdf;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;

public interface PDFProcessor {

    /**
     * This method will strip text, get list of transactions, preprocess it, organise into a list and upload to database
     */
    void processPDF() throws IOException, ParseException;

}
