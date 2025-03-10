package com.library.ui;

import com.library.model.Book;
import com.library.model.User;
import com.library.service.DataService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Random;

public class BooksPanel extends JPanel {
    private final DataService dataService;
    private JTable booksTable;
    private JTable borrowingHistoryTable;
    private DefaultTableModel tableModel;
    private DefaultTableModel historyTableModel;
    private SimpleDateFormat dateFormat;
    private JTextField searchField;
    private JComboBox<String> filterComboBox;
    
    public BooksPanel(DataService dataService) {
        this.dataService = dataService;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        setupUI();
        refreshTable();
    }

    private void showReturnDialog(Book book) {
        List<Book.BorrowRecord> activeRecords = new ArrayList<>();
        for (Book.BorrowRecord record : book.getBorrowRecords()) {
            if (!record.isReturned()) {
                activeRecords.add(record);
            }
        }

        if (activeRecords.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No active borrowers for this book",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] borrowers = activeRecords.stream()
            .map(record -> record.getUserName())
            .toArray(String[]::new);

        String selectedBorrower = (String) JOptionPane.showInputDialog(
            this,
            "Select user returning the book:",
            "Return Book",
            JOptionPane.QUESTION_MESSAGE,
            null,
            borrowers,
            borrowers[0]);

        if (selectedBorrower != null) {
            Book.BorrowRecord record = activeRecords.get(Arrays.asList(borrowers).indexOf(selectedBorrower));
            User user = dataService.getUserById(record.getUserId());
            if (user != null) {
                book.returnBook(user.getId());
                user.returnBook(book.getId());
                dataService.updateBook(book);
                dataService.updateUser(user);
                refreshTable();
                refreshHistoryTable();
                firePropertyChange("REFRESH_USERS", null, null);

                // Generate random points between 10 and 60
                Random random = new Random();
                int points = 10 + random.nextInt(51);

                // Add points to user's reward points
                user.setRewardPoints(user.getRewardPoints() + points);
                dataService.updateUser(user);

                // Fire property change event to refresh users tab
                firePropertyChange("REFRESH_USERS", null, null);

                // Show message dialog
                JOptionPane.showMessageDialog(this,
                    "Book returned successfully. " + user.getName() + " has earned " + points + " points!",
                    "Book Returned",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Create main split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.6);
        
        // Create top panel with books table
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // Add search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        filterComboBox = new JComboBox<>(new String[]{"All Books", "Available Only", "Currently Borrowed"});
        JLabel searchLabel = new JLabel("Search:");
        JLabel filterLabel = new JLabel("Filter:");
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(filterLabel);
        searchPanel.add(filterComboBox);
        
        // Add search listeners
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
        });
        
        filterComboBox.addActionListener(e -> applyFilters());
        
        topPanel.add(searchPanel, BorderLayout.NORTH);
        
        String[] columns = {"ID", "Title", "Author", "ISBN", "Available/Total"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        booksTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(booksTable);
        topPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel();
        JButton addButton = new JButton("Add Book");
        JButton editButton = new JButton("Edit Book");
        JButton deleteButton = new JButton("Delete Book");
        JButton borrowButton = new JButton("Borrow");
        JButton returnButton = new JButton("Return");
        JButton addCopyButton = new JButton("Add Copy");
        
        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(borrowButton);
        buttonsPanel.add(returnButton);
        buttonsPanel.add(addCopyButton);
        topPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Create bottom panel with borrowing history
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Borrowing History"));
        String[] historyColumns = {"Book Title", "User", "Borrow Date", "Due Date", "Return Date", "Status"};
        historyTableModel = new DefaultTableModel(historyColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        borrowingHistoryTable = new JTable(historyTableModel);
        JScrollPane historyScrollPane = new JScrollPane(borrowingHistoryTable);
        bottomPanel.add(historyScrollPane, BorderLayout.CENTER);
        
        // Add panels to split pane
        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(bottomPanel);
        add(splitPane);
        
        // Add button listeners
        addButton.addActionListener(e -> showAddBookDialog());
        editButton.addActionListener(e -> {
            int selectedRow = booksTable.getSelectedRow();
            if (selectedRow != -1) {
                String bookId = (String) tableModel.getValueAt(selectedRow, 0);
                showEditBookDialog(getBookById(bookId));
            } else {
                JOptionPane.showMessageDialog(this, "Please select a book to edit");
            }
        });
        
        deleteButton.addActionListener(e -> {
            int selectedRow = booksTable.getSelectedRow();
            if (selectedRow != -1) {
                String bookId = (String) tableModel.getValueAt(selectedRow, 0);
                if (confirmDelete()) {
                    dataService.deleteBook(bookId);
                    refreshTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a book to delete");
            }
        });

        borrowButton.addActionListener(e -> {
            int selectedRow = booksTable.getSelectedRow();
            if (selectedRow != -1) {
                String bookId = (String) tableModel.getValueAt(selectedRow, 0);
                Book book = getBookById(bookId);
                if (book != null) {
                    if (book.isAvailable()) {
                        showBorrowDialog(book);
                    } else {
                        JOptionPane.showMessageDialog(this, "No copies available");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a book");
            }
        });

        returnButton.addActionListener(e -> {
            int selectedRow = booksTable.getSelectedRow();
            if (selectedRow != -1) {
                String bookId = (String) tableModel.getValueAt(selectedRow, 0);
                Book book = getBookById(bookId);
                if (book != null) {
                    showReturnDialog(book);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a book");
            }
        });

        addCopyButton.addActionListener(e -> {
            int selectedRow = booksTable.getSelectedRow();
            if (selectedRow != -1) {
                String bookId = (String) tableModel.getValueAt(selectedRow, 0);
                Book book = getBookById(bookId);
                if (book != null) {
                    book.setTotalCopies(book.getTotalCopies() + 1);
                    dataService.updateBook(book);
                    refreshTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a book");
            }
        });

        // Add selection listener for history table
        booksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshHistoryTable();
            }
        });

        // Add overdue books tab
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Books", splitPane);
        tabbedPane.addTab("Overdue Books", createOverdueBooksPanel());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createOverdueBooksPanel() {
        JPanel overdueBooksPanel = new JPanel(new BorderLayout());
        String[] columns = {"Book Title", "User", "Borrow Date", "Due Date", "Status"};
        DefaultTableModel overdueTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable overdueBooksTable = new JTable(overdueTableModel);
        JScrollPane scrollPane = new JScrollPane(overdueBooksTable);
        overdueBooksPanel.add(scrollPane, BorderLayout.CENTER);

        // Populate overdue books table
        List<Book> books = dataService.getAllBooks();
        for (Book book : books) {
            for (Book.BorrowRecord record : book.getBorrowRecords()) {
                if (!record.isReturned() && record.getDueDate() < System.currentTimeMillis()) {
                    Object[] row = {
                        book.getTitle(),
                        record.getUserName(),
                        dateFormat.format(new Date(record.getBorrowDate())),
                        dateFormat.format(new Date(record.getDueDate())),
                        "Overdue"
                    };
                    overdueTableModel.addRow(row);
                }
            }
        }

        return overdueBooksPanel;
    }

    private void showAddBookDialog() {
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField isbnField = new JTextField();
        JSpinner copiesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        
        Object[] message = {
            "Title:", titleField,
            "Author:", authorField,
            "ISBN:", isbnField,
            "Copies:", copiesSpinner
        };
        
        int option = JOptionPane.showConfirmDialog(this, message, "Add New Book", 
            JOptionPane.OK_CANCEL_OPTION);
            
        if (option == JOptionPane.OK_OPTION) {
            String id = UUID.randomUUID().toString();
            Book book = new Book(id, titleField.getText(), authorField.getText(), isbnField.getText());
            book.setTotalCopies((Integer) copiesSpinner.getValue());
            dataService.addBook(book);
            refreshTable();
        }
    }

    private void showEditBookDialog(Book book) {
        if (book == null) return;
        
        JTextField titleField = new JTextField(book.getTitle());
        JTextField authorField = new JTextField(book.getAuthor());
        JTextField isbnField = new JTextField(book.getIsbn());
        
        Object[] message = {
            "Title:", titleField,
            "Author:", authorField,
            "ISBN:", isbnField
        };
        
        int option = JOptionPane.showConfirmDialog(this, message, "Edit Book", 
            JOptionPane.OK_CANCEL_OPTION);
            
        if (option == JOptionPane.OK_OPTION) {
            book.setTitle(titleField.getText());
            book.setAuthor(authorField.getText());
            book.setIsbn(isbnField.getText());
            dataService.updateBook(book);
            refreshTable();
        }
    }
    
    private boolean confirmDelete() {
        return JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this book?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
    
    private Book getBookById(String id) {
        return dataService.getAllBooks().stream()
                .filter(book -> book.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    private void showBorrowDialog(Book book) {
        List<User> users = dataService.getAllUsers();
        if (users.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No users registered in the system",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        JComboBox<User> userComboBox = new JComboBox<>(users.toArray(new User[0]));
        Object[] message = {
            "Select user:", userComboBox
        };

        int option = JOptionPane.showConfirmDialog(this,
            message,
            "Borrow Book",
            JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            User selectedUser = (User) userComboBox.getSelectedItem();
            book.borrowBook(selectedUser);
            selectedUser.borrowBook(book.getId());
            dataService.updateBook(book);
            dataService.updateUser(selectedUser);
            refreshTable();
            refreshHistoryTable();
            firePropertyChange("REFRESH_USERS", null, null);
            JOptionPane.showMessageDialog(this,
                "Book borrowed successfully by " + selectedUser.getName());
        }
    }

    private void refreshHistoryTable() {
        historyTableModel.setRowCount(0);
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow != -1) {
            String bookId = (String) tableModel.getValueAt(selectedRow, 0);
            Book book = getBookById(bookId);
            if (book != null) {
                for (Book.BorrowRecord record : book.getBorrowRecords()) {
                    Object[] row = {
                        book.getTitle(),
                        record.getUserName(),
                        dateFormat.format(new Date(record.getBorrowDate())),
                        dateFormat.format(new Date(record.getDueDate())), // Display due date
                        record.isReturned() ? dateFormat.format(new Date(record.getReturnDate())) : "Not returned",
                        record.isReturned() ? "Returned" : "Borrowed"
                    };
                    historyTableModel.addRow(row);
                }
            }
        }
    }

    private void applyFilters() {
        tableModel.setRowCount(0);
        String searchText = searchField.getText().toLowerCase();
        String filter = (String) filterComboBox.getSelectedItem();
        
        for (Book book : dataService.getAllBooks()) {
            boolean matchesSearch = searchText.isEmpty() ||
                book.getTitle().toLowerCase().contains(searchText) ||
                book.getAuthor().toLowerCase().contains(searchText) ||
                book.getIsbn().toLowerCase().contains(searchText);
                
            boolean matchesFilter = "All Books".equals(filter) ||
                ("Available Only".equals(filter) && book.getAvailableCopies() > 0) ||
                ("Currently Borrowed".equals(filter) && book.getAvailableCopies() < book.getTotalCopies());
                
            if (matchesSearch && matchesFilter) {
                Object[] row = {
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getIsbn(),
                    book.getAvailableCopies() + "/" + book.getTotalCopies()
                };
                tableModel.addRow(row);
            }
        }
    }

    private void refreshTable() {
        applyFilters();
        refreshHistoryTable();
    }
}