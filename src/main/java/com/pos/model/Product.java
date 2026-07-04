package com.pos.model;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty barcode;
    private final StringProperty category;
    private final ObjectProperty<BigDecimal> price;
    private final IntegerProperty stockQuantity;
    private final StringProperty description;
    private final ObjectProperty<LocalDateTime> createdAt;
    private final ObjectProperty<LocalDateTime> updatedAt;

    public Product() {
        this.id = new SimpleIntegerProperty();
        this.name = new SimpleStringProperty();
        this.barcode = new SimpleStringProperty();
        this.category = new SimpleStringProperty();
        this.price = new SimpleObjectProperty<>();
        this.stockQuantity = new SimpleIntegerProperty();
        this.description = new SimpleStringProperty();
        this.createdAt = new SimpleObjectProperty<>();
        this.updatedAt = new SimpleObjectProperty<>();
    }

    public Product(int id, String name, String barcode, String category, 
                   BigDecimal price, int stockQuantity, String description) {
        this();
        setId(id);
        setName(name);
        setBarcode(barcode);
        setCategory(category);
        setPrice(price);
        setStockQuantity(stockQuantity);
        setDescription(description);
        setCreatedAt(LocalDateTime.now());
        setUpdatedAt(LocalDateTime.now());
    }

    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // Name
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    // Barcode
    public String getBarcode() { return barcode.get(); }
    public void setBarcode(String barcode) { this.barcode.set(barcode); }
    public StringProperty barcodeProperty() { return barcode; }

    // Category
    public String getCategory() { return category.get(); }
    public void setCategory(String category) { this.category.set(category); }
    public StringProperty categoryProperty() { return category; }

    // Price
    public BigDecimal getPrice() { return price.get(); }
    public void setPrice(BigDecimal price) { this.price.set(price); }
    public ObjectProperty<BigDecimal> priceProperty() { return price; }

    // Stock Quantity
    public int getStockQuantity() { return stockQuantity.get(); }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity.set(stockQuantity); }
    public IntegerProperty stockQuantityProperty() { return stockQuantity; }

    // Description
    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public StringProperty descriptionProperty() { return description; }

    // Created At
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }

    // Updated At
    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt.set(updatedAt); }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }

    @Override
    public String toString() {
        return getName();
    }
}
