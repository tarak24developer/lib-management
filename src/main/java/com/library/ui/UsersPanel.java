package com.library.ui;

import com.library.model.User;
import com.library.service.DataService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.UUID;

public class UsersPanel extends JPanel implements PropertyChangeListener {
    private final DataService dataService;
    private JTable usersTable;
    private DefaultTableModel tableModel;

    public UsersPanel(DataService dataService) {
        this.dataService = dataService;
        setupUI();
        refreshTable();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("REFRESH_USERS".equals(evt.getPropertyName())) {
            refreshTable();
        }
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        // Create table
        String[] columns = {"ID", "Name", "Email", "Books Borrowed", "Reward Points"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        usersTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(usersTable);

        // Create buttons panel
        JPanel buttonsPanel = new JPanel();
        JButton addButton = new JButton("Add User");
        JButton editButton = new JButton("Edit User");
        JButton deleteButton = new JButton("Delete User");

        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);

        // Add components
        add(scrollPane, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        // Add button listeners
        addButton.addActionListener(e -> showAddUserDialog());
        editButton.addActionListener(e -> {
            int selectedRow = usersTable.getSelectedRow();
            if (selectedRow != -1) {
                String userId = (String) tableModel.getValueAt(selectedRow, 0);
                showEditUserDialog(getUserById(userId));
            } else {
                JOptionPane.showMessageDialog(this, "Please select a user to edit");
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = usersTable.getSelectedRow();
            if (selectedRow != -1) {
                String userId = (String) tableModel.getValueAt(selectedRow, 0);
                if (confirmDelete()) {
                    dataService.deleteUser(userId);
                    refreshTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a user to delete");
            }
        });
    }

    private void showAddUserDialog() {
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        Object[] message = {
            "Name:", nameField,
            "Email:", emailField,
            "Password:", passwordField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add New User",
            JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String id = UUID.randomUUID().toString();
            User user = new User(id, nameField.getText(), emailField.getText(),
                new String(passwordField.getPassword()));
            dataService.addUser(user);
            refreshTable();
        }
    }

    private void showEditUserDialog(User user) {
        if (user == null) return;

        JTextField nameField = new JTextField(user.getName());
        JTextField emailField = new JTextField(user.getEmail());
        JPasswordField passwordField = new JPasswordField();

        Object[] message = {
            "Name:", nameField,
            "Email:", emailField,
            "New Password (leave blank to keep current):", passwordField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Edit User",
            JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            user.setName(nameField.getText());
            user.setEmail(emailField.getText());

            String newPassword = new String(passwordField.getPassword());
            if (!newPassword.isEmpty()) {
                user.setPassword(newPassword);
            }

            dataService.updateUser(user);
            refreshTable();
        }
    }

    private boolean confirmDelete() {
        return JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this user?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private User getUserById(String id) {
        return dataService.getAllUsers().stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<User> users = dataService.getAllUsers();
        for (User user : users) {
            Object[] row = {
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBorrowedBooks().size(),
                user.getRewardPoints() // Assuming User class has a getRewardPoints method
            };
            tableModel.addRow(row);
        }
    }
}