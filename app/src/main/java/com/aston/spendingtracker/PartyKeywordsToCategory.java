package com.aston.spendingtracker;

import java.util.HashMap;

public class PartyKeywordsToCategory {
    private HashMap<String, String> keywordsToCategoryMap;

    public PartyKeywordsToCategory() {
        keywordsToCategoryMap = new HashMap<>();

        keywordsToCategoryMap.put("goldens birmingham", "Grocery");
        keywordsToCategoryMap.put("aldi", "Shopping");
        keywordsToCategoryMap.put("west midland card", "Travel");
        keywordsToCategoryMap.put("post office", "Shopping");
        keywordsToCategoryMap.put("tesco", ("Grocery"));
        keywordsToCategoryMap.put("pepes peri peri", ("Restaurant"));
        keywordsToCategoryMap.put("int'l", ("Online Shopping"));
        keywordsToCategoryMap.put("lycamobile", ("Utility Bill"));
        keywordsToCategoryMap.put("aston university", ("University"));
        keywordsToCategoryMap.put("cash lloytsb", ("ATM"));
        keywordsToCategoryMap.put("slc", ("loan"));
    }

    public HashMap<String, String> getKeywordsToCategoryMap(){
        return keywordsToCategoryMap;
    }

}
