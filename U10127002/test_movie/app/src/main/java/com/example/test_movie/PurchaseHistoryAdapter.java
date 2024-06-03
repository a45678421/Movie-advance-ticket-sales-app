package com.example.test_movie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PurchaseHistoryAdapter extends RecyclerView.Adapter<PurchaseHistoryAdapter.ViewHolder> {

    private Context context;
    private List<SeatSelectionActivity.Booking> bookingList;

    public PurchaseHistoryAdapter(Context context, List<SeatSelectionActivity.Booking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_purchase_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SeatSelectionActivity.Booking booking = bookingList.get(position);
        holder.textViewMovieTitle.setText(booking.movie);
        holder.textViewCity.setText("影城: " + booking.city);
        holder.textViewDate.setText("日期: " + booking.date);
        holder.textViewTime.setText("時間: " + booking.time);
        holder.textViewSeats.setText("座位: " + booking.seats.toString());
        holder.textViewBookingId.setText("訂單ID: " + booking.getBookingId());
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMovieTitle, textViewCity, textViewDate, textViewTime, textViewSeats, textViewBookingId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMovieTitle = itemView.findViewById(R.id.textViewMovieTitle);
            textViewCity = itemView.findViewById(R.id.textViewCity);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewSeats = itemView.findViewById(R.id.textViewSeats);
            textViewBookingId = itemView.findViewById(R.id.textViewBookingId);
        }
    }
}
