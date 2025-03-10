package com.library.ui;

import com.library.model.Book;
import com.library.model.User;
import com.library.service.DataService;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class StatsPanel extends JPanel implements PropertyChangeListener {
    private final DataService dataService;
    private JLabel totalBooksLabel;
    private JLabel availableBooksLabel;
    private JLabel borrowedBooksLabel;
    private JLabel totalUsersLabel;
    private JLabel activeUsersLabel;
    private JLabel overdueBooksLabel; // New label for overdue books
    private JTextArea popularBooksArea;
    private JTextArea activeReadersArea;
    private SimpleDateFormat dateFormat;

    public StatsPanel(DataService dataService) {
        this.dataService = dataService;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        setupUI();
        refreshStats();
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Create main panel with grid layout
        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel for quick stats
        JPanel quickStatsPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        quickStatsPanel.setBorder(BorderFactory.createTitledBorder("Quick Statistics"));

        totalBooksLabel = createStatsLabel("Total Books: 0");
        availableBooksLabel = createStatsLabel("Available Books: 0");
        borrowedBooksLabel = createStatsLabel("Borrowed Books: 0");
        totalUsersLabel = createStatsLabel("Total Users: 0");
        activeUsersLabel = createStatsLabel("Active Users: 0");
        overdueBooksLabel = createStatsLabel("Overdue Books: 0"); // New label for overdue books

        quickStatsPanel.add(totalBooksLabel);
        quickStatsPanel.add(availableBooksLabel);
        quickStatsPanel.add(borrowedBooksLabel);
        quickStatsPanel.add(totalUsersLabel);
        quickStatsPanel.add(activeUsersLabel);
        quickStatsPanel.add(overdueBooksLabel); // Add overdue books label

        // Bottom panel for detailed stats
        JPanel detailedStatsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        detailedStatsPanel.setBorder(BorderFactory.createTitledBorder("Detailed Statistics"));

        // Popular books panel
        JPanel popularBooksPanel = new JPanel(new BorderLayout());
        popularBooksPanel.setBorder(BorderFactory.createTitledBorder("Most Popular Books"));
        popularBooksArea = new JTextArea();
        popularBooksArea.setEditable(false);
        popularBooksPanel.add(new JScrollPane(popularBooksArea));

        // Active readers panel
        JPanel activeReadersPanel = new JPanel(new BorderLayout());
        activeReadersPanel.setBorder(BorderFactory.createTitledBorder("Most Active Readers"));
        activeReadersArea = new JTextArea();
        activeReadersArea.setEditable(false);
        activeReadersPanel.add(new JScrollPane(activeReadersArea));

        detailedStatsPanel.add(popularBooksPanel);
        detailedStatsPanel.add(activeReadersPanel);

        mainPanel.add(quickStatsPanel);
        mainPanel.add(detailedStatsPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JLabel createStatsLabel(String text) {
        JLabel label = new JLabel(text);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("REFRESH_USERS".equals(evt.getPropertyName()) || 
            "REFRESH_BOOKS".equals(evt.getPropertyName())) {
            refreshStats();
        }
    }

    private void refreshStats() {
        List<Book> books = dataService.getAllBooks();
        List<User> users = dataService.getAllUsers();

        // Calculate basic stats
        int totalBooks = books.stream().mapToInt(Book::getTotalCopies).sum();
        int availableBooks = books.stream().mapToInt(Book::getAvailableCopies).sum();
        int borrowedBooks = totalBooks - availableBooks;
        int totalUsers = users.size();
        int activeUsers = (int) users.stream()
            .filter(user -> !user.getBorrowedBooks().isEmpty())
            .count();
        int overdueBooks = (int) books.stream()
            .flatMap(book -> book.getBorrowRecords().stream())
            .filter(record -> !record.isReturned() && record.getDueDate() < System.currentTimeMillis())
            .count();

        // Update basic stats labels
        totalBooksLabel.setText("Total Books: " + totalBooks);
        availableBooksLabel.setText("Available Books: " + availableBooks);
        borrowedBooksLabel.setText("Borrowed Books: " + borrowedBooks);
        totalUsersLabel.setText("Total Users: " + totalUsers);
        activeUsersLabel.setText("Active Users: " + activeUsers);
        overdueBooksLabel.setText("Overdue Books: " + overdueBooks); // Update overdue books label

        // Calculate popular books
        Map<String, Integer> bookBorrowCounts = new HashMap<>();
        for (Book book : books) {
            int borrowCount = book.getBorrowRecords().size();
            bookBorrowCounts.put(book.getTitle(), borrowCount);
        }

        List<Map.Entry<String, Integer>> popularBooks = bookBorrowCounts.entrySet()
            .stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toList());

        // Update popular books text area
        StringBuilder popularBooksText = new StringBuilder();
        popularBooksText.append("Top 5 Most Borrowed Books:\n\n");
        for (Map.Entry<String, Integer> entry : popularBooks) {
            popularBooksText.append(entry.getKey())
                .append(" (")
                .append(entry.getValue())
                .append(" times)\n");
        }
        popularBooksArea.setText(popularBooksText.toString());

        // Calculate active readers
        Map<String, Integer> userBorrowCounts = new HashMap<>();
        for (User user : users) {
            userBorrowCounts.put(user.getName(), user.getBorrowedBooks().size());
        }

        List<Map.Entry<String, Integer>> activeReaders = userBorrowCounts.entrySet()
            .stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toList());

        // Update active readers text area
        StringBuilder activeReadersText = new StringBuilder();
        activeReadersText.append("Top 5 Most Active Readers:\n\n");
        for (Map.Entry<String, Integer> entry : activeReaders) {
            activeReadersText.append(entry.getKey())
                .append(" (")
                .append(entry.getValue())
                .append(" books)\n");
        }
        activeReadersArea.setText(activeReadersText.toString());
    }
}