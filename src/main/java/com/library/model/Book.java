package com.library.model;

public class Book {
    private int    id;
    private String title;
    private String author;
    private String isbn;
    private String genre;
    private String publisher;
    private int    year;
    private int    totalCopies;
    private int    availableCopies;

    public Book() {}

    public Book(String title, String author, String isbn, String genre,
                String publisher, int year, int totalCopies) {
        this.title           = title;
        this.author          = author;
        this.isbn            = isbn;
        this.genre           = genre;
        this.publisher       = publisher;
        this.year            = year;
        this.totalCopies     = totalCopies;
        this.availableCopies = totalCopies;
    }

    public int    getId()              { return id; }
    public String getTitle()           { return title; }
    public String getAuthor()          { return author; }
    public String getIsbn()            { return isbn; }
    public String getGenre()           { return genre; }
    public String getPublisher()       { return publisher; }
    public int    getYear()            { return year; }
    public int    getTotalCopies()     { return totalCopies; }
    public int    getAvailableCopies() { return availableCopies; }

    public void setId(int id)                  { this.id = id; }
    public void setTitle(String title)         { this.title = title; }
    public void setAuthor(String author)       { this.author = author; }
    public void setIsbn(String isbn)           { this.isbn = isbn; }
    public void setGenre(String genre)         { this.genre = genre; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public void setYear(int year)              { this.year = year; }
    public void setTotalCopies(int t)          { this.totalCopies = t; }
    public void setAvailableCopies(int a)      { this.availableCopies = a; }

    public boolean isAvailable() { return availableCopies > 0; }

    @Override public String toString() { return title + " — " + author; }
}