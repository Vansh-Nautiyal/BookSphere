package com.library.model;

public class User {
    private int    id;
    private String globalId;
    private String name;
    private String email;
    private String phone;
    private String role;       // "student", "member", "admin"
    private String password;
    private String joinedDate;
    private String status;     // "active", "suspended"

    public User() {}

    public User(String globalId, String name, String email, String phone,
                String role, String password) {
        this.globalId = globalId;
        this.name     = name;
        this.email    = email;
        this.phone    = phone;
        this.role     = role;
        this.password = password;
        this.status   = "active";
    }

    public int    getId()         { return id; }
    public String getGlobalId()   { return globalId; }
    public String getName()       { return name; }
    public String getEmail()      { return email; }
    public String getPhone()      { return phone; }
    public String getRole()       { return role; }
    public String getPassword()   { return password; }
    public String getJoinedDate() { return joinedDate; }
    public String getStatus()     { return status; }

    public void setId(int id)                { this.id = id; }
    public void setGlobalId(String globalId) { this.globalId = globalId; }
    public void setName(String name)         { this.name = name; }
    public void setEmail(String email)       { this.email = email; }
    public void setPhone(String phone)       { this.phone = phone; }
    public void setRole(String role)         { this.role = role; }
    public void setPassword(String pass)     { this.password = pass; }
    public void setJoinedDate(String date)   { this.joinedDate = date; }
    public void setStatus(String status)     { this.status = status; }

    @Override public String toString() { return name + " (" + globalId + ")"; }
}