package com.pos.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Sale {
    private final IntegerProperty id;
    private final ObjectProperty<LocalDateTime> saleDate;
    private final ObjectProperty<BigDecimal> subtotal;
    private final ObjectProperty<BigDecimal> tax;
    private final ObjectProperty<BigDecimal> discount;
    private final ObjectProperty<BigDecimal> total;
    private final StringProperty paymentMethod;
    private final StringProperty cashier;
    private final StringProperty receiptNumber;
    private final ObservableList<CartItem> items;

    public Sale() {
        this.id = new SimpleIntegerProperty();
        this.saleDate = new SimpleObjectProperty<>(LocalDateTime.now());
        this.subtotal = new SimpleObjectProperty<>(BigDecimal.ZERO);
        this.tax = new SimpleObjectProperty<>(BigDecimal.ZERO);
        this.discount = new SimpleObjectProperty<>(BigDecimal.ZERO);
        this.total = new SimpleObjectProperty<>(BigDecimal.ZERO);
        this.paymentMethod = new SimpleStringProperty("Cash");
        this.cashier = new SimpleStringProperty();
        this.receiptNumber = new SimpleStringProperty();
        this.items = FXCollections.observableArrayList();
    }

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public LocalDateTime getSaleDate() { return saleDate.get(); }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate.set(saleDate); }
    public ObjectProperty<LocalDateTime> saleDateProperty() { return saleDate; }

    public BigDecimal getSubtotal() { return subtotal.get(); }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal.set(subtotal); }
    public ObjectProperty<BigDecimal> subtotalProperty() { return subtotal; }

    public BigDecimal getTax() { return tax.get(); }
    public void setTax(BigDecimal tax) { this.tax.set(tax); }
    public ObjectProperty<BigDecimal> taxProperty() { return tax; }

    public BigDecimal getDiscount() { return discount.get(); }
    public void setDiscount(BigDecimal discount) { this.discount.set(discount); }
    public ObjectProperty<BigDecimal> discountProperty() { return discount; }

    public BigDecimal getTotal() { return total.get(); }
    public void setTotal(BigDecimal total) { this.total.set(total); }
    public ObjectProperty<BigDecimal> totalProperty() { return total; }

    public String getPaymentMethod() { return paymentMethod.get(); }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod.set(paymentMethod); }
    public StringProperty paymentMethodProperty() { return paymentMethod; }

    public String getCashier() { return cashier.get(); }
    public void setCashier(String cashier) { this.cashier.set(cashier); }
    public StringProperty cashierProperty() { return cashier; }

    public String getReceiptNumber() { return receiptNumber.get(); }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber.set(receiptNumber); }
    public StringProperty receiptNumberProperty() { return receiptNumber; }

    public ObservableList<CartItem> getItems() { return items; }

    public void addItem(CartItem item) {
        items.add(item);
        recalculateTotals();
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        recalculateTotals();
    }

    public void recalculateTotals() {
        BigDecimal newSubtotal = items.stream()
            .map(CartItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        setSubtotal(newSubtotal);

        BigDecimal taxRate = new BigDecimal("0.08"); // 8% tax
        BigDecimal newTax = newSubtotal.multiply(taxRate);
        setTax(newTax);

        BigDecimal newTotal = newSubtotal.add(newTax).subtract(getDiscount());
        setTotal(newTotal.max(BigDecimal.ZERO));
    }
}
