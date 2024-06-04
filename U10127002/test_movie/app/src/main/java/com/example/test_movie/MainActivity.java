package com.example.test_movie;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference usersDatabase;
    private DatabaseReference ownerDatabase;
    private VideoView videoView;
    private FirebaseStorage storage;
    private List<String> videoUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usersDatabase = FirebaseDatabase.getInstance().getReference("users");
        ownerDatabase = FirebaseDatabase.getInstance().getReference("owner");
        storage = FirebaseStorage.getInstance();
        videoView = findViewById(R.id.videoView);

        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginDialog();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // 獲取 Firebase Storage 中的影片 URL 列表
        getVideoUrlsFromFirebase();

        // 設置影片播放完成的監聽器
        videoView.setOnCompletionListener(mp -> playRandomVideo());
    }

    private void getVideoUrlsFromFirebase() {
        StorageReference listRef = storage.getReference().child("videos");

        listRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference item : listResult.getItems()) {
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    videoUrls.add(uri.toString());
                    // 在這裡確保所有 URL 都已獲取後開始播放影片
                    if (videoUrls.size() == listResult.getItems().size()) {
                        playRandomVideo();
                    }
                }).addOnFailureListener(exception -> {
                    Toast.makeText(MainActivity.this, "Error getting video URL", Toast.LENGTH_SHORT).show();
                });
            }
        }).addOnFailureListener(exception -> {
            Toast.makeText(MainActivity.this, "Error listing videos", Toast.LENGTH_SHORT).show();
        });
    }

    private void playRandomVideo() {
        if (videoUrls.isEmpty()) {
            Toast.makeText(this, "No videos found in Firebase Storage", Toast.LENGTH_SHORT).show();
            return;
        }

        // 隨機選擇一個影片 URL
        String randomVideoUrl = videoUrls.get(new Random().nextInt(videoUrls.size()));

        // 設置 VideoView
        Uri uri = Uri.parse(randomVideoUrl);
        videoView.setVideoURI(uri);

        // 添加控制器
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        // 開始播放影片
        videoView.start();
    }

    private void showLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_login, null);

        EditText usernameEditText = view.findViewById(R.id.dialog_username);
        EditText passwordEditText = view.findViewById(R.id.dialog_password);

        builder.setView(view)
                .setTitle("Login")
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String username = usernameEditText.getText().toString();
                        String password = passwordEditText.getText().toString();

                        if (!username.isEmpty() && !password.isEmpty()) {
                            loginUser(username, password);
                        } else {
                            Toast.makeText(MainActivity.this, "Username and password cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void loginUser(final String username, final String password) {
        ownerDatabase.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String ownerPassword = dataSnapshot.child("password").getValue(String.class);
                    if (ownerPassword != null && ownerPassword.equals(password)) {
                        Toast.makeText(MainActivity.this, "Owner login successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, OwnerActivity.class);
                        startActivity(intent);
                    } else {
                        checkUserCredentials(username, password);
                    }
                } else {
                    checkUserCredentials(username, password);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Database error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserCredentials(final String username, final String password) {
        usersDatabase.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String storedPassword = dataSnapshot.child("password").getValue(String.class);
                    if (storedPassword != null && storedPassword.equals(password)) {
                        Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, MovieInfoActivity.class);
                        intent.putExtra("Nickname", username);  // 將Username作為Nickname傳遞
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "Invalid password. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Username not found. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Database error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
