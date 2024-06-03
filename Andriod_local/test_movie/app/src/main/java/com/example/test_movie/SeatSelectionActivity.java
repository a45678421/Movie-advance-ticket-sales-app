package com.example.test_movie;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeatSelectionActivity extends AppCompatActivity {

    private GridLayout gridLayoutSeats;
    private Button buttonBuyTicket;
    private TextView textViewMovieTitle, textViewMovieDetails;
    private ImageView imageViewMoviePoster;
    private static final String TAG = "SeatSelectionActivity";
    private Set<String> selectedSeats = new HashSet<>();
    private Set<String> bookedSeats = new HashSet<>();
    private String selectedCity;
    private String selectedMovie;
    private String selectedDate;
    private String selectedTime;
    private String nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        gridLayoutSeats = findViewById(R.id.gridLayoutSeats);
        buttonBuyTicket = findViewById(R.id.buttonBuyTicket);
        textViewMovieTitle = findViewById(R.id.textViewMovieTitle);
        textViewMovieDetails = findViewById(R.id.textViewMovieDetails);
        imageViewMoviePoster = findViewById(R.id.imageViewMoviePoster);

        // Get data from intent
        Intent intent = getIntent();
        selectedCity = intent.getStringExtra("selectedCity");
        selectedMovie = intent.getStringExtra("selectedMovie");
        selectedDate = intent.getStringExtra("selectedDate");
        selectedTime = intent.getStringExtra("selectedTime");
        nickname = intent.getStringExtra("Nickname");  // 獲取Nickname

        // Log the received nickname
        Log.d(TAG, "Received nickname: " + nickname);

        // Display selected options
        textViewMovieTitle.setText(selectedMovie);
        textViewMovieDetails.setText(selectedDate + " " + selectedTime + "\n" + selectedCity);

        // Load movie poster from Firebase Storage using Chinese title
        loadMoviePoster(selectedMovie);

        // Fetch booked seats from Firebase
        fetchBookedSeats(selectedCity, selectedMovie, selectedDate, selectedTime);

        buttonBuyTicket.setOnClickListener(v -> {
            showConfirmationDialog(selectedCity, selectedMovie, selectedDate, selectedTime, nickname);
        });
    }

    private String extractMovieTitle(String fullTitle) {
        int start = fullTitle.indexOf(')');
        int end = fullTitle.indexOf('(', start);
        if (start != -1 && end != -1 && end > start + 1) {
            return fullTitle.substring(start + 1, end).trim();
        }
        return fullTitle; // Return the full title if the pattern is not found
    }

    private void loadMoviePoster(String movieTitle) {
        // Extract the part of the movie title between the parentheses
        String extractedTitle = extractMovieTitle(movieTitle);

        // Log the extracted title
        Log.d(TAG, "Extracted movie title: " + extractedTitle);

        // Use the Chinese title directly for the file name
        String fileName = extractedTitle.replace(" ", "_") + ".jpg";

        // Log the file name
        Log.d(TAG, "File name for movie poster: " + fileName);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/" + fileName);

        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            // Use Picasso to load the image
            Picasso.get().load(uri).into(imageViewMoviePoster);
            // Log the successful URL retrieval
            Log.d(TAG, "Successfully retrieved movie poster URL: " + uri.toString());
        }).addOnFailureListener(exception -> {
            Toast.makeText(SeatSelectionActivity.this, "無法加載電影海報", Toast.LENGTH_SHORT).show();
            // Log the failure
            Log.e(TAG, "Failed to retrieve movie poster URL", exception);
        });
    }

    private void fetchBookedSeats(String city, String movie, String date, String time) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("AllBookings");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Booking booking = snapshot.getValue(Booking.class);
                    if (booking != null && booking.city.equals(city) && booking.movie.equals(movie) && booking.date.equals(date) && booking.time.equals(time)) {
                        if (booking.seats != null) { // Check if booking.seats is not null
                            bookedSeats.addAll(booking.seats);
                        }
                    }
                }
                // Generate seat buttons after fetching booked seats
                generateSeatButtons();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read booked seats", databaseError.toException());
            }
        });
    }

    private void generateSeatButtons() {
        gridLayoutSeats.removeAllViews();  // Clear existing buttons before adding new ones
        // Example seat layout, you should fetch real data from your data source
        char[][] seatLayout = {
                {'A', 'A', 'A', 'A'},
                {'B', 'B', 'B', 'B'},
                {'C', 'C', 'C', 'C'},
                {'D', 'D', 'D', 'D'},
                {'E', 'E', 'E', 'E'},
                {'F', 'F', 'F', 'F'},
                {'G', 'G', 'G', 'G'},
                {'H', 'H', 'H', 'H'},
                {'I', 'I', 'I', 'I'},
        };

        for (int row = 0; row < seatLayout.length; row++) {
            for (int col = 0; col < seatLayout[row].length; col++) {
                Button seatButton = new Button(this);
                String seatTag = row + "-" + col;
                seatButton.setText(String.valueOf(seatLayout[row][col]));
                seatButton.setTag(seatTag);

                if (bookedSeats.contains(seatTag)) {
                    // Mark booked seats as red and disable them
                    seatButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
                    seatButton.setEnabled(false);
                } else {
                    seatButton.setBackgroundResource(android.R.drawable.btn_default); // Default button background
                    seatButton.setOnClickListener(new View.OnClickListener() {
                        private boolean isSelected = false;

                        @Override
                        public void onClick(View v) {
                            if (isSelected) {
                                // Restore to default state
                                v.setBackgroundResource(android.R.drawable.btn_default);
                                ((Button) v).setCompoundDrawables(null, null, null, null);
                                isSelected = false;
                                selectedSeats.remove(v.getTag().toString());
                            } else {
                                // Change to selected state
                                v.setBackgroundColor(ContextCompat.getColor(SeatSelectionActivity.this, android.R.color.holo_green_light)); // Change background color to green
                                Drawable checkIcon = ContextCompat.getDrawable(SeatSelectionActivity.this, R.drawable.ic_check);
                                if (checkIcon != null) {
                                    checkIcon.setBounds(0, 0, 60, 60); // Adjust the size of the check icon if necessary
                                    ((Button) v).setCompoundDrawables(null, null, null, checkIcon); // Set the check icon at the bottom
                                }
                                isSelected = true;
                                selectedSeats.add(v.getTag().toString());
                            }
                            Toast.makeText(SeatSelectionActivity.this, "選擇座位: " + v.getTag(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                // Add button to GridLayout
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = GridLayout.LayoutParams.WRAP_CONTENT;
                params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                params.rowSpec = GridLayout.spec(row);
                params.columnSpec = GridLayout.spec(col);
                seatButton.setLayoutParams(params);
                gridLayoutSeats.addView(seatButton);
            }
        }
    }

    private void showConfirmationDialog(String city, String movie, String date, String time, String nickname) {
        new AlertDialog.Builder(this)
                .setTitle("確認購買")
                .setMessage("您確定要繼續購買嗎？")
                .setPositiveButton("是", (dialog, which) -> {
                    if (selectedSeats.isEmpty()) {
                        Toast.makeText(SeatSelectionActivity.this, "請選擇座位後再進行購買", Toast.LENGTH_SHORT).show();
                    } else {
                        saveSelectedSeatsToFirebase(city, movie, date, time, nickname);
                        Toast.makeText(SeatSelectionActivity.this, "座位已成功保存！", Toast.LENGTH_SHORT).show();
                        // 刷新畫面
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                })
                .setNegativeButton("否", (dialog, which) -> {
                    // 跳轉到購買紀錄選單
                    if (!selectedSeats.isEmpty()) {
                        saveSelectedSeatsToFirebase(city, movie, date, time, nickname);
                    }
                    Intent intent = new Intent(SeatSelectionActivity.this, PurchaseHistoryActivity.class);
                    intent.putExtra("Nickname", nickname); // 傳遞nickname
                    startActivity(intent);
                    finish(); // 結束當前Activity
                })
                .show();
    }

    private void saveSelectedSeatsToFirebase(String city, String movie, String date, String time, String nickname) {
        DatabaseReference userBookingsReference = FirebaseDatabase.getInstance().getReference("Bookings").child(nickname);
        DatabaseReference allBookingsReference = FirebaseDatabase.getInstance().getReference("AllBookings");

        String bookingId = userBookingsReference.push().getKey();
        if (bookingId != null) {
            List<String> selectedSeatsList = new ArrayList<>(selectedSeats); // Convert Set to List
            Booking booking = new Booking(city, movie, date, time, selectedSeatsList);

            userBookingsReference.child(bookingId).setValue(booking)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Booking saved successfully to user bookings");
                        refreshSeats(city, movie, date, time); // Refresh seats after successful booking
                        Toast.makeText(SeatSelectionActivity.this, "座位已成功保存！", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SeatSelectionActivity.this, "保存座位失敗", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to save booking to user bookings", e);
                    });

            allBookingsReference.child(bookingId).setValue(booking)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Booking saved successfully to all bookings"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to save booking to all bookings", e));
        } else {
            Toast.makeText(this, "生成訂單ID失敗", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshSeats(String city, String movie, String date, String time) {
        selectedSeats.clear(); // Clear currently selected seats
        fetchBookedSeats(city, movie, date, time); // Re-fetch booked seats from Firebase
    }

    static class Booking {
        public String city;
        public String movie;
        public String date;
        public String time;
        public List<String> seats; // Change from Set to List
        private String bookingId;

        public Booking() {
            // Default constructor required for calls to DataSnapshot.getValue(Booking.class)
        }

        public Booking(String city, String movie, String date, String time, List<String> seats) {
            this.city = city;
            this.movie = movie;
            this.date = date;
            this.time = time;
            this.seats = seats;
        }

        public String getBookingId() {
            return bookingId;
        }

        public void setBookingId(String bookingId) {
            this.bookingId = bookingId;
        }
    }

}
