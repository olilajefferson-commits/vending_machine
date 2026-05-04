package com.mabini.vending;

public class CartItem {
    public Product product;
    public int quantity;
    public boolean isMabiniStudent;

    public CartItem(Product p, int q, boolean isMabiniStudent) {
        this.product = p;
        this.quantity = q;
        this.isMabiniStudent = isMabiniStudent;
    }

    public double getSubtotal() {
        return (isMabiniStudent ? product.getStudentPrice() : product.getPrice()) * quantity;
    }
}