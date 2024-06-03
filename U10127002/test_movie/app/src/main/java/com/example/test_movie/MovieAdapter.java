package com.example.test_movie;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private Context context;
    private List<Movie> movieList;
    private String nickname;

    public MovieAdapter(Context context, List<Movie> movieList, String nickname) {
        this.context = context;
        this.movieList = movieList;
        this.nickname = nickname;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.movieNameChinese.setText(movie.getTitleChinese());
        holder.movieNameEnglish.setText(movie.getTitleEnglish());
        holder.releaseDate.setText(movie.getReleaseDate());
        Picasso.get().load(movie.getImageUrl()).into(holder.imageView);

        View.OnClickListener listener = v -> {
            Intent intent = new Intent(context, MovieDetails.class);
            intent.putExtra("movie_title_english", movie.getTitleEnglish());
            intent.putExtra("Nickname", nickname); // 傳遞nickname
            context.startActivity(intent);
        };

        holder.imageView.setOnClickListener(listener);
        holder.movieNameChinese.setOnClickListener(listener);
        holder.movieNameEnglish.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView movieNameChinese;
        TextView movieNameEnglish;
        TextView releaseDate;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            movieNameChinese = itemView.findViewById(R.id.movie_name_chinese);
            movieNameEnglish = itemView.findViewById(R.id.movie_name_english);
            releaseDate = itemView.findViewById(R.id.release_date);
        }
    }
}
