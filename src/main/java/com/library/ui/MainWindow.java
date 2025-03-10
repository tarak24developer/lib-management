package com.library.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.library.model.User;
import com.library.service.DataService;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;

public class MainWindow extends JFrame {
    private final DataService dataService;
    private User currentUser;
    private JTabbedPane tabbedPane;
    private BooksPanel booksPanel;
    private UsersPanel usersPanel;
    
    public MainWindow() {
        this.dataService = new DataService();
        setupUI();
    }
    
    private void setupUI() {
        setTitle("Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Create main tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Create panels
        booksPanel = new BooksPanel(dataService);
        usersPanel = new UsersPanel(dataService);
        StatsPanel statsPanel = new StatsPanel(dataService);
        
        // Add property change listeners
        PropertyChangeListener statsListener = (PropertyChangeListener) statsPanel;
        booksPanel.addPropertyChangeListener(statsListener);
        usersPanel.addPropertyChangeListener(statsListener);
        
        // Add tabs
        tabbedPane.addTab("Books", booksPanel);
        tabbedPane.addTab("Users", usersPanel);
        tabbedPane.addTab("Statistics", statsPanel);
        tabbedPane.addTab("Blocklisted Users", booksPanel.createBlocklistedUsersPanel()); // Add blocklisted users tab
        
        // Add to frame
        add(tabbedPane);
        
        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        
        setJMenuBar(menuBar);
    }
    
    public static void main(String[] args) {
        // Set the look and feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create and show the application window
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}