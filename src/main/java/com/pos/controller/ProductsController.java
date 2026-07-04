package com.pos.controller;

import com.pos.dao.ProductDAO;
import com.pos.model.Product;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.Optional;

public class ProductsController {

    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colBarcode;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, BigDecimal> colPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TextField txtSearch;
    @FXML private TextField txtName;
    @FXML private TextField txtBarcode;
    @FXML private TextField txtCategory;
    @FXML private TextField txtPrice;
    @FXML private TextField txtStock;
    @FXML private TextArea txtDescription;
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    private final ProductDAO productDAO = new ProductDAO();
    private Product selectedProduct;

    @FXML
    public void initialize() {
        setupTable();
        loadProducts();
        setupSearch();
        setupTableSelection();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("$%.2f", price));
            }
        });
    }

    private void loadProducts() {
        productsTable.setItems(productDAO.getAllProducts());
    }

    private void setupSearch() {
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                loadProducts();
            } else {
                productsTable.setItems(productDAO.searchProducts(newVal));
            }
        });
    }

    private void setupTableSelection() {
        productsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedProduct = newVal;
                populateFields(newVal);
                btnAdd.setText("New");
                btnUpdate.setDisable(false);
                btnDelete.setDisable(false);
            }
        });
    }

    private void populateFields(Product product) {
        txtName.setText(product.getName());
        txtBarcode.setText(product.getBarcode());
        txtCategory.setText(product.getCategory());
        txtPrice.setText(product.getPrice().toString());
        txtStock.setText(String.valueOf(product.getStockQuantity()));
        txtDescription.setText(product.getDescription());
    }

    @FXML
    private void handleAdd() {
        if (btnAdd.getText().equals("New")) {
            clearFields();
            btnAdd.setText("Add Product");
            btnUpdate.setDisable(true);
            btnDelete.setDisable(true);
            selectedProduct = null;
            return;
        }

        if (!validateFields()) return;

        Product product = new Product();
        product.setName(txtName.getText().trim());
        product.setBarcode(txtBarcode.getText().trim());
        product.setCategory(txtCategory.getText().trim());
        product.setPrice(new BigDecimal(txtPrice.getText().trim()));
        product.setStockQuantity(Integer.parseInt(txtStock.getText().trim()));
        product.setDescription(txtDescription.getText().trim());

        productDAO.addProduct(product);
        loadProducts();
        clearFields();
        showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully!");
    }

    @FXML
    private void handleUpdate() {
        if (selectedProduct == null) return;
        if (!validateFields()) return;

        selectedProduct.setName(txtName.getText().trim());
        selectedProduct.setBarcode(txtBarcode.getText().trim());
        selectedProduct.setCategory(txtCategory.getText().trim());
        selectedProduct.setPrice(new BigDecimal(txtPrice.getText().trim()));
        selectedProduct.setStockQuantity(Integer.parseInt(txtStock.getText().trim()));
        selectedProduct.setDescription(txtDescription.getText().trim());

        productDAO.updateProduct(selectedProduct);
        loadProducts();
        showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully!");
    }

    @FXML
    private void handleDelete() {
        if (selectedProduct == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Product");
        alert.setHeaderText("Delete " + selectedProduct.getName() + "?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            productDAO.deleteProduct(selectedProduct.getId());
            loadProducts();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully!");
        }
    }

    @FXML
    private void handleClear() {
        clearFields();
        productsTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleClose() {
        ((Stage) productsTable.getScene().getWindow()).close();
    }

    private void clearFields() {
        txtName.clear();
        txtBarcode.clear();
        txtCategory.clear();
        txtPrice.clear();
        txtStock.clear();
        txtDescription.clear();
        btnAdd.setText("Add Product");
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        selectedProduct = null;
    }

    private boolean validateFields() {
        if (txtName.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Product name is required.");
            return false;
        }
        try {
            new BigDecimal(txtPrice.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid price.");
            return false;
        }
        try {
            Integer.parseInt(txtStock.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid stock quantity.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
