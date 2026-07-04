package com.pos.dao;

import com.pos.model.CartItem;
import com.pos.model.Sale;
import com.pos.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SaleDAO {

    public int saveSale(Sale sale) {
        String sql = "INSERT INTO sales (subtotal, tax, discount, total, payment_method, cashier, receipt_number) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setDouble(1, sale.getSubtotal().doubleValue());
            pstmt.setDouble(2, sale.getTax().doubleValue());
            pstmt.setDouble(3, sale.getDiscount().doubleValue());
            pstmt.setDouble(4, sale.getTotal().doubleValue());
            pstmt.setString(5, sale.getPaymentMethod());
            pstmt.setString(6, sale.getCashier());
            pstmt.setString(7, sale.getReceiptNumber());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int saleId = rs.getInt(1);
                saveSaleItems(saleId, sale);
                return saleId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void saveSaleItems(int saleId, Sale sale) throws SQLException {
        String sql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (CartItem item : sale.getItems()) {
                pstmt.setInt(1, saleId);
                pstmt.setInt(2, item.getProduct().getId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.setDouble(4, item.getProduct().getPrice().doubleValue());
                pstmt.setDouble(5, item.getSubtotal().doubleValue());
                pstmt.addBatch();

                // Update stock
                ProductDAO productDAO = new ProductDAO();
                productDAO.updateStock(item.getProduct().getId(), -item.getQuantity());
            }
            pstmt.executeBatch();
        }
    }

    public ObservableList<Sale> getAllSales() {
        ObservableList<Sale> sales = FXCollections.observableArrayList();
        String sql = "SELECT * FROM sales ORDER BY sale_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                sales.add(mapResultSetToSale(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }

    public ObservableList<Sale> getSalesByDateRange(LocalDateTime start, LocalDateTime end) {
        ObservableList<Sale> sales = FXCollections.observableArrayList();
        String sql = "SELECT * FROM sales WHERE sale_date BETWEEN ? AND ? ORDER BY sale_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(start));
            pstmt.setTimestamp(2, Timestamp.valueOf(end));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                sales.add(mapResultSetToSale(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }

    public Sale getSaleById(int id) {
        String sql = "SELECT * FROM sales WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToSale(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ObservableList<CartItem> getSaleItems(int saleId) {
        ObservableList<CartItem> items = FXCollections.observableArrayList();
        String sql = "SELECT si.*, p.name, p.barcode, p.category FROM sale_items si JOIN products p ON si.product_id = p.id WHERE si.sale_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saleId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                CartItem item = new CartItem();
                com.pos.model.Product product = new com.pos.model.Product();
                product.setId(rs.getInt("product_id"));
                product.setName(rs.getString("name"));
                product.setBarcode(rs.getString("barcode"));
                product.setCategory(rs.getString("category"));
                product.setPrice(BigDecimal.valueOf(rs.getDouble("unit_price")));
                item.setProduct(product);
                item.setQuantity(rs.getInt("quantity"));
                item.setSubtotal(BigDecimal.valueOf(rs.getDouble("subtotal")));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public BigDecimal getTotalSalesForDate(LocalDateTime date) {
        String sql = "SELECT SUM(total) as total FROM sales WHERE DATE(sale_date) = DATE(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(date));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return BigDecimal.valueOf(rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    public int getTotalTransactionsForDate(LocalDateTime date) {
        String sql = "SELECT COUNT(*) as count FROM sales WHERE DATE(sale_date) = DATE(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(date));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Sale mapResultSetToSale(ResultSet rs) throws SQLException {
        Sale sale = new Sale();
        sale.setId(rs.getInt("id"));
        if (rs.getTimestamp("sale_date") != null) {
            sale.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());
        }
        sale.setSubtotal(BigDecimal.valueOf(rs.getDouble("subtotal")));
        sale.setTax(BigDecimal.valueOf(rs.getDouble("tax")));
        sale.setDiscount(BigDecimal.valueOf(rs.getDouble("discount")));
        sale.setTotal(BigDecimal.valueOf(rs.getDouble("total")));
        sale.setPaymentMethod(rs.getString("payment_method"));
        sale.setCashier(rs.getString("cashier"));
        sale.setReceiptNumber(rs.getString("receipt_number"));
        return sale;
    }
}
