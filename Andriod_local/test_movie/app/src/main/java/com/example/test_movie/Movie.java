package com.example.test_movie;

public class Movie {
    private String titleChinese;
    private String titleEnglish;
    private String releaseDate;
    private String imageUrl;

    public Movie() {
        // Default constructor required for calls to DataSnapshot.getValue(Movie.class)
    }

    public Movie(String titleChinese, String titleEnglish, String releaseDate, String imageUrl) {
        this.titleChinese = titleChinese;
        this.titleEnglish = titleEnglish;
        this.releaseDate = releaseDate;
        this.imageUrl = imageUrl;
    }

    public String getTitleChinese() {
        return titleChinese;
    }

    public String getTitleEnglish() {
        return titleEnglish;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
