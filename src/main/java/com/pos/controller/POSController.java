package com.pos.controller;

import com.pos.dao.ProductDAO;
import com.pos.dao.SaleDAO;
import com.pos.model.CartItem;
import com.pos.model.Product;
import com.pos.model.Sale;
import com.pos.service.ReceiptService;
import com.pos.service.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

public class POSController {

    @FXML private TextField searchField;
    @FXML private TextField barcodeField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private FlowPane productGrid;
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> colItemName;
    @FXML private TableColumn<CartItem, Integer> colQty;
    @FXML private TableColumn<CartItem, BigDecimal> colPrice;
    @FXML private TableColumn<CartItem, BigDecimal> colSubtotal;
    @FXML private TableColumn<CartItem, Void> colAction;
    @FXML private Label lblSubtotal;
    @FXML private Label lblTax;
    @FXML private Label lblDiscount;
    @FXML private Label lblTotal;
    @FXML private Label lblItemCount;
    @FXML private Label lblCashierName;
    @FXML private Label lblDateTime;
    @FXML private Button btnCheckout;
    @FXML private Button btnClearCart;
    @FXML private Button btnApplyDiscount;
    @FXML private Button btnProducts;
    @FXML private Button btnSalesHistory;
    @FXML private Button btnReports;
    @FXML private Button btnUsers;
    @FXML private Button btnLogout;
    @FXML private Button btnExit;
    @FXML private BorderPane mainPane;
    @FXML private ScrollPane productScrollPane;

    private final ProductDAO productDAO = new ProductDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    private Sale currentSale;

    @FXML
    public void initialize() {
        setupCartTable();
        setupProductGrid();
        setupCategoryFilter();
        setupSearch();
        setupBarcodeScanner();
        updateDateTime();
        updateCashierInfo();
        setupAdminControls();
        setupAutoRefresh();

        currentSale = new Sale();
        currentSale.setCashier(SessionManager.getCashierName());

        // Load all products initially
        loadProducts(productDAO.getAllProducts());
    }

    private void setupCartTable() {
        cartTable.setItems(cartItems);

        colItemName.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProduct().getName()));

        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQty.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer qty, boolean empty) {
                super.updateItem(qty, empty);
                if (empty || qty == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5);
                    box.setAlignment(Pos.CENTER);

                    Button btnMinus = new Button("−");
                    btnMinus.setStyle("-fx-font-size: 10; -fx-padding: 2 6; -fx-background-color: #e74c3c; -fx-text-fill: white;");
                    btnMinus.setOnAction(e -> {
                        CartItem item = getTableView().getItems().get(getIndex());
                        if (item.getQuantity() > 1) {
                            item.decrementQuantity();
                            cartTable.refresh();
                            updateTotals();
                        }
                    });

                    Label lblQty = new Label(String.valueOf(qty));
                    lblQty.setFont(Font.font("System", FontWeight.BOLD, 14));
                    lblQty.setMinWidth(30);
                    lblQty.setAlignment(Pos.CENTER);

                    Button btnPlus = new Button("+");
                    btnPlus.setStyle("-fx-font-size: 10; -fx-padding: 2 6; -fx-background-color: #27ae60; -fx-text-fill: white;");
                    btnPlus.setOnAction(e -> {
                        CartItem item = getTableView().getItems().get(getIndex());
                        item.incrementQuantity();
                        cartTable.refresh();
                        updateTotals();
                    });

                    box.getChildren().addAll(btnMinus, lblQty, btnPlus);
                    setGraphic(box);
                }
            }
        });

        colPrice.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getProduct().getPrice()));
        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : currencyFormat.format(price));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });

        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colSubtotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal subtotal, boolean empty) {
                super.updateItem(subtotal, empty);
                setText(empty || subtotal == null ? null : currencyFormat.format(subtotal));
                setAlignment(Pos.CENTER_RIGHT);
                setFont(Font.font("System", FontWeight.BOLD, 12));
            }
        });

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnRemove = new Button("✕");
            {
                btnRemove.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10; -fx-padding: 2 8;");
                btnRemove.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    cartItems.remove(item);
                    updateTotals();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnRemove);
                setAlignment(Pos.CENTER);
            }
        });

        cartTable.setPlaceholder(new Label("Cart is empty"));
    }

    private void setupProductGrid() {
        productGrid.setHgap(10);
        productGrid.setVgap(10);
        productGrid.setPadding(new Insets(10));
    }

    private void setupCategoryFilter() {
        categoryFilter.getItems().add("All Categories");
        categoryFilter.getItems().addAll(productDAO.getAllCategories());
        categoryFilter.getSelectionModel().selectFirst();

        categoryFilter.setOnAction(e -> {
            String selected = categoryFilter.getValue();
            if ("All Categories".equals(selected)) {
                loadProducts(productDAO.getAllProducts());
            } else {
                ObservableList<Product> filtered = FXCollections.observableArrayList();
                for (Product p : productDAO.getAllProducts()) {
                    if (selected.equals(p.getCategory())) {
                        filtered.add(p);
                    }
                }
                loadProducts(filtered);
            }
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                loadProducts(productDAO.getAllProducts());
            } else {
                loadProducts(productDAO.searchProducts(newVal));
            }
        });
    }

    private void setupBarcodeScanner() {
        barcodeField.setOnAction(e -> {
            String barcode = barcodeField.getText().trim();
            if (!barcode.isEmpty()) {
                Product product = productDAO.getProductByBarcode(barcode);
                if (product != null) {
                    addToCart(product);
                    barcodeField.clear();
                } else {
                    showAlert(Alert.AlertType.WARNING, "Product Not Found", 
                        "No product found with barcode: " + barcode);
                    barcodeField.clear();
                }
            }
        });
    }

    private void loadProducts(ObservableList<Product> products) {
        productGrid.getChildren().clear();

        for (Product product : products) {
            VBox card = createProductCard(product);
            productGrid.getChildren().add(card);
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(5);
        card.setPrefWidth(160);
        card.setPrefHeight(180);
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 8; " +
                      "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Product icon/placeholder
        Label icon = new Label(getCategoryIcon(product.getCategory()));
        icon.setFont(Font.font("Segoe UI Emoji", 36));

        // Product name
        Label name = new Label(product.getName());
        name.setFont(Font.font("System", FontWeight.BOLD, 11));
        name.setWrapText(true);
        name.setAlignment(Pos.CENTER);
        name.setMaxWidth(140);

        // Price
        Label price = new Label(currencyFormat.format(product.getPrice()));
        price.setFont(Font.font("System", FontWeight.BOLD, 14));
        price.setTextFill(Color.web("#27ae60"));

        // Stock indicator
        Label stock = new Label("Stock: " + product.getStockQuantity());
        stock.setFont(Font.font("System", 10));
        stock.setTextFill(product.getStockQuantity() < 10 ? Color.web("#e74c3c") : Color.web("#7f8c8d"));

        // Add button
        Button btnAdd = new Button("Add to Cart");
        btnAdd.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 5; -fx-padding: 5 15;");
        btnAdd.setMaxWidth(Double.MAX_VALUE);
        btnAdd.setOnAction(e -> addToCart(product));

        card.getChildren().addAll(icon, name, price, stock, btnAdd);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace("rgba(0,0,0,0.1)", "rgba(0,0,0,0.2)")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("rgba(0,0,0,0.2)", "rgba(0,0,0,0.1)")));

        return card;
    }

    private String getCategoryIcon(String category) {
        if (category == null) return "📦";
        return switch (category.toLowerCase()) {
            case "electronics" -> "📱";
            case "accessories" -> "🎧";
            case "kitchen" -> "☕";
            case "stationery" -> "✏️";
            case "furniture" -> "🪑";
            case "clothing" -> "👕";
            case "footwear" -> "👟";
            default -> "📦";
        };
    }

    private void addToCart(Product product) {
        // Check if already in cart
        for (CartItem item : cartItems) {
            if (item.getProduct().getId() == product.getId()) {
                if (item.getQuantity() < product.getStockQuantity()) {
                    item.incrementQuantity();
                    cartTable.refresh();
                    updateTotals();
                } else {
                    showAlert(Alert.AlertType.WARNING, "Stock Limit", 
                        "Cannot add more. Only " + product.getStockQuantity() + " in stock.");
                }
                return;
            }
        }

        // Check stock
        if (product.getStockQuantity() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Out of Stock", 
                product.getName() + " is currently out of stock.");
            return;
        }

        CartItem newItem = new CartItem(product, 1);
        cartItems.add(newItem);
        updateTotals();
    }

    private void updateTotals() {
        BigDecimal subtotal = cartItems.stream()
            .map(CartItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxRate = new BigDecimal("0.08");
        BigDecimal tax = subtotal.multiply(taxRate);
        BigDecimal discount = currentSale.getDiscount();
        BigDecimal total = subtotal.add(tax).subtract(discount).max(BigDecimal.ZERO);

        currentSale.setSubtotal(subtotal);
        currentSale.setTax(tax);
        currentSale.setTotal(total);

        lblSubtotal.setText(currencyFormat.format(subtotal));
        lblTax.setText(currencyFormat.format(tax));
        lblDiscount.setText(currencyFormat.format(discount));
        lblTotal.setText(currencyFormat.format(total));

        int itemCount = cartItems.stream().mapToInt(CartItem::getQuantity).sum();
        lblItemCount.setText(String.valueOf(itemCount));

        btnCheckout.setDisable(cartItems.isEmpty());
        btnClearCart.setDisable(cartItems.isEmpty());
    }

    @FXML
    private void handleClearCart() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Cart");
        alert.setHeaderText("Clear all items from cart?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            cartItems.clear();
            currentSale.setDiscount(BigDecimal.ZERO);
            updateTotals();
        }
    }

    @FXML
    private void handleApplyDiscount() {
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Apply Discount");
        dialog.setHeaderText("Enter discount amount");
        dialog.setContentText("Discount ($):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amount -> {
            try {
                BigDecimal discount = new BigDecimal(amount);
                if (discount.compareTo(BigDecimal.ZERO) >= 0) {
                    currentSale.setDiscount(discount);
                    updateTotals();
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Amount", "Please enter a valid number.");
            }
        });
    }

    @FXML
    private void handleCheckout() {
        if (cartItems.isEmpty()) return;

        // Payment dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Checkout");
        dialog.setHeaderText("Complete Payment");
        dialog.initModality(Modality.APPLICATION_MODAL);

        ButtonType payButtonType = new ButtonType("Complete Payment", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(payButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label lblTotalAmount = new Label("Total: " + currencyFormat.format(currentSale.getTotal()));
        lblTotalAmount.setFont(Font.font("System", FontWeight.BOLD, 24));
        lblTotalAmount.setTextFill(Color.web("#27ae60"));

        ComboBox<String> paymentMethod = new ComboBox<>();
        paymentMethod.getItems().addAll("Cash", "Credit Card", "Debit Card", "Mobile Payment");
        paymentMethod.setValue("Cash");
        paymentMethod.setPrefWidth(250);

        TextField amountPaid = new TextField();
        amountPaid.setPromptText("Amount Paid");
        amountPaid.setText(currentSale.getTotal().toString());

        Label lblChange = new Label("Change: $0.00");
        lblChange.setFont(Font.font("System", FontWeight.BOLD, 16));

        amountPaid.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                BigDecimal paid = new BigDecimal(newVal.isEmpty() ? "0" : newVal);
                BigDecimal change = paid.subtract(currentSale.getTotal());
                lblChange.setText("Change: " + currencyFormat.format(change.max(BigDecimal.ZERO)));
            } catch (NumberFormatException e) {
                lblChange.setText("Change: $0.00");
            }
        });

        content.getChildren().addAll(
            lblTotalAmount,
            new Label("Payment Method:"),
            paymentMethod,
            new Label("Amount Paid:"),
            amountPaid,
            lblChange
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == payButtonType) {
                return paymentMethod.getValue();
            }
            return null;
        });

        Optional<String> paymentResult = dialog.showAndWait();
        paymentResult.ifPresent(method -> {
            // Complete the sale
            currentSale.setPaymentMethod(method);
            currentSale.setReceiptNumber(ReceiptService.generateReceiptNumber());

            // Add items to sale
            for (CartItem item : cartItems) {
                currentSale.addItem(item);
            }

            // Save to database
            int saleId = saleDAO.saveSale(currentSale);

            if (saleId > 0) {
                showReceipt(currentSale);
                cartItems.clear();
                currentSale = new Sale();
                currentSale.setCashier(SessionManager.getCashierName());
                updateTotals();
                loadProducts(productDAO.getAllProducts()); // Refresh stock
            }
        });
    }

    private void showReceipt(Sale sale) {
        Stage receiptStage = new Stage();
        receiptStage.initModality(Modality.APPLICATION_MODAL);
        receiptStage.setTitle("Receipt - " + sale.getReceiptNumber());
        receiptStage.initStyle(StageStyle.UTILITY);

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");
        root.setPrefWidth(400);

        TextArea receiptText = new TextArea(ReceiptService.generateReceiptText(sale));
        receiptText.setEditable(false);
        receiptText.setFont(Font.font("Monospaced", 11));
        receiptText.setPrefHeight(500);

        Button btnPrint = new Button("Print Receipt");
        btnPrint.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnPrint.setOnAction(e -> {
            // Print functionality would go here
            showAlert(Alert.AlertType.INFORMATION, "Print", "Receipt sent to printer!");
        });

        Button btnClose = new Button("Close");
        btnClose.setOnAction(e -> receiptStage.close());

        HBox buttons = new HBox(10, btnPrint, btnClose);
        buttons.setAlignment(Pos.CENTER);

        root.getChildren().addAll(receiptText, buttons);

        Scene scene = new Scene(root);
        receiptStage.setScene(scene);
        receiptStage.showAndWait();
    }

    @FXML
    private void handleProducts() {
        openWindow("Products Management", "/com/pos/view/products.fxml", 1000, 700);
    }

    @FXML
    private void handleSalesHistory() {
        openWindow("Sales History", "/com/pos/view/sales_history.fxml", 1100, 700);
    }

    @FXML
    private void handleReports() {
        openWindow("Reports", "/com/pos/view/reports.fxml", 900, 700);
    }

    @FXML
    private void handleUsers() {
        if (!SessionManager.isAdmin()) {
            showAlert(Alert.AlertType.ERROR, "Access Denied", "Only administrators can manage users.");
            return;
        }
        openWindow("User Management", "/com/pos/view/users.fxml", 800, 600);
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SessionManager.logout();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pos/view/login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) mainPane.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("POS System - Login");
                stage.setMaximized(false);
                stage.setWidth(500);
                stage.setHeight(400);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    private void openWindow(String title, String fxmlPath, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root, width, height));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open " + title);
        }
    }

    private void updateDateTime() {
        lblDateTime.setText(java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("EEE, MMM dd yyyy HH:mm:ss")));
    }

    private void updateCashierInfo() {
        lblCashierName.setText(SessionManager.getCashierName());
    }

    private void setupAdminControls() {
        boolean isAdmin = SessionManager.isAdmin();
        btnUsers.setVisible(isAdmin);
        btnUsers.setManaged(isAdmin);
    }

    private void setupAutoRefresh() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> updateDateTime())
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
