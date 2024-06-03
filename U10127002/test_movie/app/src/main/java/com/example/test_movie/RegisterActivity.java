package com.example.test_movie;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {
    private EditText nicknameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button registerButton;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nicknameEditText = findViewById(R.id.nickname);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        registerButton = findViewById(R.id.register);
        database = FirebaseDatabase.getInstance().getReference("users");

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nickname = nicknameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (!nickname.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    checkIfNicknameExists(nickname, email, password);
                } else {
                    Toast.makeText(RegisterActivity.this, "Nickname, email, and password cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkIfNicknameExists(final String nickname, final String email, final String password) {
        database.child(nickname).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(RegisterActivity.this, "Nickname already exists. Please try again.", Toast.LENGTH_SHORT).show();
                } else {
                    checkIfEmailExists(nickname, email, password);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("RegisterActivity", "Database error", databaseError.toException());
            }
        });
    }

    private void checkIfEmailExists(final String nickname, final String email, final String password) {
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean emailExists = false;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String existingEmail = userSnapshot.child("email").getValue(String.class);
                    if (existingEmail != null && existingEmail.equals(email)) {
                        emailExists = true;
                        break;
                    }
                }
                if (emailExists) {
                    Toast.makeText(RegisterActivity.this, "Email already exists. Please try again.", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(nickname, email, password);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("RegisterActivity", "Database error", databaseError.toException());
            }
        });
    }

    private void registerUser(String nickname, String email, String password) {
        User user = new User(email, password);
        database.child(nickname).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Close RegisterActivity
                    } else {
                        Toast.makeText(RegisterActivity.this, "User registration failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static class User {
        public String email;
        public String password;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
}