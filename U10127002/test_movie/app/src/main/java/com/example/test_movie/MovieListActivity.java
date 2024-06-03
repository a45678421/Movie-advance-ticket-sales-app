package com.example.test_movie;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MovieListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList;
    private String nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        // 獲取從上個 Activity 傳遞過來的 Nickname
        nickname = getIntent().getStringExtra("Nickname");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(this, movieList, nickname);  // 傳遞 nickname
        recyclerView.setAdapter(movieAdapter);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Movie");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                movieList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String titleChinese = snapshot.child("Title(Chinese)").getValue(String.class);
                    String titleEnglish = snapshot.child("Title(English)").getValue(String.class);
                    String releaseDate = snapshot.child("Release Date").getValue(String.class);
                    String imageUrl = "https://<your_firebase_storage_bucket>/images/" + titleEnglish + ".jpg";

                    Movie movie = new Movie(titleChinese, titleEnglish, releaseDate, imageUrl);
                    movieList.add(movie);
                }
                movieAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }
}
