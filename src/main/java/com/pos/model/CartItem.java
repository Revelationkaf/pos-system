package com.pos.model;

import javafx.beans.property.*;
import java.math.BigDecimal;

public class CartItem {
    private final ObjectProperty<Product> product;
    private final IntegerProperty quantity;
    private final ObjectProperty<BigDecimal> subtotal;

    public CartItem() {
        this.product = new SimpleObjectProperty<>();
        this.quantity = new SimpleIntegerProperty(1);
        this.subtotal = new SimpleObjectProperty<>();
        quantity.addListener((obs, oldVal, newVal) -> calculateSubtotal());
    }

    public CartItem(Product product, int quantity) {
        this();
        setProduct(product);
        setQuantity(quantity);
        calculateSubtotal();
    }

    private void calculateSubtotal() {
        if (getProduct() != null && getProduct().getPrice() != null) {
            BigDecimal sub = getProduct().getPrice().multiply(BigDecimal.valueOf(getQuantity()));
            setSubtotal(sub);
        }
    }

    public Product getProduct() { return product.get(); }
    public void setProduct(Product product) { 
        this.product.set(product); 
        calculateSubtotal();
    }
    public ObjectProperty<Product> productProperty() { return product; }

    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public IntegerProperty quantityProperty() { return quantity; }

    public BigDecimal getSubtotal() { return subtotal.get(); }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal.set(subtotal); }
    public ObjectProperty<BigDecimal> subtotalProperty() { return subtotal; }

    public void incrementQuantity() {
        setQuantity(getQuantity() + 1);
    }

    public void decrementQuantity() {
        if (getQuantity() > 1) {
            setQuantity(getQuantity() - 1);
        }
    }

    @Override
    public String toString() {
        return getProduct().getName() + " x" + getQuantity();
    }
}
