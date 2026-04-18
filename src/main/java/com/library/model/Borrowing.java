package com.library.model;

public class Borrowing {
    private int    id;
    private int    bookId;
    private int    userId;
    private String bookTitle;
    private String userName;
    private String borrowDate;
    private String dueDate;
    private String returnDate;
    private double fine;
    private String status;   // "active", "returned"

    public Borrowing() {}

    public int    getId()          { return id; }
    public int    getBookId()      { return bookId; }
    public int    getUserId()      { return userId; }
    public String getBookTitle()   { return bookTitle; }
    public String getUserName()    { return userName; }
    public String getBorrowDate()  { return borrowDate; }
    public String getDueDate()     { return dueDate; }
    public String getReturnDate()  { return returnDate; }
    public double getFine()        { return fine; }
    public String getStatus()      { return status; }

    public void setId(int id)                    { this.id = id; }
    public void setBookId(int bookId)            { this.bookId = bookId; }
    public void setUserId(int userId)            { this.userId = userId; }
    public void setBookTitle(String bookTitle)   { this.bookTitle = bookTitle; }
    public void setUserName(String userName)     { this.userName = userName; }
    public void setBorrowDate(String borrowDate) { this.borrowDate = borrowDate; }
    public void setDueDate(String dueDate)       { this.dueDate = dueDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }
    public void setFine(double fine)             { this.fine = fine; }
    public void setStatus(String status)         { this.status = status; }

    public boolean isReturned() { return returnDate != null && !returnDate.isEmpty(); }
}