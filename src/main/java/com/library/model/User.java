package com.library.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String id;
    private String name;
    private String email;
    private String password;
    private List<String> borrowedBooks;
    private int rewardPoints; // New field for reward points

    public User(String id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.borrowedBooks = new ArrayList<>();
        this.rewardPoints = 0; // Initialize reward points to 0
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public List<String> getBorrowedBooks() { return borrowedBooks; }
    public void setBorrowedBooks(List<String> borrowedBooks) { this.borrowedBooks = borrowedBooks; }

    public int getRewardPoints() { return rewardPoints; } // Getter for reward points
    public void setRewardPoints(int rewardPoints) { this.rewardPoints = rewardPoints; } // Setter for reward points

    public void borrowBook(String bookId) {
        if (!borrowedBooks.contains(bookId)) {
            borrowedBooks.add(bookId);
        }
    }

    public void returnBook(String bookId) {
        borrowedBooks.remove(bookId);
    }

    @Override
    public String toString() {
        return name + " (" + email + ")";
    }
}