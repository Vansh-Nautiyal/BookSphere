package com.library.model;

public class Fine {
    private int    id;
    private int    borrowingId;
    private int    userId;
    private String userName;
    private String bookTitle;
    private double amount;
    private boolean paid;
    private String dueDate;
    private String paidDate;

    public Fine() {}

    public int     getId()          { return id; }
    public int     getBorrowingId() { return borrowingId; }
    public int     getUserId()      { return userId; }
    public String  getUserName()    { return userName; }
    public String  getBookTitle()   { return bookTitle; }
    public double  getAmount()      { return amount; }
    public boolean isPaid()         { return paid; }
    public String  getDueDate()     { return dueDate; }
    public String  getPaidDate()    { return paidDate; }

    public void setId(int id)                   { this.id = id; }
    public void setBorrowingId(int borrowingId) { this.borrowingId = borrowingId; }
    public void setUserId(int userId)           { this.userId = userId; }
    public void setUserName(String userName)    { this.userName = userName; }
    public void setBookTitle(String bookTitle)  { this.bookTitle = bookTitle; }
    public void setAmount(double amount)        { this.amount = amount; }
    public void setPaid(boolean paid)           { this.paid = paid; }
    public void setDueDate(String dueDate)      { this.dueDate = dueDate; }
    public void setPaidDate(String paidDate)    { this.paidDate = paidDate; }
}