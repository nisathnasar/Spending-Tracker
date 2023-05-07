package com.aston.spendingtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.aston.spendingtracker.authorization.LoginActivity;
import com.aston.spendingtracker.authorization.RegisterUserActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);


        EditText email_et = findViewById(R.id.email_for_password_reset_et);
        Button sendLinkBtn = findViewById(R.id.send_link_button);

        sendLinkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email_address = email_et.getText().toString().trim().toLowerCase();

                if(email_address.isEmpty()){
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter email", Toast.LENGTH_LONG).show();
                    email_et.requestFocus();
                    return;
                }
                if(!Patterns.EMAIL_ADDRESS.matcher(email_address).matches()){
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter valid email", Toast.LENGTH_LONG).show();
                    email_et.requestFocus();
                    return;
                }

                ProgressBar bar = findViewById(R.id.loading);
                bar.setVisibility(View.VISIBLE);

                FirebaseAuth.getInstance().sendPasswordResetEmail(email_address).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        bar.setVisibility(View.INVISIBLE);
                    }
                });

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ForgotPasswordActivity.this);
                //AlertDialog.Builder builder = new AlertDialog.Builder(ViewTransaction.this);
                builder.setTitle("Reset password has been sent");
                builder.setMessage("If the email entered is correct, you should receive the email shortly.")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                // Create the AlertDialog object and return it
                builder.show();



            }
        });

        Button notReceived = findViewById(R.id.not_recieved_email_btn);

        notReceived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ForgotPasswordActivity.this);
                //AlertDialog.Builder builder = new AlertDialog.Builder(ViewTransaction.this);
                //builder.setTitle("Reset password has been sent");
                builder.setMessage("If you have not received a link, please wait slightly longer, or try again.")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                // Create the AlertDialog object and return it
                builder.show();

            }
        });

        Button loginButton = findViewById(R.id.back_to_login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });


        Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this, RegisterUserActivity.class);
                startActivity(intent);
            }
        });
    }
}