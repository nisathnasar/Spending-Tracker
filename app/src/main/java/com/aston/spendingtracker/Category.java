package com.aston.spendingtracker;

public class Category {
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    String category;

    public Category(String name){
        this.category = name;
    }

    public Category(){

    }

}
