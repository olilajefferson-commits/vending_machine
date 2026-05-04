package com.mabini.vending;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class VendingGUI {
    private static ArrayList<Product> inventory;
    private static ArrayList<CartItem> cart = new ArrayList<>();
    private static boolean isMabiniStudent = false;
    private static String studentID = null;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> loginGUI());
    }

    // LOGIN with QR - uses your existing QRScanner.java
    public static void loginGUI() {
        JFrame loginFrame = new JFrame("Mabini Vending - Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLayout(new GridLayout(3, 1, 10, 10));

        JButton scanBtn = new JButton("Scan Mabini QR for 10% Discount");
        JButton guestBtn = new JButton("Continue as Guest");
        JLabel info = new JLabel("Mabini students get 10% off", SwingConstants.CENTER);

        scanBtn.addActionListener(e -> {
            // CALLING YOUR QRSCANNER - NO CHANGES
            studentID = QRScanner.scanQR(loginFrame);
            if (studentID != null && !studentID.isEmpty()) {
                isMabiniStudent = true;
                JOptionPane.showMessageDialog(loginFrame, "Welcome Mabini Student: " + studentID);
                loginFrame.dispose();
                mainGUI();
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Scan failed. Continuing as guest.");
                loginFrame.dispose();
                mainGUI();
            }
        });

        guestBtn.addActionListener(e -> {
            loginFrame.dispose();
            mainGUI();
        });

        loginFrame.add(info);
        loginFrame.add(scanBtn);
        loginFrame.add(guestBtn);
        loginFrame.setSize(350, 200);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setVisible(true);
    }

    // MAIN GUI with 4 category tabs
    public static void mainGUI() {
        JFrame frame = new JFrame("Vending Machine - " + (isMabiniStudent ? "Student: " + studentID : "GUEST"));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // HARD-CODED INVENTORY - 4 CATEGORIES
        inventory = new ArrayList<>();
        // DESSERT
        inventory.add(new Product(1, "Chocolate Cake", "DESSERT", 45.00, 10));
        inventory.add(new Product(2, "Leche Flan", "DESSERT", 35.00, 8));
        // DRINKS
        inventory.add(new Product(3, "Coke", "DRINKS", 25.00, 15));
        inventory.add(new Product(4, "Sprite", "DRINKS", 25.00, 15));
        inventory.add(new Product(5, "Bottled Water", "DRINKS", 10.00, 30));
        // SNACKS
        inventory.add(new Product(6, "Nova", "SNACKS", 15.00, 20));
        inventory.add(new Product(7, "Piattos", "SNACKS", 20.00, 20));
        inventory.add(new Product(8, "Chippy", "SNACKS", 18.00, 25));
        // ICE COFFEE
        inventory.add(new Product(9, "Iced Americano Bottled", "ICE COFFEE", 55.00, 12));
        inventory.add(new Product(10, "Iced Latte Bottled", "ICE COFFEE", 65.00, 12));

        // TABBED PANE FOR 4 SECTIONS
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("DESSERT", createCategoryPanel("DESSERT", frame));
        tabbedPane.addTab("DRINKS", createCategoryPanel("DRINKS", frame));
        tabbedPane.addTab("SNACKS", createCategoryPanel("SNACKS", frame));
        tabbedPane.addTab("ICE COFFEE", createCategoryPanel("ICE COFFEE", frame));

        // Bottom buttons
        JButton viewCartBtn = new JButton("View Cart");
        JButton checkoutBtn = new JButton("Checkout");
        
        viewCartBtn.addActionListener(e -> viewCart(frame));
        checkoutBtn.addActionListener(e -> checkout(frame));

        JPanel btnPanel = new JPanel();
        btnPanel.add(viewCartBtn);
        btnPanel.add(checkoutBtn);
        if (isMabiniStudent) {
            btnPanel.add(new JLabel("10% MABINI STUDENT DISCOUNT "));
        }

        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.add(btnPanel, BorderLayout.SOUTH);
        frame.setSize(600, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static JPanel createCategoryPanel(String category, JFrame parent) {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultListModel<Product> model = new DefaultListModel<>();
        for (Product p : inventory) {
            if (p.getCategory().equals(category)) {
                model.addElement(p);
            }
        }
        
        JList<Product> list = new JList<>(model);
        JButton addBtn = new JButton("Add to Cart");
        
        addBtn.addActionListener(e -> {
            Product p = list.getSelectedValue();
            if (p == null) {
                JOptionPane.showMessageDialog(parent, "Select a product first.");
                return;
            }
            
            String qtyStr = JOptionPane.showInputDialog(parent, "Enter quantity for " + p.getName());
            if (qtyStr == null) return;
            
            try {
                int qty = Integer.parseInt(qtyStr);
                while (qty > p.getStock() || qty <= 0) {
                    JOptionPane.showMessageDialog(parent, "WARNING: Only " + p.getStock() + " in stock.");
                    qtyStr = JOptionPane.showInputDialog(parent, "Enter new quantity:");
                    if (qtyStr == null) return;
                    qty = Integer.parseInt(qtyStr);
                }
                cart.add(new CartItem(p, qty, isMabiniStudent));
                JOptionPane.showMessageDialog(parent, "Added " + qty + "x " + p.getName() + " to cart.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(parent, "Invalid quantity!");
            }
        });
        
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        panel.add(addBtn, BorderLayout.SOUTH);
        return panel;
    }

    private static void viewCart(JFrame parent) {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Cart is empty.");
            return;
        }
        StringBuilder sb = new StringBuilder("=== YOUR CART ===\n");
        for (CartItem item : cart) {
            sb.append(String.format("%s x%d = ₱%.2f\n", 
                item.product.getName(), item.quantity, item.getSubtotal()));
        }
        JOptionPane.showMessageDialog(parent, sb.toString());
    }

    private static void checkout(JFrame parent) {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Cart is empty.");
            return;
        }

        double grossTotal = 0;
        int totalItems = 0;
        StringBuilder itemsList = new StringBuilder();
        
        for (CartItem item : cart) {
            grossTotal += item.getSubtotal();
            totalItems += item.getQuantity();
            itemsList.append(String.format("- %s\n  Quantity per item: %d\n  Subtotal per item: ₱%.2f\n", 
                item.product.getName(), item.quantity, item.getSubtotal()));
        }

        // DISCOUNT RULES
        double discount = 0;
        String discountReason = "None";
        
        if (totalItems >= 5) { // Bulk discount
            discount = grossTotal * 0.05;
            discountReason = "Bulk Discount (5+ items) - 5%";
        }
        if (grossTotal > 200) { // Total amount discount
            discount = grossTotal * 0.10;
            discountReason = "Total Amount Discount (>₱200) - 10%";
        }
        if (isMabiniStudent) { // Mabini 10% stacks
            double mabiniDiscount = grossTotal * 0.10;
            discount += mabiniDiscount;
            discountReason += discountReason.equals("None") ? "Mabini Student 10%" : " + Mabini Student 10%";
        }

        double finalTotal = grossTotal - discount;

        // Payment dialog
        String paymentStr = JOptionPane.showInputDialog(parent, 
            String.format("Total cost: ₱%.2f\nPayment given:", finalTotal));
        if (paymentStr == null) return;
        
        double payment = Double.parseDouble(paymentStr);
        if (payment < finalTotal) {
            JOptionPane.showMessageDialog(parent, "Insufficient payment!");
            return;
        }
        
        double change = payment - finalTotal;

        // Update stock - CRUD UPDATE
        for (CartItem item : cart) {
            item.product.reduceStock(item.quantity);
        }

        // OUTPUT FORMAT - EXACTLY AS REQUIRED
        StringBuilder summary = new StringBuilder("========== TRANSACTION SUMMARY ==========\n");
        summary.append("List of all items purchased:\n").append(itemsList);
        summary.append("-------------------------------------------\n");
        summary.append(String.format("Gross Total: ₱%.2f\n", grossTotal));
        summary.append(String.format("Discount applied: ₱%.2f (%s)\n", discount, discountReason));
        summary.append(String.format("Total cost: ₱%.2f\n", finalTotal));
        summary.append(String.format("Payment given: ₱%.2f\n", payment));
        summary.append(String.format("Change: ₱%.2f\n", change));
        summary.append("\nRemaining stock:\n");
        for (CartItem item : cart) {
            summary.append(String.format("- %s: %d left\n", item.product.getName(), item.product.getStock()));
        }
        summary.append("===========================================");

        JTextArea textArea = new JTextArea(summary.toString());
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(parent, new JScrollPane(textArea), "Receipt", JOptionPane.INFORMATION_MESSAGE);
        
        cart.clear();
    }
}