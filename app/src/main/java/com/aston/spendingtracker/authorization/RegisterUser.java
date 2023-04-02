package com.aston.spendingtracker.authorization;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.aston.spendingtracker.R;
import com.aston.spendingtracker.tutorial.TutorialActivity;
import com.aston.spendingtracker.entity.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterUser extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private Button registerNewUserButton, loginRedirectButton;
    private EditText etFirstName, etSurname, etEmail, etPassword, etPasswordTwice;
    private ProgressBar progressBar;

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
        progressBar = findViewById(R.id.progressBar);

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

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            User user = new User(firstName, surname, email);

                            // Write a message to the database
                            FirebaseDatabase.getInstance()
                                    .getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(RegisterUser.this, "User has been registered successfully!", Toast.LENGTH_LONG).show();
                                                progressBar.setVisibility(View.GONE);
                                                startActivity(new Intent(RegisterUser.this, TutorialActivity.class));
                                            }
                                            else{
                                                Toast.makeText(RegisterUser.this, "Failed to register! Try again!", Toast.LENGTH_LONG).show();
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        } else{
                            Toast.makeText(RegisterUser.this, "Failed to register! Try again!", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });


    }

    @Override
    public void onBackPressed() {
        // Disable back button
    }

}