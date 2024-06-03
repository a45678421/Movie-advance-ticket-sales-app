package com.example.test_movie;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PurchaseHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPurchaseHistory;
    private PurchaseHistoryAdapter adapter;
    private List<SeatSelectionActivity.Booking> bookingList;
    private String nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_history);

        // Get nickname from intent
        Intent intent = getIntent();
        nickname = intent.getStringExtra("Nickname");

        Button buttonLogout = findViewById(R.id.buttonLogout);
        Button buttonBackToMovieInfo = findViewById(R.id.buttonReturnToMovieInfo);
        recyclerViewPurchaseHistory = findViewById(R.id.recyclerViewPurchaseHistory);

        recyclerViewPurchaseHistory.setLayoutManager(new LinearLayoutManager(this));
        bookingList = new ArrayList<>();
        adapter = new PurchaseHistoryAdapter(bookingList);
        recyclerViewPurchaseHistory.setAdapter(adapter);

        loadPurchaseHistory();

        buttonLogout.setOnClickListener(v -> {
            Intent logoutIntent = new Intent(PurchaseHistoryActivity.this, MainActivity.class);
            startActivity(logoutIntent);
            finish();
        });

        buttonBackToMovieInfo.setOnClickListener(v -> {
            Intent backToMovieInfoIntent = new Intent(PurchaseHistoryActivity.this, MovieInfoActivity.class);
            backToMovieInfoIntent.putExtra("Nickname", nickname);
            startActivity(backToMovieInfoIntent);
            finish();
        });
    }

    private void loadPurchaseHistory() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bookings").child(nickname);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bookingList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    SeatSelectionActivity.Booking booking = snapshot.getValue(SeatSelectionActivity.Booking.class);
                    booking.setBookingId(snapshot.getKey()); // Set the booking ID
                    bookingList.add(booking);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("PurchaseHistoryActivity", "Failed to load purchase history", databaseError.toException());
            }
        });
    }

    private class PurchaseHistoryAdapter extends RecyclerView.Adapter<PurchaseHistoryAdapter.ViewHolder> {

        private List<SeatSelectionActivity.Booking> bookingList;

        public PurchaseHistoryAdapter(List<SeatSelectionActivity.Booking> bookingList) {
            this.bookingList = bookingList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_purchase_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SeatSelectionActivity.Booking booking = bookingList.get(position);
            holder.bind(booking);
        }

        @Override
        public int getItemCount() {
            return bookingList.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            private TextView textViewMovieTitle, textViewCity, textViewDate, textViewTime, textViewSeats, textViewBookingId;
            private ImageView imageViewMoviePoster;
            private ImageView buttonRefundTicketIcon;
            private TextView buttonRefundTicketText;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewMovieTitle = itemView.findViewById(R.id.textViewMovieTitle);
                textViewCity = itemView.findViewById(R.id.textViewCity);
                textViewDate = itemView.findViewById(R.id.textViewDate);
                textViewTime = itemView.findViewById(R.id.textViewTime);
                textViewSeats = itemView.findViewById(R.id.textViewSeats);
                textViewBookingId = itemView.findViewById(R.id.textViewBookingId);
                imageViewMoviePoster = itemView.findViewById(R.id.imageViewMoviePoster);
                buttonRefundTicketIcon = itemView.findViewById(R.id.buttonRefundTicketIcon);
                buttonRefundTicketText = itemView.findViewById(R.id.buttonRefundTicketText);

                View.OnClickListener refundClickListener = v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        SeatSelectionActivity.Booking booking = bookingList.get(position);
                        refundTicket(booking);
                    }
                };

                buttonRefundTicketIcon.setOnClickListener(refundClickListener);
                buttonRefundTicketText.setOnClickListener(refundClickListener);
            }

            public void bind(SeatSelectionActivity.Booking booking) {
                textViewMovieTitle.setText(booking.movie);
                textViewCity.setText("影院: " + booking.city);
                textViewDate.setText("日期: " + booking.date);
                textViewTime.setText("時間: " + booking.time);
                textViewSeats.setText("座位: " + booking.seats);
                textViewBookingId.setText("訂單ID: " + booking.getBookingId());

                // Load movie poster
                loadMoviePoster(booking.movie, imageViewMoviePoster);
            }
        }

        private String extractMovieTitle(String fullTitle) {
            int start = fullTitle.indexOf(')');
            int end = fullTitle.indexOf('(', start);
            if (start != -1 && end != -1 && end > start + 1) {
                return fullTitle.substring(start + 1, end).trim();
            }
            return fullTitle; // Return the full title if the pattern is not found
        }

        private void loadMoviePoster(String movieTitle, ImageView imageViewMoviePoster) {
            // Extract the part of the movie title between the parentheses
            String extractedTitle = extractMovieTitle(movieTitle);

            // Log the extracted title
            Log.d("PurchaseHistoryActivity", "Extracted movie title: " + extractedTitle);

            // Use the Chinese title directly for the file name
            String fileName = extractedTitle.replace(" ", "_") + ".jpg";

            // Log the file name
            Log.d("PurchaseHistoryActivity", "File name for movie poster: " + fileName);

            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/" + fileName);

            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                // Use Picasso to load the image
                Picasso.get().load(uri).into(imageViewMoviePoster);
                // Log the successful URL retrieval
                Log.d("PurchaseHistoryActivity", "Successfully retrieved movie poster URL: " + uri.toString());
            }).addOnFailureListener(exception -> {
                Toast.makeText(imageViewMoviePoster.getContext(), "無法加載電影海報", Toast.LENGTH_SHORT).show();
                // Log the failure
                Log.e("PurchaseHistoryActivity", "Failed to retrieve movie poster URL", exception);
            });
        }

        private void refundTicket(SeatSelectionActivity.Booking booking) {
            String result = canRefund(booking);
            if (!result.equals("OK")) {
                Toast.makeText(PurchaseHistoryActivity.this, result, Toast.LENGTH_SHORT).show();
                return;
            }

            // Delete booking from user's bookings
            DatabaseReference userBookingRef = FirebaseDatabase.getInstance().getReference("Bookings").child(nickname).child(booking.getBookingId());
            userBookingRef.removeValue().addOnSuccessListener(aVoid -> {
                // Delete booking from AllBookings
                DatabaseReference allBookingRef = FirebaseDatabase.getInstance().getReference("AllBookings").child(booking.getBookingId());
                allBookingRef.removeValue().addOnSuccessListener(aVoid1 -> {
                    Toast.makeText(PurchaseHistoryActivity.this, "退票成功", Toast.LENGTH_SHORT).show();
                    loadPurchaseHistory(); // Refresh the purchase history
                }).addOnFailureListener(e -> {
                    Toast.makeText(PurchaseHistoryActivity.this, "無法從所有訂單中刪除", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(PurchaseHistoryActivity.this, "無法從用戶訂單中刪除", Toast.LENGTH_SHORT).show();
            });
        }

        private String canRefund(SeatSelectionActivity.Booking booking) {
            String dateStr = booking.date;
            String timeStr = booking.time;

            // Remove the day of the week from the dateStr
            String cleanedDateStr = dateStr.replaceAll("星期[一二三四五六日]", "").trim();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault());
            String dateTimeStr = cleanedDateStr + " " + timeStr;

            // Append a default year to the date and time string for comparison
            String defaultYearDateTimeStr = "2024年" + dateTimeStr;
            SimpleDateFormat defaultYearFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault());

            try {
                Date dateTime = defaultYearFormat.parse(defaultYearDateTimeStr);
                Date now = new Date();

                if (dateTime != null) {
                    long timeDiff = dateTime.getTime() - now.getTime();
                    long minutesDiff = timeDiff / (1000 * 60);

                    if (minutesDiff < 0) {
                        return "票卷已過期";
                    } else if (minutesDiff <= 15) {
                        return "距離開場時間不到15分鐘，無法退票";
                    } else {
                        return "OK";
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return "無法解析票卷時間";
        }
    }
}
