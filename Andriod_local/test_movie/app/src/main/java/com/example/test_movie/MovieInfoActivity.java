package com.example.test_movie;

// MovieInfoActivity.java
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class MovieInfoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList;
    private List<Movie> filteredMovieList;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_info);

        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        movieList = new ArrayList<>();
        filteredMovieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(this, filteredMovieList);
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
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference("images/" + titleEnglish + ".jpg");

                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        Movie movie = new Movie(titleChinese, titleEnglish, releaseDate, uri.toString());
                        movieList.add(movie);
                        filterMovies("");
                    }).addOnFailureListener(exception -> {
                        // Handle any errors.
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterMovies(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterMovies(newText);
                return false;
            }
        });
    }

    private void filterMovies(String query) {
        filteredMovieList.clear();
        if (query.isEmpty()) {
            filteredMovieList.addAll(movieList);
        } else {
            for (Movie movie : movieList) {
                if (movie.getTitleChinese().contains(query) || movie.getTitleEnglish().contains(query)) {
                    filteredMovieList.add(movie);
                }
            }
        }
        movieAdapter.notifyDataSetChanged();
    }
}





