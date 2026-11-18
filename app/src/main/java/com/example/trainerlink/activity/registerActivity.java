package com.example.trainerlink.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trainerlink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import module.User;

public class registerActivity extends AppCompatActivity {

    EditText firstName, lastName, etRegEmail, etRegPassword, confirmPassword;
    Button btnRegister;
    TextView txtGoLogin;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        // connect views
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        txtGoLogin = findViewById(R.id.txtGoLogin);

        btnRegister.setOnClickListener(v -> registerUser());

        txtGoLogin.setOnClickListener(v ->
                startActivity(new Intent(registerActivity.this, loginActivity.class))
        );
    }

    private void registerUser() {


        String email = etRegEmail.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();
        String confirm = confirmPassword.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }else if (password.isEmpty()){
            Toast.makeText(this, "password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }else if(confirm.isEmpty()){
            Toast.makeText(this, "This cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }else if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }else if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        String uid = mAuth.getCurrentUser().getUid();

                        String fName = firstName.getText().toString().trim();
                        String lName = lastName.getText().toString().trim();


                        User user = new User(uid, fName, lName, email);

                        DatabaseReference ref = FirebaseDatabase
                                .getInstance()
                                .getReference("Users");

                        ref.child(uid).setValue(user)
                                .addOnSuccessListener(unused -> {

                                    Toast.makeText(this, "DB Write Success", Toast.LENGTH_LONG).show();

                                    startActivity(new Intent(registerActivity.this, mainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this,
                                            "DB FAILED: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });
                    }else {
                        Toast.makeText(this,
                                task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}