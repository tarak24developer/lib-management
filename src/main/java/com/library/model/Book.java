package com.library.model;

import java.util.ArrayList;
import java.util.List;

public class Book {
    private String id;
    private String title;
    private String author;
    private String isbn;
    private int totalCopies;
    private List<BorrowRecord> borrowRecords;

    public Book(String id, String title, String author, String isbn) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.totalCopies = 1;
        this.borrowRecords = new ArrayList<>();
    }

    public static class BorrowRecord {
        private String userId;
        private String userName;
        private long borrowDate;
        private long returnDate;
        private long dueDate; // New field for due date
        private boolean isReturned;

        // Default constructor for GSON
        public BorrowRecord() {
            this.borrowDate = System.currentTimeMillis();
            this.dueDate = this.borrowDate + (14 * 24 * 60 * 60 * 1000L); // Set due date to 14 days from borrow date
            this.isReturned = false;
        }

        public BorrowRecord(String userId, String userName) {
            this();
            this.userId = userId;
            this.userName = userName;
        }

        // Getters and Setters for GSON
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public long getBorrowDate() { return borrowDate; }
        public void setBorrowDate(long borrowDate) { this.borrowDate = borrowDate; }
        
        public long getReturnDate() { return returnDate; }
        public void setReturnDate(long returnDate) { this.returnDate = returnDate; }
        
        public long getDueDate() { return dueDate; } // Getter for due date
        public void setDueDate(long dueDate) { this.dueDate = dueDate; } // Setter for due date
        
        public boolean isReturned() { return isReturned; }
        public void setReturned(boolean returned) { isReturned = returned; }

        public void returnBook() {
            this.returnDate = System.currentTimeMillis();
            this.isReturned = true;
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    
    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }

    public List<BorrowRecord> getBorrowRecords() { return borrowRecords; }
    
    public int getAvailableCopies() {
        int borrowed = 0;
        for (BorrowRecord record : borrowRecords) {
            if (!record.isReturned) {
                borrowed++;
            }
        }
        return totalCopies - borrowed;
    }

    public boolean isAvailable() {
        return getAvailableCopies() > 0;
    }

    public void borrowBook(User user) {
        if (isAvailable()) {
            borrowRecords.add(new BorrowRecord(user.getId(), user.getName()));
        }
    }

    public void borrowBook(User user, long dueDate) {
        if (isAvailable()) {
            BorrowRecord record = new BorrowRecord(user.getId(), user.getName());
            record.setDueDate(dueDate);
            borrowRecords.add(record);
        }
    }

    public boolean returnBook(String userId) {
        for (BorrowRecord record : borrowRecords) {
            if (record.getUserId().equals(userId) && !record.isReturned) {
                record.returnBook();
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return title + " by " + author;
    }
}