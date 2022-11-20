package com.aston.spendingtracker.entity;

public class User {
    public String firstName, surname, email;

    public User(){

    }

    public User(String firstName, String surname, String email){
        this.firstName = firstName;
        this.surname = surname;
        this.email = email;
    }
}
