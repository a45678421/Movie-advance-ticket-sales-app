package com.example.test_movie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BuyTicketActivity extends AppCompatActivity {

    private Spinner spinnerCity, spinnerMovie, spinnerDate, spinnerTime;
    private Button buttonReturnToMovieDetails, buttonViewSeats;
    private DatabaseReference databaseReference;
    private Map<String, Map<String, Map<String, List<String>>>> showTimesMap = new HashMap<>();

    private static final String TAG = "BuyTicketActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_ticket);

        Intent intent = getIntent();
        String nickname = intent.getStringExtra("Nickname");

        // Log the received nickname
        Log.d(TAG, "Received nickname: " + nickname);

        spinnerCity = findViewById(R.id.spinnerCity);
        spinnerMovie = findViewById(R.id.spinnerMovie);
        spinnerDate = findViewById(R.id.spinnerDate);
        spinnerTime = findViewById(R.id.spinnerTime);
        buttonViewSeats = findViewById(R.id.buttonViewSeats);
        buttonReturnToMovieDetails = findViewById(R.id.buttonReturnToMovieDetails);

        // Initialize Firebase Realtime Database with custom URL
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://u10127002-movie-default-rtdb.firebaseio.com/");
        databaseReference = database.getReference("ShowTimes");

        // Fetch data from Firebase
        fetchShowTimes();

        // Setup listeners for spinners
        setupListeners();

        buttonReturnToMovieDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BuyTicketActivity.this, MovieInfoActivity.class);
                intent.putExtra("Nickname", nickname);
                startActivity(intent);
            }
        });

        buttonViewSeats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get selected options
                String selectedCity = (String) spinnerCity.getSelectedItem();
                String selectedMovie = (String) spinnerMovie.getSelectedItem();
                String selectedDate = (String) spinnerDate.getSelectedItem();
                String selectedTime = (String) spinnerTime.getSelectedItem();

                // Create intent to start SeatSelectionActivity
                Intent intent = new Intent(BuyTicketActivity.this, SeatSelectionActivity.class);
                intent.putExtra("selectedCity", selectedCity);
                intent.putExtra("selectedMovie", selectedMovie);
                intent.putExtra("selectedDate", selectedDate);
                intent.putExtra("selectedTime", selectedTime);
                intent.putExtra("Nickname", nickname);  // 傳遞 Nickname
                startActivity(intent);
            }
        });
    }

    private void fetchShowTimes() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot citySnapshot : dataSnapshot.getChildren()) {
                    String city = citySnapshot.getKey();
                    Map<String, Map<String, List<String>>> moviesMap = new HashMap<>();
                    for (DataSnapshot movieSnapshot : citySnapshot.getChildren()) {
                        String movie = movieSnapshot.getKey();
                        Map<String, List<String>> datesMap = new HashMap<>();
                        for (DataSnapshot dateSnapshot : movieSnapshot.getChildren()) {
                            String date = dateSnapshot.getKey();
                            List<String> timesList = new ArrayList<>();
                            for (DataSnapshot timeSnapshot : dateSnapshot.getChildren()) {
                                String time = timeSnapshot.getValue(String.class);
                                timesList.add(time);
                            }
                            datesMap.put(date, timesList);
                        }
                        moviesMap.put(movie, datesMap);
                    }
                    showTimesMap.put(city, moviesMap);
                }
                setupCitySpinner();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    private void setupListeners() {
        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = (String) parent.getItemAtPosition(position);
                setupMovieSpinner(selectedCity);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerMovie.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = (String) spinnerCity.getSelectedItem();
                String selectedMovie = (String) parent.getItemAtPosition(position);
                setupDateSpinner(selectedCity, selectedMovie);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = (String) spinnerCity.getSelectedItem();
                String selectedMovie = (String) spinnerMovie.getSelectedItem();
                String selectedDate = (String) parent.getItemAtPosition(position);
                setupTimeSpinner(selectedCity, selectedMovie, selectedDate);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupCitySpinner() {
        List<String> cities = new ArrayList<>(showTimesMap.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(adapter);
    }

    private void setupMovieSpinner(String city) {
        List<String> movies = new ArrayList<>(showTimesMap.get(city).keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, movies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMovie.setAdapter(adapter);
    }

    private void setupDateSpinner(String city, String movie) {
        List<String> dates = new ArrayList<>(showTimesMap.get(city).get(movie).keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dates);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDate.setAdapter(adapter);
    }

    private void setupTimeSpinner(String city, String movie, String date) {
        List<String> times = showTimesMap.get(city).get(movie).get(date);
        List<String> filteredTimes = new ArrayList<>();
        for (String time : times) {
            if (isFutureDate(date, time)) {
                filteredTimes.add(time);
            }
        }
        if (filteredTimes.isEmpty()) {
            filteredTimes.add("沒有場次");
            buttonViewSeats.setEnabled(false);
        } else {
            buttonViewSeats.setEnabled(true);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filteredTimes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTime.setAdapter(adapter);
    }

    private boolean isFutureDate(String dateStr, String timeStr) {
        // Remove the day of the week from the dateStr
        String cleanedDateStr = dateStr.replaceAll("星期[一二三四五六日]", "").trim();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault());
        String dateTimeStr = cleanedDateStr + " " + timeStr;

        // Append a default year to the date and time string for comparison
        String defaultYearDateTimeStr = "2024年" + dateTimeStr;
        SimpleDateFormat defaultYearFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault());

        try {
            Date dateTime = defaultYearFormat.parse(defaultYearDateTimeStr);

            // Get current date and time
            Date now = new Date();

            Log.d(TAG, "Comparing date and time: " + dateTime + " with current date and time: " + now);

            if (dateTime != null) {
                if (dateTime.getYear() < now.getYear()) {
                    return false;
                } else if (dateTime.getYear() == now.getYear()) {
                    if (dateTime.getMonth() < now.getMonth()) {
                        return false;
                    } else if (dateTime.getMonth() == now.getMonth()) {
                        if (dateTime.getDate() < now.getDate()) {
                            return false;
                        } else if (dateTime.getDate() == now.getDate()) {
                            return dateTime.getTime() > now.getTime();
                        }
                    }
                }
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
}
