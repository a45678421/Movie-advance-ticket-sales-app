package com.example.test_movie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MovieDetails extends AppCompatActivity {

    private static final String TAG = "MovieDetails";

    private ImageView imageView;
    private TextView movieName, movieName_EN, releaseDate, type, duration, description;
    private String movieTitleEnglish;
    private String nickname;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details); // 確保這裡的文件名正確

        imageView = findViewById(R.id.imageView);
        movieName = findViewById(R.id.movie_name);
        movieName_EN = findViewById(R.id.movie_name_EN);
        releaseDate = findViewById(R.id.release_date);
        type = findViewById(R.id.type);
        duration = findViewById(R.id.duration);
        description = findViewById(R.id.description);
        Button buttonMovieInfo = findViewById(R.id.button_movie_info);
        Button buttonBuyTicket = findViewById(R.id.button_buy_ticket);

        Intent intent = getIntent();
        movieTitleEnglish = intent.getStringExtra("movie_title_english");
        nickname = intent.getStringExtra("Nickname");  // 接收 Nickname

        // Log the received nickname
        Log.d(TAG, "Received nickname: " + nickname);

        if (movieTitleEnglish == null || movieTitleEnglish.isEmpty()) {
            movieTitleEnglish = "WINNIE THE POOH BLOOD AND HONEY 2";
        }

        // Initialize Firebase Realtime Database with custom URL
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://u10127002-movie-default-rtdb.firebaseio.com/");
        DatabaseReference databaseReference = database.getReference("Movie/" + movieTitleEnglish);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Fetch data
                String movieNameStr = dataSnapshot.child("Title(Chinese)").getValue(String.class);
                String movieName_ENStr = dataSnapshot.child("Title(English)").getValue(String.class);
                String releaseDateStr = dataSnapshot.child("Release Date").getValue(String.class);
                String typeStr = dataSnapshot.child("Type").getValue(String.class);
                String durationStr = dataSnapshot.child("Duration").getValue(String.class);
                String descriptionStr = dataSnapshot.child("Description").getValue(String.class);

                // Update UI
                movieName.setText(movieNameStr);
                movieName_EN.setText(movieName_ENStr);
                releaseDate.setText(releaseDateStr);
                type.setText(typeStr);
                duration.setText(durationStr);
                description.setText(descriptionStr);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read data", databaseError.toException());
            }
        });

        // Firebase Storage reference
        String imagePath = "images/" + movieTitleEnglish + ".jpg";
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(imagePath);
        Log.d(TAG, "Fetching image from: " + imagePath);
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            // Load image using Picasso
            Picasso.get().load(uri).into(imageView);
        }).addOnFailureListener(exception -> {
            Log.e(TAG, "Failed to fetch image", exception);
        });

        buttonMovieInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MovieDetails.this, MovieInfoActivity.class);
                intent.putExtra("Nickname", nickname);  // 傳遞 Nickname
                startActivity(intent);
            }
        });

        buttonBuyTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MovieDetails.this, BuyTicketActivity.class);
                intent.putExtra("Nickname", nickname);  // 傳遞 Nickname
                startActivity(intent);
            }
        });
    }
}
