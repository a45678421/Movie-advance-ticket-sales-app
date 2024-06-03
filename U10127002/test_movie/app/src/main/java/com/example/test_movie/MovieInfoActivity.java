package com.example.test_movie;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MovieInfoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList;
    private List<Movie> filteredMovieList;
    private String nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_info);

        // Get nickname from intent
        Intent intent = getIntent();
        nickname = intent.getStringExtra("Nickname");
        Log.d("MovieInfoActivity", "Nickname: " + nickname);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SearchView searchView = findViewById(R.id.searchView); // 使用 androidx.appcompat.widget.SearchView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        movieList = new ArrayList<>();
        filteredMovieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(this, filteredMovieList, nickname); // 傳遞nickname
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
                        filterMovies("");  // Initial filter with empty query
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_menu) {
            // Create the PopupMenu
            View menuItemView = findViewById(R.id.action_menu);
            PopupMenu popupMenu = new PopupMenu(this, menuItemView);
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

            // Use reflection to force show icons
            try {
                Field[] fields = popupMenu.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if ("mPopup".equals(field.getName())) {
                        field.setAccessible(true);
                        Object menuPopupHelper = field.get(popupMenu);
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    int itemId = menuItem.getItemId();
                    if (itemId == R.id.action_purchase_history) {
                        Intent purchaseHistoryIntent = new Intent(MovieInfoActivity.this, PurchaseHistoryActivity.class);
                        purchaseHistoryIntent.putExtra("Nickname", nickname);
                        startActivity(purchaseHistoryIntent);
                        return true;
                    } else if (itemId == R.id.action_logout) {
                        Intent logoutIntent = new Intent(MovieInfoActivity.this, MainActivity.class);
                        startActivity(logoutIntent);
                        finish();
                        return true;
                    }
                    return false;
                }
            });

            popupMenu.show();

            // Start frame animation for purchase history item
            MenuItem purchaseHistoryItem = popupMenu.getMenu().findItem(R.id.action_purchase_history);
            if (purchaseHistoryItem != null) {
                Drawable icon = purchaseHistoryItem.getIcon();
                if (icon instanceof AnimationDrawable) {
                    ((AnimationDrawable) icon).start();
                }
            }

            // Start frame animation for logout item
            MenuItem logoutItem = popupMenu.getMenu().findItem(R.id.action_logout);
            if (logoutItem != null) {
                Drawable icon = logoutItem.getIcon();
                if (icon instanceof AnimationDrawable) {
                    ((AnimationDrawable) icon).start();
                }
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
