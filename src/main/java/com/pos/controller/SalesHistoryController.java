package com.pos.controller;

import com.pos.dao.SaleDAO;
import com.pos.model.CartItem;
import com.pos.model.Sale;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SalesHistoryController {

    @FXML private TableView<Sale> salesTable;
    @FXML private TableColumn<Sale, Integer> colId;
    @FXML private TableColumn<Sale, String> colDate;
    @FXML private TableColumn<Sale, String> colReceipt;
    @FXML private TableColumn<Sale, BigDecimal> colTotal;
    @FXML private TableColumn<Sale, String> colPayment;
    @FXML private TableColumn<Sale, String> colCashier;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private Button btnFilter;
    @FXML private Button btnViewDetails;
    @FXML private Button btnClose;
    @FXML private Label lblTotalSales;
    @FXML private Label lblTotalTransactions;

    private final SaleDAO saleDAO = new SaleDAO();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        setupTable();
        loadSales();
        setupDatePickers();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getSaleDate().format(formatter)));
        colReceipt.setCellValueFactory(new PropertyValueFactory<>("receiptNumber"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colPayment.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colCashier.setCellValueFactory(new PropertyValueFactory<>("cashier"));

        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal total, boolean empty) {
                super.updateItem(total, empty);
                setText(empty || total == null ? null : String.format("$%.2f", total));
                setStyle(empty ? "" : "-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold;");
            }
        });
    }

    private void loadSales() {
        ObservableList<Sale> sales = saleDAO.getAllSales();
        salesTable.setItems(sales);
        updateSummary(sales);
    }

    private void updateSummary(ObservableList<Sale> sales) {
        BigDecimal totalSales = sales.stream()
            .map(Sale::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotalSales.setText(String.format("$%.2f", totalSales));
        lblTotalTransactions.setText(String.valueOf(sales.size()));
    }

    private void setupDatePickers() {
        dateFrom.setValue(LocalDate.now().minusDays(7));
        dateTo.setValue(LocalDate.now());
    }

    @FXML
    private void handleFilter() {
        if (dateFrom.getValue() == null || dateTo.getValue() == null) {
            loadSales();
            return;
        }

        LocalDateTime start = dateFrom.getValue().atStartOfDay();
        LocalDateTime end = dateTo.getValue().atTime(23, 59, 59);

        ObservableList<Sale> sales = saleDAO.getSalesByDateRange(start, end);
        salesTable.setItems(sales);
        updateSummary(sales);
    }

    @FXML
    private void handleViewDetails() {
        Sale selected = salesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a sale to view details.");
            return;
        }

        Sale fullSale = saleDAO.getSaleById(selected.getId());
        if (fullSale == null) return;

        ObservableList<CartItem> items = saleDAO.getSaleItems(selected.getId());

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Sale Details - " + selected.getReceiptNumber());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(15));
        content.setPrefWidth(500);

        Label info = new Label(String.format("Date: %s\nCashier: %s\nPayment: %s",
            selected.getSaleDate().format(formatter),
            selected.getCashier(),
            selected.getPaymentMethod()));
        info.setStyle("-fx-font-size: 12;");

        TableView<CartItem> itemsTable = new TableView<>();
        itemsTable.setPrefHeight(200);

        TableColumn<CartItem, String> colItem = new TableColumn<>("Item");
        colItem.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProduct().getName()));
        colItem.setPrefWidth(200);

        TableColumn<CartItem, Integer> colQty = new TableColumn<>("Qty");
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQty.setPrefWidth(60);

        TableColumn<CartItem, BigDecimal> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getProduct().getPrice()));
        colPrice.setPrefWidth(80);
        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("$%.2f", price));
            }
        });

        TableColumn<CartItem, BigDecimal> colSubtotal = new TableColumn<>("Subtotal");
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colSubtotal.setPrefWidth(100);
        colSubtotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal subtotal, boolean empty) {
                super.updateItem(subtotal, empty);
                setText(empty || subtotal == null ? null : String.format("$%.2f", subtotal));
                setStyle("-fx-font-weight: bold;");
            }
        });

        itemsTable.getColumns().addAll(colItem, colQty, colPrice, colSubtotal);
        itemsTable.setItems(items);

        Label totals = new Label(String.format("Subtotal: $%.2f\nTax: $%.2f\nDiscount: $%.2f\nTOTAL: $%.2f",
            selected.getSubtotal(), selected.getTax(), selected.getDiscount(), selected.getTotal()));
        totals.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-padding: 10;");

        content.getChildren().addAll(info, itemsTable, totals);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    @FXML
    private void handleClose() {
        ((Stage) salesTable.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
