package com.mabini.vending;

public class Product {
    private int id;
    private String name;
    private String category; // DESSERT, DRINKS, SNACKS, ICE COFFEE
    private double price;
    private int stock;

    public Product(int id, String name, String category, double price, int stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public void reduceStock(int qty) { this.stock -= qty; }

    public double getStudentPrice() { 
        return price * 0.90; 
    }

    @Override
    public String toString() {
        return String.format("%s | ₱%.2f | Stock:%d", name, price, stock);
    }
}