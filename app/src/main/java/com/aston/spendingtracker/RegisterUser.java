package com.aston.spendingtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.aston.spendingtracker.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterUser extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private Button registerNewUserButton, loginRedirectButton;
    private EditText etFirstName, etSurname, etEmail, etPassword, etPasswordTwice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        registerNewUserButton = findViewById(R.id.btn_register_user);
        registerNewUserButton.setOnClickListener(this);
        loginRedirectButton = findViewById(R.id.btn_login_redirect);
        loginRedirectButton.setOnClickListener(this);
        etFirstName = findViewById(R.id.first_name_et);
        etSurname = findViewById(R.id.surname_et);
        etEmail = findViewById(R.id.email_et);
        etPassword = findViewById(R.id.password_et);
        etPasswordTwice = findViewById(R.id.password_repeat_et);

        mAuth = FirebaseAuth.getInstance();



    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_login_redirect:
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case R.id.btn_register_user:
                registerUser();
                break;
        }
    }

    private void registerUser(){
        String firstName = etFirstName.getText().toString().trim();
        String surname = etSurname.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String passwordTwice = etPasswordTwice.getText().toString().trim();

        if(firstName.isEmpty()){
            etFirstName.setError("Please type in your first name");
            etFirstName.requestFocus();
            return;
        }
        if(surname.isEmpty()){
            etSurname.setError("Please type in your surname");
            etSurname.requestFocus();
            return;
        }
        if(email.isEmpty()){
            etEmail.setError("Please type in your email");
            etEmail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError("Please type a valid email");
            etEmail.requestFocus();
            return;
        }
        if(password.isEmpty()){
            etPassword.setError("Please create a password");
            etPassword.requestFocus();
            return;
        }
        if(password.length() < 6){
            etPassword.setError("Minimum password length should be 6 characters.");
            etPassword.requestFocus();
            return;
        }
        if(!password.equals(passwordTwice)){
            etPasswordTwice.setError("Your passwords do not match, please check again.");
            etPasswordTwice.setText("");
            etPasswordTwice.requestFocus();
            return;
        }



    }
}