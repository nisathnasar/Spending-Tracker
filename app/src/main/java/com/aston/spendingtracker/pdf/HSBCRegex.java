package com.aston.spendingtracker.pdf;

public class HSBCRegex {



      //System.out.println("10 aug 22 whatever d563 rfr".matches("^[0-9]{2}\\s[a-zA-Z]{3}\\s[0-9]{2}\\s[a-zA-Z0-9_-].*$"));
      //System.out.println("STR@13:05 10.00".matches("[a-zA-Z0-9_-].*\\s[0-9]{1}[0-9]*\\.[0-9]{2}$"));
      //System.out.println("STR@13:05 123,456,789.00".matches("[a-zA-Z0-9_-].*\\s[0-9]{1}[0-9]*,[0-9]{3}.*\\.[0-9]{2}$"));
      //System.out.println("ererb, erbe, efr. 120,20.00". replaceAll(",", ""));


    public static boolean startsWithDate(String str) {
        return str.matches("^[0-9]{2}\\s[a-zA-Z]{3}\\s[0-9]{2}\\s[()a-zA-Z0-9_-].*$");
    }

    public static boolean startsWithFormattedDate(String str) {
        return str.matches("^[0-9]{2}-[a-zA-Z]{3}-[0-9]{2}\\s[()a-zA-Z0-9_-].*$");
    }

    public static boolean endsWithMoneyValue(String str) {
        return str.matches("[,()a-zA-Z0-9_-].*\\s[0-9]{1}[0-9]*\\.[0-9]{2}$");
    }

    public static boolean endsWith1MoneyValue(String str) {
        return str.matches("[,()a-zA-Z0-9_-].*\\s[0-9]{1}[0-9]*\\.[0-9]{2}$") && !str.matches("[,()a-zA-Z0-9_-].*\\s[0-9]{1}[0-9]*\\.[0-9]{2}\\s[0-9]{1}[0-9]*\\.[0-9]{2}$");
    }

    public static boolean endsWith2MoneyValues(String str) {
        return str.matches("[,()a-zA-Z0-9_-].*\\s[0-9]{1}[0-9]*\\.[0-9]{2}\\s[0-9]{1}[0-9]*\\.[0-9]{2}$");
    }

}
