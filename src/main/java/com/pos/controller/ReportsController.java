package com.pos.controller;

import com.pos.dao.SaleDAO;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportsController {

    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private Button btnGenerate;
    @FXML private Button btnClose;
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblTotalTransactions;
    @FXML private Label lblAverageSale;
    @FXML private VBox chartContainer;
    @FXML private BarChart<String, Number> salesChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    private final SaleDAO saleDAO = new SaleDAO();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        dateFrom.setValue(LocalDate.now().minusDays(30));
        dateTo.setValue(LocalDate.now());

        xAxis.setLabel("Date");
        yAxis.setLabel("Sales ($)");
        salesChart.setTitle("Daily Sales");
        salesChart.setLegendVisible(false);

        generateReport();
    }

    @FXML
    private void handleGenerate() {
        generateReport();
    }

    private void generateReport() {
        LocalDate start = dateFrom.getValue();
        LocalDate end = dateTo.getValue();

        if (start == null || end == null) {
            showAlert(Alert.AlertType.WARNING, "Invalid Dates", "Please select both start and end dates.");
            return;
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Sales");

        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalTransactions = 0;
        int days = 0;

        LocalDate current = start;
        while (!current.isAfter(end)) {
            LocalDateTime dayStart = current.atStartOfDay();
            BigDecimal dayTotal = saleDAO.getTotalSalesForDate(dayStart);
            int dayTransactions = saleDAO.getTotalTransactionsForDate(dayStart);

            if (dayTotal.compareTo(BigDecimal.ZERO) > 0) {
                series.getData().add(new XYChart.Data<>(current.format(formatter), dayTotal.doubleValue()));
                totalRevenue = totalRevenue.add(dayTotal);
                totalTransactions += dayTransactions;
                days++;
            }
            current = current.plusDays(1);
        }

        salesChart.getData().clear();
        salesChart.getData().add(series);

        lblTotalRevenue.setText(String.format("$%.2f", totalRevenue));
        lblTotalTransactions.setText(String.valueOf(totalTransactions));

        BigDecimal avgSale = totalTransactions > 0 
            ? totalRevenue.divide(BigDecimal.valueOf(totalTransactions), 2, BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO;
        lblAverageSale.setText(String.format("$%.2f", avgSale));
    }

    @FXML
    private void handleClose() {
        ((Stage) salesChart.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
